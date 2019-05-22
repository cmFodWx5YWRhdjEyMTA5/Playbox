package uk.co.darkerwaters.staveinvaders.notes;

import android.content.Context;

import java.util.ArrayList;

import uk.co.darkerwaters.staveinvaders.R;

public class Notes {

    private final Note[] notes;

    public static Notes CreateNotes(Context context) {
        // create the list of notes from the resources in the context
        String[] frequency_resources = context.getResources().getStringArray(R.array.piano_note_frequencies);
        String[] nameArray = context.getResources().getStringArray(R.array.piano_note_names);

        ArrayList<Note> noteList = new ArrayList<Note>(frequency_resources.length* 2);
        float lower, upper, frequency;
        for (int index = 0; index < frequency_resources.length; ++index) {
            frequency = Float.parseFloat(frequency_resources[index]);
            if (index > 0) {
                lower = frequency - ((frequency - Float.parseFloat(frequency_resources[index-1])) * 0.5f);
            }
            else {
                // balance the first note range from the upper range
                lower = frequency - (Float.parseFloat(frequency_resources[index+1]) - frequency) * 0.5f;
            }
            if (index < frequency_resources.length - 1) {
                // not over the end
                upper = frequency + (Float.parseFloat(frequency_resources[index+1]) - frequency) * 0.5f;
            }
            else {
                // are at the end now - balance the range according to the lower
                upper = frequency + (frequency - Float.parseFloat(frequency_resources[index-1])) * 0.5f;
            }
            // this note might be more than one (a sharp can be a flat too!)
            for (String noteName : nameArray[index].split("/")) {
                // for each note name, create a note that represents the frequency
                noteList.add(new Note(noteName, frequency, lower, upper));
            }
        }
        // create the notes from this list
        Notes notes = new Notes(noteList.toArray(new Note[noteList.size()]));
        return notes;
    }

    private Notes(Note[] array) {
        // create the list (private constructor so just done in the static creation function)
        this.notes = array;
        //created okay
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
