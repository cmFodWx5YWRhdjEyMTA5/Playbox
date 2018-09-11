package uk.co.darkerwaters.noteinvaders;

import android.app.Activity;
import android.content.Context;

public class Notes {

    private static Notes INSTANCE = null;

    private final Note[] notes;

    public static void CreateNotes(Context context) {
        // create the list of notes from the resources in the context
        String[] frequency_resources = context.getResources().getStringArray(R.array.piano_note_frequencies);
        String[] nameArray = context.getResources().getStringArray(R.array.piano_note_names);

        INSTANCE = new Notes(frequency_resources.length);
        float lower, upper, frequency;
        for (int index = 0; index < INSTANCE.notes.length; ++index) {
            frequency = Float.parseFloat(frequency_resources[index]);
            if (index > 0) {
                lower = frequency - ((frequency - INSTANCE.notes[index-1].frequency) / 2.0f);
            }
            else {
                // balance the first note range from the upper range
                lower = frequency - ((Float.parseFloat(frequency_resources[index+1]) - frequency) / 2.0f);
            }
            if (index < INSTANCE.notes.length - 1) {
                // not over the end
                upper = frequency + ((Float.parseFloat(frequency_resources[index+1]) - frequency) / 2.0f);
            }
            else {
                // are at the end now - balance the range according to the lower
                upper = frequency + ((frequency - INSTANCE.notes[index-1].frequency) / 2.0f);
            }
            // create the frequency that represents this note
            INSTANCE.notes[index] = new Note(nameArray[index], frequency, lower, upper);
        }
    }

    private Notes(int noteCount) {
        // create the list (private constructor so just done in the static creation function)
        this.notes = new Note[noteCount];
        //created okay
    }

    public static Notes instance() {
        return INSTANCE;
    }

    public int getNoteCount() {
        return this.notes.length;
    }

    public Note getNote(int index) {
        return this.notes[index];
    }

    public Note getNote(String name) {
        Note result = null;
        for (Note note : this.notes) {
            if (note.isNote(name)) {
                result = note;
                break;
            }
        }
        return result;
    }

    public Note getNote(float frequency) {
        Note result = null;
        for (Note note : this.notes) {
            if (note.isNote(frequency)) {
                result = note;
                break;
            }
        }
        return result;
    }

    public int getNoteIndex(float frequency) {
        for (int i = 0; i < this.notes.length; ++i) {
            if (this.notes[i].isNote(frequency)) {
                return i;
            }
        }
        return -1;
    }
}
