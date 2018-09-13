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
import uk.co.darkerwaters.noteinvaders.state.Game;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Notes;
import uk.co.darkerwaters.noteinvaders.state.State;

public class GameCard extends SelectableItem {

    private final Game game;
    private Bitmap titleImage = null;

    public GameCard(Activity context, Game game) {
        super(context, game.name, R.drawable.piano);
        this.game = game;
    }

    public Game getGame() {
        return this.game;
    }

    @Override
    public String getSubtitle() {
        return "Play this game";
    }

    @Override
    public void onBindViewHolder(final SelectableItemActivity context, SelectableItemAdapter.MyViewHolder holder) {
        super.onBindViewHolder(context, holder);

        // in here, called each time the activity is shown now, we can set the data on the profile card according
        // to our latest data from the state class
        this.titleImage = SelectableItem.getBitmapFromAssets(game.image, context);

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
