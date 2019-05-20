package uk.co.darkerwaters.staveinvaders.actvities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.games.Game;
import uk.co.darkerwaters.staveinvaders.games.GameList;
import uk.co.darkerwaters.staveinvaders.games.GameParentCardHolder;
import uk.co.darkerwaters.staveinvaders.views.GameProgressView;
import uk.co.darkerwaters.staveinvaders.views.MusicView;

public class GameSelectActivity extends AppCompatActivity {

    private Game parentGame = null;
    private Game selectedGame = null;

    private GameProgressView progressView;
    private MusicView musicView;
    private int gameIndex = -1;

    private ImageButton nextButton;
    private ImageButton prevButton;

    private TextView gameTitle;

    private RadioGroup radioClefs;
    private Application application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_select);
        this.application = (Application) this.getApplication();

        this.nextButton = findViewById(R.id.nextButton);
        this.prevButton = findViewById(R.id.prevButton);

        this.gameTitle = findViewById(R.id.game_title);
        this.musicView = findViewById(R.id.musicView);

        this.radioClefs = findViewById(R.id.radioGroupClefs);

        Intent intent = getIntent();
        String parentGameName = intent.getStringExtra(GameParentCardHolder.K_SELECTED_CARD_FULL_NAME);
        this.parentGame = GameList.findLoadedGame(parentGameName);
        if (null == this.parentGame) {
            Log.error("Game name of " + parentGameName + " does not correspond to a game");
        }
        else {
            // set our title to be the name of the game parent
            setTitle(parentGame.name);

            // get the last game we have any progress for and select by default
            this.gameIndex = -1;
            for (Game child : parentGame.children) {
                // this can be our last game
                this.selectedGame = child;
                ++this.gameIndex;
                if (false == child.getIsGamePassed()) {
                    // this game cannot be passed by
                    break;
                }
            }

            // card is created, find all our children views and stuff here
            this.progressView = (GameProgressView) this.findViewById(R.id.gameProgress);
            this.progressView.setViewData(parentGame);
        }

        // do the next and previous buttons
        this.prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSelectedGame(-1);
            }
        });
        this.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSelectedGame(+1);
            }
        });

        // do the bass and treble button listeners
        this.radioClefs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                // set the available clefs on the game
                switch (i) {
                    case R.id.radioTrebleClef:
                        setAvailableClefs(new MusicView.Clefs[] {MusicView.Clefs.treble});
                        break;
                    case R.id.radioBassClef:
                        setAvailableClefs(new MusicView.Clefs[] {MusicView.Clefs.bass});
                        break;
                    case R.id.radioMixedClefs:
                        setAvailableClefs(new MusicView.Clefs[] {MusicView.Clefs.treble, MusicView.Clefs.bass});
                        break;
                }
            }
        });

        // set the data
        setSelectedGameData();
        // and enable the buttons
        enableNextAndBackButtons();
    }

    private void setAvailableClefs(MusicView.Clefs[] clefs) {
        // set this on the application so remembers the choice and updates the game, music view etc
        Settings settings = application.getSettings();
        settings.setSelectedClefs(clefs).commitChanges();
        // hide the checks if they are not available in the settings
        if (settings.getIsHideClef(MusicView.Clefs.treble)) {
            // hide it all as there is no choice to make
            this.radioClefs.setVisibility(View.GONE);
            // and set the selected clef to be bass, treble cannot be selected, nor can both
            settings.setSelectedClefs(new MusicView.Clefs[] {MusicView.Clefs.bass});
            // set the check item to match that set and available in the application
            this.radioClefs.check(R.id.radioBassClef);
        }
        else if (settings.getIsHideClef(MusicView.Clefs.bass)) {
            // hide it all as there is no choice to make
            this.radioClefs.setVisibility(View.GONE);
            // and set the selected clef to be treble, bass cannot be selected, nor can both
            settings.setSelectedClefs(new MusicView.Clefs[]{MusicView.Clefs.treble});
        }
        MusicView.Clefs[] selectedClefs = settings.getSelectedClefs();
        // set the check item to match that set and available in the application
        if (selectedClefs.length == 2) {
            // both are selected
            this.radioClefs.check(R.id.radioMixedClefs);
        }
        else if (selectedClefs.length == 1) {
            switch (selectedClefs[0]) {
                case treble:
                    this.radioClefs.check(R.id.radioTrebleClef);
                    break;
                case bass:
                    this.radioClefs.check(R.id.radioBassClef);
                    break;
            }
        }
        // set these on the music view
        musicView.setPermittedClefs(selectedClefs);
    }


    private void setSelectedGameData() {
        gameTitle.setText(this.selectedGame.name);
        progressView.setSelectedChild(this.selectedGame);
        // set the clefs available properly
        MusicView.Clefs[] selectedClefs = this.application.getSettings().getSelectedClefs();
        setAvailableClefs(selectedClefs);
        // set this on the music view
        musicView.setActiveGame(this.selectedGame);
        // and update the views
        progressView.invalidate();
        musicView.invalidate();
    }

    private void changeSelectedGame(int change) {
        // try to change the index
        this.gameIndex += change;
        if (this.gameIndex < 0 || this.gameIndex >= this.parentGame.children.length) {
            // this is too many or too few
            this.gameIndex -= change;
        }
        else {
            // get the selected game
            this.selectedGame = this.parentGame.children[this.gameIndex];
            setSelectedGameData();
        }
        // update the buttons
        enableNextAndBackButtons();
    }

    private void enableNextAndBackButtons() {
        if (null == this.selectedGame) {
            // no good
            this.nextButton.setEnabled(false);
            this.prevButton.setEnabled(false);
        }
        else {
            // enable prev if there are games previous to this
            this.prevButton.setEnabled(gameIndex > 0);
            // the selected game will be at the index of previous children, is there one after?
            if (this.gameIndex < this.parentGame.children.length - 1) {
                this.nextButton.setEnabled(this.selectedGame.getIsGamePassed());
            }
            else {
                // there is none after us
                this.nextButton.setEnabled(false);
            }
        }
    }
}
