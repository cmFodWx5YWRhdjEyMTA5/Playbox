package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.games.Game;
import uk.co.darkerwaters.staveinvaders.input.Input;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Note;

public class MusicViewPlaying extends MusicView implements InputSelector.InputListener {

    private final static long K_TIME_TO_SHOW_HIT = 250l;

    private Paint laserPaint;

    private class Target {
        private final Game.GameEntry entry;
        private final float[] yPositions;
        private final float xPosition;
        private final float radius;

        long timeToShow = K_TIME_TO_SHOW_HIT;

        Target(Game.GameEntry entry, float x, float radius) {
            this.entry = entry;
            // create the array of y positions
            this.yPositions = new float[entry.chord.notes.length];
            Arrays.fill(this.yPositions, -1f);
            this.xPosition = x;
            this.radius = radius;
        }

        boolean isShowTarget(long timeElapsed) {
            timeToShow -= timeElapsed;
            return timeToShow > 0l;
        }

        boolean isShowLaser() {
            return timeToShow > K_TIME_TO_SHOW_HIT - 100l;
        }
    }

    private Target target = null;
    private final List<Target> hitTargets = new ArrayList<Target>();

    private final List<Note> detectedNotes = new ArrayList<Note>();

    public MusicViewPlaying(Context context) {
        super(context);
    }

    public MusicViewPlaying(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MusicViewPlaying(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MusicViewPlaying(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void initialiseView(Context context) {
        super.initialiseView(context);

        this.laserPaint = new Paint();
        this.laserPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.laserPaint.setStrokeWidth(getResources().getDimension(R.dimen.music_line_stroke));
        this.laserPaint.setColor(getResources().getColor(R.color.colorLaser));
        this.laserPaint.setAntiAlias(true);
        this.laserPaint.setAlpha(150);

        this.application.getInputSelector().addListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        // remove us as a listener
        this.application.getInputSelector().removeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    protected void drawView(long timeElapsed, Assets assets, Canvas canvas) {
        // at the start of this draw, we want to intercept the first draw
        this.target = null;
        // let the base class draw things
        super.drawView(timeElapsed, assets, canvas);

        Chord attempt;
        synchronized (this.detectedNotes) {
            // create the attempt
            if (this.detectedNotes.isEmpty()) {
                // nothing to shoot at
                attempt = null;
            }
            else {
                // create the chord of notes to shoot at
                attempt = new Chord(this.detectedNotes.toArray(new Note[0]));
            }
        }
        // remember where we are shooting from
        float gunWidth = clefWidth * 0.5f;
        float startX = this.bounds.drawingLeft + 0.5f * gunWidth;
        float startY = this.bounds.viewHeight - 0.5f * gunWidth;

        Target[] targets;
        synchronized (this.hitTargets) {
            targets = this.hitTargets.toArray(new Target[0]);
        }
        for (Target hit : targets) {
            // draw all the old hits in
            if (hit.isShowTarget(timeElapsed)) {
                // draw this target in by drawing each note
                for (int i = 0; i < hit.yPositions.length; ++i) {
                    // shoot at this
                    if (hit.isShowLaser()) {
                        shootAtTarget(startX, startY, hit.xPosition, hit.yPositions[i], canvas);
                    }
                    // and draw in the hit
                    drawHitTarget(hit, hit.yPositions[i], canvas);
                }
            }
            else {
                // this is no longer to be draw, remove it
                synchronized (this.hitTargets) {
                    this.hitTargets.remove(hit);
                }
            }
        }

        if (null != this.target) {
            // there is a target to shoot at, we can do some shooting
            Set<Note> notesInTargetHit = new HashSet<Note>();
            if (null != attempt) {
                // we are shooting at something, use the Y from each note and open fire
                // at each note we are shooting at
                for (int i = 0; i < attempt.notes.length; ++i) {
                    Note note = attempt.notes[i];
                    float yPosition = getYPosition(getCurrentClef(), note);
                    if (yPosition > 0f) {
                        // shoot at this
                        shootAtTarget(startX, startY, this.target.xPosition, yPosition, canvas);
                        // is this note in the target we are shooting at
                        int chordNoteIndex = this.target.entry.chord.findNoteIndex(note);
                        if (chordNoteIndex >= 0) {
                            // draw the note in red as we have hit something here
                            drawHitTarget(this.target, this.target.yPositions[chordNoteIndex], canvas);
                            // and remember we hit this note
                            notesInTargetHit.add(note);
                        }
                    }
                }
            }
            // draw the barrel over the top
            int targetIndex = (int)(target.yPositions.length / 2f);
            float opposite = target.xPosition - startX;
            float adjacent = target.yPositions[targetIndex] - startY;
            float angleDeg = (float)(Math.atan(opposite / adjacent) * -180 / Math.PI);

            // rotate the barrel to the target and draw it in
            canvas.save();
            canvas.rotate(angleDeg - 90f, startX, startY);
            canvas.drawRoundRect(startX,
                    startY - gunWidth * 0.25f,
                    startX + gunWidth,
                    startY + gunWidth * 0.25f,
                    gunWidth * 0.1f, gunWidth * 0.1f,
                    assets.letterPaint);
            canvas.restore();

            // here we are drawing a target and have an attempt, did we win?
            if (notesInTargetHit.size() == this.target.entry.chord.getNoteCount()) {
                // we hit everything
                onTargetDestroyed();
            }
        }

        // now we can draw our gun and handle all that business
        canvas.drawOval(this.bounds.drawingLeft,
                this.bounds.viewHeight - gunWidth,
                this.bounds.drawingLeft + gunWidth,
                this.bounds.viewHeight,
                assets.objectPaint);
        canvas.drawRoundRect(this.bounds.drawingLeft,
                this.bounds.viewHeight - gunWidth * 0.5f,
                this.bounds.drawingLeft + gunWidth,
                this.bounds.viewHeight,
                5f, 5f,
                assets.objectPaint);
    }

    private void shootAtTarget(float startX, float startY, float xPosition, float yPosition, Canvas canvas) {
        // shoot at this with a line
        canvas.drawLine(startX,
                startY,
                xPosition,
                yPosition,
                this.laserPaint);
    }

    private void drawHitTarget(Target hit, float yPosition, Canvas canvas) {
        // and draw the hit note
        drawNote(hit.entry,
                hit.xPosition,
                yPosition,
                hit.radius,
                canvas,
                this.laserPaint);
    }

    private void onTargetDestroyed() {
        // handle the destroying of targets
        if (null != this.noteProvider &&
                null != this.target &&
                this.noteProvider.destroyNote(this.target.entry)) {
            // we removed this target, add to the list of hit to show for a second more
            synchronized (this.hitTargets) {
                this.hitTargets.add(this.target);
            }
            // remove the notes we used from the list of detected
            removeNotesFromDetected(this.target.entry.chord);

        }
        // the target is no longer valid, have killed it
        this.target = null;
    }

    private void onMissfire(Chord chord) {
        // handle the scoring of this
        if (null != this.noteProvider && null != this.target) {
            this.noteProvider.registerMisfire(this.target.entry, chord);
        }
    }

    @Override
    protected void drawNote(Game.GameEntry toDraw, float xPosition, float yPosition, float noteRadius, Canvas canvas, Paint laserPaint) {
        // draw the note
        super.drawNote(toDraw, xPosition, yPosition, noteRadius, canvas, laserPaint);
        if (this.target == null) {
            // this is the first drawn, this is our target
            this.target = new Target(toDraw, xPosition, noteRadius);
        }
        if (this.target.entry == toDraw) {
            // this is the target we are drawing a part of, remember all the y's
            // find the first invalid yPosition and put this yPosition there
            for (int i = 0; i < this.target.yPositions.length; ++i) {
                if (this.target.yPositions[i] < 0) {
                    // this is not set, set it now
                    this.target.yPositions[i] = yPosition;
                    break;
                }
            }
        }
    }

    private boolean removeNotesFromDetected(Chord chord) {
        boolean retVal;
        synchronized (this.detectedNotes) {
            // remove all the notes in chord from the list of detected notes
            List<Note> toRemove = new ArrayList<Note>();
            for (Note note : chord.notes) {
                for (Note detected : this.detectedNotes) {
                    if (detected.equals(note)) {
                        toRemove.add(detected);
                    }
                }
            }
            retVal = this.detectedNotes.removeAll(toRemove);
        }
        return retVal;
    }

    @Override
    public void onNoteDetected(Settings.InputType type, Chord chord, boolean isDetection, float probability) {
        // as a note is pressed, build our chord of notes we are firing at
        synchronized (this.detectedNotes) {
            if (isDetection) {
                // this is an addition if the probability is ok
                if (probability > Input.K_DETECTION_PROBABILITY_THRESHOLD) {
                    // add all the detected notes to our list
                    for (Note note : chord.notes) {
                        this.detectedNotes.add(note);
                    }
                }
            }
            else {
                // this is a release, remove these notes
                if (removeNotesFromDetected(chord)) {
                    // this was removed, so it was not removed when it hit something
                    // this is a missfire then
                    onMissfire(chord);
                }
            }
        }
    }
}
