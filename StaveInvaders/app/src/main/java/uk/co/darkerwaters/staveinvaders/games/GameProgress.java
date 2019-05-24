package uk.co.darkerwaters.staveinvaders.games;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Clef;

public class GameProgress {

    public static final int K_LEVEL_POINTS_GOAL = 250;
    public static final int K_LIVES = 5;
    public static final int K_SHOTS = 10;
    public static final int K_MAX_HELP_TEMPO = GameScore.K_BPMS[4];

    private int tempo = GameScore.K_DEFAULT_BPM;
    private boolean isHelpOn = true;
    private int maxTempo = 0;
    private int livesLeft = 0;
    private int shotsLeft = 0;

    private class Points {
        final Clef clef;
        int points;
        Points(Clef clef) {
            this.clef = clef;
            this.points = 0;
        }
    }

    public enum Type {
        gameStarted,
        lifeLost,
        shotLost,
        targetHit,
        tempoIncrease,
        lettersDisabled,
        gameOver
    }

    private Points[] points = new Points[Clef.values().length];

    public interface GameProgressListener {
        void onGameProgressChanged(GameProgress source, Type changeType, Object data);
    }

    private final List<GameProgressListener> listeners;

    public GameProgress() {
        this.listeners = new ArrayList<GameProgressListener>();
    }

    public void startNewGame(int tempo, boolean isHelpOn) {
        // start the new game, reset the counters etc
        this.tempo = tempo;
        this.isHelpOn = isHelpOn;
        this.maxTempo = 0;
        this.livesLeft = K_LIVES;
        this.shotsLeft = K_SHOTS;
        // clear the points
        clearPointsAccumulation();
        // inform the listeners of the game state
        informListeners(Type.gameStarted, null);
    }

    private void clearPointsAccumulation() {
        // clear the points out
        for (int i = 0; i < this.points.length; ++i) {
            this.points[i] = new Points(Clef.values()[i]);
        }
    }

    public int getPoints() {
        int score = 0;
        for (Points point : this.points) {
            if (null != point) {
                score += point.points;
            }
        }
        return score;
    }

    private void informListeners(Type type, Object data) {
        synchronized (this.listeners) {
            for (GameProgressListener listener : this.listeners) {
                listener.onGameProgressChanged(this, type, data);
            }
        }
    }

    public boolean isGameActive() {
        return this.livesLeft > 0 && this.shotsLeft > 0 && this.maxTempo < GameScore.K_MAX_BPM;
    }

    public boolean isGameWon() {
        return this.maxTempo == GameScore.K_MAX_BPM && this.livesLeft > 0 && this.shotsLeft > 0;
    }

    public int getLivesLeft() {
        return this.livesLeft;
    }

    public int getShotsLeft() {
        return this.shotsLeft;
    }

    public boolean addListener(GameProgressListener listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    public boolean removeListener(GameProgressListener listener) {
        synchronized (this.listeners) {
            return this.listeners.remove(listener);
        }
    }

    public void setTempo(int tempo) {
        this.tempo = tempo;
    }

    public int getTempo() {
        return this.tempo;
    }

    public float getBeatsPerSecond() {
        return this.tempo / 60f;
    }

    public void setIsHelpOn(boolean isHelpOn) {
        this.isHelpOn = isHelpOn;
    }

    public boolean getIsHelpOn() {
        return this.isHelpOn;
    }

    private void increaseTempo() {
        int tempoIndex = 0;
        // the current tempo was completed (yey)
        this.maxTempo = this.tempo;
        for (int i = 0; i < GameScore.K_BPMS.length; ++i) {
            if (this.tempo >= GameScore.K_BPMS[i]) {
                tempoIndex = i;
            }
        }
        if (tempoIndex == GameScore.K_BPMS.length - 1) {
            // this is the final victory
            informListeners(Type.gameOver, null);
        }
        else {
            // move on a tempo
            Type type;
            if (isHelpOn && this.tempo >= K_MAX_HELP_TEMPO) {
                // we are going too quick to allow help, get rid of it
                this.isHelpOn = false;
                type = Type.lettersDisabled;
                // but to be nice, don't speed up
            }
            else {
                // increase the speed instead
                this.tempo = GameScore.K_BPMS[tempoIndex + 1];
                type = Type.tempoIncrease;
            }
            // clear the points to start this new level
            clearPointsAccumulation();
            // inform listeners of this setting change
            informListeners(type, null);
        }
    }

    public void recordHit(Clef clef, Chord chord, float offsetBeats) {
        // count the hits to see when we can progress the tempo
        // the tempo is the same as us, whatever, it is the seconds and the clef we are interested
        Points point = this.points[clef.val];
        // the sooner they hit the note, the more points they get
        point.points += offsetBeats;
        // this is a hit
        informListeners(Type.targetHit, chord);
        // do we need to change the level now?
        if (getPoints() >= K_LEVEL_POINTS_GOAL) {
            // have exceeded or met the goal, move on the tempo
            increaseTempo();
        }
    }

    public void recordMiss(Clef clef, Chord chord) {
        // this causes a loss of a life
        --this.livesLeft;
        // inform listeners of this
        informListeners(Type.lifeLost, chord);
        if (false == isGameActive()) {
            // game over (lose)
            informListeners(Type.gameOver, null);
        }
    }

    public void recordMissire(Clef clef, Chord target, Chord actual) {
        // this causes a loss of a shot
        --this.shotsLeft;
        // inform listeners of this
        informListeners(Type.shotLost, new Chord[] {target, actual});
        if (false == isGameActive()) {
            // game over (lose)
            informListeners(Type.gameOver, null);
        }
    }
}
