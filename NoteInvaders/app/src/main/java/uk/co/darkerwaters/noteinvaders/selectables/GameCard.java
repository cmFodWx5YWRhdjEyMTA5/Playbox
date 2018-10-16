package uk.co.darkerwaters.noteinvaders.selectables;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.GameActivity;
import uk.co.darkerwaters.noteinvaders.MicrophoneSetupActivity;
import uk.co.darkerwaters.noteinvaders.R;
import uk.co.darkerwaters.noteinvaders.SelectableItem;
import uk.co.darkerwaters.noteinvaders.SelectableItemActivity;
import uk.co.darkerwaters.noteinvaders.SelectableItemAdapter;
import uk.co.darkerwaters.noteinvaders.state.ActiveScore;
import uk.co.darkerwaters.noteinvaders.state.Game;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Notes;
import uk.co.darkerwaters.noteinvaders.state.State;

public class GameCard extends SelectableItem {

    private Bitmap titleImage = null;

    private final Game game;

    public GameCard(Activity context, Game game) {
        super(context);
        this.game = game;
    }

    public Game getGame() {
        return this.game;
    }

    @Override
    public String getTitle(Activity context) {
        return getGame().name;
    }

    @Override
    public int getThumbnail() {
        return R.drawable.piano;
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
        this.titleImage = SelectableItem.getBitmapFromAssets(getGame().image, context);

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
