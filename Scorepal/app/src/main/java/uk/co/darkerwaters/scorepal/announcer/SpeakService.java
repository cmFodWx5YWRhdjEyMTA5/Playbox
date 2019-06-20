package uk.co.darkerwaters.scorepal.announcer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class SpeakService extends Service implements TextToSpeech.OnInitListener {

    private TextToSpeech ttsEngine;
    private volatile int activeMessages = 0;
    private String utteranceId;

    private static class InstanceRunner {
        SpeakService RunningInstance = null;
        boolean isServiceRequested = false;
        final ArrayList<ToSpeak> toSpeakList = new ArrayList<ToSpeak>();
    }

    private static class ToSpeak {
        final String message;
        final boolean isFlush;
        final boolean isMessagePart;
        ToSpeak(String message, boolean isFlush, boolean isMessagePart) {
            this.message = message;
            this.isFlush = isFlush;
            this.isMessagePart = isMessagePart;
        }
    }

    private static final InstanceRunner Instance = new InstanceRunner();

    public static void SpeakMessage(Context context, String message, boolean isFlushOld) {
        synchronized (Instance) {
            // add the thing to speak to the list
            Instance.toSpeakList.add(new ToSpeak(message, isFlushOld, false));
            // if there is a service, we need to add to the stack of what it is saying at this moment
            if (null == Instance.RunningInstance && false == Instance.isServiceRequested) {
                // there is no instance and one has not been requested, start one here
                Intent intent = new Intent(context.getApplicationContext(), SpeakService.class);
                intent.putExtra("message", message);
                // start the service that will speak this out
                context.getApplicationContext().startService(intent);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*
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
        this.ttsEngine = new TextToSpeech(this, this);
        this.utteranceId = Integer.toString(this.hashCode());

        this.ttsEngine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
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
        if (this.activeMessages <= 0) {
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
            int result = this.ttsEngine.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // show the user that this doesn't work
                Toast.makeText(this, "TTS language is not supported", Toast.LENGTH_LONG).show();
            }
        }
        flushMessages();
    }

    private void flushMessages() {
        boolean isKeepLooking = true;
        ToSpeak toSpeak;
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
                // speak this
                this.ttsEngine.speak(toSpeak.message,
                        toSpeak.isFlush ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD,
                        null, this.utteranceId);
                if (toSpeak.isMessagePart) {
                    // we are just a part of a message, break from the loop
                    // we we speak this part and come back here as we ended
                    break;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if (this.ttsEngine != null) {
            this.ttsEngine.stop();
            this.ttsEngine.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}