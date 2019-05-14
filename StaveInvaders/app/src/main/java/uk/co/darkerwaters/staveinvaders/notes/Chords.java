package uk.co.darkerwaters.staveinvaders.notes;

import java.util.ArrayList;
import java.util.Arrays;

public class Chords {

    private final Chord[] chords;

    public static Chords CreateSingleChords(Notes notes) {
        // create the list of chords that represents notes too, gathering
        // two of each same note into a single sound of two notes
        int noteCount = notes.getNoteCount();
        ArrayList<Chord> soundlist = new ArrayList<Chord>(noteCount);
        ArrayList<Note> notesGathered = new ArrayList<Note>(2);
        for (int i = 0; i < noteCount; ++i) {
            // for each note, create the sound
            Note note = notes.getNote(i);
            if (notesGathered.isEmpty()) {
                // first one
                notesGathered.add(note);
            }
            else if (notesGathered.get(notesGathered.size() - 1).getFrequency() ==
                    note.getFrequency()) {
                // the last note is the same as this one
                notesGathered.add(note);
            }
            else {
                // this note is different, create the sound from the previous
                soundlist.add(new Chord(notesGathered.toArray(new Note[notesGathered.size()])));
                // clear the list, added these notes
                notesGathered.clear();
                // this note is the start of a new list, add this
                notesGathered.add(note);
            }
        }
        // we end up with a trailing sound not added to the list yet, do it now
        soundlist.add(new Chord(notesGathered.toArray(new Note[notesGathered.size()])));
        // and return the result
        return new Chords(soundlist.toArray(new Chord[soundlist.size()]));
    }

    public Chords(Chord[] chords) {
        // create the list (private constructor so just done in the static creation function)
        this.chords = Arrays.copyOf(chords, chords.length);
        //created okay
    }

    public int getSize() {
        return this.chords.length;
    }

    public Chord getChord(int index) {
        return this.chords[index];
    }

    public Chord getChord(String title) {
        Chord result = null;
        for (Chord chord : this.chords) {
            if (chord.title.equals(title)) {
                result = chord;
                break;
            }
        }
        return result;
    }

    public Chord getChord(float frequency) {
        Chord result = null;
        for (Chord chord : this.chords) {
            if (chord.isNoteContained(frequency)) {
                result = chord;
                break;
            }
        }
        return result;
    }

    public int getChordIndex(String title) {
        for (int i = 0; i < this.chords.length; ++i) {
            if (this.chords[i].title.equals(title)) {
                return i;
            }
        }
        return -1;
    }

    public int getChordIndex(float frequency) {
        for (int i = 0; i < this.chords.length; ++i) {
            if (this.chords[i].isNoteContained(frequency)) {
                return i;
            }
        }
        return -1;
    }

    public Chord replace(int i, Chord chord) {
        Chord replaced = this.chords[i];
        this.chords[i] = chord;
        return replaced;
    }
}
