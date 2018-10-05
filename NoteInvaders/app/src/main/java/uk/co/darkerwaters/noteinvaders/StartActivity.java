package uk.co.darkerwaters.noteinvaders;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.selectables.Instrument;
import uk.co.darkerwaters.noteinvaders.selectables.Profile;
import uk.co.darkerwaters.noteinvaders.selectables.GameCard;
import uk.co.darkerwaters.noteinvaders.state.Game;
import uk.co.darkerwaters.noteinvaders.state.State;

public class StartActivity extends SelectableItemActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initialise our state immediately
        State.getInstance().initialise(this);
        // and create this activity
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentViewRes() {
        return R.layout.activity_start;
    }

    @Override
    protected List<SelectableItem> getItemList() {
        List<SelectableItem> cardList = new ArrayList<SelectableItem>();
        // add the cards to the list the view will display
        //cardList.add(new Profile(this));

        // load in all our games
        int gameCount = State.getInstance().getAvailableGameCount();
        for (int i = 0; i < gameCount; ++i) {
            Game game = State.getInstance().getAvailableGame(i);
            cardList.add(new GameCard(this, game));
        }

        return cardList;
    }

    @Override
    protected int getTitleImageRes() {

        Instrument instrument = State.getInstance().getInstrument();
        if (null != instrument) {
            return instrument.getThumbnail();
        }
        else {
            return R.drawable.instruments;
        }
    }

    @Override
    public void onSelectableItemClicked(SelectableItem item) {
        if (item instanceof Profile) {
            // they have clicked the profile item, let them change their instrument
            Intent myIntent = new Intent(this, InstrumentActivity.class);
            //myIntent.putExtra("instrument", item.getName()); //Optional parameters
            this.startActivity(myIntent);
        }
        else if (item instanceof GameCard) {
            // get the game this card represents
            Game selectedGame = ((GameCard)item).getGame();
            // set this on our state
            State.getInstance().selectGame(selectedGame);
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
            myIntent.putExtra("game", item.getName()); //Optional parameters
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
