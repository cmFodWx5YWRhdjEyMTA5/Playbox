package uk.co.darkerwaters.noteinvaders.state;

public class ActiveScore {

    public static final int K_PERMITTED_MISS_COUNT = 10;
    public static final int K_PERMITTED_FALSE_SHOT_COUNT = 10;
    public static final int K_PERMITTED_ERRORS = K_PERMITTED_MISS_COUNT + K_PERMITTED_FALSE_SHOT_COUNT;

    private int hits;
    private int misses;
    private int falseShots;

    public ActiveScore() {
        this.hits = 0;
        this.misses = 0;
        this.falseShots = 0;
    }

    public int getMisses() {
        return this.misses;
    }

    public int getFalseShots() {
        return this.falseShots;
    }

    public int getHits() {
        return this.hits;
    }

    public int incHits() {
        return ++this.hits;
    }

    public int incMisses() {
        return ++this.misses;
    }

    public int incFalseShots() {
        return ++this.falseShots;
    }
}
