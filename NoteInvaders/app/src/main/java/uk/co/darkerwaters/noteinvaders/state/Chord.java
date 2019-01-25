package uk.co.darkerwaters.noteinvaders.state;

import java.util.ArrayList;

public class Chord extends Playable  {

    private ArrayList<Note> notes = new ArrayList<Note>();
    private final String name;

    public Chord(String name) {
        this.name = name;
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

    public boolean addNote(Note note) {
        return this.notes.add(note);
    }

    @Override
    public int getNoteCount() {
        return this.notes.size();
    }

    @Override
    public Note getNote(int index) {
        return this.notes.get(index);
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
