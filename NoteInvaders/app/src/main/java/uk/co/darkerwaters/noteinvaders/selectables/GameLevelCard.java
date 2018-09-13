package uk.co.darkerwaters.noteinvaders.selectables;

import android.app.Activity;

import uk.co.darkerwaters.noteinvaders.R;
import uk.co.darkerwaters.noteinvaders.SelectableItem;
import uk.co.darkerwaters.noteinvaders.SelectableItemActivity;
import uk.co.darkerwaters.noteinvaders.SelectableItemAdapter;
import uk.co.darkerwaters.noteinvaders.state.Game;

public class GameLevelCard extends SelectableItem {

    private final Game level;

    public GameLevelCard(Activity context, Game level) {
        super(context, level.name, R.drawable.piano);
        this.level = level;
    }

    public Game getLevel() {
        return this.level;
    }

    @Override
    public String getSubtitle() {
        return "Play this level";
    }

    @Override
    public void onBindViewHolder(final SelectableItemActivity context, SelectableItemAdapter.MyViewHolder holder) {
        super.onBindViewHolder(context, holder);

        // in here, called each time the activity is shown now, we can set the data on the profile card according
        // to our latest data from the state class

    }
}
