package uk.co.darkerwaters.noteinvaders.state;

import java.util.ArrayList;

public class Chord extends Playable  {

    private ArrayList<Note> notes = new ArrayList<Note>();
    private ArrayList<String> noteNames = new ArrayList<String>();
    private final String name;

    public Chord(String name) {
        this.name = name;
    }

    public Chord(String name, Note[] notes) {
        this.name = name;
        for (Note note : notes) {
            // for each passed, add the note
            addNote(note, note.getName());
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getName(int index) {
        return this.name;
    }

    @Override
    public int getNameCount() { return 1; }

    public boolean addNote(Note note, String noteName) {
        return this.notes.add(note) && this.noteNames.add(noteName);
    }

    @Override
    public int getNoteCount() {
        return this.notes.size();
    }

    @Override
    public Note getNote(int index) {
        return this.notes.get(index);
    }

    public String getNoteName(int index) {
        return this.noteNames.get(index);
    }

    public boolean contains(Note toCompare) {
        boolean isInList = false;
        for (Note inList : this.notes) {
            if (inList.equals(toCompare)) {
                // found
                isInList = true;
                break;
            }
        }
        return isInList;
    }

    public boolean contains(String noteName) {
        boolean isInList = false;
        for (String inList : this.noteNames) {
            if (inList.equals(noteName)) {
                // found
                isInList = true;
                break;
            }
        }
        return isInList;
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

    @Override
    public int compareTo(Playable note) {
        if (note instanceof Chord) {
            return (int) ((this.getLowest().getFrequency() * 100000.0) - (((Chord)note).getLowest().getFrequency() * 100000.0));
        }
        else {
            return 0;
        }
    }
}
