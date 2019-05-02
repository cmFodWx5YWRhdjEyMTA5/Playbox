package uk.co.darkerwaters.staveinvaders.notes;

public class Range {
    private Chord start;
    private Chord end;

    public Range(Chord start, Chord end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        String range = "--";
        if (null != this.start && null != this.end) {
            range = this.start.getTitle() + " -- " + this.end.getTitle();
        }
        return range;
    }

    public Chord getStart() {
        return this.start;
    }

    public Chord getEnd() {
        return this.end;
    }

    public void setStart(Chord start) {
        this.start = start;
    }

    public void setEnd(Chord end) {
        this.end = end;
    }
}
