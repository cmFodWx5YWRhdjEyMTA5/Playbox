package uk.co.darkerwaters.noteinvaders.views;

import android.content.Context;
import android.content.res.Configuration;
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

    public interface MusicViewListener {
        void onNotePopped(Note note);
    }
    private final List<MusicViewListener> listeners = new ArrayList<MusicViewListener>();

    private Paint blackPaint;
    private Paint notePaint;
    private Paint redPaint;
    private VectorDrawableCompat trebleDrawable;
    private VectorDrawableCompat bassDrawable;

    private boolean isDrawTreble = true;
    private boolean isDrawBass = true;

    private boolean isDrawNoteName = false;

    // store an array of notes that the treble and bass clefs represent
    private Note[] notesTreble;
    private Note[] notesBass;
    private float noteRadius = 1f;
    private float lineHeight = 1f;

    private MusicViewNoteProvider noteProvider;

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
        this.blackPaint.setStrokeWidth(2f);
        this.blackPaint.setColor(Color.BLACK);
        this.blackPaint.setAntiAlias(true);
        // and for the notes
        this.notePaint = new Paint();
        this.notePaint.setStyle(Paint.Style.STROKE);
        this.notePaint.setStrokeWidth(2f);
        this.notePaint.setColor(Color.BLACK);
        this.notePaint.setAntiAlias(true);
        // and for the pressed keys
        this.redPaint = new Paint();
        this.redPaint.setStyle(Paint.Style.FILL);
        this.redPaint.setColor(Color.RED);
        this.redPaint.setAntiAlias(true);

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

    public void showBass(boolean isShow) {
        this.isDrawBass = isShow;
    }

    public void showTreble(boolean isShow) {
        this.isDrawTreble = isShow;
    }

    private String getBasicNoteName(Note note) {
        String noteName = note.getName(0);
        return noteName.substring(0, noteName.length() - 1).toLowerCase();
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
        return position;
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

         // allocations per draw cycle.
        Rect padding = getCanvasPadding();
        int contentWidth = getWidth() - (padding.left + padding.right);
        int contentHeight = getHeight() - (padding.top + padding.bottom);

        int linesToDraw = 0;
        if (this.isDrawTreble) {
            // this is five lines, but we want to leave room at the top and bottom too
            linesToDraw += K_LINESINCLEF + (K_LINESABOVEANDBELOW * 2);
        }
        if (this.isDrawBass) {
            // this is five lines, but we want to leave room at the top and bottom too
            linesToDraw += K_LINESINCLEF + (K_LINESABOVEANDBELOW * 2);
            if (this.isDrawTreble) {
                // we are drawing both, leave one between them too
                linesToDraw += K_LINESBETWEENCLEFS;
            }
        }

        // the last line is the bottom of the view, don't actually draw that one
        --linesToDraw;
        if (linesToDraw > 0) {
            // do the drawing then
            this.lineHeight = contentHeight / linesToDraw;
            float clefHeight = lineHeight * K_LINESINCLEF;
            float clefWidth = clefHeight * 0.5f;
            this.blackPaint.setTextSize(lineHeight);

            // draw the treble clef
            float yPosition = padding.top;
            float bassClefYOffset = 0;
            if (this.isDrawTreble) {
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
                yPosition += K_LINESABOVEANDBELOW * lineHeight;
            }
            if (this.isDrawBass) {
                if (this.isDrawTreble) {
                    // doing both, leave the required gap
                    yPosition += K_LINESBETWEENCLEFS * lineHeight;
                }
                bassClefYOffset = (int)yPosition;
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
                yPosition += K_LINESABOVEANDBELOW * lineHeight;
            }

            // setup the note to draw
            this.noteRadius = lineHeight * 0.3f;
            this.notePaint.setStrokeWidth(noteRadius * 0.5f);
            float noteOffset = lineHeight / 2.0f;

            // draw in any notes we want to draw
            MusicViewNote[] toDrawTreble = this.noteProvider.getNotesToDrawTreble();
            MusicViewNote[] toDrawBass = this.noteProvider.getNotesToDrawBass();

            for (int i = 0; i < toDrawTreble.length + toDrawBass.length; ++i) {
                // for each note, draw it on each clef
                MusicViewNote note = null;
                boolean isFromTrebleList = i < toDrawTreble.length;
                if (isFromTrebleList) {
                    // this is a treble note, get it
                    note = toDrawTreble[i];
                }
                else {
                    // done with the trebles - get bass
                    note = toDrawBass[i - toDrawTreble.length];
                }
                float xPosition = note.getXPosition();
                if (xPosition < getNoteStartX()) {
                    // this is gone from the view
                    if (isFromTrebleList) {
                        this.noteProvider.removeNoteTreble(note);
                    }
                    else {
                        this.noteProvider.removeNoteBass(note);
                    }
                    synchronized (this.listeners) {
                        for (MusicViewListener listener : this.listeners) {
                            listener.onNotePopped(note.note);
                        }
                    }
                }
                if (xPosition <= contentWidth + 24) {
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
                        drawNoteOnClef(canvas, xPosition, yPosition, getBasicNoteName(note.note));
                        // draw the line over this note if we didn't have a long one in already
                        if (isDrawLine(position)) {
                            canvas.drawLine(
                                    xPosition - lineHeight * 0.8f,
                                    yPosition,
                                    xPosition + lineHeight * 0.8f,
                                    yPosition,
                                    notePaint);
                        }
                    }
                }
            }
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
            canvas.drawText(noteTitle, xPosition - noteRadius, yPosition - noteRadius * 2f, blackPaint);
        }
    }

    private boolean isDrawLine(int notePosition) {
        // draw a line when we are supposed to be on a line but are not drawing it
        return notePosition % 2 == 0 && (notePosition < K_LINESABOVEANDBELOW * 2 || notePosition >= (K_LINESABOVEANDBELOW + K_LINESINCLEF) * 2);
    }
}
