package uk.co.darkerwaters.noteinvaders;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.selectables.GameLevelCard;
import uk.co.darkerwaters.noteinvaders.state.Game;
import uk.co.darkerwaters.noteinvaders.state.State;

public class GameActivity extends SelectableItemActivity {

    private Game game = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // load in the game that this card is showing the data for
        this.game = State.getInstance().getGame();
        // and create this activity
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentViewRes() {
        return R.layout.activity_game;
    }

    @Override
    protected List<GameLevelCard> getItemList() {
        List<GameLevelCard> cardList = new ArrayList<GameLevelCard>(this.game.levels.length);

        // load in all the levels for the game this card represents
        for (int i = 0; i < this.game.levels.length; ++i) {
            Game.GameLevel level = this.game.levels[i];
            cardList.add(new GameLevelCard(this, level));
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
        if (item instanceof GameLevelCard) {
            // play this level, set this in the state for later
            State.getInstance().setGameLevel(((GameLevelCard)item).getLevel());
            // and show the activity for this
            Intent myIntent = new Intent(this, PlayActivity.class);
            myIntent.putExtra("level", item.getName()); //Optional parameters
            this.startActivity(myIntent);
        }
    }

    @Override
    protected int getSpan() {
        int span = 1;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            // if we are landscape then we can show 2
            span = 2;
        }
        return span;
    }
}
