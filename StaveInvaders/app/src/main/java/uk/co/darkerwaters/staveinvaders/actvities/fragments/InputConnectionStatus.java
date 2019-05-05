package uk.co.darkerwaters.staveinvaders.actvities.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.input.Input;
import uk.co.darkerwaters.staveinvaders.notes.Chord;

public class InputConnectionStatus extends Fragment {

    private final static int K_PROGRESS_REDUCTION_INTERVAL = 100;
    private final static int K_PROGRESS_REDUCTION_AMOUNT = 10;

    private InputSelector.InputStatusListener statusListener = null;
    private InputSelector.InputTypeListener typeListener = null;
    private InputSelector.InputListener inputListener = null;
    private Activity parent;
    private Application application = null;

    private ImageView image;
    private TextView text;
    private ProgressBar progress;

    private int progressToShow = 0;
    private volatile boolean isRunThread = true;
    private Thread progressReductionThread = null;

    public InputConnectionStatus() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflated = inflater.inflate(R.layout.fragment_input_connection_status, container, false);

        // get our controls
        this.image = inflated.findViewById(R.id.input_conn_image);
        this.text = inflated.findViewById(R.id.input_conn_text);
        this.progress = inflated.findViewById(R.id.input_conn_progress);

        // create the reduction thread
        this.progressReductionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (InputConnectionStatus.this.isRunThread) {
                    // reduce the progress down
                    int oldProgress = progressToShow;
                    progressToShow = Math.max(0, progressToShow - K_PROGRESS_REDUCTION_AMOUNT);
                    if (oldProgress != progressToShow && isRunThread) {
                        updateDisplay();
                    }
                    // and sleep
                    try {
                        Thread.sleep(K_PROGRESS_REDUCTION_INTERVAL);
                    } catch (InterruptedException e) {
                        Log.error(e);
                    }
                }
            }
        });
        // start this thread to do the work here
        this.progressReductionThread.start();

        // and update the view
        updateDisplay();

        return inflated;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.parent = (Activity) context;
        this.application = (Application)parent.getApplication();

        // so we are working now, we can listen for updates to input type and status and show
        // this information to the user
        this.statusListener = new InputSelector.InputStatusListener() {
            @Override
            public void onStatusChanged(Input source, InputSelector.Status oldStatus, InputSelector.Status newStatus) {
                updateDisplay();
            }

            @Override
            public void onInputProcessingData(Input source, InputSelector.Status status) {
                // show that we are processing some input data here now
                showDataIsProcessing(10);
            }
        };
        this.typeListener = new InputSelector.InputTypeListener() {
            @Override
            public void onInputTypeChanged(Settings.InputType newType) {
                updateDisplay();
            }
        };
        this.inputListener = new InputSelector.InputListener() {
            @Override
            public void onNoteDetected(Settings.InputType type, Chord chord, boolean isDetection, float probability) {
                showDataIsProcessing(100);
            }
        };
        // add all these listeners
        InputSelector inputSelector = this.application.getInputSelector();
        inputSelector.addListener(this.statusListener);
        inputSelector.addListener(this.typeListener);
        inputSelector.addListener(this.inputListener);
    }

    private void updateDisplay() {
        // update our display of the data here
        final InputSelector inputSelector = this.application.getInputSelector();
        this.parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                InputSelector.Status status = inputSelector.getStatus();
                if (null != image) {
                    // set the image
                    image.setImageResource(inputSelector.getActiveInput().getStatusDrawable(status));
                    text.setText(status.toString());
                    progress.setProgress(progressToShow);
                }
            }
        });
    }

    private void showDataIsProcessing(int percentage) {
        int oldProgress = this.progressToShow;
        this.progressToShow = Math.min(100, this.progressToShow + percentage);
        if (oldProgress != this.progressToShow) {
            updateDisplay();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // kill the thread
        this.isRunThread = false;
        InputSelector inputSelector = this.application.getInputSelector();
        // remove listeners
        inputSelector.removeListener(this.statusListener);
        inputSelector.removeListener(this.typeListener);
        inputSelector.removeListener(this.inputListener);
        // and kill them
        this.statusListener = null;
        this.typeListener = null;
        this.inputListener = null;
        // and forget the thread
        this.progressReductionThread = null;
    }
}
