package uk.co.darkerwaters.staveinvaders.notes;

import java.util.Arrays;
import java.util.Comparator;

public class Chord {
    public final String title;
    public final Note[] notes;

    public Chord(Note[] notes) {
        // create the title from the notes
        this(createNotesTitle(notes), notes);
    }

    private static String createNotesTitle(Note[] notes) {
        StringBuilder noteTitle = new StringBuilder();
        for (Note note : notes) {
            noteTitle.append(note.name);
            noteTitle.append('/');
        }
        if (noteTitle.length() > 0) {
            // remove the trailing slash
            noteTitle.deleteCharAt(noteTitle.length() - 1);
        }
        return noteTitle.toString();
    }

    public Chord(String title, Note[] notes) {
        // construct this object nicely
        this.notes = Arrays.copyOf(notes, notes.length);
        // put this list in order to make comparison easier (always in note freq order)
        Arrays.sort(this.notes, new Comparator<Note>() {
            @Override
            public int compare(Note note, Note t1) {
                return (int)(note.frequency * 1000000 - t1.frequency * 1000000);
            }
        });
        // set the title too
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    @Override
    public String toString() {
        return this.title;
    }

    public Note[] getNotes() {
        return Arrays.copyOf(this.notes, this.notes.length);
    }

    public boolean hasSharp() {
        boolean hasSharp = false;
        for (Note note : this.notes) {
            if (note.isSharp()) {
                hasSharp = true;
                break;
            }
        }
        return hasSharp;
    }

    public boolean hasFlat() {
        boolean hasFlat = false;
        for (Note note : this.notes) {
            if (note.isFlat()) {
                hasFlat = true;
                break;
            }
        }
        return hasFlat;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || false == obj instanceof Chord) {
            // not the same
            return false;
        }
        else {
            Chord toCompare = (Chord)obj;
            if (this.notes.length != toCompare.notes.length) {
                // different number of notes
                return false;
            }
            else {
                // compare the notes
                for (int i = 0; i < this.notes.length; ++i) {
                    if (this.notes[i].frequency != toCompare.notes[i].frequency) {
                        // the note is different
                        return false;
                    }
                }
                // if here okay then all the notes are the same frequency
                return true;
            }
        }
    }

    public boolean isNoteContained(Note test) {
        return isNoteContained(test.frequency);
    }

    public boolean isNoteContained(float frequency) {
        boolean isContained = false;
        for (Note note : this.notes) {
            if (frequency >= note.lower && frequency <= note.upper) {
                // here it is
                isContained = true;
                break;
            }
        }
        return isContained;
    }

    public int findNoteIndex(Note test) {
        int foundIndex = -1;
        for (int i = 0; i < notes.length; ++i) {
            if (notes[i].frequency >= test.lower && notes[i].frequency <= test.upper) {
                // here it is
                foundIndex = i;
                break;
            }
        }
        return foundIndex;
    }

    public int getNumberContained(Chord toTest) {
        int count = 0;
        for (Note test : toTest.notes) {
            if (isNoteContained(test)) {
                ++count;
            }
        }
        return count;
    }

    public int getNoteCount() {
        return this.notes.length;
    }


    public Note getLowest() {
        // find the lowest
        Note lowest = null;
        for (Note note : this.notes) {
            if (null == lowest || note.getFrequency() < lowest.getFrequency()) {
                lowest = note;
            }
        }
        return lowest;
    }

    public Note getHighest() {
        // find the highest
        Note highest = null;
        for (Note note : this.notes) {
            if (null == highest || note.getFrequency() > highest.getFrequency()) {
                highest = note;
            }
        }
        return highest;
    }

    public Note root() {
        if (null == this.notes || this.notes.length == 0) {
            return null;
        }
        else {
            return this.notes[0];
        }
    }
}
