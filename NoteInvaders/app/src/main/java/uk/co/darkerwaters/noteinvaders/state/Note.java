package uk.co.darkerwaters.noteinvaders.state;

import uk.co.darkerwaters.noteinvaders.PlayActivity;

public class Note extends Playable {

    final String[] names;
    final float frequency;
    final float upper;
    final float lower;

    public Note(String name, float frequency, float lower, float upper) {
        this.names = name.split("/");
        this.frequency = frequency;
        this.lower = lower;
        this.upper = upper;
    }

    public boolean isNote(String nameToTest) {
        boolean result = false;
        for (String name : this.names) {
            if (name.equalsIgnoreCase(nameToTest)) {
                result = true;
                break;
            }
        }
        return result;
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

    @Override
    public String getName() {
        StringBuilder name = new StringBuilder();
        for (String entry : this.names) {
            if (name.length() > 0) {
                name.append('/');
            }
            name.append(entry);
        }
        return name.toString();
    }

    @Override
    public String getName(int index) {
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

    @Override
    public int getNameCount() {
        return this.names.length;
    }

    public float getFrequency() {
        return this.frequency;
    }

    public Note getLowest() {
        // just the one
        return this;
    }
    public Note getHighest() {
        // just the one
        return this;
    }

    @Override
    public int getNoteCount() {
        return 1;
    }

    @Override
    public Note getNote(int index) {
        return this;
    }

    public float getFrequencyUpper() {
        return this.upper;
    }

    public float getFrequencyLower() {
        return this.lower;
    }

    public boolean isSharp() {
        boolean isSharpFound = false;
        for (String name : this.names) {
            if (name.contains("#")) {
                isSharpFound = true;
                break;
            }
        }
        return isSharpFound;
    }

    public char getSharpPrimative() {
        boolean isSharpFound = false;
        char notePrimative = 0;
        for (String name : this.names) {
            if (name.contains("#")) {
                notePrimative = name.charAt(0);
                break;
            }
        }
        return notePrimative;
    }

    public String getNameSharp() {
        String toReturn = "";
        for (String name : this.names) {
            if (name.contains("#")) {
                toReturn = name;
                break;
            }
        }
        return toReturn;
    }

    public boolean isFlat() {
        boolean isFlatFound = false;
        for (String name : this.names) {
            if (name.contains("b")) {
                isFlatFound = true;
                break;
            }
        }
        return isFlatFound;
    }

    public char getFlatPrimative() {
        char notePrimative = 0;
        for (String name : this.names) {
            if (name.contains("b")) {
                notePrimative = name.charAt(0);
                break;
            }
        }
        return notePrimative;
    }

    public String getNameFlat() {
        String toReturn = "";
        for (String name : this.names) {
            if (name.contains("b")) {
                toReturn = name;
                break;
            }
        }
        return toReturn;
    }

    public char getNotePrimative() {
        return this.names[0].charAt(0);
    }

    public int getNotePrimativeIndex() {
        switch (this.names[0].charAt(0)) {
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
        return Integer.parseInt("" + this.names[0].charAt(this.names[0].length() - 1));
    }

    @Override
    public int compareTo(Playable note) {
        if (note instanceof Note) {
            return (int) ((this.getFrequency() * 100000.0) - (((Note) note).getFrequency() * 100000.0));
        }
        else {
            return 0;
        }
    }
}
