package uk.co.darkerwaters.staveinvaders.application;

import android.content.SharedPreferences;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.games.GameScore;
import uk.co.darkerwaters.staveinvaders.notes.Clef;
import uk.co.darkerwaters.staveinvaders.games.Game;

public class Scores {

    private final SharedPreferences preferences;
    private final Application application;

    public Scores(Application app) {
        // get all the variables
        this.application = app;
        this.preferences = this.application.getSharedPreferences("Scores", 0); // 0 - for private mode

        // initialise all the scores
        Log.debug("Scores initialised...");
    }

    public class Score {
        private final int[] clefTopTempo;
        private final int[] clefTimesPlayed;
        private final boolean[] clefSkipped;
        private final Game game;

        Score(Game game) {
            this.game = game;
            int noClefs = Clef.values().length;
            this.clefTopTempo = new int[noClefs];
            this.clefTimesPlayed = new int[noClefs];
            this.clefSkipped = new boolean[noClefs];
            for (int i = 0; i < noClefs; ++i) {
                this.clefTopTempo[i] = 0;
                this.clefTimesPlayed[i] = 0;
                this.clefSkipped[i] = false;
            }
        }

        Game getGame() {
            return this.game;
        }

        public void setTopTempo(Clef clef, int bpm) {
            // set this
            this.clefTopTempo[clef.val] = bpm;
        }

        public int getTopTempo(Clef clef) {
            return this.clefTopTempo[clef.val];
        }

        public void setClefSkipped(Clef clef, boolean isSkipped) {
            // set this
            this.clefSkipped[clef.val] = isSkipped;
        }

        public boolean isClefSkipped(Clef clef) {
            return this.clefSkipped[clef.val];
        }

        public int incrementTimesPlayed(Clef clef) {
            return ++(this.clefTimesPlayed[clef.val]);
        }

        public void setTimesPlayed(Clef clef, int newTimesPlayed) {
            // set this
            this.clefTimesPlayed[clef.val] = newTimesPlayed;
        }

        public int getTimesPlayed(Clef clef) {
            return this.clefTimesPlayed[clef.val];
        }

        public boolean isClefPassed(Clef clef) {
            return getTopTempo(clef) > GameScore.K_PASS_BPM || isClefSkipped(clef);
        }
    }

    public Score getScore(Game game) {
        // get the score for this game, create the class
        Score score = new Score(game);
        // and set the data on this score class
        for (Clef clef : Clef.values()) {
            String scoreString = this.preferences.getString(game.getFullName() + clef.name(), "");
            parseScoreStringToScore(clef, scoreString, score);
        }
        // and return the score
        return score;
    }

    public void setScore(Score score) {
        // set the score in the preferences
        SharedPreferences.Editor editor = this.preferences.edit();
        for (Clef clef : Clef.values()) {
            // put the score in for each clef against the name of the game
            editor.putString(score.getGame().getFullName() + clef.name(), getScoreString(clef, score));
        }
        // and commit these scores now
        editor.commit();
    }

    private String getScoreString(Clef clef, Score score) {
        // return the score as a single string, the first is the top tempo
        StringBuilder builder = new StringBuilder(score.getTopTempo(clef));
        builder.append(",");
        // second is the times we played
        builder.append(score.getTimesPlayed(clef));
        builder.append(",");
        // and lastly is if we skipped this level
        builder.append(score.isClefSkipped(clef));
        return builder.toString();
    }

    private void parseScoreStringToScore(Clef clef, String scoreString, Score score) {
        String[] elements = scoreString.split(",");
        if (elements.length > 0) {
            // the first is the top tempo
            try {
                score.setTopTempo(clef, Integer.parseInt(elements[0]));
            }
            catch (Exception e) {
                Log.error("Failed to parse the score from the value " + elements[0], e);
            }
        }
        if (elements.length > 1) {
            // the second is the times we played
            try {
                score.setTimesPlayed(clef, Integer.parseInt(elements[1]));
            }
            catch (Exception e) {
                Log.error("Failed to parse the times played from the value " + elements[1], e);
            }
        }
        if (elements.length > 2) {
            // the third is if we have skipped this level
            try {
                score.setClefSkipped(clef, Boolean.parseBoolean(elements[2]));
            }
            catch (Exception e) {
                Log.error("Failed to parse the skipped from the value " + elements[2], e);
            }
        }
    }

    public void wipeAllScores() {
        SharedPreferences.Editor editor = this.preferences.edit();
        if (null != editor) {
            editor.clear().commit();
        }
    }
}
