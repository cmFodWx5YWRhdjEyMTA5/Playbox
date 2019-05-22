package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.util.AttributeSet;

import uk.co.darkerwaters.staveinvaders.notes.Clef;
import uk.co.darkerwaters.staveinvaders.games.Game;

public class ClefProgressView extends CircleProgressView {


    public ClefProgressView(Context context) {
        super(context);
    }

    public ClefProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClefProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initialiseView(Context context) {
        // Initialize new data for this view

    }

    public void setProgress(Game game, Clef clef) {
        // set the progress for this game
        float progress = game.getGameProgress(clef);
        int tempo = game.getGameTopTempo(clef);
        if (game.children.length > 0) {
            // this game has children, the progress and tempo is the average of the top tempo
            // of these children that have been passed
            int noPassed = 0;
            for (Game child : game.children) {
                if (child.getIsGamePassed(clef)) {
                    progress += child.getGameProgress(clef);
                    tempo += child.getGameTopTempo(clef);
                    ++noPassed;
                }
            }
            if (noPassed > 0) {
                progress /= noPassed;
                tempo /= noPassed;
            }
        }

        super.setProgress(progress, Integer.toString(tempo));
    }
}
