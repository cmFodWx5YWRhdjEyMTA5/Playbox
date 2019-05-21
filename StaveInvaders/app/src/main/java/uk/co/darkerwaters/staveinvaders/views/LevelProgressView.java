package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.games.Game;

public class LevelProgressView extends CircleProgressView {


    public LevelProgressView(Context context) {
        super(context);
    }

    public LevelProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LevelProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initialiseView(Context context) {
        // Initialize new data for this view

    }

    public void setProgress(Game game) {
        // show the levels complete on both clefs
        Settings settings = this.application.getSettings();
        List<MusicView.Clefs> clefs = new ArrayList<MusicView.Clefs>(2);
        if (!settings.getIsHideClef(MusicView.Clefs.treble)) {
            // are doing treble
            clefs.add(MusicView.Clefs.treble);
        }
        if (!settings.getIsHideClef(MusicView.Clefs.bass)) {
            // are doing bass
            clefs.add(MusicView.Clefs.bass);
        }
        // draw the game count, how many are passed?
        int gamesPassed = 0;
        int gamesAvailable = 0;
        for (int i = 0; i < game.children.length; ++i) {
            // get the passed game count for the non-hidden clefs
            for (MusicView.Clefs clef : clefs) {
                // this is a game
                ++gamesAvailable;
                if (game.children[i].getIsGamePassed(clef)) {
                    // this game is passed
                    ++gamesPassed;
                }
            }
        }
        String gameCount = gamesPassed + "/" + gamesAvailable;
        float progress = gamesAvailable == 0 ? 0 : gamesPassed / (float)gamesAvailable;
        // set this progress
        super.setProgress(progress, gameCount);
    }
}
