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
import java.util.UUID;

public class SpeakService implements TextToSpeech.OnInitListener {

    private final Context context;
    private final TextToSpeech ttsEngine;
    private volatile int activeMessages = -1;

    final ArrayList<ToSpeak> toSpeakList = new ArrayList<ToSpeak>();

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

    public SpeakService(Context context) {
        this.context = context;

        // initialise the engine
        this.ttsEngine = new TextToSpeech(this.context, this);

        this.ttsEngine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                // starting is ok, remember what we are saying here
                ++activeMessages;
            }
            @Override
            public void onDone(String s) {
                --activeMessages;
                // now we can flush any new messages out
                flushMessages();

            }
            @Override
            public void onError(String s) {
                --activeMessages;
                // now we can flush any new messages out
                flushMessages();
            }
        });
    }

    public void speakMessage(String message, boolean isFlushOld) {
        synchronized (this.toSpeakList) {
            if (isFlushOld) {
                // remove anything proceeding this
                this.toSpeakList.clear();
            }
            // add the new one to speak to the end
            this.toSpeakList.add(new ToSpeak(message, isFlushOld, false));
        }
        // speak all in the list
        flushMessages();
    }

    public void close() {
        this.ttsEngine.stop();
        this.ttsEngine.shutdown();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = this.ttsEngine.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // show the user that this doesn't work
                Toast.makeText(this.context, "TTS language is not supported", Toast.LENGTH_LONG).show();
            }
            // else we are successfully initialised, start working
            this.activeMessages = 0;
        }
        flushMessages();
    }

    private void flushMessages() {
        ToSpeak toSpeak;
        // while we are without error and initialised, flush the next message from the queue
        while (this.activeMessages >= 0) {
            synchronized (this.toSpeakList) {
                if (false == this.toSpeakList.isEmpty()) {
                    // get the thing to speak
                    toSpeak = this.toSpeakList.remove(0);
                } else {
                    // we are done quit looking
                    break;
                }
            }
            if (null != toSpeak) {
                // speak this
                this.ttsEngine.speak(toSpeak.message,
                        toSpeak.isFlush ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD,
                        null, UUID.randomUUID().toString());
                if (toSpeak.isMessagePart) {
                    // we are just a part of a message, break from the loop
                    // we we speak this part and come back here as we ended
                    break;
                }
            }
        }
    }
}