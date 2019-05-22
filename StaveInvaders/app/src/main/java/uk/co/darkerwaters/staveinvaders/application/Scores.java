package uk.co.darkerwaters.staveinvaders.application;

import android.content.SharedPreferences;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.notes.Clef;
import uk.co.darkerwaters.staveinvaders.games.Game;

public class Scores {

    private final SharedPreferences preferences;
    private final Application application;
    private final SharedPreferences.Editor editor;

    public Scores(Application app) {
        // get all the variables
        this.application = app;
        this.preferences = this.application.getSharedPreferences("Scores", 0); // 0 - for private mode
        this.editor = this.preferences.edit();

        // initialise all the scores
        Log.debug("Scores initialised...");
    }

    public class Score {
        private final int[] topBpm;
        private final Game game;

        Score(Game game) {
            this.game = game;
            this.topBpm = new int[Clef.values().length];
            for (int i = 0; i < this.topBpm.length; ++i) {
                this.topBpm[i] = 0;
            }
        }

        Game getGame() {
            return this.game;
        }

        public void setTopBpm(Clef clef, int bpm) {
            // set this
            this.topBpm[clef.val] = bpm;
        }

        public int getTopBpm(Clef clef) {
            return this.topBpm[clef.val];
        }
    }

    public Score getScore(Game game) {
        // get the score for this game, create the class
        Score score = new Score(game);
        // and set the data on this score class
        for (Clef clef : Clef.values()) {
            int topBpm = this.preferences.getInt(game.getFullName() + clef.name(), 0);
            score.setTopBpm(clef, topBpm);
        }
        // and return the score
        return score;
    }

    public void setScore(Score score) {
        // set the score in the preferences
        for (Clef clef : Clef.values()) {
            // put the score in for each clef against the name of the game
            this.editor.putInt(score.getGame().getFullName() + clef.name(), score.getTopBpm(clef));
        }
        // and commit these scores now
        this.editor.commit();
    }
}
