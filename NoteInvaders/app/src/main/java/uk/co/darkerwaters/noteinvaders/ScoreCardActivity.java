package uk.co.darkerwaters.noteinvaders;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import uk.co.darkerwaters.noteinvaders.state.ActiveScore;
import uk.co.darkerwaters.noteinvaders.state.Game;
import uk.co.darkerwaters.noteinvaders.state.Note;

import uk.co.darkerwaters.noteinvaders.state.input.InputConnectionInterface;
import uk.co.darkerwaters.noteinvaders.state.input.InputMicrophone;
import uk.co.darkerwaters.noteinvaders.views.PianoView;
import uk.co.darkerwaters.noteinvaders.views.ScoreActiveView;

public class ScoreCardActivity extends AppCompatActivity  {

    private ScoreActiveView scoreView;

    private TextView scoreText;
    private TextView missedNumberText;
    private TextView falseFiresNumberText;
    private TextView missedLabelText;
    private TextView falseFiresLabelText;

    private TextView topBpmNumber;
    private TextView helpOn;

    private Button nextButton;
    private Button againButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecard);

        ActiveScore score = NoteInvaders.getAppContext().getCurrentActiveScore();

        this.scoreView = (ScoreActiveView) findViewById(R.id.score_view);
        this.scoreView.setScore(score);

        // get the controls
        this.scoreText = (TextView) findViewById(R.id.score_text);
        this.missedNumberText = (TextView) findViewById(R.id.missed_notes_number);
        this.missedLabelText = (TextView) findViewById(R.id.missed_notes_label);
        this.falseFiresNumberText = (TextView) findViewById(R.id.false_fires_number);
        this.falseFiresLabelText = (TextView) findViewById(R.id.false_fires_label);

        this.topBpmNumber = (TextView) findViewById(R.id.top_bpm_number);
        this.helpOn = (TextView) findViewById(R.id.help_on_off);

        this.nextButton = (Button) findViewById(R.id.button_play_next);
        this.againButton = (Button) findViewById(R.id.button_play_again);

        this.scoreText.setText(Integer.toString(score.getScorePercent()) + "%");

        // set all the data
        this.missedNumberText.setTextColor(getResources().getColor(R.color.colorMiss));
        this.missedLabelText.setTextColor(getResources().getColor(R.color.colorMiss));
        this.missedNumberText.setText(Integer.toString(score.getMisses()));

        this.falseFiresNumberText.setTextColor(getResources().getColor(R.color.colorFalseFire));
        this.falseFiresLabelText.setTextColor(getResources().getColor(R.color.colorFalseFire));
        this.falseFiresNumberText.setText(Integer.toString(score.getFalseShots()));

        this.topBpmNumber.setText(Integer.toString(score.getTopBpmCompleted()));
        this.helpOn.setText(score.isHelpOn() ? R.string.on : R.string.off);

        if (null != NoteInvaders.getAppContext().getNextGame()) {
            this.nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Game nextGame = NoteInvaders.getAppContext().getNextGame();
                    if (null != nextGame && nextGame.isPlayable()) {
                        // clear the current score
                        NoteInvaders.getAppContext().getCurrentActiveScore().reset();
                        // set the selected game to the next one
                        NoteInvaders.getAppContext().selectGame(nextGame);
                        // show the card for this game by finishing this activity
                        finish();
                    }
                }
            });
        }
        else {
            this.nextButton.setEnabled(false);
        }
        // play the game again
        this.againButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the current game
                Game game = NoteInvaders.getAppContext().getGameSelected();
                if (null != game && game.isPlayable()) {
                    // clear the current score
                    NoteInvaders.getAppContext().getCurrentActiveScore().reset();
                    // show the card for this game by finishing this activity
                    finish();

                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {


        super.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
