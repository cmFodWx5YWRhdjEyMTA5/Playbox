package uk.co.darkerwaters.noteinvaders.selectables;

import android.app.Activity;

import com.bumptech.glide.Glide;

import uk.co.darkerwaters.noteinvaders.SelectableItem;
import uk.co.darkerwaters.noteinvaders.SelectableItemActivity;
import uk.co.darkerwaters.noteinvaders.SelectableItemAdapter;
import uk.co.darkerwaters.noteinvaders.state.NoteRange;

public class InputSource extends SelectableItem {

    public InputSource(Activity context, String name, int thumbnail) {
        super(context, name, thumbnail);
    }

    public String getSubtitle() {
        return "Some input";
    }

    @Override
    public void onBindViewHolder(SelectableItemActivity context, SelectableItemAdapter.MyViewHolder holder) {
        super.onBindViewHolder(context, holder);

        // loading album cover using Glide library
        Glide.with(context).load(getThumbnail()).into(holder.thumbnail);
    }
}
