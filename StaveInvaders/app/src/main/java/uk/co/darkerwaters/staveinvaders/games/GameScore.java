package uk.co.darkerwaters.staveinvaders.games;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Clef;

public class GameScore {

    public static final int[] K_BPMS = {20,40,60,80,100,130,150,180};
    public static final int K_MIN_BPM = K_BPMS[0];
    public static final int K_MAX_BPM = K_BPMS[K_BPMS.length - 1];

    public static final int K_DEFAULT_BPM = K_BPMS[2];
    public static final int K_PASS_BPM = K_BPMS[4];
    public static final float K_PASS_BPM_FACTOR = 0.50f;

    private final Game game;

    private class Hit {
        final Chord target;
        final Clef clef;
        int count;
        Hit(Clef clef, Chord target) {
            this.clef = clef;
            this.target = target;
            count = 1;
        }
    }

    private class Miss {
        final Chord target;
        final Clef clef;
        final List<Hit> actuals;
        Miss(Clef clef, Chord target, Chord actual) {
            this.target = target;
            this.clef = clef;
            this.actuals = new ArrayList<Hit>();
            // add the actual to the list
            this.actuals.add(new Hit(clef, actual));
        }
        void addActual(Clef clef, Chord actual) {
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
                this.actuals.add(new Hit(clef, actual));
            }
            else {
                // increment
                ++hit.count;
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

    public int getMissfireCount(int tempo) {
        int missCount = 0;
        for (Miss miss : getMissFireList(tempo)) {
            missCount += miss.getMissCount();
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

    public int getMissfireCount(Clef clef, int tempo) {
        int missCount = 0;
        for (Miss miss : getMissFireList(tempo)) {
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
            hit = new Hit(clef, chord);
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
            miss = new Hit(clef, chord);
            missList.add(miss);
        }
        else {
            // increment the counter
            ++miss.count;
        }
    }

    public void recordMissfire(Clef clef, int tempo, Chord target, Chord actual) {
        Miss miss = null;
        List<Miss> missList = getMissFireList(tempo);
        for (Miss existing : missList) {
            if (existing.clef == clef && existing.target.equals(target)) {
                // this is it
                miss = existing;
                break;
            }
        }
        if (null == null) {
            // create new
            miss = new Miss(clef, target, actual);
            missList.add(miss);
        }
        else {
            // increment counter for the actual
            miss.addActual(clef, actual);
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

    private List<Miss> getMissFireList(int tempo) {
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
