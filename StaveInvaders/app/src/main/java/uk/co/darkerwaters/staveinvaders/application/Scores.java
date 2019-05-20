package uk.co.darkerwaters.staveinvaders.application;

import android.content.SharedPreferences;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.games.Game;
import uk.co.darkerwaters.staveinvaders.views.MusicView;

public class Scores {

    public static final int[] K_BPMS = {20,40,60,80,100,130,150,180};
    public static final int K_MIN_BPM = K_BPMS[0];
    public static final int K_MAX_BPM = K_BPMS[K_BPMS.length - 1];

    public static final int K_PASS_BPM = 100;
    public static final float K_PASS_BPM_FACTOR = 0.50f;

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
            this.topBpm = new int[MusicView.Clefs.values().length];
            for (int i = 0; i < this.topBpm.length; ++i) {
                this.topBpm[i] = 0;
            }
        }

        Game getGame() {
            return this.game;
        }

        public void setTopBpm(MusicView.Clefs clef, int bpm) {
            // set this
            this.topBpm[clef.val] = bpm;
        }

        public int getTopBpm(MusicView.Clefs clef) {
            return this.topBpm[clef.val];
        }
    }

    public Score getScore(Game game) {
        // get the score for this game, create the class
        Score score = new Score(game);
        // and set the data on this score class
        for (MusicView.Clefs clef : MusicView.Clefs.values()) {
            int topBpm = this.preferences.getInt(game.getFullName() + clef.name(), 0);
            score.setTopBpm(clef, topBpm);
        }
        // and return the score
        return score;
    }

    public void setScore(Score score) {
        // set the score in the preferences
        for (MusicView.Clefs clef : MusicView.Clefs.values()) {
            // put the score in for each clef against the name of the game
            this.editor.putInt(score.getGame().getFullName() + clef.name(), score.getTopBpm(clef));
        }
        // and commit these scores now
        this.editor.commit();
    }
}
