package uk.co.darkerwaters.noteinvaders;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.selectables.GameCard;
import uk.co.darkerwaters.noteinvaders.state.Game;


public class GameActivity extends SelectableItemActivity {

    private Game game = null;
    private TextView textSubtitle;
    private TextView textTitle;
    private ImageView titleImageView;
    private Bitmap titleImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // load in the game that this card is showing the data for
        this.game = NoteInvaders.getAppContext().getGameSelected();
        // and create this activity
        super.onCreate(savedInstanceState);
        // set the title for this
        this.titleImageView = (ImageView) findViewById(R.id.backdrop);
        this.textTitle = (TextView) findViewById(R.id.instrument_title);
        this.textSubtitle = (TextView) findViewById(R.id.instrument_subtitle);

        String subtitle = new String(this.game.name);
        Game gameParent = this.game.parent;
        while (null != gameParent) {
            subtitle = gameParent.name + " -- " + subtitle;
            gameParent = gameParent.parent;
        }
        this.textSubtitle.setText(subtitle);
        this.titleImage = SelectableItem.getBitmapFromAssets(game.image, GameActivity.this);

        this.titleImageView.post(new Runnable() {
            @Override
            public void run() {
                // set the image to be the image for the selected game
                titleImageView.setImageBitmap(titleImage);
            }
        });
    }

    @Override
    protected void onResume() {
        // this is the currently selected game, set this
        NoteInvaders.getAppContext().selectGame(this.game);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int getContentViewRes() {
        return R.layout.activity_game;
    }

    @Override
    protected List<GameCard> getItemList() {
        int noChildren = 0;
        if (null != this.game && null != this.game.children) {
            noChildren = this.game.children.length;
        }
        List<GameCard> cardList = new ArrayList<GameCard>(noChildren);

        // load in all the levels for the game this card represents
        for (int i = 0; i < noChildren; ++i) {
            Game child = this.game.children[i];
            cardList.add(new GameCard(this, child));
        }

        return cardList;
    }

    @Override
    protected int getTitleImageRes() {
        return R.drawable.instruments;
    }

    @Override
    public void onSelectableItemClicked(SelectableItem item) {
        // this is the level selected to play the game for, play this level now
        if (item instanceof GameCard) {
            // get the game this card represents
            Game selectedGame = ((GameCard)item).getGame();
            // set this on our state
            NoteInvaders.getAppContext().selectGame(selectedGame);
            // show the card for this game
            Intent myIntent;
            if (selectedGame.isPlayable()) {
                // this is a playable (final) game, show the play activity
                myIntent = new Intent(this, PlayActivity.class);

            }
            else {
                // show the game card for the further options available to the user
                myIntent = new Intent(this, GameActivity.class);
            }
            myIntent.putExtra("game", selectedGame.id); //Optional parameters
            this.startActivity(myIntent);
        }
    }

    @Override
    protected int getSpan() {
        int span = 2;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            // if we are landscape then we can show 2
            span = 3;
        }
        return span;
    }
}
