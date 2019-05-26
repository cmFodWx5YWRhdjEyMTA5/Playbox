package uk.co.darkerwaters.staveinvaders.sounds;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.SoundEffectConstants;
import android.view.View;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.notes.Note;

public class SoundPlayer {

    private final AssetManager assetManager;
    private SoundPool soundPool = null;
    private int falseFireSound = -1;
    private int missedSound = -1;
    private int gameOverSound = -1;

    private final Map<Note, Integer> loadedNotes;
    private final Map<Note, Note> sharpToFlat;
    private NoteSounds noteSounds = null;

    private static SoundPlayer INSTANCE = null;

    private SoundPlayer(Context context, Application application) {
        int maxStreams = 9;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(maxStreams)
                    .build();
        } else {
            soundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0);
        }

        // fill your sounds
        this.falseFireSound = soundPool.load(context, R.raw.false_fire, 1);
        this.missedSound = soundPool.load(context, R.raw.missed, 1);
        this.gameOverSound = soundPool.load(context, R.raw.game_over, 1);

        this.loadedNotes = new HashMap<Note, Integer>();
        this.assetManager = context.getAssets();

        // also we need a map of sharps to flats to play the flat instead each time
        this.sharpToFlat = new HashMap<Note, Note>();
        Chords singleChords = application.getSingleChords();
        for (int i = 0; i < singleChords.getSize(); ++i) {
            // for each chord, if it contains a sharp, map it to the flat
            Chord chord = singleChords.getChord(i);
            Note flat = null, sharp = null;
            for (Note note : chord.notes) {
                if (note.isSharp()) {
                    sharp = note;
                }
                else if (note.isFlat()) {
                    flat = note;
                }
            }
            if (null != flat && null != sharp) {
                // map one to the other
                this.sharpToFlat.put(sharp, flat);
            }
        }
    }

    public static void initialise(Context context, Application application) {
        if (INSTANCE == null) {
            INSTANCE = new SoundPlayer(context, application);
        }
    }

    public static void close() {
        if (INSTANCE != null) {
            INSTANCE.soundPool.release();
            INSTANCE = null;
        }
    }

    public static SoundPlayer getINSTANCE() {
        return SoundPlayer.INSTANCE;
    }

    public void missed() {
        this.soundPool.play(this.missedSound, 0.7f, 0.3f, 1, 0, 1f);
    }

    public void falseFire() {
        this.soundPool.play(this.falseFireSound, 0.3f, 0.7f, 1, 0, 1f);
    }

    public void gameOver() {
        this.soundPool.play(this.gameOverSound, 0.7f, 0.7f, 1, 0, 1f);
    }

    public void playSound(Chord chord) {
        // play all the notes in the chord
        for (Note note : chord.notes) {
            playNote(note);
        }
    }

    private void playNote(Note note) {
        // first is this sound loaded?
        Integer soundIndex;
        synchronized (this.loadedNotes) {
            soundIndex = this.loadedNotes.get(note);
            if (null == soundIndex) {
                // there is no index, try to load it now
                try {
                    //if it is a sharp - we need the equivilent flat to get the MP3 for it
                    String noteName = note.getName();
                    if (note.isSharp()) {
                        Note flat = this.sharpToFlat.get(note);
                        if (null != flat) {
                            // use this name instead
                            noteName = flat.getName();
                        }
                    }
                    String notePath = "notes/piano/" + noteName + ".mp3";
                    // loading won't load in time to play later in the loop, so add a listener to play
                    soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                        @Override
                        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                            playLoadedNote(sampleId);
                        }
                    });
                    soundIndex = soundPool.load(assetManager.openFd(notePath), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                    // don't try again
                    soundIndex = -1;
                }
                // and put back in the map for the next time
                this.loadedNotes.put(note, soundIndex);
            } else if (soundIndex > 0) {
                // there is a valid index, play it here
                playLoadedNote(soundIndex);
            } else {
                // tried to find and failed, use the tone generator instead
                if (null == this.noteSounds) {
                    this.noteSounds = new NoteSounds();
                }
                this.noteSounds.playSound(note);
            }
        }
    }

    private void playLoadedNote(int soundIndex) {
        this.soundPool.play(soundIndex, 0.8f, 1f, 1, 0, 1f);
    }
}
