package uk.co.darkerwaters.noteinvaders.state;

public abstract class Playable implements Comparable<Playable> {

    public abstract Note getLowest();
    public abstract Note getHighest();

    public abstract String getName();
    public abstract String getName(int index);
    public abstract int getNameCount();

    public abstract int getNoteCount();
    public abstract Note getNote(int index);

    public Note[] toNoteArray() {
        Note[] notes = new Note[getNoteCount()];
        for (int i = 0; i < notes.length; ++i) {
            notes[i] = getNote(i);
        }
        return notes;
    }

    public char getPrimative() {
        return getName().charAt(0);
    }

    @Override
    public String toString() {
        return getName();
    }
}
