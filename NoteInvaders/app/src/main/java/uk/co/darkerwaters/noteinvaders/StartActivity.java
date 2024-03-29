package uk.co.darkerwaters.noteinvaders;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.selectables.Instrument;
import uk.co.darkerwaters.noteinvaders.selectables.LastGameCard;
import uk.co.darkerwaters.noteinvaders.selectables.Profile;
import uk.co.darkerwaters.noteinvaders.selectables.GameCard;
import uk.co.darkerwaters.noteinvaders.state.Game;

import uk.co.darkerwaters.noteinvaders.state.input.InputMidi;

public class StartActivity extends SelectableItemActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // and create this activity
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);

        NoteInvaders application = NoteInvaders.getAppContext();
        if (application.getAvailableInstrumentCount() <= 1) {
            // hide the option
            menu.findItem(R.id.menu_item_instrument).setVisible(false);
        }
        if (false == application.isInputAvailable(NoteInvaders.InputType.microphone)) {
            // hide the option
            menu.findItem(R.id.menu_item_setupmicrophone).setVisible(false);
        }
        if (false == application.isInputAvailable(NoteInvaders.InputType.usb)) {
            // hide the option
            menu.findItem(R.id.menu_item_setupusb).setVisible(false);
        }
        if (false == application.isInputAvailable(NoteInvaders.InputType.bt)) {
            // hide the option
            menu.findItem(R.id.menu_item_setupbt).setVisible(false);
        }
        return true;
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

        Game lastGame = NoteInvaders.getAppContext().getGamePlayedLast();
        if (null != lastGame) {
            cardList.add(new LastGameCard(this, lastGame));
        }

        // load in all our games
        int gameCount = NoteInvaders.getAppContext().getAvailableGameCount();
        for (int i = 0; i < gameCount; ++i) {
            Game game = NoteInvaders.getAppContext().getAvailableGame(i);
            cardList.add(new GameCard(this, game));
        }

        return cardList;
    }

    @Override
    protected int getTitleImageRes() {

        Instrument instrument = NoteInvaders.getAppContext().getInstrument();
        if (null != instrument) {
            return instrument.getThumbnail();
        }
        else {
            return R.drawable.instruments;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent newIntent = null;
        switch (item.getItemId()) {
            case R.id.menu_item_instrument :
                newIntent = new Intent(this, InstrumentActivity.class);
                break;
            case R.id.menu_item_setupmicrophone :
                newIntent = new Intent(this, MicrophoneSetupActivity.class);
                break;
            case R.id.menu_item_setupusb :
                newIntent = new Intent(this, UsbSetupActivity.class);
                break;
            case R.id.menu_item_setupbt :
                newIntent = new Intent(this, BtSetupActivity.class);
                break;
            case R.id.menu_item_attributions :
                newIntent = new Intent(this, AttributionsActivity.class);
                break;

        }
        if (null == newIntent) {
            return super.onOptionsItemSelected(item);
        }
        else {
            this.startActivity(newIntent);
            // handled this okay
            return true;
        }
    }

    @Override
    public void onSelectableItemClicked(SelectableItem item) {
        Game selectedGame = null;
        if (item instanceof Profile) {
            // they have clicked the profile item, let them change their instrument
            Intent myIntent = new Intent(this, InstrumentActivity.class);
            //myIntent.putExtra("instrument", item.getName()); //Optional parameters
            this.startActivity(myIntent);
        }
        else if (item instanceof GameCard) {
            // get the game this card represents
            selectedGame = ((GameCard) item).getGame();
        }
        else if (item instanceof LastGameCard) {
            // get the game this card represents
            selectedGame = ((LastGameCard) item).getGame();
        }
        if (null != selectedGame) {
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
        int span = 1;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            // if we are landscape then we can show 2
            span = 2;
        }
        return span;
    }
}
