package uk.co.darkerwaters.testingsounddetection;

import android.app.Activity;
import android.content.res.TypedArray;

public class Notes {

    private class NotePair {
        final String[] names;
        final float frequency;
        final float upper;
        final float lower;

        NotePair(String name, float frequency, float lower, float upper) {
            this.names = name.split("/");
            this.frequency = frequency;
            this.lower = lower;
            this.upper = upper;
        }

        boolean isNote(String nameToTest) {
            boolean result = false;
            for (String name : this.names) {
                if (name.equalsIgnoreCase(nameToTest)) {
                    result = true;
                    break;
                }
            }
            return result;
        }

        boolean isNote(float frequency) {
            return frequency > this.lower && frequency < this.upper;
        }

        String getName() {
            StringBuilder name = new StringBuilder();
            for (String entry : this.names) {
                if (name.length() > 0) {
                    name.append('/');
                }
                name.append(entry);
            }
            return name.toString();
        }

        String getName(int index) {
            String result;
            if (index < 0) {
                // return the cumulative string
                result = getName();
            }
            else if (this.names.length < index) {
                // return the one requested
                result = this.names[index];
            }
            else {
                // return the last available
                result = this.names[this.names.length - 1];
            }
            return result;
        }

        int getNameCount() {
            return this.names.length;
        }

        float getFrequency() {
            return this.frequency;
        }

        float getFrequencyUpper() {
            return this.upper;
        }

        float getFrequencyLower() {
            return this.lower;
        }
    }

    private final NotePair[] notes;

    public Notes(Activity context) {

        String[] frequency_resources = context.getResources().getStringArray(R.array.piano_note_frequencies);
        String[] nameArray = context.getResources().getStringArray(R.array.piano_note_names);

        this.notes = new NotePair[frequency_resources.length];
        float lower, upper, frequency;
        for (int index = 0; index < this.notes.length; ++index) {
            frequency = Float.parseFloat(frequency_resources[index]);
            if (index > 0) {
                lower = frequency - ((frequency - this.notes[index-1].frequency) / 2.0f);
            }
            else {
                // balance the first note range from the upper range
                lower = frequency - ((Float.parseFloat(frequency_resources[index+1]) - frequency) / 2.0f);
            }
            if (index < this.notes.length - 1) {
                // not over the end
                upper = frequency + ((Float.parseFloat(frequency_resources[index+1]) - frequency) / 2.0f);
            }
            else {
                // are at the end now - balance the range according to the lower
                upper = frequency + ((frequency - this.notes[index-1].frequency) / 2.0f);
            }
            // create the frequency that represents this note
            this.notes[index] = new NotePair(nameArray[index], frequency, lower, upper);
        }
    }

    public int getNoteCount() {
        return this.notes.length;
    }

    public String[] getNotesNames() {
        return getNotesNames(-1);
    }

    public String[] getNotesNames(int nameIndex) {
        String[] result = new String[this.notes.length];
        for (int index = 0; index < result.length; index++) {
            result[index] = this.notes[index].getName(nameIndex);
        }
        return result;
    }

    public float getNoteFrequency(String noteName) {
        float result = -1.0f;
        for (NotePair note : notes) {
            if (note.isNote(noteName)) {
                result = note.getFrequency();
                break;
            }
        }
        return result;
    }

    public String getNote(float frequency) {
        return this.getNote(frequency, -1);
    }

    public String getNote(float frequency, int nameIndex) {
        String result = "";
        for (NotePair note : this.notes) {
            if (note.isNote(frequency)) {
                result = note.getName(nameIndex);
                break;
            }
        }
        return result;
    }
}
