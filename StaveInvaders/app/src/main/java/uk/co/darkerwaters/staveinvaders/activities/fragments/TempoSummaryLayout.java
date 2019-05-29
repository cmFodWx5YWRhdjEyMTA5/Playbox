package uk.co.darkerwaters.staveinvaders.activities.fragments;

import android.app.Activity;
import android.view.View;

import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.games.Game;
import uk.co.darkerwaters.staveinvaders.notes.Clef;
import uk.co.darkerwaters.staveinvaders.views.ClefProgressView;
import uk.co.darkerwaters.staveinvaders.views.LevelProgressView;

public class TempoSummaryLayout {

    private final ClefProgressView trebleProgressView;
    private final LevelProgressView levelsProgressView;
    private final ClefProgressView bassProgressView;

    public TempoSummaryLayout(View parent) {
        // find all the controls we play with
        this.trebleProgressView = parent.findViewById(R.id.treble_progress_view);
        this.levelsProgressView = parent.findViewById(R.id.levels_progress_view);
        this.bassProgressView = parent.findViewById(R.id.bass_progress_view);
    }

    public TempoSummaryLayout(Activity parent) {
        // find all the controls we play with
        this.trebleProgressView = parent.findViewById(R.id.treble_progress_view);
        this.levelsProgressView = parent.findViewById(R.id.levels_progress_view);
        this.bassProgressView = parent.findViewById(R.id.bass_progress_view);
    }

    public void setProgress(Game game) {
        // set the progress on the bass and treble views
        this.trebleProgressView.setProgress(game, Clef.treble);
        this.bassProgressView.setProgress(game, Clef.bass);
        // and the levels too
        this.levelsProgressView.setProgress(game);
    }
}
