package uk.co.darkerwaters.noteinvaders.selectables;

import android.app.Activity;
import android.content.Context;

import com.bumptech.glide.Glide;

import uk.co.darkerwaters.noteinvaders.SelectableItem;
import uk.co.darkerwaters.noteinvaders.SelectableItemActivity;
import uk.co.darkerwaters.noteinvaders.SelectableItemAdapter;
import uk.co.darkerwaters.noteinvaders.state.ActiveScore;
import uk.co.darkerwaters.noteinvaders.state.NoteRange;


public class InputSource extends SelectableItem {

    private final String title;
    private final int thumbnail;

    public InputSource(Activity context, String name, int thumbnail) {
        super(context);
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

    @Override
    public String getSubtitle(Activity context) {
        return "Some input";
    }

    @Override
    public int getProgress(Activity context) {
        return -1;
    }

    @Override
    public void onItemRefreshed(SelectableItemActivity context, SelectableItemAdapter.MyViewHolder holder) {
        super.onItemRefreshed(context, holder);

        // loading album cover using Glide library
        Glide.with(context).load(getThumbnail()).into(holder.thumbnail);
    }
}
