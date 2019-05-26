package uk.co.darkerwaters.staveinvaders.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.notes.Note;

public class PianoPlaying extends PianoView {

    private Paint keyPressPaint;
    private final List<Chord> depressedNotes = new ArrayList<Chord>();

    public PianoPlaying(Context context) {
        super(context);
    }

    public PianoPlaying(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PianoPlaying(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initialiseView(Context context) {
        super.initialiseView(context);

        // initialise the paint for the pressed keys
        this.keyPressPaint = new Paint();
        this.keyPressPaint.setStyle(Paint.Style.FILL);
        this.keyPressPaint.setColor(getResources().getColor(R.color.colorKeyPress));
        this.keyPressPaint.setAntiAlias(true);
    }

    @Override
    public void closeView() {
        // close the base
        super.closeView();
    }

    @Override
    protected void drawKey(Canvas canvas, RectF keyRect, Chord keyNote) {
        // let the base class draw the key
        super.drawKey(canvas, keyRect, keyNote);
        // is it pressed?
        if (isNoteDepressed(keyNote)) {
            // highlight this pressed note
            canvas.drawRect(keyRect.left + 2,
                    keyRect.top + 2,
                    keyRect.right - 2,
                    keyRect.bottom - 2,
                    keyPressPaint);
        }
    }

    public void depressNote(Chord chord) {
        // remember that this note is depressed
        synchronized (this.depressedNotes) {
            this.depressedNotes.add(chord);
        }
        // inform listeners that this note is depressed now
        synchronized (this.listeners) {
            for (IKeysViewListener listener : this.listeners) {
                listener.noteDepressed(chord);
            }
        }
    }

    public void releaseAllNotes() {
        // release everything
        Chord[] notesReleased;
        synchronized (this.depressedNotes) {
            notesReleased = this.depressedNotes.toArray(new Chord[0]);
            this.depressedNotes.clear();
        }
        // and inform the listeners
        synchronized (this.listeners) {
            for (Chord released : notesReleased) {
                for (IKeysViewListener listener : this.listeners) {
                    listener.noteReleased(released);
                }
            }
        }
        // and invalidate this view
        invalidateOurselves();
    }

    public void releaseNote(Chord note) {
        // release this specified note
        synchronized (this.depressedNotes) {
            List<Chord> toRemove = new ArrayList<Chord>();
            for (Chord chord : this.depressedNotes) {
                if (chord.equals(note)) {
                    // this is it
                    toRemove.add(chord);
                }
            }
            this.depressedNotes.removeAll(toRemove);
        }
        // and inform the listeners
        synchronized (this.listeners) {
            for (IKeysViewListener listener : this.listeners) {
                listener.noteReleased(note);
            }
        }
        // and invalidate this view
        invalidateOurselves();
    }

    private void invalidateOurselves() {
        Context context = getContext();
        if (null != context && context instanceof Activity) {
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            });
        }
    }

    public Chord getDepressedNotes() {
        ArrayList<Note> notesPressed = new ArrayList<Note>();
        synchronized (this.depressedNotes) {
            for (Chord chord : this.depressedNotes) {
                // add all the nodes in the chord to our master list
                Collections.addAll(notesPressed, chord.getNotes());
            }
        }
        // return the sound that now contains all the depressed notes
        return new Chord(notesPressed.toArray(new Note[notesPressed.size()]));
    }

    public boolean isNoteDepressed(Chord note) {
        boolean isNoteInDepressedList = false;
        synchronized (this.depressedNotes) {
            for (Chord chord : this.depressedNotes) {
                if (chord.equals(note)) {
                    // this is it
                    isNoteInDepressedList = true;
                    break;
                }
            }
        }
        // return if this note is in the list of depressed notes
        return isNoteInDepressedList;
    }
}
