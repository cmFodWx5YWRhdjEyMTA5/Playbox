package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.games.GameScore;
import uk.co.darkerwaters.staveinvaders.notes.Clef;
import uk.co.darkerwaters.staveinvaders.games.Game;
import uk.co.darkerwaters.staveinvaders.games.GameNote;
import uk.co.darkerwaters.staveinvaders.games.GamePlayer;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.notes.Note;

public class MusicView extends BaseView {

    private static final float K_VIEW_HEIGHT_FACTOR = 3f;

    protected ViewBounds bounds;
    protected float clefWidth;
    protected int lineCount;
    protected int spaceCount;
    protected int noteCount;
    protected float lineHeight;
    protected float noteHeight;
    protected float clefHeight;

    private class MusicViewBounds extends ViewBounds {
        MusicViewBounds() {
            super();
        }

        @Override
        float calculateYBorder() {
            // this is overridden because on this view we want to make sure we
            // are super skinny, the width of drawing needs to be a lot more
            // than the height to show it properly
            if (this.viewWidth < this.viewHeight * K_VIEW_HEIGHT_FACTOR) {
                // we are not wide enough, increase the Y border to shrink it down
                float requiredHeight = this.viewWidth / K_VIEW_HEIGHT_FACTOR;
                return (this.viewHeight - requiredHeight) * 0.5f;
            }
            else {
                // just a little more than the base
                return this.viewHeight * 0.15f;
            }
        }
    }

    public final static int K_BEATS_ON_VIEW = 8;
    private final static int K_LINES_ON_VIEW = 5;
    private final static int K_LINES_ABOVEBELOW = 2;
    private final static int K_NOTES_BELOW = 9;
    private final static int K_NOTES_ABOVE = 9;
    private final static int K_NOTES_COUNT = 1 + K_NOTES_ABOVE + K_NOTES_BELOW;

    private static final long K_ANIMATION_DELAY = 50;

    private Animator animator = null;

    // the notes we are drawing on the clefs
    private Chords[] clefNotes;
    private List<Clef> permittedClefs;

    private VectorDrawableCompat trebleDrawable;
    private VectorDrawableCompat bassDrawable;

    private long lastDrawn = 0l;

    protected GamePlayer noteProvider = null;

    private class Animator {
        volatile float clefYOffset = 0f;
        volatile float clefYTarget = 0f;
        final float clefSpeed;

        final static float K_CLEFANIMATIONSECONDS = 0.5f;

        volatile boolean isRunning = true;

        private final Thread animationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // animate any movements here
                final Handler handler = new Handler(Looper.getMainLooper());
                while (isRunning) {
                    handler.post(new Runnable() {
                            @Override
                            public void run() {
                                invalidate();
                            }
                        });
                    try {
                        Thread.sleep(K_ANIMATION_DELAY);
                    }
                    catch (Exception e) {
                        // whatever
                    }
                }
            }
        });

        Animator(ViewBounds bounds) {
            // setup the speeds
            this.clefSpeed = bounds.viewHeight / K_CLEFANIMATIONSECONDS;
            // start the thread
            this.animationThread.start();
        }

        void stop() {
            this.isRunning = false;
            synchronized (this) {
                notify();
            }
        }

        public void updateAnimations(long timeElapsed) {
            float seconds = timeElapsed / 1000f;
            if (clefYTarget < clefYOffset) {
                // reduce this
                clefYOffset -= clefSpeed * seconds;
                if (clefYOffset < clefYTarget) {
                    // too far!
                    clefYOffset = clefYTarget;
                }
            }
            else if (clefYTarget > clefYOffset) {
                // increase this
                clefYOffset += clefSpeed * seconds;
                if (clefYOffset > clefYTarget) {
                    // too far!
                    clefYOffset = clefYTarget;
                }
            }
        }
    }

    public MusicView(Context context) {
        super(context);
    }

    public MusicView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MusicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MusicView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean setPermittedClef(Clef clef, boolean isPermitted) {
        boolean result;
        if (isPermitted) {
            result = this.permittedClefs.add(clef);
        }
        else {
            result = this.permittedClefs.remove(clef);
        }
        // and return the result
        return result;
    }

    public void setPermittedClefs(Clef[] permittedClefs) {
        // remove them all
        this.permittedClefs.clear();
        // put back those permitted
        for (Clef clef : permittedClefs) {
            setPermittedClef(clef, true);
        }
        // and change the note provider if that is set
        if (null != this.noteProvider) {
            this.noteProvider.setPermittedClef(Clef.treble, this.permittedClefs.contains(Clef.treble));
            this.noteProvider.setPermittedClef(Clef.bass, this.permittedClefs.contains(Clef.bass));
        }
    }

    public GamePlayer setActiveGame(Game game) {
        if (null == game) {
            this.noteProvider = null;
        }
        else {
            // get the game player for this game and setup the clefs
            this.noteProvider = game.getGamePlayer(this.application);
            if (null != this.noteProvider) {
                // need to set the permitted clefs on this provider to match that selected here on this view
                this.noteProvider.setPermittedClef(Clef.treble, this.permittedClefs.contains(Clef.treble));
                this.noteProvider.setPermittedClef(Clef.bass, this.permittedClefs.contains(Clef.bass));
            }
            // re-animate the appearance of the notes for nice
            stopAnimation();
        }
        return this.noteProvider;
    }

    @Override
    protected void initialiseView(Context context) {
        // initialise this view and everything we need to use on it
        // create the notes that
        // represent the notes we are drawing, there will be one or three (flats and sharps)
        Chords singleChords = this.application.getSingleChords();
        this.clefNotes = new Chords[Clef.values().length];
        // by default both clefs are active
        this.permittedClefs = new ArrayList<Clef>(2);
        this.permittedClefs.add(Clef.treble);
        this.permittedClefs.add(Clef.bass);

        for (int iClef = 0; iClef < this.clefNotes.length; ++iClef) {
            // for each clef, create the list of notes
            Clef clef = Clef.get(iClef);
            // find the middle note in the list of single notes
            int iMiddle = singleChords.getChordIndex(clef.middleNoteName);
            // now get all the notes down from here and add to the list
            ArrayList<Chord> notes = new ArrayList<Chord>();
            for (int i = iMiddle - 1; notes.size() < K_NOTES_BELOW; --i) {
                // get the note at this index
                Chord note = singleChords.getChord(i);
                if (!note.hasFlat() && !note.hasSharp()) {
                    // this is a nice raw note, add this to the head of the list
                    notes.add(0, note);
                }
            }
            // we also need to go up the list getting those above
            for (int i = iMiddle; notes.size() < K_NOTES_COUNT; ++i) {
                // get the note at this index
                Chord note = singleChords.getChord(i);
                if (!note.hasFlat() && !note.hasSharp()) {
                    // this is a nice raw note, add this to the tail of the list
                    notes.add(note);
                }
            }
            // now create the array of chords for this clef from the list
            this.clefNotes[iClef] = new Chords(notes.toArray(new Chord[0]));
        }

        // we have our list of notes we are going to display, but these are pure notes
        // and don't include sharps or flats, let's find those here and add to the list
        for (int i = 0; i < singleChords.getSize(); ++i) {
            Chord chord = singleChords.getChord(i);
            if (chord.hasSharp() || chord.hasFlat()) {
                // get the primative name of this
                for (int iNote = 0; iNote < chord.notes.length; ++iNote) {
                    Note note = chord.notes[iNote];
                    if (note.isFlat() || note.isSharp()) {
                        //get the raw name for this note (not a sharp or a flat)
                        String name = String.format("%s%d", note.getNotePrimative(), note.getNoteScaleIndex());
                        // find this flat/sharp in our list of notes we are drawing and add it
                        for (int iClef = 0; iClef < this.clefNotes.length; ++iClef) {
                            // do on each clef, will be somewhere (hopefully)
                            int findIndex = this.clefNotes[iClef].getChordIndex(name);
                            if (findIndex >= 0) {
                                // this is in the list, add the sharp / flat to the base we are storing
                                List<Note> chordNotes = new ArrayList<Note>();
                                // add all the notes already in the chord
                                chordNotes.addAll(Arrays.asList(this.clefNotes[iClef].getChord(findIndex).getNotes()));
                                // and the one we found
                                chordNotes.add(note);
                                // and replace the chord in the list with the amalgam
                                this.clefNotes[iClef].replace(findIndex, new Chord(name, chordNotes.toArray(new Note[0])));
                            }
                        }
                    }
                }
            }
        }

        this.lastDrawn = System.currentTimeMillis();

        // initialise our drawing assets
        this.trebleDrawable = VectorDrawableCompat.create(getContext().getResources(), R.drawable.ic_treble, null);
        this.bassDrawable = VectorDrawableCompat.create(getContext().getResources(), R.drawable.ic_bass, null);
    }

    public int getTempo() {
        if (null != this.noteProvider) {
            return this.noteProvider.getTempo();
        }
        else {
            return GameScore.K_DEFAULT_BPM;
        }
    }

    public boolean getIsHelpLettersShowing() {
        if (null != this.noteProvider) {
            return this.noteProvider.isHelpLettersShowing();
        }
        else {
            return true;
        }
    }

    public void setTempo(int tempo) {
        if (null != this.noteProvider) {
            this.noteProvider.setTempo(tempo);
        }
    }

    public void setIsHelpLettersShowing(boolean isShowing) {
        if (null != this.noteProvider) {
            this.noteProvider.setIsHelpLettersShowing(isShowing);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw in the bounds allocated to us
        this.bounds = new MusicViewBounds();

        Assets assets = getAssets();

        // calculate the draw time
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - this.lastDrawn;
        if (timeElapsed < 0l) {
            // oops
            timeElapsed = 0;
        }
        this.lastDrawn = currentTime;
        // draw this view with the time elapsed
        drawView(timeElapsed, assets, canvas);
    }

    protected void drawView(long timeElapsed, Assets assets, Canvas canvas) {
        // update the animator
        if (null == this.animator) {
            // start animating
            this.animator = new Animator(bounds);
            this.animator.clefYTarget = -bounds.viewHeight * 0.5f;
        }
        this.animator.updateAnimations(timeElapsed);
        // also update the provider
        if (null != this.noteProvider) {
            this.noteProvider.updateNotes(timeElapsed / 1000f * this.noteProvider.getBeatsPerSecond());
        }

        // draw the active clef lines on the view
        this.lineCount = K_LINES_ON_VIEW + (K_LINES_ABOVEBELOW * 2);
        this.spaceCount = lineCount + 1;
        this.noteCount = (int)((lineCount * 2f) + 1f);
        if (this.clefNotes[Clef.bass.val].getSize() != noteCount) {
            Log.error("There are incorrect number of notes in the bass list");
        }
        if (this.clefNotes[Clef.treble.val].getSize() != noteCount) {
            Log.error("There are incorrect number of notes in the bass list");
        }
        this.lineHeight = bounds.drawingHeight / spaceCount;
        // the height of a note (in a space or on a line) will be half this then
        this.noteHeight = lineHeight * 0.5f;

        this.clefHeight = lineHeight * K_LINES_ON_VIEW;
        this.clefWidth = clefHeight * 0.5f;

        for (int iLine = K_LINES_ABOVEBELOW; iLine < lineCount - K_LINES_ABOVEBELOW; ++iLine) {
            // for each line, draw them in
            canvas.drawLine(bounds.drawingLeft,
                    bounds.drawingTop + iLine * lineHeight,
                    bounds.drawingRight,
                    bounds.drawingTop + iLine * lineHeight,
                    assets.blackPaint);
        }

        // draw in the clef signs here, might be half-way through an animation or whatever here
        canvas.save();
        canvas.translate(bounds.drawingLeft - clefWidth * 0.3f, this.animator.clefYOffset);
        // move to the correct location and draw in the treble clef
        canvas.translate(0, bounds.drawingTop + (lineHeight * (K_LINES_ON_VIEW + K_LINES_ABOVEBELOW)) - clefHeight);
        this.trebleDrawable.setBounds(0, 0, (int) clefWidth, (int) clefHeight);
        this.trebleDrawable.draw(canvas);
        // move down the height of the view and draw in the bass clef
        canvas.translate(24, lineHeight * 0.25f + bounds.viewHeight);
        this.bassDrawable.setBounds(0, 0, (int) (clefWidth * 0.6f), (int) (clefHeight * 0.6f));
        this.bassDrawable.draw(canvas);
        canvas.restore();

        // draw in the tempo
        if (null != this.noteProvider && this.noteProvider.isGameActive()) {
            String tempoString = Integer.toString(getTempo());
            Paint.Align textAlign = assets.letterPaint.getTextAlign();
            assets.letterPaint.setTextAlign(Paint.Align.LEFT);
            float letterBorder = assets.letterPaint.getTextSize() * 0.2f;
            canvas.drawText(tempoString, bounds.drawingLeft, bounds.drawingTop + letterBorder, assets.letterPaint);
            assets.letterPaint.setTextAlign(textAlign);
        }

        // get the active clef we are drawing the notes for
        Clef currentClef = getCurrentClef();
        switch (currentClef) {
            case treble:
                // the treble clef should have a zero offset to draw the correct diagram in
                this.animator.clefYTarget = 0;
                break;
            case bass:
                // the bass clef should be offset to draw the bass in (via the animator)
                this.animator.clefYTarget = -bounds.viewHeight;
        }

        // calculate the area we want to draw the notes on here
        float noteAreaLeft = bounds.drawingLeft + clefWidth;
        // draw all the notes in here
        float noteRadius = noteHeight * 0.8f;
        float beatsToPixels = (bounds.drawingRight - noteAreaLeft) / K_BEATS_ON_VIEW;
        float noteAnimationOffset = this.animator != null ? (this.animator.clefYOffset - this.animator.clefYTarget) : 0;
        if (null != this.noteProvider) {
            for (GameNote note : this.noteProvider.getNotesToDraw()) {
                Game.GameEntry toDraw = note.getChord();
                if (null == toDraw || toDraw.clef != currentClef) {
                    // not drawing this clef at this time, don't draw this note, or any subsequent ones
                    break;
                }
                else {
                    float xPosition = noteAreaLeft + (note.getOffsetBeats() * beatsToPixels) - (noteRadius * 1.2f);
                    // for each note in the chord, find the position on the clef to draw it
                    float topYPosition = -1f;
                    for (Note noteToDraw : toDraw.chord.notes) {
                        // for each note to draw, find it's location and draw it
                        int noteToDrawIndex = this.clefNotes[toDraw.clef.val].getChordIndex(noteToDraw.getFrequency());
                        if (noteToDrawIndex >= 0) {
                            // this is a valid index, get the yPosition for this note
                            noteToDrawIndex = noteCount - 1 - noteToDrawIndex;
                            float yPosition = noteAnimationOffset + getYPosition(toDraw.clef, noteToDraw);
                            if (topYPosition == -1f) {
                                topYPosition = yPosition;
                            } else {
                                topYPosition = Math.min(topYPosition, yPosition);
                            }
                            // draw the note itself
                            drawNote(toDraw, xPosition, yPosition, noteRadius, canvas, getAssets().blackPaint);
                            if (noteToDrawIndex % 2 != 0) {
                                // this is a lined note, do we need to add a line
                                if (noteToDrawIndex <= K_LINES_ABOVEBELOW * 2 ||
                                        noteToDrawIndex >= noteCount - K_LINES_ABOVEBELOW * 2) {
                                    // add the line as below or above the mass of lines
                                    canvas.drawLine(xPosition - noteRadius * 2f, yPosition, xPosition + noteRadius * 2f, yPosition, assets.blackPaint);
                                }
                            }
                        }
                    }
                    // and the title if we are showing this helpful thing
                    if (toDraw.name != null && toDraw.name.isEmpty() == false) {
                        // there is a name, should we draw it
                        if (getIsHelpLettersShowing()) {
                            float yText = topYPosition - noteRadius * 2f;
                            /*Paint.FontMetrics fontMetrics = assets.letterPaint.getFontMetrics();
                            float letterWidth = assets.letterPaint.measureText(toDraw.name);
                            canvas.drawRect(xText, yText + fontMetrics.top, xPosition + letterWidth, yText + fontMetrics.bottom, assets.whitePaint);
                            */
                            canvas.drawText(toDraw.name, xPosition, yText, assets.letterPaint);
                        }
                    }
                    if (toDraw.fingering != null && toDraw.fingering.isEmpty() == false) {
                        // there is a little notation, draw this in now
                        canvas.drawText("" + toDraw.fingering, xPosition, this.bounds.drawingBottom, assets.letterPaint);
                    }
                }
            }
        }

        /*
        // DEVELOPER OPTION TO SHOW VIEW BOUNDS
        canvas.drawLine(0, 0, canvas.getWidth(), canvas.getHeight(), getAssets().blackPaint);
        canvas.drawLine(canvas.getWidth(), 0, 0, canvas.getHeight(), getAssets().blackPaint);
        */
    }

    protected Clef getCurrentClef() {
        return this.noteProvider == null ? Clef.treble : this.noteProvider.getCurrentClef();
    }

    protected float getYPosition(Clef clef, Note noteToDraw) {
        int noteToDrawIndex = this.clefNotes[clef.val].getChordIndex(noteToDraw.getFrequency());
        float yPosition = -1f;
        if (noteToDrawIndex >= 0) {
            // this is a valid index, get the yPosition for this note
            noteToDrawIndex = noteCount - 1 - noteToDrawIndex;
            yPosition = (bounds.drawingTop - noteHeight) + (noteToDrawIndex * noteHeight);
        }
        return yPosition;
    }

    protected void drawNote(Game.GameEntry toDraw, float xPosition, float yPosition, float noteRadius, Canvas canvas, Paint paint) {
        float stickX;
        if (Build.VERSION.SDK_INT >= 21) {
            // there is a function to draw it a little oval-like, do this
            canvas.drawOval(xPosition - noteRadius * 1.2f,
                    yPosition - noteRadius,
                    xPosition + noteRadius * 1.2f,
                    yPosition + noteRadius,
                    paint);
            stickX = xPosition + noteRadius * 1.2f;
        } else {
            // fall back to drawing a circle then
            canvas.drawCircle(xPosition, yPosition, noteRadius, paint);
            stickX = xPosition + noteRadius;
        }
        // draw the stick up from the note
        canvas.drawLine(stickX, yPosition, stickX, yPosition - lineHeight * 1.5f, paint);
    }

    private void stopAnimation() {
        if (null != this.animator) {
            this.animator.stop();
            this.animator = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        // and debug this
        Log.info("Music view detached okay");
        super.onDetachedFromWindow();
    }
}
