package uk.co.darkerwaters.staveinvaders.actvities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.application.Scores;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.games.Game;
import uk.co.darkerwaters.staveinvaders.games.GameList;
import uk.co.darkerwaters.staveinvaders.games.GameProgress;
import uk.co.darkerwaters.staveinvaders.games.GameScore;
import uk.co.darkerwaters.staveinvaders.notes.Clef;
import uk.co.darkerwaters.staveinvaders.views.ClefProgressView;

import static uk.co.darkerwaters.staveinvaders.actvities.fragments.GameParentCardHolder.K_SELECTED_CARD_FULL_NAME;

public class GameOverActivity extends AppCompatActivity {
    private Application application;

    private Game selectedGame;

    private int maxTempo = 0;

    private Scores.Score score;
    private GameScore gameScore;
    private GameProgress gameProgress;

    private TextView trebleSummaryText;
    private TextView bassSummaryText;

    private TextView livesNumberText;
    private TextView shotsNumberText;

    private ClefProgressView trebleProgressView;
    private ClefProgressView trebleGlobalProgressView;
    private ClefProgressView bassProgressView;
    private ClefProgressView bassGlobalProgressView;

    private View trebleTempoSummary;
    private View bassTempoSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        this.application = (Application) this.getApplication();
        Settings settings = this.application.getSettings();
        Scores scores = this.application.getScores();

        // get the last instances of score and progress, these are our detailed data
        this.gameScore = GameScore.GetLastInstance(false);
        this.gameProgress = GameProgress.GetLastInstance(false);

        this.livesNumberText = findViewById(R.id.livesNumberText);
        this.shotsNumberText = findViewById(R.id.shotsNumberText);

        this.trebleTempoSummary = findViewById(R.id.trebleClefSummary);
        this.trebleProgressView = findViewById(R.id.treble_progress_view);
        this.trebleSummaryText = findViewById(R.id.trebleTempoTextSummary);
        this.trebleGlobalProgressView = findViewById(R.id.treble_progress_view_global);

        this.bassTempoSummary = findViewById(R.id.bassClefSummary);
        this.bassProgressView = findViewById(R.id.bass_progress_view);
        this.bassSummaryText = findViewById(R.id.bassTempoTextSummary);
        this.bassGlobalProgressView = findViewById(R.id.bass_progress_view_global);

        Intent intent = getIntent();
        String parentGameName = intent.getStringExtra(K_SELECTED_CARD_FULL_NAME);
        this.selectedGame = GameList.findLoadedGame(parentGameName);
        if (null == this.selectedGame) {
            Log.error("Game name of " + parentGameName + " does not correspond to a game");
        }
        else {
            // get the data we want to show here
            Scores.Score score = scores.getScore(this.selectedGame);

            this.livesNumberText.setText(Integer.toString(this.gameProgress.getLivesLeft()));
            this.shotsNumberText.setText(Integer.toString(this.gameProgress.getShotsLeft()));

            // set the max tempo for treble and bass on the text (will only show those played)
            int maxTempo = this.gameProgress.getMaxTempo();
            this.trebleProgressView.setProgress(this.selectedGame.getGameProgress(maxTempo), Integer.toString(maxTempo));
            this.bassProgressView.setProgress(this.selectedGame.getGameProgress(maxTempo), Integer.toString(maxTempo));
            // so is this new? Check the score
            createSummaryText(maxTempo, score.getTopBpm(Clef.treble), this.trebleSummaryText);
            createSummaryText(maxTempo, score.getTopBpm(Clef.bass), this.bassSummaryText);

            // now we have compared etc, set the scores to the application for next time
            // for the clefs that are selected, set the new max on the score
            for (Clef clef : this.application.getSettings().getSelectedClefs()) {
                // for each clef we played, set the top BPM achieved
                int newMax = Math.max(maxTempo, score.getTopBpm(clef));
                score.setTopBpm(clef, newMax);
            }
            // and set this score back on the application so it is properly logged
            scores.setScore(score);

            // show the available clefs and set their progress for this data
            setAvailableClefs(settings.getSelectedClefs());
        }
    }

    private void createSummaryText(int maxTempo, int topBpm, TextView summaryText) {
        if (maxTempo > topBpm) {
            // this is a new high for the score
            summaryText.setText(R.string.new_max);
        }
        else {
            // show the actual max
            summaryText.setText(getResources().getText(R.string.previously) + " " + topBpm);
        }
    }

    private void setAvailableClefs(Clef[] selectedClefs) {
        // show the correct controls for the clefs they played
        if (selectedClefs.length == 2) {
            // both are selected
            this.trebleTempoSummary.setVisibility(View.VISIBLE);
            this.bassTempoSummary.setVisibility(View.VISIBLE);

        }
        else if (selectedClefs.length == 1) {
            switch (selectedClefs[0]) {
                case treble:
                    // show the progress for this only
                    this.trebleTempoSummary.setVisibility(View.VISIBLE);
                    this.bassTempoSummary.setVisibility(View.INVISIBLE);
                    break;
                case bass:
                    // show the progress for this only
                    this.trebleTempoSummary.setVisibility(View.INVISIBLE);
                    this.bassTempoSummary.setVisibility(View.VISIBLE);
                    break;
            }
        }
        // and the progress views
        this.trebleGlobalProgressView.setProgress(this.selectedGame, Clef.treble);
        // and bass
        this.bassProgressView.setProgress(this.selectedGame, Clef.bass);
    }
}
