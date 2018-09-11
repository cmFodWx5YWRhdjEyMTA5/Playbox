package uk.co.darkerwaters.testingsounddetection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class MainActivity extends AppCompatActivity implements NotesDetector.NoteDectectionInterface {

    private NotesDetector notesDetector = null;
    private TextView[] notesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.notesDetector = new NotesDetector(this);
        this.notesDetector.addListener(this);


        final String[] noteNames = this.notesDetector.getNotesNames();
        this.notesText = new TextView[noteNames.length];
        final LinearLayout noteContainer = (LinearLayout) findViewById(R.id.noteContainer);
        LinearLayout rowContainer = null;
        final String sep = System.getProperty("line.separator");
        for (int index = 0; index < noteNames.length; ++index) {
            if (index % 12 == 0) {
                rowContainer = new LinearLayout(this);
                rowContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                noteContainer.addView(rowContainer);
            }
            notesText[index] = new TextView(this);
            notesText[index].setText(noteNames[index]);
            notesText[index].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowContainer.addView(notesText[index]);
        }
        noteContainer.requestLayout();

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.notesDetector.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        this.notesDetector.removeListener(this);
        this.notesDetector.stop();

        super.onDestroy();
    }

    @Override
    public void onNoteDetected(final String name, float pitch, float probability, final int frequency) {
        // on the main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // highlight the note that was played
                for (int index = 0; index < notesText.length; ++index) {
                    if (name.equals(notesText[index].getText())) {
                        final TextView highlight = notesText[index];
                        if (frequency <= 0) {
                            highlight.setBackgroundColor(0x0);
                        }
                        else if (frequency < 5) {
                            highlight.setBackgroundColor(0xff00ff00);
                        }
                        else if (frequency < 10) {
                            highlight.setBackgroundColor(0xff0000ff);
                        }
                        else {
                            highlight.setBackgroundColor(0xffff0000);
                        }
                    }
                }
            }
        });

    }
}
