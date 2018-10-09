package uk.co.darkerwaters.noteinvaders;

import android.Manifest;
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
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.State;
import uk.co.darkerwaters.noteinvaders.state.input.InputConnectionInterface;
import uk.co.darkerwaters.noteinvaders.state.input.InputMicrophone;
import uk.co.darkerwaters.noteinvaders.views.PianoView;
import uk.co.darkerwaters.noteinvaders.views.ScoreActiveView;

public class ScoreCardActivity extends AppCompatActivity  {

    private ScoreActiveView scoreView;

    private TextView missedNumberText;
    private TextView falseFiresNumberText;
    private TextView missedLabelText;
    private TextView falseFiresLabelText;

    private TextView topBpmNumber;
    private TextView helpOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecard);

        ActiveScore score = State.getInstance().getCurrentActiveScore();

        this.scoreView = (ScoreActiveView) findViewById(R.id.score_view);
        this.scoreView.setScore(score);

        // get the controls
        this.missedNumberText = (TextView) findViewById(R.id.missed_notes_number);
        this.missedLabelText = (TextView) findViewById(R.id.missed_notes_label);
        this.falseFiresNumberText = (TextView) findViewById(R.id.false_fires_number);
        this.falseFiresLabelText = (TextView) findViewById(R.id.false_fires_label);

        this.topBpmNumber = (TextView) findViewById(R.id.top_bpm_number);
        this.helpOn = (TextView) findViewById(R.id.help_on_off);

        // set all the data
        this.missedNumberText.setTextColor(getResources().getColor(R.color.colorMiss));
        this.missedLabelText.setTextColor(getResources().getColor(R.color.colorMiss));
        this.missedNumberText.setText(Integer.toString(score.getMisses()));

        this.falseFiresNumberText.setTextColor(getResources().getColor(R.color.colorFalseFire));
        this.falseFiresLabelText.setTextColor(getResources().getColor(R.color.colorFalseFire));
        this.falseFiresNumberText.setText(Integer.toString(score.getFalseShots()));

        this.topBpmNumber.setText(Integer.toString(score.getTopBpm()));
        this.helpOn.setText(score.isHelpOn() ? R.string.on : R.string.off);
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
