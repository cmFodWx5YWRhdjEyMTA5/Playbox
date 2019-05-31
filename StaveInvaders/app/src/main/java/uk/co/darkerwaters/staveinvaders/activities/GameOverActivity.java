package uk.co.darkerwaters.staveinvaders.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.activities.handlers.MissedTargetRecyclerAdapter;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.application.Scores;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.games.Game;
import uk.co.darkerwaters.staveinvaders.games.GameList;
import uk.co.darkerwaters.staveinvaders.games.GameProgress;
import uk.co.darkerwaters.staveinvaders.games.GameScore;
import uk.co.darkerwaters.staveinvaders.notes.Clef;
import uk.co.darkerwaters.staveinvaders.views.ClefProgressView;

import static uk.co.darkerwaters.staveinvaders.activities.fragments.GameParentCardHolder.K_SELECTED_CARD_FULL_NAME;

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

    private TextView hitsText;

    private TextView trebleHitsText;
    private TextView trebleLivesLostText;
    private TextView trebleShotsMissedText;

    private TextView bassHitsText;
    private TextView bassLivesLostText;
    private TextView bassShotsMissedText;
    
    private ImageButton trebleMoreButton;
    private ImageButton bassMoreButton;

    private ClefProgressView trebleProgressView;
    private ClefProgressView trebleGlobalProgressView;
    private ClefProgressView bassProgressView;
    private ClefProgressView bassGlobalProgressView;

    private RecyclerView trebleNotesList;
    private MissedTargetRecyclerAdapter trebleNotesAdapter;
    private RecyclerView bassNotesList;
    private MissedTargetRecyclerAdapter bassNotesAdapter;

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

        this.hitsText = findViewById(R.id.hitsNumberText);
        this.trebleHitsText = findViewById(R.id.trebleHits);
        this.trebleLivesLostText = findViewById(R.id.trebleLivesLost);
        this.trebleShotsMissedText = findViewById(R.id.trebleShotsMissed);
        this.bassHitsText = findViewById(R.id.bassHits);
        this.bassLivesLostText = findViewById(R.id.bassLivesLost);
        this.bassShotsMissedText = findViewById(R.id.bassShotsMissed);

        this.trebleTempoSummary = findViewById(R.id.trebleClefSummary);
        this.trebleProgressView = findViewById(R.id.treble_progress_view);
        this.trebleSummaryText = findViewById(R.id.trebleTempoTextSummary);
        this.trebleGlobalProgressView = findViewById(R.id.treble_progress_view_global);

        this.bassTempoSummary = findViewById(R.id.bassClefSummary);
        this.bassProgressView = findViewById(R.id.bass_progress_view);
        this.bassSummaryText = findViewById(R.id.bassTempoTextSummary);
        this.bassGlobalProgressView = findViewById(R.id.bass_progress_view_global);

        int span;
        switch(getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                span = 2;
                break;
            default:
                span = 1;
                break;
        }

        this.trebleNotesAdapter = new MissedTargetRecyclerAdapter(this.application, this, Clef.treble);
        // setup the list
        this.trebleNotesList = findViewById(R.id.trebleNotesCardView);
        this.trebleNotesList.setLayoutManager(new GridLayoutManager(this, span));
        this.trebleNotesList.setAdapter(this.trebleNotesAdapter);

        this.bassNotesAdapter = new MissedTargetRecyclerAdapter(this.application, this, Clef.bass);
        // setup the list
        this.bassNotesList = findViewById(R.id.bassNotesCardView);
        this.bassNotesList.setLayoutManager(new GridLayoutManager(this, span));
        this.bassNotesList.setAdapter(this.bassNotesAdapter);

        this.trebleMoreButton = findViewById(R.id.trebleMoreButton);
        if (this.trebleNotesAdapter.isEmpty()) {
            this.trebleMoreButton.setVisibility(View.INVISIBLE);
        }
        else {
            this.trebleMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (trebleNotesList.getVisibility() == View.VISIBLE) {
                        trebleNotesList.setVisibility(View.GONE);
                        trebleMoreButton.setImageResource(R.drawable.ic_baseline_unfold_more_24px);
                    } else {
                        trebleNotesList.setVisibility(View.VISIBLE);
                        trebleMoreButton.setImageResource(R.drawable.ic_baseline_unfold_less_24px);
                    }
                }
            });
        }
        this.bassMoreButton = findViewById(R.id.bassMoreButton);
        if (this.bassNotesAdapter.isEmpty()) {
            this.bassMoreButton.setVisibility(View.INVISIBLE);
        }
        else {
            this.bassMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (bassNotesList.getVisibility() == View.VISIBLE) {
                        bassNotesList.setVisibility(View.GONE);
                        bassMoreButton.setImageResource(R.drawable.ic_baseline_unfold_more_24px);
                    } else {
                        bassNotesList.setVisibility(View.VISIBLE);
                        bassMoreButton.setImageResource(R.drawable.ic_baseline_unfold_less_24px);
                    }
                }
            });
        }

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

            this.hitsText.setText(Integer.toString(this.gameScore.getHitCount()));
            this.trebleHitsText.setText(Integer.toString(this.gameScore.getHitCount(Clef.treble)));
            this.trebleLivesLostText.setText(Integer.toString(this.gameScore.getMissCount(Clef.treble)));
            this.trebleShotsMissedText.setText(Integer.toString(this.gameScore.getMisfireCount(Clef.treble)));
            this.bassHitsText.setText(Integer.toString(this.gameScore.getHitCount(Clef.bass)));
            this.bassLivesLostText.setText(Integer.toString(this.gameScore.getMissCount(Clef.bass)));
            this.bassShotsMissedText.setText(Integer.toString(this.gameScore.getMisfireCount(Clef.bass)));

            // set the max tempo for treble and bass on the text (will only show those played)
            int maxTempo = this.gameProgress.getMaxTempo();
            this.trebleProgressView.setProgress(this.selectedGame.getGameProgress(maxTempo), Integer.toString(maxTempo));
            this.bassProgressView.setProgress(this.selectedGame.getGameProgress(maxTempo), Integer.toString(maxTempo));
            // so is this new? Check the score
            createSummaryText(maxTempo, score.getTopTempo(Clef.treble), this.trebleSummaryText);
            createSummaryText(maxTempo, score.getTopTempo(Clef.bass), this.bassSummaryText);

            // now we have compared etc, set the scores to the application for next time
            // for the clefs that are selected, set the new max on the score
            Clef[] selectedClefs = this.application.getSettings().getSelectedClefs();
            for (Clef clef : selectedClefs) {
                // for each clef we played, set the top BPM achieved
                int newMax = Math.max(maxTempo, score.getTopTempo(clef));
                score.setTopTempo(clef, newMax);
                int newTimesPlayed = score.incrementTimesPlayed(clef);
                // check this score is passed, or not annoying the player
                if (false == score.isClefPassed(clef) &&
                        newTimesPlayed > 0 &&
                        newTimesPlayed % 2 == GameScore.K_ASK_SKIP_TIMES) {
                    // this is a tenth time for a clef that is not already skipped
                    // and still is not passed
                    askUserAboutSkippingScore(score, clef);
                }
            }
            // and set this score back on the application so it is properly logged
            scores.setScore(score);

            // show the available clefs and set their progress for this data
            setAvailableClefs(settings.getSelectedClefs());
        }
    }

    private void askUserAboutSkippingScore(Scores.Score score, final Clef clef) {
        // prepare for the response
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked, skip this clef then please - first get the score that is
                        // set as this might have changed in the interveining time
                        Scores.Score score = application.getScores().getScore(selectedGame);
                        // set this to be skipped
                        score.setClefSkipped(clef, true);
                        // and put it back
                        application.getScores().setScore(score);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked, leave it alone until the next time they hit 10
                        break;
                }
            }
        };
        // show the dialog
        String message;
        switch (clef) {
            case treble:
                message = getResources().getString(R.string.doYouWantToSkipTreble);
                break;
            case bass: default:
                message = getResources().getString(R.string.doYouWantToSkipBass);
                break;
        }
        // format the numbers in here, the number of times and the level needed
        String messageString = String.format(message, score.getTimesPlayed(clef), GameScore.K_PASS_BPM);

        // show the dialog to ask if they are ready to skip this annoying level
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(messageString).setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();
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
                    this.bassTempoSummary.setVisibility(View.GONE);
                    break;
                case bass:
                    // show the progress for this only
                    this.trebleTempoSummary.setVisibility(View.GONE);
                    this.bassTempoSummary.setVisibility(View.VISIBLE);
                    break;
            }
        }
        // and the progress views
        this.trebleGlobalProgressView.setProgress(this.selectedGame, Clef.treble);
        // and bass
        this.bassGlobalProgressView.setProgress(this.selectedGame, Clef.bass);
    }
}
