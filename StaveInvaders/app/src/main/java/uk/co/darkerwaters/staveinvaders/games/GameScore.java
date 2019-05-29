package uk.co.darkerwaters.staveinvaders.games;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Clef;

public class GameScore implements Serializable {

    public static final int[] K_BPMS = {20,40,60,80,100,130,150,180};
    public static final int K_MIN_BPM = K_BPMS[0];
    public static final int K_MAX_BPM = K_BPMS[K_BPMS.length - 1];

    public static final int K_DEFAULT_BPM = K_BPMS[2];
    public static final int K_PASS_BPM = K_BPMS[4];
    public static final float K_PASS_BPM_FACTOR = 0.50f;

    private static GameScore LAST_INSTANCE = null;

    public static GameScore GetLastInstance(boolean isClear) {
        GameScore instance = LAST_INSTANCE;
        if (isClear) {
            LAST_INSTANCE = null;
        }
        return instance;
    }

    private final Game game;

    public class Hit {
        public final Chord target;
        public final Clef clef;
        public int count;
        Hit(Clef clef, Chord target, int count) {
            this.clef = clef;
            this.target = target;
            this.count = count;
        }
    }

    public class Miss {
        public final Chord target;
        public final Clef clef;
        public final List<Hit> actuals;
        Miss(Clef clef, Chord target, Chord actual) {
            this.target = target;
            this.clef = clef;
            this.actuals = new ArrayList<Hit>();
            // add the actual to the list
            this.actuals.add(new Hit(clef, actual, 1));
        }

        public void addActual(Clef clef, Chord actual, int count) {
            Hit hit = null;
            for (Hit existing : this.actuals) {
                if (existing.target == actual && existing.clef == clef) {
                    // this is the existing one
                    hit = existing;
                    break;
                }
            }
            if (null == hit) {
                // create new
                this.actuals.add(new Hit(clef, actual, count));
            }
            else {
                // increment the specified amount
                hit.count += count;
            }
        }

        public int getMissCount() {
            int missCount = 0;
            for (Hit actual : this.actuals) {
                missCount += actual.count;
            }
            return missCount;
        }
    }

    private final List<Hit>[] hits = new List[K_BPMS.length];
    private final List<Hit>[] misses = new List[K_BPMS.length];
    private final List<Miss>[] missFires = new List[K_BPMS.length];

    public GameScore(Game game) {
        LAST_INSTANCE = this;

        this.game = game;

        for (int i = 0; i < K_BPMS.length; ++i) {
            hits[i] = new ArrayList<Hit>();
            misses[i] = new ArrayList<Hit>();
            missFires[i] = new ArrayList<Miss>();
        }
    }

    public int getHitCount(int tempo) {
        // return the number of hits only
        int hitCount = 0;
        for (Hit hit : getHitList(tempo)) {
            hitCount += hit.count;
        }
        return hitCount;
    }

    public int getMissCount(int tempo) {
        // return the number of misses only
        int missCount = 0;
        for (Hit hit : getMissList(tempo)) {
            missCount += hit.count;
        }
        return missCount;
    }

    public int getMisfireCount(int tempo) {
        int missCount = 0;
        for (Miss miss : getMisfireList(tempo)) {
            missCount += miss.getMissCount();
        }
        return missCount;
    }

    public int getHitCount() {
        // return the total number of hits made
        int hitCount = 0;
        for (int tempo : K_BPMS) {
            hitCount += getHitCount(tempo);
        }
        return hitCount;
    }

    public int getHitCount(Clef clef) {
        // return the number of hits for this clef
        int hitCount = 0;
        for (int tempo : K_BPMS) {
            hitCount += getHitCount(clef, tempo);
        }
        return hitCount;
    }

    public int getMissCount(Clef clef) {
        // return the number of misses for this clef
        int missCount = 0;
        for (int tempo : K_BPMS) {
            missCount += getMissCount(clef, tempo);
        }
        return missCount;
    }

    public int getMisfireCount(Clef clef) {
        // return the number of misfires for this clef
        int missCount = 0;
        for (int tempo : K_BPMS) {
            missCount += getMisfireCount(clef, tempo);
        }
        return missCount;
    }

    public int getHitCount(Clef clef, int tempo) {
        // return the number of hits only
        int hitCount = 0;
        for (Hit hit : getHitList(tempo)) {
            if (hit.clef == clef) {
                hitCount += hit.count;
            }
        }
        return hitCount;
    }

    public int getMissCount(Clef clef, int tempo) {
        // return the number of misses only
        int missCount = 0;
        for (Hit miss : getMissList(tempo)) {
            if (miss.clef == clef) {
                missCount += miss.count;
            }
        }
        return missCount;
    }

    public int getMisfireCount(Clef clef, int tempo) {
        int missCount = 0;
        for (Miss miss : getMisfireList(tempo)) {
            if (miss.clef == clef) {
                missCount += miss.getMissCount();
            }
        }
        return missCount;
    }

    public void recordHit(Clef clef, int tempo, Chord chord) {
        // find the hit we already know about
        Hit hit = null;
        List<Hit> hitList = getHitList(tempo);
        for (Hit existing : hitList) {
            if (existing.clef == clef && existing.target.equals(chord)) {
                // this is it
                hit = existing;
                break;
            }
        }
        if (hit == null) {
            // create it
            hit = new Hit(clef, chord, 1);
            hitList.add(hit);
        }
        else {
            // increment the counter
            ++hit.count;
        }
    }

    public void recordMiss(Clef clef, int tempo, Chord chord) {
        // find the miss we already know about
        Hit miss = null;
        List<Hit> missList = getMissList(tempo);
        for (Hit existing : missList) {
            if (existing.clef == clef && existing.target.equals(chord)) {
                // this is it
                miss = existing;
                break;
            }
        }
        if (miss == null) {
            // create it
            miss = new Hit(clef, chord, 1);
            missList.add(miss);
        }
        else {
            // increment the counter
            ++miss.count;
        }
    }

    public void recordMisfire(Clef clef, int tempo, Chord target, Chord actual) {
        Miss miss = null;
        List<Miss> missList = getMisfireList(tempo);
        for (Miss existing : missList) {
            if (existing.clef == clef && existing.target.equals(target)) {
                // this is it
                miss = existing;
                break;
            }
        }
        if (miss == null) {
            // create new
            miss = new Miss(clef, target, actual);
            missList.add(miss);
        }
        else {
            // increment counter for the actual
            miss.addActual(clef, actual, 1);
        }
    }

    private List<Hit> getHitList(int tempo) {
        List<Hit> hitList = null;
        for (int i = 0; i < K_BPMS.length; ++i) {
            if (K_BPMS[i] >= tempo) {
                // this is it
                hitList = this.hits[i];
                break;
            }
        }
        return hitList;
    }

    private List<Hit> getMissList(int tempo) {
        List<Hit> missList = null;
        for (int i = 0; i < K_BPMS.length; ++i) {
            if (K_BPMS[i] >= tempo) {
                // this is it
                missList = this.misses[i];
                break;
            }
        }
        return missList;
    }

    public List<Hit> getMisses() {
        List<Hit> missList = new ArrayList<Hit>();
        for (int i = 0; i < K_BPMS.length; ++i) {
            // add all these for every entry
            missList.addAll(this.misses[i]);
        }
        return missList;
    }

    public List<Miss> getMisfires() {
        List<Miss> missList = new ArrayList<Miss>();
        for (int i = 0; i < K_BPMS.length; ++i) {
            // add all these for every entry
            missList.addAll(this.missFires[i]);
        }
        return missList;
    }

    private List<Miss> getMisfireList(int tempo) {
        List<Miss> missList = null;
        for (int i = 0; i < K_BPMS.length; ++i) {
            if (K_BPMS[i] >= tempo) {
                // this is it
                missList = this.missFires[i];
                break;
            }
        }
        return missList;
    }
}
