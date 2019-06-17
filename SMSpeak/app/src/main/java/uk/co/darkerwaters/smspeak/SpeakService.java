package uk.co.darkerwaters.smspeak;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class SpeakService extends Service implements TextToSpeech.OnInitListener {
    private TextToSpeech ttsEngine;
    private volatile int activeMessages = 0;

    private static class ToSpeak {
        final String number;
        final String name;
        final String message;
        final String contact;
        ToSpeak(String number, String name, String message) {
            this.number = number;
            this.name = name;
            this.message = message;

            if (name != null && false == name.isEmpty()) {
                // there is a contact name to use, use it
                this.contact = name;
            }
            else if (number != null && false == number.isEmpty()) {
                // else use the number
                this.contact = number;
            }
            else {
                // unkonwn
                this.contact = "blocked";
            }
        }
    }

    private static class InstanceRunner {
        SpeakService RunningInstance = null;
        boolean isServiceRequested = false;
        final ArrayList<ToSpeak> toSpeakList = new ArrayList<ToSpeak>();
    }

    private HashMap<String, String> utterancesMap = new HashMap<String, String>();


    private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm");

    private static final InstanceRunner Instance = new InstanceRunner();

    public static void SpeakMessage(Context context, String senderNum, String contactName, String message) {
        synchronized (Instance) {
            // add the thing to speak to the list
            Instance.toSpeakList.add(new ToSpeak(senderNum, contactName, message));
            // if there is a service, we need to add to the stack of what it is saying at this moment
            if (null == Instance.RunningInstance && false == Instance.isServiceRequested) {
                // there is no instance and one has not been requested, start one here
                Intent intent = new Intent(context.getApplicationContext(), SpeakService.class);
                intent.putExtra("number", senderNum);
                intent.putExtra("name", contactName);
                intent.putExtra("message", message);
                // start the service that will speak this out
                context.getApplicationContext().startService(intent);
            }
        }
    }

    public static String getTimeToRead() {
        return timeFormat.format(Calendar.getInstance().getTime());
    }

    public static String constructMessage(Context context, String contact, String content) {
        StringBuilder exampleMessage = new StringBuilder();
        State state = State.GetInstance(context);
        if (state.isTalkTime()) {
            exampleMessage.append(getTimeToRead());
        }
        if (state.isTalkIntro()) {
            if (exampleMessage.length() > 0) {
                exampleMessage.append(" ");
            }
            exampleMessage.append(state.getIntro());
        }
        if (state.isTalkContact()) {
            if (exampleMessage.length() > 0) {
                exampleMessage.append(" ");
            }
            exampleMessage.append(contact);
        }
        if (state.isTalkMessage()) {
            if (exampleMessage.length() > 0) {
                exampleMessage.append(". ");
            }
            exampleMessage.append(content);
        }
        return exampleMessage.toString();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*this.number = intent.getStringExtra("number");
        this.name = intent.getStringExtra("name");
        this.message = intent.getStringExtra("message");
        */
        // we are started, remember this with our instance
        synchronized (Instance) {
            Instance.RunningInstance = this;
            Instance.isServiceRequested = false;
        }
        // and create with the base
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        // initialise the engine
        ttsEngine = new TextToSpeech(this, this);
        utterancesMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UUID.randomUUID().toString());

        ttsEngine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                // starting is ok, remember what we are saying here
                ++activeMessages;
            }
            @Override
            public void onDone(String s) {
                // when done, close this service down
                --activeMessages;
                endSpeaking();
            }
            @Override
            public void onError(String s) {
                // if errored, close this service down
                --activeMessages;
                endSpeaking();
            }
        });
    }

    private void endSpeaking() {
        if (activeMessages <= 0) {
            synchronized (Instance) {
                if (false == Instance.toSpeakList.isEmpty()) {
                    flushMessages();
                }
                else {
                    // done
                    Instance.RunningInstance = null;
                    // and stop the service
                    stopSelf();
                }
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = ttsEngine.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // show the user that this doesn't work
                Toast.makeText(this, "TTS language is not supported", Toast.LENGTH_LONG).show();
            }
        }
        flushMessages();
    }

    private void flushMessages() {
        ToSpeak toSpeak = null;
        ToSpeak spoken = null;
        boolean isKeepLooking = true;
        while (isKeepLooking) {
            synchronized (Instance) {
                if (false == Instance.toSpeakList.isEmpty()) {
                    // get the thing to speak
                    toSpeak = Instance.toSpeakList.remove(0);
                } else {
                    // we are done and will quit, set the pointer to null so another will try again
                    Instance.RunningInstance = null;
                    toSpeak = null;
                    isKeepLooking = false;
                }
            }
            if (null != toSpeak) {
                // do we want to say it all?
                String message;
                if (null != spoken && spoken.contact.equals(toSpeak.contact)) {
                    // the contact is the same as we just said something from, just add the content
                    message = toSpeak.message;
                } else {
                    // construct the thing to speak from all our settings and the name / number
                    message = constructMessage(this, toSpeak.contact, toSpeak.message);
                }
                // and speak this
                ttsEngine.speak(message, TextToSpeech.QUEUE_ADD, utterancesMap);
                // remembering we did
                spoken = toSpeak;
            }
        }
    }

    @Override
    public void onDestroy() {
        if (ttsEngine != null) {
            ttsEngine.stop();
            ttsEngine.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
