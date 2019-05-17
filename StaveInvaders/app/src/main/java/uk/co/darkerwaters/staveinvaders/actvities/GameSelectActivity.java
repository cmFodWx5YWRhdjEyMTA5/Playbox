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

import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_select);

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
        //TODO hide the checks if they are not available in the settings

        // set the data
        setSelectedGameData();
        // and enable the buttons
        enableNextAndBackButtons();
    }

    private void setAvailableClefs(MusicView.Clefs[] clefs) {
        //TODO set this on the application so remembers the choice and updates the game, music view etc

    }


    private void setSelectedGameData() {
        gameTitle.setText(this.selectedGame.name);
        progressView.setSelectedChild(this.selectedGame);

        //TODO set the check item to match that set and available in the application
        this.radioClefs.check(R.id.radioTrebleClef);
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
