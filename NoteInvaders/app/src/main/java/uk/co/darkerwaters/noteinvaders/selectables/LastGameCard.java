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

public class LastGameCard extends SelectableItem {

    private Bitmap titleImage = null;

    private final Game game;

    public LastGameCard(Activity context, Game game) {
        super(context);
        this.game = game;
    }

    public Game getGame() {
        return State.getInstance().getGamePlayedLast();
    }

    @Override
    public String getTitle(Activity context) {
        return getGame().getFullName();
    }

    @Override
    public int getThumbnail() {
        return R.drawable.score;
    }

    @Override
    public String getSubtitle(Activity context) {
        Game game = getGame();
        if (game.isPlayable()) {
            return "Last Played " + State.getInstance().getTimeGameLastPlayedStr(context, game);
            //return "Top BPM: " + State.getInstance().getGameTopTempo(this.game);
        }
        else {
            // return the number of children
            return Integer.toString(game.children.length) + " options...";
        }
    }

    @Override
    public int getProgress(Activity context) {
        int topTempo = State.getInstance().getGameTopTempo(getGame());
        return ActiveScore.GetTempoAsPercent(topTempo);
    }

    @Override
    public void onItemRefreshed(final SelectableItemActivity context, SelectableItemAdapter.MyViewHolder holder) {
        super.onItemRefreshed(context, holder);

        // in here, called each time the activity is shown now, we can set the data on the profile card according
        // to our latest data from the state class
        this.titleImage = SelectableItem.getBitmapFromAssets("games/score.jpg", context);

        final ImageView imageView = holder.thumbnail;
        imageView.post(new Runnable() {
            @Override
            public void run() {
                // set the image to be the image for the selected game
                imageView.setImageBitmap(titleImage);
            }
        });
    }
}