package uk.co.darkerwaters.staveinvaders.actvities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.games.Game;
import uk.co.darkerwaters.staveinvaders.games.GameList;
import uk.co.darkerwaters.staveinvaders.games.GamePlayer;
import uk.co.darkerwaters.staveinvaders.views.CircleProgressView;
import uk.co.darkerwaters.staveinvaders.views.GameProgressView;
import uk.co.darkerwaters.staveinvaders.views.MusicView;
import uk.co.darkerwaters.staveinvaders.views.MusicViewPlaying;
import uk.co.darkerwaters.staveinvaders.views.PianoView;

import static uk.co.darkerwaters.staveinvaders.actvities.fragments.GameParentCardHolder.K_SELECTED_CARD_FULL_NAME;

public class GamePlayActivity extends AppCompatActivity {

    private static final long K_PLAY_COUNTDOWN = 5000l;
    private Application application;

    private Game selectedGame;

    private CircleProgressView progressView;
    private MusicViewPlaying musicView;
    private PianoView pianoView;

    private volatile boolean isPerformCountdown = true;
    private GamePlayer gamePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        this.application = (Application) this.getApplication();
        Settings settings = this.application.getSettings();

        this.progressView = findViewById(R.id.circleProgressView);
        this.musicView = findViewById(R.id.musicView);
        this.pianoView = findViewById(R.id.pianoView);

        Intent intent = getIntent();
        String parentGameName = intent.getStringExtra(K_SELECTED_CARD_FULL_NAME);
        this.selectedGame = GameList.findLoadedGame(parentGameName);
        if (null == this.selectedGame) {
            Log.error("Game name of " + parentGameName + " does not correspond to a game");
        }
        else {
            // set our title to be the name of the game parent
            setTitle(selectedGame.name);
        }

        // setup the music view
        musicView.setPermittedClefs(settings.getSelectedClefs());
        // create the game player and setup the music view accordingly
        this.gamePlayer = this.musicView.setActiveGame(this.selectedGame);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // start our countdown
        this.isPerformCountdown = true;
        this.progressView.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // perform the countdown
                countDownToStartOfPlay();
            }
        }).start();
    }

    @Override
    protected void onPause() {
        this.isPerformCountdown = false;
        synchronized (this) {
            this.notifyAll();
        }
        super.onPause();
    }

    private void countDownToStartOfPlay() {
        // countdown to play time
        long playTime = System.currentTimeMillis() + K_PLAY_COUNTDOWN;
        while (this.isPerformCountdown) {
            // while we are counting down to play, play
            final float secondsTillPlay =  (playTime - System.currentTimeMillis()) / 1000f;
            if (secondsTillPlay <= 0f) {
                // start the game
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startPlaying();
                    }
                });
                // and quit out of this thread
                break;
            }
            else {
                // show the seconds left on the display
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float progress = secondsTillPlay / (K_PLAY_COUNTDOWN / 1000f);
                        progressView.setProgress(progress, Integer.toString((int)(secondsTillPlay + 1)));
                        progressView.invalidate();
                    }
                });
            }
            synchronized (this) {
                try {
                    this.wait(100);
                } catch (InterruptedException e) {
                    // whatever
                }
            }
        }
    }

    private void startPlaying() {
        // lose the progress countdown timer
        this.progressView.setVisibility(View.GONE);

        // start the game
        if (null != this.gamePlayer) {
            // turn help off, this sets us playing the game
            this.gamePlayer.startNewGame();
        }
    }
}
