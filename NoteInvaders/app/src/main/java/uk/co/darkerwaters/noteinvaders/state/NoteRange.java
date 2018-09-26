package uk.co.darkerwaters.noteinvaders.state;

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
            range = this.start.getName(0) + " -- " + this.end.getName(0);
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
}
