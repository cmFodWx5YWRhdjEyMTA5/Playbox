package uk.co.darkerwaters.staveinvaders.notes;

import android.content.Context;

import uk.co.darkerwaters.staveinvaders.R;

public class Notes {

    private final Note[] notes;

    public static Notes CreateNotes(Context context) {
        // create the list of notes from the resources in the context
        String[] frequency_resources = context.getResources().getStringArray(R.array.piano_note_frequencies);
        String[] nameArray = context.getResources().getStringArray(R.array.piano_note_names);

        Notes notes = new Notes(frequency_resources.length);
        float lower, upper, frequency;
        for (int index = 0; index < notes.notes.length; ++index) {
            frequency = Float.parseFloat(frequency_resources[index]);
            if (index > 0) {
                lower = frequency - ((frequency - notes.notes[index-1].frequency) / 2.0f);
            }
            else {
                // balance the first note range from the upper range
                lower = frequency - ((Float.parseFloat(frequency_resources[index+1]) - frequency) / 2.0f);
            }
            if (index < notes.notes.length - 1) {
                // not over the end
                upper = frequency + ((Float.parseFloat(frequency_resources[index+1]) - frequency) / 2.0f);
            }
            else {
                // are at the end now - balance the range according to the lower
                upper = frequency + ((frequency - notes.notes[index-1].frequency) / 2.0f);
            }
            // this note might be more than one (a sharp can be a flat too!)
            for (String noteName : nameArray[index].split("/")) {
                // for each note name, create a note that represents the frequency
                notes.notes[index] = new Note(noteName, frequency, lower, upper);
            }
        }
        return notes;
    }

    private Notes(int noteCount) {
        // create the list (private constructor so just done in the static creation function)
        this.notes = new Note[noteCount];
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

    public NoteRange getFullRange() {
        return new NoteRange(getNote(0), getNote(getNoteCount() - 1));
    }
}
