package uk.co.darkerwaters.noteinvaders.selectables;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;

import uk.co.darkerwaters.noteinvaders.R;
import uk.co.darkerwaters.noteinvaders.SelectableItem;
import uk.co.darkerwaters.noteinvaders.SelectableItemActivity;
import uk.co.darkerwaters.noteinvaders.SelectableItemAdapter;
import uk.co.darkerwaters.noteinvaders.state.ActiveScore;
import uk.co.darkerwaters.noteinvaders.state.Game;
import uk.co.darkerwaters.noteinvaders.state.State;

public class LastGameCard extends GameCard {

    public LastGameCard(Activity context, Game game) {
        super(context, game);
    }

    public Game getGame() {
        return State.getInstance().getGamePlayedLast();
    }

    @Override
    public String getTitle(Activity context) {
        return getGame().getFullName();
    }
}
