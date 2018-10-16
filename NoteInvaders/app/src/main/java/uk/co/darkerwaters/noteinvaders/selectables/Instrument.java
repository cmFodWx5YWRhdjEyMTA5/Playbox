package uk.co.darkerwaters.noteinvaders.selectables;

import android.app.Activity;

import com.bumptech.glide.Glide;

import uk.co.darkerwaters.noteinvaders.SelectableItem;
import uk.co.darkerwaters.noteinvaders.SelectableItemActivity;
import uk.co.darkerwaters.noteinvaders.SelectableItemAdapter;
import uk.co.darkerwaters.noteinvaders.state.NoteRange;

public class Instrument extends SelectableItem {

    private final NoteRange noteRange;

    public Instrument(Activity context, String name, NoteRange noteRange, int thumbnail) {
        super(context, name, thumbnail);
        this.noteRange = noteRange;
    }

    public NoteRange getNoteRange() {
        return this.noteRange;
    }

    public String getSubtitle(Activity context) {
        return "Some progress";
    }

    @Override
    public int getProgress(Activity context) {
        //TODO get the average progress for all the games that can be played by this instrument
        return -1;
    }

    @Override
    public void onBindViewHolder(SelectableItemActivity context, SelectableItemAdapter.MyViewHolder holder) {
        super.onBindViewHolder(context, holder);

        // loading album cover using Glide library
        Glide.with(context).load(getThumbnail()).into(holder.thumbnail);
    }
}
