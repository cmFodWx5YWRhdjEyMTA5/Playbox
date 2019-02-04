package uk.co.darkerwaters.noteinvaders.selectables;

import android.app.Activity;
import android.content.Context;

import com.bumptech.glide.Glide;

import uk.co.darkerwaters.noteinvaders.SelectableItem;
import uk.co.darkerwaters.noteinvaders.SelectableItemActivity;
import uk.co.darkerwaters.noteinvaders.SelectableItemAdapter;
import uk.co.darkerwaters.noteinvaders.state.NoteRange;

public class Instrument extends SelectableItem {

    private final NoteRange noteRange;

    private final String title;
    private final int thumbnail;

    public Instrument(Context context, String name, NoteRange noteRange, int thumbnail) {
        super(context);
        this.noteRange = noteRange;
        this.title = name;
        this.thumbnail = thumbnail;
    }

    @Override
    public String getTitle(Activity context) {
        return title;
    }

    @Override
    public int getThumbnail() {
        return thumbnail;
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
    public void onItemRefreshed(SelectableItemActivity context, SelectableItemAdapter.MyViewHolder holder) {
        super.onItemRefreshed(context, holder);

        // loading album cover using Glide library
        Glide.with(context).load(getThumbnail()).into(holder.thumbnail);
    }
}
