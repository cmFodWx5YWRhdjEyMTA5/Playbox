package uk.co.darkerwaters.scorepal.activities;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.ProgressBar;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.controllers.Controller;
import uk.co.darkerwaters.scorepal.controllers.TapController;

public class ControllerTapSettingsActivity extends AppCompatActivity implements TapController.TapControllerRawListener {

    private ProgressBar dXProgress;
    private ProgressBar dYProgress;
    private ProgressBar dZProgress;

    private ProgressBar rXProgress;
    private ProgressBar rYProgress;
    private ProgressBar rZProgress;

    private TextView currentX, currentY, currentZ, currentRX, currentRY, currentRZ, detectionTitle;

    private ToneGenerator toneGen1;

    private TapController tapController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller_tap_settings);
        initializeViews();

        this.tapController = new TapController(this);
        this.tapController.addListener(this);
        this.tapController.addListener(new Controller.ControllerListener() {
            @Override
            public void onControllerInput(Controller.InputType type) {
                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
            }
        });

        this.toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        //TODO - wait for a period of silence followed by just dX + rX + rY and not the others

    }

    public void initializeViews() {
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);

        currentRX = (TextView) findViewById(R.id.currentRX);
        currentRY = (TextView) findViewById(R.id.currentRY);
        currentRZ = (TextView) findViewById(R.id.currentRZ);

        detectionTitle = findViewById(R.id.detectionIndex);

        dXProgress = findViewById(R.id.dXProgress);
        dYProgress = findViewById(R.id.dYProgress);
        dZProgress = findViewById(R.id.dZProgress);

        rXProgress = findViewById(R.id.rXProgress);
        rYProgress = findViewById(R.id.rYProgress);
        rZProgress = findViewById(R.id.rZProgress);
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.tapController.start();
    }

    @Override
    public void onTapControllerDataUpdate(
            final float tx,
            final float ty,
            final float tz,
            final float rx,
            final float ry,
            final float rz,
            final int detectionIndex) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detectionTitle.setText(Integer.toString(detectionIndex));

                currentX.setText(Float.toString(tx));
                currentY.setText(Float.toString(ty));
                currentZ.setText(Float.toString(tz));

                currentRX.setText(Float.toString(rx));
                currentRY.setText(Float.toString(ry));
                currentRZ.setText(Float.toString(rz));

                dXProgress.setProgress((int)(tx) + 50);
                dYProgress.setProgress((int)(ty) + 50);
                dZProgress.setProgress((int)(tz) + 50);

                rXProgress.setProgress((int)(rx * 10) + 50);
                rYProgress.setProgress((int)(ry * 10) + 50);
                rZProgress.setProgress((int)(rz * 10) + 50);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.tapController.stop();
    }
}
