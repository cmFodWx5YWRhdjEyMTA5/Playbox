package uk.co.darkerwaters.staveinvaders.sounds;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.HashMap;
import java.util.Map;

import uk.co.darkerwaters.staveinvaders.notes.Note;

public class NoteSounds {
    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html

    private final double duration = 0.2; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = (int)(duration * sampleRate);

    private final Map<Note, byte[]> generatedSounds;

    private final AudioTrack audioTrack;
    private boolean isAudioInitialised = false;

    NoteSounds() {
        this.generatedSounds = new HashMap<Note, byte[]>();
        this.audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 2 * numSamples,
                AudioTrack.MODE_STATIC);
    }

    private byte[] getGeneratedSnd(Note note) {
        byte[] generatedSnd;
        synchronized (this.generatedSounds) {
            generatedSnd = this.generatedSounds.get(note);
            if (generatedSnd == null) {
                // generate the array
                generatedSnd = genTone(note.getFrequency());
                this.generatedSounds.put(note, generatedSnd);
            }
        }
        return generatedSnd;
    }

    private byte[] genTone(double frequency){
        // fill out the array
        byte generatedSnd[] = new byte[2 * numSamples];
        double sample[] = new double[numSamples];
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/frequency));
        }
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
        return generatedSnd;
    }

    void playSound(Note note) {
        // get the sound
        byte[] generatedSnd = getGeneratedSnd(note);
        // and play this
        if (this.isAudioInitialised) {
            this.audioTrack.stop();
        }
        // write the note and play it
        this.audioTrack.write(generatedSnd, 0, generatedSnd.length);
        this.audioTrack.play();
        this.isAudioInitialised = true;
    }
}