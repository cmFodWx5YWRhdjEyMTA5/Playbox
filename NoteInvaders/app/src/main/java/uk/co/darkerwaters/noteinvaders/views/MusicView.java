package uk.co.darkerwaters.noteinvaders.views;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.darkerwaters.noteinvaders.R;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Notes;
import uk.co.darkerwaters.noteinvaders.state.State;

public class MusicView extends View {

    private final static int K_LINESINCLEF = 5;
    private final static int K_LINESABOVEANDBELOW = 2;
    private final static int K_LINESBETWEENCLEFS = 0;

    private final static String K_BASSNOTEBOTTOM = "C2";
    private final static String K_BASSNOTETOP = "E4";

    private final static String K_TREBLENOTEBOTTOM = "A3";
    private final static String K_TREBLENOTETOP = "C6";

    private final static float K_LASERSPEEDSEC = 0.5f;   // seconds to traverse the entire range
    private final static float K_LASERDURATIONSEC = 0.2f;   // seconds to fire the laser for

    public interface MusicViewListener {
        void onNotePopped(Note note);
        void onNoteDestroyed(Note note);
        void onNoteMisfire(Note trebleLaserTarget);
    }
    private final List<MusicViewListener> listeners = new ArrayList<MusicViewListener>();

    private Paint blackPaint;
    private Paint notePaint;
    private Paint letterPaint;
    private Paint redPaint;
    private Paint railPaint;
    private Paint laserPaint;
    private VectorDrawableCompat trebleDrawable;
    private VectorDrawableCompat bassDrawable;

    private boolean isDrawTreble = true;
    private boolean isDrawBass = true;
    private boolean isDrawLaser = false;
    private boolean isDrawNoteName = true;

    // store an array of notes that the treble and bass clefs represent
    private Note[] notesTreble;
    private Note[] notesBass;
    private float noteRadius = 1f;
    private float lineHeight = 1f;

    private int showTempo = 0;

    private MusicViewNoteProvider noteProvider;

    private MusicViewLaser trebleLaser = null;
    private MusicViewLaser bassLaser = null;

    private class LaserTarget {
        Note target = null;
        MusicViewNote note = null;
        boolean isTreble = false;
    }
    // create the target we are shooting at
    private final LaserTarget laserTarget = new LaserTarget();

    private long lastTimeDrawn = 0l;

    private class MusicViewLaser {
        final float yTop;
        final float yBottom;
        float yPosition;
        final float xPosition;
        final float ySpeed;     /* pixels per second */
        float tLaserFire;
        float targetY;

        MusicViewLaser(float yTop, float yBottom, float xPosition) {
            this.yTop = yTop;
            this.yBottom = yBottom;
            this.xPosition = xPosition;
            float height = yBottom - yTop;
            this.yPosition = (height * 0.5f) + yTop;
            this.targetY = yPosition;

            // calculate the speed of Y to be a constant number of seconds to move the entire range
            this.ySpeed = height / K_LASERSPEEDSEC;
        }

        boolean moveTargetPos(float timeElapsedSec) {
            float oldPos = this.yPosition;
            if (this.targetY != this.yPosition) {
                float movement = timeElapsedSec * this.ySpeed;
                if (yPosition < targetY) {
                    // move down the Y axis
                    yPosition = Math.min(targetY, yPosition + movement);
                } else {
                    // move up the Y axis
                    yPosition = Math.max(targetY, yPosition - movement);
                }
            }
            // return if we moved or not
            return oldPos != this.yPosition;
        }
    }

    public MusicView(Context context) {
        super(context);
        init(context);
    }

    public MusicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MusicView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void closeView() {
        // clear all the notes when we close out the view
        this.noteProvider.clearNotes();
    }

    public void setViewProvider(MusicViewNoteProvider provider) {
        this.noteProvider = provider;
    }

    public void updateViewProvider() {
        this.noteProvider.updateNotes(this);
    }

    public MusicViewNoteProvider getNoteProvider() {
        return this.noteProvider;
    }

    protected void init(final Context context) {
        // Initialize new paints
        this.blackPaint = new Paint();
        this.blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.blackPaint.setStrokeWidth(getResources().getDimension(R.dimen.music_line_stroke));
        this.blackPaint.setColor(Color.BLACK);
        this.blackPaint.setAntiAlias(true);
        // and for the notes
        this.notePaint = new Paint();
        this.notePaint.setStyle(Paint.Style.STROKE);
        this.notePaint.setStrokeWidth(getResources().getDimension(R.dimen.note_stroke));
        this.notePaint.setColor(Color.BLACK);
        this.notePaint.setAntiAlias(true);
        // and for the notes
        this.letterPaint = new Paint();
        this.letterPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.letterPaint.setStrokeWidth(getResources().getDimension(R.dimen.letter_stroke));
        this.letterPaint.setColor(getResources().getColor(R.color.colorLaser));
        this.letterPaint.setAntiAlias(true);
        // and for the missed keys
        this.redPaint = new Paint();
        this.redPaint.setStyle(Paint.Style.FILL);
        this.redPaint.setColor(getResources().getColor(R.color.colorMiss));
        this.redPaint.setAntiAlias(true);
        // and for the laser rail
        this.railPaint = new Paint();
        this.railPaint.setStyle(Paint.Style.STROKE);
        this.railPaint.setStrokeWidth(getResources().getDimension(R.dimen.laser_rail_stroke));
        this.railPaint.setColor(getResources().getColor(R.color.colorLaserRail));
        this.railPaint.setAntiAlias(true);
        // and for the laser
        this.laserPaint = new Paint();
        this.laserPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.laserPaint.setStrokeWidth(getResources().getDimension(R.dimen.laser_stroke));
        this.laserPaint.setColor(getResources().getColor(R.color.colorLaser));
        this.laserPaint.setAntiAlias(true);

        this.trebleDrawable = VectorDrawableCompat.create(getContext().getResources(), R.drawable.ic_treble, null);
        this.bassDrawable = VectorDrawableCompat.create(getContext().getResources(), R.drawable.ic_bass, null);

        // and initialise the notes we are going to show on the piano, do the whole range...
        if (null == Notes.instance()) {
            Notes.CreateNotes(context);
        }

        // get the notes that are represented on the clefs, start with treble (top to bottom for drawing ease)
        int numberNotes = (2 * (K_LINESINCLEF + (2 * K_LINESABOVEANDBELOW))) - 1;
        this.notesTreble = new Note[numberNotes];
        this.notesBass = new Note[numberNotes];
        initialiseNoteRange(this.notesTreble, K_TREBLENOTETOP, K_TREBLENOTEBOTTOM);
        initialiseNoteRange(this.notesBass, K_BASSNOTETOP, K_BASSNOTEBOTTOM);
    }

    public boolean addListener(MusicViewListener listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    public boolean removeListener(MusicViewListener listener) {
        synchronized (this.listeners) {
            return this.listeners.remove(listener);
        }
    }

    private void initialiseNoteRange(Note[] noteArray, String topNoteName, String bottomNoteName) {
        int noteIndex = -1;
        Notes notes = Notes.instance();
        for (int i = notes.getNoteCount() - 1; i >= 0; --i) {
            Note note = notes.getNote(i);
            if (noteIndex < 0) {
                // search for the first (top) note
                if (note.getName().equals(topNoteName)) {
                    // have the first note, set the index to store and store it
                    noteIndex = 0;
                    noteArray[noteIndex++] = note;
                }
            }
            else if (false == note.isSharp()){
                // this is a normal note (not a sharp, store this)
                noteArray[noteIndex++] = note;
            }
            if (noteIndex > noteArray.length - 1) {
                // have exceeded the notes we need, quit this loop
                if (false == noteArray[noteArray.length - 1].getName().equals(bottomNoteName)) {
                    // not the correct range!
                    Log.e(State.K_APPTAG, "Note range is incorrect, " + noteArray[noteArray.length - 1].getName() + " should be " + bottomNoteName);
                }
                break;
            }
        }
    }

    public void setShowTempo(int newTempo) { this.showTempo = newTempo; }

    public void showBass(boolean isShow) {
        this.isDrawBass = isShow;
    }

    public void showTreble(boolean isShow) {
        this.isDrawTreble = isShow;
    }

    public void setIsDrawLaser(boolean isDrawLaser) { this.isDrawLaser = isDrawLaser; }

    private boolean killTarget() {
        boolean isRemoved = false;
        synchronized (this.laserTarget) {
            // check the target, if there was one already shooting at, we shot it already
            if (null != this.laserTarget.note) {
                // we already shot at a note, remove this
                if (this.laserTarget.isTreble) {
                    // remove the treble target we shot at
                    isRemoved = this.noteProvider.removeNoteTreble(this.laserTarget.note);
                }
                else {
                    // remove the bass target we shot at
                    isRemoved = this.noteProvider.removeNoteBass(this.laserTarget.note);
                }
                if (isRemoved) {
                    // this is a hit, inform listeners of this
                    // inform listeners of this
                    synchronized (this.listeners) {
                        for (MusicViewListener listener : this.listeners) {
                            listener.onNoteDestroyed(this.laserTarget.note.note);
                        }
                    }
                    // this is popped, stop shooting at it
                    this.laserTarget.note = null;
                }
            }
        }
        return isRemoved;
    }

    public boolean fireLaser(Note target) {
        float notePositionTreble = -1f;
        boolean fireResult = false;
        MusicViewNote[] notes = this.noteProvider.getNotesToDrawTreble();
        for (int i = 0; i < notes.length; ++i) {
            if (target.equals(notes[i].note)) {
                // this is the note, remember this index
                notePositionTreble = notes[i].getXPosition();
                break;
            }
        }
        float notePositionBass = -1f;
        notes = this.noteProvider.getNotesToDrawBass();
        for (int i = 0; i < notes.length; ++i) {
            if (target.equals(notes[i].note)) {
                // this is the note, remember this index
                notePositionBass = notes[i].getXPosition();
                break;
            }
        }
        if (notePositionTreble >= 0f && notePositionBass >= 0f) {
            // there are notes on bass and treble we can fire at, shoot at the closest
            if (notePositionTreble < notePositionBass) {
                fireResult = fireTrebleLaser(target);
            } else {
                fireResult = fireBassLaser(target);
            }
        }
        else if (notePositionBass < 0f) {
            // there is no bass note to fire at, but there is one on the treble
            fireResult = fireTrebleLaser(target);
        }
        else if (notePositionTreble < 0f) {
            // there is no treble note, but there is one on the bass
            fireResult = fireBassLaser(target);
        }
        else {
            // there are no valid notes on either clef
            fireResult = fireTrebleLaser(target);
            boolean bassResult = fireBassLaser(target);
            fireResult = bassResult || fireResult;
        }
        return fireResult;
    }

    public boolean fireTrebleLaser(Note target) {
        boolean isValidShoot = false;
        if (isDrawTreble) {
            for (Note trebleNote : this.notesTreble) {
                if (trebleNote.equals(target)) {
                    // this is a note that is in the treble list, can hit this so shoot at it
                    isValidShoot = true;
                    break;
                }
            }
        }
        if (isValidShoot) {
            // shoot at this new target, first kill any already shot at but not yet killed
            killTarget();
            synchronized (this.laserTarget) {
                // now set the new target to shoot at
                this.laserTarget.target = target;
                this.laserTarget.isTreble = true;
            }
        }
        return isValidShoot;
    }

    public boolean fireBassLaser(Note target) {
        boolean isValidShoot = false;
        if (isDrawBass) {
            for (Note bassNote : this.notesBass) {
                if (bassNote.equals(target)) {
                    // this is a note that is in the bass list, can hit this so shoot at it
                    isValidShoot = true;
                    break;
                }
            }
        }
        if (isValidShoot) {
            // shoot at this new target, first kill any already shot at but not yet killed
            killTarget();
            synchronized (this.laserTarget) {
                // now set the new target to shoot at
                this.laserTarget.target = target;
                this.laserTarget.isTreble = false;
            }
        }
        return isValidShoot;
    }

    private String getBasicNoteName(MusicViewNote note) {
        String noteName = note.noteName;
        if (noteName == null || noteName.isEmpty()) {
            noteName = note.note.getName(0);
            noteName = noteName.substring(0, noteName.length() - 1).toLowerCase();
        }
        return noteName;
    }

    private int getNotePosition(Note[] noteArray, Note note) {
        // get the position of the passed note on the passed clef
        int position = -1;
        if (note.isSharp()) {
            // TODO - need to sort out sharps and flats as different depending on which it is
        }
        else {
            // get the position of the note on the treble clef
            for (int i = 0; i < noteArray.length; ++i) {
                // just count till we find it
                if (noteArray[i].equals(note)) {
                    // this is it
                    position = i;
                    break;
                }
            }
        }
        if (position == -1) {
            // this is a problem!
            System.out.println("Asking for a note that isn't in the list...");
        }
        return position;
    }

    public void setIsDrawNoteName(boolean isDrawNoteName) {
        this.isDrawNoteName = isDrawNoteName;
        this.invalidate();
    }

    protected float getNoteStartX() {
        return this.lineHeight * K_LINESINCLEF * 0.5f;
    }

    protected Rect getCanvasPadding() {
        // adding a little to the top to draw the top note and bottom notes
        return new Rect(getPaddingLeft(), getPaddingTop() + 24, getPaddingRight(), getPaddingBottom() + 8);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw a solid color on the canvas as background
        canvas.drawColor(Color.WHITE);

        // calculate the time between each draw to help our animations
        if (this.lastTimeDrawn == 0) {
            this.lastTimeDrawn = System.currentTimeMillis();
        }
        // calculate the elapsed time since we last drew the data
        long elapsedTime = System.currentTimeMillis() - this.lastTimeDrawn;
        float secondsElapsed;
        if (elapsedTime < 0) {
            // the elapsed time since 1970 just overran, ignore this, first start the time again
            this.lastTimeDrawn = System.currentTimeMillis();
            secondsElapsed = 0f;
        }
        else {
            // if we are here then some time has elapsed, need to move all the notes to their correct
            // locations on the view for the times the are to represent
            secondsElapsed = elapsedTime / 1000f;
        }

        // allocations per draw cycle.
        Rect padding = getCanvasPadding();
        int contentWidth = getWidth() - (padding.left + padding.right);
        int contentHeight = getHeight() - (padding.top + padding.bottom);

        int linesToDraw = 0;
        if (this.isDrawTreble) {
            // this is five lines, but we want to leave room at the top and bottom too
            linesToDraw += K_LINESINCLEF + (K_LINESABOVEANDBELOW * 2);

            // the last line is the bottom of the view, don't actually draw that one
            --linesToDraw;
        }
        if (this.isDrawBass) {
            // this is five lines, but we want to leave room at the top and bottom too
            linesToDraw += K_LINESINCLEF + (K_LINESABOVEANDBELOW * 2);
            if (this.isDrawTreble) {
                // we are drawing both, leave one between them too
                linesToDraw += K_LINESBETWEENCLEFS;
            }
            // the last line is the bottom of the view, don't actually draw that one
            --linesToDraw;
        }
        if (linesToDraw > 0) {
            // do the drawing then
            this.lineHeight = contentHeight / linesToDraw;
            float clefHeight = lineHeight * K_LINESINCLEF;
            float clefWidth = clefHeight * 0.5f;

            // draw the treble clef
            float yPosition = padding.top;
            float bassClefYOffset = 0;
            if (this.isDrawTreble) {
                float yTop = yPosition;
                yPosition += K_LINESABOVEANDBELOW * lineHeight;
                // and then the lines
                for (int i = 0; i < K_LINESINCLEF; ++i) {
                    // draw each line in then
                    canvas.drawLine(padding.left, yPosition, contentWidth - padding.right, yPosition, blackPaint);
                    yPosition += lineHeight;
                }
                // yPosition is on the bottom line of the stave - put in the clef
                canvas.save();
                canvas.translate(padding.left, yPosition - clefHeight);
                this.trebleDrawable.setBounds(0, 0, (int) clefWidth, (int) clefHeight);
                this.trebleDrawable.draw(canvas);
                canvas.restore();
                // move down the empty lines before we draw any more
                yPosition += (K_LINESABOVEANDBELOW - 1) * lineHeight;
                if (this.isDrawLaser && this.trebleLaser == null) {
                    // we want a laser but we don't have one, create it here
                    this.trebleLaser = new MusicViewLaser(yTop, yPosition, padding.left + (clefWidth * 1.1f));
                }
            }
            if (this.isDrawBass) {
                if (this.isDrawTreble) {
                    // doing both, leave the required gap
                    yPosition += K_LINESBETWEENCLEFS * lineHeight;
                }
                bassClefYOffset = (int) yPosition;
                yPosition += K_LINESABOVEANDBELOW * lineHeight;
                // and then the lines
                for (int i = 0; i < K_LINESINCLEF; ++i) {
                    // draw each line in then
                    canvas.drawLine(padding.left, yPosition, contentWidth - padding.right, yPosition, blackPaint);
                    yPosition += lineHeight;
                }
                // yPosition is on the bottom line of the stave - put in the clef
                canvas.save();
                canvas.translate(padding.left + 24, yPosition - clefHeight);
                this.bassDrawable.setBounds(0, 0, (int) (clefWidth * 0.6f), (int) (clefHeight * 0.6f));
                this.bassDrawable.draw(canvas);
                canvas.restore();
                // move down the empty lines before we draw any more
                yPosition += (K_LINESABOVEANDBELOW - 1) * lineHeight;
                if (this.isDrawLaser && this.bassLaser == null) {
                    // we want a laser but we don't have one, create it here
                    this.bassLaser = new MusicViewLaser(bassClefYOffset, yPosition, padding.left + (clefWidth * 1.1f));
                }
            }

            // setup the note to draw
            this.noteRadius = lineHeight * 0.3f;
            this.notePaint.setStrokeWidth(noteRadius * 0.5f);
            this.letterPaint.setTextSize(lineHeight);
            float noteOffset = lineHeight / 2.0f;

            // draw the note that represents the tempo if we want to
            if (this.showTempo > 0) {
                drawNoteOnClef(canvas, padding.left + lineHeight, padding.top + (lineHeight * 1f), "");
                canvas.drawText("=" + Integer.toString(this.showTempo), padding.left + (lineHeight * 1.5f), padding.top + lineHeight, letterPaint);
            }

            // draw in any notes we want to draw
            MusicViewNote[] toDrawTreble = this.noteProvider.getNotesToDrawTreble();
            MusicViewNote[] toDrawBass = this.noteProvider.getNotesToDrawBass();

            float laserXPosition = 0f;
            if (this.isDrawLaser) {
                // get the X position of the laser if there is one (to know if notes have made it past)
                synchronized (this.laserTarget) {
                    if (null != this.trebleLaser) {
                        laserXPosition = this.trebleLaser.xPosition;
                    }
                    else if (null != this.bassLaser) {
                        laserXPosition = this.bassLaser.xPosition;
                    }
                }
            }

            for (int i = 0; i < toDrawTreble.length + toDrawBass.length; ++i) {
                // for each note, draw it on each clef
                MusicViewNote note = null;
                boolean isFromTrebleList = i < toDrawTreble.length;
                if (isFromTrebleList) {
                    // this is a treble note, get it
                    note = toDrawTreble[i];
                } else {
                    // done with the trebles - get bass
                    note = toDrawBass[i - toDrawTreble.length];
                }
                float xPosition = note.getXPosition();
                if (xPosition < getNoteStartX()) {
                    // this is gone from the view
                    boolean isNoteRemoved = false;
                    if (isFromTrebleList) {
                        isNoteRemoved = this.noteProvider.removeNoteTreble(note);
                    } else {
                        isNoteRemoved = this.noteProvider.removeNoteBass(note);
                    }
                    if (isNoteRemoved) {
                        synchronized (this.listeners) {
                            for (MusicViewListener listener : this.listeners) {
                                listener.onNotePopped(note.note);
                            }
                        }
                    }
                    else {
                        System.out.println("Error, failed to remove the note: " + note.note.getName());
                    }

                }
                else if (xPosition <= contentWidth + 24) {
                    // draw this note in as it is in range of the view
                    int position = -1;
                    if (this.isDrawTreble && isFromTrebleList) {
                        position = getNotePosition(this.notesTreble, note.note);
                        yPosition = padding.top + (position * noteOffset);
                    }
                    if (this.isDrawBass && false == isFromTrebleList) {
                        // can't draw this on treble, but we have bass, try this
                        position = getNotePosition(this.notesBass, note.note);
                        yPosition = bassClefYOffset + (position * noteOffset);
                    }
                    if (position != -1) {
                        // draw the note in the correct position
                        drawNoteOnClef(canvas, xPosition, yPosition, getBasicNoteName(note));
                        // draw the line over this note if we didn't have a long one in already
                        if (isDrawLine(position)) {
                            canvas.drawLine(
                                    xPosition - lineHeight * 0.8f,
                                    yPosition,
                                    xPosition + lineHeight * 0.8f,
                                    yPosition,
                                    notePaint);
                        }
                        if (isDrawLaser) {
                            // did we shoot this note down at all?
                            if (xPosition < laserXPosition) {
                                // the note made it past us
                                canvas.drawCircle(xPosition, yPosition, noteRadius, redPaint);
                            }
                            else if (isFromTrebleList) {
                                // test the treble target
                                synchronized (this.laserTarget) {
                                    // if there is a target and it is on the treble, is it this note?
                                    if (null != this.trebleLaser
                                            && this.laserTarget.isTreble
                                            && null != this.laserTarget.target
                                            && note.note.equals(this.laserTarget.target)) {
                                        // this note is our note we are shooting at
                                        this.laserTarget.note = note;
                                        // this is a hit for the treble target, draw in this laser
                                        this.trebleLaser.targetY = yPosition;
                                        this.trebleLaser.tLaserFire = K_LASERDURATIONSEC;
                                        // and don't fire at another
                                        this.laserTarget.target = null;
                                    }
                                }
                            } else {
                                // test the bass target
                                synchronized (this.laserTarget) {
                                    // if there is a target and it is on the treble, is it this note?
                                    if (null != this.bassLaser
                                            && false == this.laserTarget.isTreble
                                            && null != this.laserTarget.target
                                            && note.note.equals(this.laserTarget.target)) {
                                        // this note is our note we are shooting at
                                        this.laserTarget.note = note;
                                        // this is a hit for the treble target, draw in this laser
                                        this.bassLaser.targetY = yPosition;
                                        this.bassLaser.tLaserFire = K_LASERDURATIONSEC;
                                        // and don't fire at another
                                        this.laserTarget.target = null;
                                    }
                                }
                            }
                        }
                    }
                    else {
                        // an invisible note?
                        if (isFromTrebleList) {
                            Log.e(State.K_APPTAG, "Added a note that cannot be drawn on treble: " + note.note.getName());
                            // fix for the player by removing it
                            this.noteProvider.removeNoteTreble(note);
                        } else {
                            Log.e(State.K_APPTAG, "Added a note that cannot be drawn on bass: " + note.note.getName());
                            // fix for the player by removing it
                            this.noteProvider.removeNoteBass(note);
                        }
                    }
                }
            }
            // if we didn't fire yet, there was nothing to fire at
            synchronized (this.laserTarget) {
                if (null != this.laserTarget.target
                        && this.laserTarget.isTreble
                        && null != this.trebleLaser) {
                    // this was fired at but was not on the view, this is a misfire
                    int position = getNotePosition(this.notesTreble, this.laserTarget.target);
                    if (position != -1) {
                        // move the laser to the miss position
                        this.trebleLaser.targetY = padding.top + (position * noteOffset);
                    }
                    synchronized (this.listeners) {
                        for (MusicViewListener listener : this.listeners) {
                            listener.onNoteMisfire(this.laserTarget.target);
                        }
                    }
                    // reset the target to be null, shot and missed
                    this.laserTarget.target = null;
                }
            }
            synchronized (this.laserTarget) {
                if (null != this.laserTarget.target
                        && false == this.laserTarget.isTreble
                        && null != this.bassLaser) {
                    // this was fired at but was not on the view, this is a misfire
                    int position = getNotePosition(this.notesBass, this.laserTarget.target);
                    if (position != -1) {
                        // move the laser to the miss position
                        this.bassLaser.targetY = padding.top + (position * noteOffset) + bassClefYOffset;
                    }
                    synchronized (this.listeners) {
                        for (MusicViewListener listener : this.listeners) {
                            listener.onNoteMisfire(this.laserTarget.target);
                        }
                    }
                    // reset the target to be null, shot and missed
                    this.laserTarget.target = null;
                }
            }
        }
        // draw in the lasers on top of this
        drawLaserOnClef(canvas, secondsElapsed);

        // reset the time calc so we can animate the correct amount on each draw cycle
        this.lastTimeDrawn = System.currentTimeMillis();
    }

    private void drawLaserOnClef(Canvas canvas, float secondsElapsed) {
        if (this.isDrawLaser) {
            // first move the lasers accordingly, then draw them in their place
            synchronized (this.laserTarget) {
                if (null != this.trebleLaser) {
                    // move this laser
                    this.trebleLaser.moveTargetPos(secondsElapsed);
                    // draw in our lasers, first the rail on which they will be moving
                    drawLaser(canvas, this.trebleLaser, secondsElapsed, this.laserTarget.isTreble);
                    if (null != this.laserTarget.note
                            && this.laserTarget.isTreble
                            && this.trebleLaser.tLaserFire <= 0f) {
                        // there is a target but no firing, remove the target (we shot it down)
                        killTarget();
                    }
                }
                if (null != this.bassLaser) {
                    // move this laser
                    this.bassLaser.moveTargetPos(secondsElapsed);
                    // draw in our lasers, first the rail on which they will be moving
                    drawLaser(canvas, this.bassLaser, secondsElapsed, !this.laserTarget.isTreble);
                    if (null != this.laserTarget.note
                            && false == this.laserTarget.isTreble
                            && this.bassLaser.tLaserFire <= 0f) {
                        // there is a target but no firing, remove the target (we shot it down)
                        killTarget();
                    }
                }
            }
        }
    }

    private void drawLaser(Canvas canvas, MusicViewLaser laser, float secondsElapsed, boolean isFiring) {
        // draw in the laser
        canvas.drawLine(laser.xPosition, laser.yTop, laser.xPosition, laser.yBottom, railPaint);
        // and now the actual laser
        canvas.drawCircle(laser.xPosition, laser.yPosition, this.lineHeight * 0.3f, laserPaint);
        canvas.drawLine(laser.xPosition, laser.yPosition - this.lineHeight * 0.15f, laser.xPosition + this.lineHeight * 0.5f, laser.yPosition, laserPaint);
        canvas.drawLine(laser.xPosition, laser.yPosition + this.lineHeight * 0.15f, laser.xPosition + this.lineHeight * 0.5f, laser.yPosition, laserPaint);
        if (laser.tLaserFire > 0f && laser.yPosition == laser.targetY) {
            // there is laser to fire and we are at the location to fire, fire it
            float xTargetPosition = -1;
            if (isFiring && null != this.laserTarget.note) {
                xTargetPosition = this.laserTarget.note.getXPosition();
            }
            if (xTargetPosition != -1) {
                // draw the line to this hit target
                canvas.drawLine(laser.xPosition, laser.yPosition, xTargetPosition, laser.yPosition, laserPaint);
                // and the circle we hit
                canvas.drawCircle(xTargetPosition, laser.yPosition, noteRadius, laserPaint);
            }
            // and reduce the time fired by the correct amount
            laser.tLaserFire = Math.max(0f, laser.tLaserFire -= secondsElapsed);

        }

    }

    private void drawNoteOnClef(Canvas canvas, float xPosition, float yPosition, String noteTitle) {
        // we have a note, yPosition set for bass or treble, draw it now then
        float stickX;
        if (Build.VERSION.SDK_INT >= 21) {
            // there is a function to draw it a little oval-like, do this
            canvas.drawOval(xPosition - noteRadius * 1.2f,
                    yPosition - noteRadius,
                    xPosition + noteRadius * 1.2f,
                    yPosition + noteRadius,
                    notePaint);
            stickX = xPosition + noteRadius * 1.2f;
        }
        else {
            // fall back to drawing a circle then
            canvas.drawCircle(xPosition, yPosition, noteRadius, notePaint);
            stickX = xPosition + noteRadius;
        }
        // draw the stick up from the note
        canvas.drawLine(stickX, yPosition, stickX, yPosition - lineHeight * 1.5f, notePaint);
        // and the title if we are showing this helpful thing
        if (isDrawNoteName) {
            canvas.drawText(noteTitle, xPosition - noteRadius, yPosition - noteRadius * 2f, letterPaint);
        }
    }

    private boolean isDrawLine(int notePosition) {
        // draw a line when we are supposed to be on a line but are not drawing it
        return notePosition % 2 == 0 && (notePosition < K_LINESABOVEANDBELOW * 2 || notePosition >= (K_LINESABOVEANDBELOW + K_LINESINCLEF) * 2);
    }
}
