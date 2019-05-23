package uk.co.darkerwaters.staveinvaders.actvities;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.application.Scores;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.games.GameProgress;
import uk.co.darkerwaters.staveinvaders.games.GameScore;
import uk.co.darkerwaters.staveinvaders.notes.Clef;
import uk.co.darkerwaters.staveinvaders.games.Game;
import uk.co.darkerwaters.staveinvaders.games.GameList;
import uk.co.darkerwaters.staveinvaders.actvities.fragments.GameParentCardHolder;
import uk.co.darkerwaters.staveinvaders.views.ClefProgressView;
import uk.co.darkerwaters.staveinvaders.views.GameProgressView;
import uk.co.darkerwaters.staveinvaders.views.MusicView;

import static uk.co.darkerwaters.staveinvaders.actvities.fragments.GameParentCardHolder.K_IS_STARTING_HELP_ON;
import static uk.co.darkerwaters.staveinvaders.actvities.fragments.GameParentCardHolder.K_SELECTED_CARD_FULL_NAME;
import static uk.co.darkerwaters.staveinvaders.actvities.fragments.GameParentCardHolder.K_STARTING_TEMPO;

public class GameSelectActivity extends AppCompatActivity {

    private Game parentGame = null;
    private Game selectedGame = null;

    private ImageView imageViewTitlebar;
    private TextView imageTitle;

    private GameProgressView progressView;
    private ClefProgressView trebleProgressView;
    private ClefProgressView bassProgressView;

    private FloatingActionButton playActionButton;

    private View trebleProgress;
    private View bassProgress;

    private MusicView musicView;
    private int gameIndex = -1;

    private TextView tempoText;
    private ImageButton tempoLessButton;
    private ImageButton tempoMoreButton;
    private Switch helpSwitch;

    private ImageButton nextButton;
    private ImageButton prevButton;

    private TextView gameTitle;

    private RadioGroup radioClefs;
    private Application application;
    private int tempo = GameScore.K_DEFAULT_BPM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_select);
        this.application = (Application) this.getApplication();

        this.imageTitle = findViewById(R.id.imageViewTitle);
        this.imageViewTitlebar = findViewById(R.id.imageViewTitlebar);
        this.nextButton = findViewById(R.id.nextButton);
        this.prevButton = findViewById(R.id.prevButton);
        this.playActionButton = findViewById(R.id.playActionButton);

        this.gameTitle = findViewById(R.id.game_title);
        this.musicView = findViewById(R.id.musicView);

        this.tempoText = findViewById(R.id.tempoSelectedTextView);
        this.tempoLessButton = findViewById(R.id.tempoLessButton);
        this.tempoMoreButton = findViewById(R.id.tempoMoreButton);
        this.helpSwitch = findViewById(R.id.helpSwitch);

        this.radioClefs = findViewById(R.id.radioGroupClefs);
        this.trebleProgressView = findViewById(R.id.treble_progress_view);
        this.trebleProgress = findViewById(R.id.treble_progress_layout);
        this.bassProgressView = findViewById(R.id.bass_progress_view);
        this.bassProgress = findViewById(R.id.bass_progress_layout);

        Intent intent = getIntent();
        String parentGameName = intent.getStringExtra(K_SELECTED_CARD_FULL_NAME);
        this.parentGame = GameList.findLoadedGame(parentGameName);
        if (null == this.parentGame) {
            Log.error("Game name of " + parentGameName + " does not correspond to a game");
        }
        else {
            // set our title to be the name of the game parent
            setTitle(parentGame.name);

            setTopGameSelected();

            // card is created, find all our children views and stuff here
            this.progressView = (GameProgressView) this.findViewById(R.id.gameProgress);
            this.progressView.setViewData(parentGame);
        }
        // default the tempo and help controls
        this.helpSwitch.setChecked(true);
        updateTempoAndHelp();

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
        // and the play button
        this.playActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSelectedGame();
            }
        });

        this.tempoLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTempo(-1);
            }
        });
        this.tempoMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTempo(+1);
            }
        });
        this.helpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateTempoAndHelp();
            }
        });

        // do the bass and treble button listeners
        this.radioClefs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                // set the available clefs on the game
                switch (i) {
                    case R.id.radioTrebleClef:
                        setAvailableClefs(new Clef[] {Clef.treble});
                        break;
                    case R.id.radioBassClef:
                        setAvailableClefs(new Clef[] {Clef.bass});
                        break;
                    case R.id.radioMixedClefs:
                        setAvailableClefs(new Clef[] {Clef.treble, Clef.bass});
                        break;
                }
                setTopGameSelected();
                setSelectedGameData();
            }
        });

        // set the data
        setSelectedGameData();
    }

    private void updateTempoAndHelp() {
        // update the help and tempo things
        this.tempoText.setText(Integer.toString(this.tempo));
        if (this.tempo > GameProgress.K_MAX_HELP_TEMPO) {
            // not allowed help
            this.helpSwitch.setChecked(false);
        }
    }

    private void changeTempo(int delta) {
        int tempoIndex = 0;
        for (int i = 0; i < GameScore.K_BPMS.length; ++i) {
            if (this.tempo >= GameScore.K_BPMS[i]) {
                tempoIndex = i;
            }
        }
        // change this index
        tempoIndex += delta;
        if (tempoIndex <= 0) {
            // at the bottom
            tempoIndex = 0;
            this.tempoLessButton.setEnabled(false);
        }
        else {
            this.tempoLessButton.setEnabled(true);
        }
        if (tempoIndex >= GameScore.K_BPMS.length - 1) {
            // at the end
            tempoIndex = GameScore.K_BPMS.length - 1;
            this.tempoMoreButton.setEnabled(false);
        }
        else {
            this.tempoMoreButton.setEnabled(true);
        }
        // get the tempo
        this.tempo = GameScore.K_BPMS[tempoIndex];
        updateTempoAndHelp();
    }

    private void setTopGameSelected() {
        // get the last game we have any progress for and select by default
        this.gameIndex = -1;
        for (Game child : parentGame.children) {
            // this can be our last game
            this.selectedGame = child;
            ++this.gameIndex;
            if (false == isGamePassed(child)) {
                // this game cannot be passed by
                break;
            }
        }
    }

    private void playSelectedGame() {
        // play the selected game by showing the game activity
        Intent intent = new Intent(this, GamePlayActivity.class);
        intent.putExtra(K_SELECTED_CARD_FULL_NAME, this.selectedGame.getFullName());
        intent.putExtra(K_STARTING_TEMPO, this.tempo);
        intent.putExtra(K_IS_STARTING_HELP_ON, this.helpSwitch.isChecked());
        // and start the activity
        startActivity(intent);
    }

    private boolean isGamePassed(Game game) {
        // get the selected clefs and see if we passed them all
        boolean isPassed = true;
        Clef[] selectedClefs = application.getSettings().getSelectedClefs();
        for (Clef clef : selectedClefs) {
            if (false == game.getIsGamePassed(clef)) {
                // this was not passed
                isPassed = false;
                break;
            }
        }
        return isPassed;
    }

    private void setAvailableClefs(Clef[] clefs) {
        // set this on the application so remembers the choice and updates the game, music view etc
        Settings settings = application.getSettings();
        settings.setSelectedClefs(clefs).commitChanges();
        int maxBpm = 0;
        // hide the checks if they are not available in the settings
        if (settings.getIsHideClef(Clef.treble)) {
            // hide it all as there is no choice to make
            this.radioClefs.setVisibility(View.GONE);
            // and set the selected clef to be bass, treble cannot be selected, nor can both
            settings.setSelectedClefs(new Clef[] {Clef.bass});
            // set the check item to match that set and available in the application
            this.radioClefs.check(R.id.radioBassClef);
        }
        else if (settings.getIsHideClef(Clef.bass)) {
            // hide it all as there is no choice to make
            this.radioClefs.setVisibility(View.GONE);
            // and set the selected clef to be treble, bass cannot be selected, nor can both
            settings.setSelectedClefs(new Clef[]{Clef.treble});
        }
        Clef[] selectedClefs = settings.getSelectedClefs();
        // set the check item to match that set and available in the application
        Scores.Score score = this.application.getScores().getScore(this.selectedGame);
        if (selectedClefs.length == 2) {
            // both are selected
            this.radioClefs.check(R.id.radioMixedClefs);
            // show the progress for both
            this.trebleProgress.setVisibility(View.VISIBLE);
            this.bassProgress.setVisibility(View.VISIBLE);
            // find the max BPM from the score
            maxBpm = Math.max(score.getTopBpm(Clef.treble), score.getTopBpm(Clef.bass));

        }
        else if (selectedClefs.length == 1) {
            switch (selectedClefs[0]) {
                case treble:
                    this.radioClefs.check(R.id.radioTrebleClef);
                    // show the progress for this only
                    this.trebleProgress.setVisibility(View.VISIBLE);
                    this.bassProgress.setVisibility(View.INVISIBLE);
                    // and get the max bpm
                    maxBpm = score.getTopBpm(Clef.treble);
                    break;
                case bass:
                    this.radioClefs.check(R.id.radioBassClef);
                    // show the progress for this only
                    this.trebleProgress.setVisibility(View.INVISIBLE);
                    this.bassProgress.setVisibility(View.VISIBLE);
                    // and get the max bpm
                    maxBpm = score.getTopBpm(Clef.bass);
                    break;
            }
        }
        // set these on the music view
        musicView.setPermittedClefs(selectedClefs);
        // update the game progress view
        this.progressView.invalidate();
        // and the progress views
        this.trebleProgressView.setProgress(this.selectedGame, Clef.treble);
        // and bass
        this.bassProgressView.setProgress(this.selectedGame, Clef.bass);
        // and enable the buttons
        enableNextAndBackButtons();

        // set the tempo
        if (maxBpm == 0) {
            this.tempo = GameScore.K_DEFAULT_BPM;
        }
        else {
            this.tempo = maxBpm;
        }
        // and update the controls
        updateTempoAndHelp();
    }


    private void setSelectedGameData() {
        gameTitle.setText(this.selectedGame.name);
        progressView.setSelectedChild(this.selectedGame);
        // set the clefs available properly
        Clef[] selectedClefs = this.application.getSettings().getSelectedClefs();
        setAvailableClefs(selectedClefs);
        // set this on the music view
        musicView.setActiveGame(this.selectedGame);
        // and update the views
        progressView.invalidate();
        musicView.invalidate();

        // and load the image for the title bar
        setTitleImage(this.selectedGame);
    }

    private void setTitleImage(Game game) {
        // set the title
        this.imageTitle.setText(game.name);
        if (null != game.image && false == game.image.isEmpty()) {
            // set the image
            this.imageViewTitlebar.setImageBitmap(GameParentCardHolder.getBitmapFromAssets(game.image, this));
        }
        else if (null != game.parent) {
            setTitleImage(game.parent);
        }
        else {
            this.imageViewTitlebar.setImageResource(R.drawable.piano);
        }
    }

    private boolean changeSelectedGame(int change) {
        // try to change the index
        this.gameIndex += change;
        boolean isChanged = false;
        if (this.gameIndex < 0 || this.gameIndex >= this.parentGame.children.length) {
            // this is too many or too few
            this.gameIndex -= change;
        }
        else {
            // get the selected game
            this.selectedGame = this.parentGame.children[this.gameIndex];
            setSelectedGameData();
            isChanged = true;
        }
        // update the buttons
        enableNextAndBackButtons();
        return isChanged;
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
                this.nextButton.setEnabled(isGamePassed(this.selectedGame));
            }
            else {
                // there is none after us
                this.nextButton.setEnabled(false);
            }
        }
    }
}
