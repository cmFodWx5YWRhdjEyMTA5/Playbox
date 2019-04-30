package uk.co.darkerwaters.staveinvaders.notes;

public class Note {

    final String name;
    final float frequency;
    final float upper;
    final float lower;

    public Note(String name, float frequency, float lower, float upper) {
        this.name = name;
        this.frequency = frequency;
        this.lower = lower;
        this.upper = upper;
    }

    public boolean isNote(String nameToTest) {
        return this.name.equalsIgnoreCase(nameToTest);
    }

    public boolean isNote(float frequency) {
        return frequency > this.lower && frequency < this.upper;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || false == obj instanceof Note) {
            // comparison is not a note
            return false;
        }
        else {
            // compare root frequencies
            Note toCompare = (Note) obj;
            return toCompare.frequency == this.frequency;
        }
    }

    public String getName() {
        return this.name;
    }

    public float getFrequency() {
        return this.frequency;
    }

    public float getFrequencyUpper() {
        return this.upper;
    }

    public float getFrequencyLower() {
        return this.lower;
    }

    public boolean isSharp() {
        return this.name.contains("#");
    }

    public char getSharpPrimative() {
        char notePrimative = 0;
        if (this.name.contains("#")) {
            notePrimative = this.name.charAt(0);
        }
        return notePrimative;
    }

    public boolean isFlat() {
        return this.name.contains("b");
    }

    public char getFlatPrimative() {
        char notePrimative = 0;
        if (this.name.contains("b")) {
            notePrimative = this.name.charAt(0);
        }
        return notePrimative;
    }

    public int getNotePrimativeIndex() {
        switch (this.name.charAt(0)) {
            case 'A':
                return 0;
            case 'B':
                return 1;
            case 'C':
                return 2;
            case 'D':
                return 3;
            case 'E':
                return 4;
            case 'F':
                return 5;
            case 'G':
                return 6;
            default:
                return -1;
        }
    }

    public int getNoteScaleIndex() {
        return Integer.parseInt("" + this.name.charAt(this.name.length() - 1));
    }
}
