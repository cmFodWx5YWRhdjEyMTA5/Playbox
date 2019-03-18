package uk.co.darkerwaters.smspeak;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class SpeakService extends Service implements TextToSpeech.OnInitListener {
    private TextToSpeech ttsEngine;
    private String number;
    private String name;
    private String message;

    private HashMap<String, String> utterancesMap = new HashMap<String, String>();

    public static void SpeakMessage(Context context, String senderNum, String contactName, String message) {
        // start the service that will speak this message
        Intent intent = new Intent(context.getApplicationContext(), SpeakService.class);
        intent.putExtra("number", senderNum);
        intent.putExtra("name", contactName);
        intent.putExtra("message", message);
        // start the service that will speak this out
        context.getApplicationContext().startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.number = intent.getStringExtra("number");
        this.name = intent.getStringExtra("name");
        this.message = intent.getStringExtra("message");

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
                // starting is ok, nothing particularly interesting to do
            }
            @Override
            public void onDone(String s) {
                // when done, close this service down
                stopSelf();
            }
            @Override
            public void onError(String s) {
                // if errored, close this service down
                stopSelf();
            }
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS && null != message) {
            int result = ttsEngine.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "TTS language is not supported", Toast.LENGTH_LONG).show();
            } else {
                String toSpeak = this.message;
                if (null != this.name && false == name.isEmpty()) {
                    // there is a name
                    toSpeak = "Message from " + this.name + "." + this.message;
                }
                // speak this
                ttsEngine.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, utterancesMap);
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
