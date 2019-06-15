package uk.co.darkerwaters.scorepal.score;

public enum TennisSets {
    FIVE(5),
    THREE(3),
    ONE(1);

    public final int val;
    final int target;

    TennisSets(int value) {
        this.val = value;
        this.target = (int)((value + 1f) / 2f);
    }

    public static TennisSets fromValue(int setsValue) {
        for (TennisSets set : TennisSets.values()) {
            if (set.val == setsValue) {
                // this is it
                return set;
            }
        }
        // oops
        return FIVE;
    }

    public boolean isFirst() {
        return this == ONE;
    }

    public boolean isLast() {
        return this == FIVE;
    }

    public TennisSets prev() {
        switch (this) {
            case ONE:
            case THREE:
                return ONE;
            case FIVE:
                return THREE;
        }
        // something very wrong here
        return this;
    }

    public TennisSets next() {
        switch (this) {
            case ONE:
                return THREE;
            case THREE:
            case FIVE:
                return FIVE;
        }
        // something very wrong here
        return this;
    }
}
