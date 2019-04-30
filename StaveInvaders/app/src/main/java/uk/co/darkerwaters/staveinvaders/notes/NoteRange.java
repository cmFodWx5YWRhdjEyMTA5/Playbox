package uk.co.darkerwaters.staveinvaders.notes;

public class NoteRange {
    private Note start;
    private Note end;

    public NoteRange(Note start, Note end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        String range = "--";
        if (null != this.start && null != this.end) {
            range = this.start.getName() + " -- " + this.end.getName();
        }
        return range;
    }

    public Note getStart() {
        return this.start;
    }

    public Note getEnd() {
        return this.end;
    }

    public void setStart(Note start) {
        this.start = start;
    }

    public void setEnd(Note end) {
        this.end = end;
    }

    public boolean contains(Note note) {
        if (null == note) {
            return false;
        }
        else {
            return (null == this.start || note.getFrequency() >= this.start.getFrequency()) &&
                    (null == this.end || note.getFrequency() <= this.end.getFrequency());
        }
    }
}
