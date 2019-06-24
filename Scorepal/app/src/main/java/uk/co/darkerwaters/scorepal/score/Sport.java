package uk.co.darkerwaters.scorepal.score;

import android.app.Activity;
import android.content.Context;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.PointsSetupActivity;
import uk.co.darkerwaters.scorepal.activities.TennisPlayActivity;
import uk.co.darkerwaters.scorepal.activities.TennisSetupActivity;

public enum Sport {
    POINTS(0
            , "images/points.jpg"
            , R.string.points_sport
            , R.string.pointsSubtitle
            , PointsSetupActivity.class
            , null /* PointsPlayActivity */),
    TENNIS(1
            , "images/tennis.jpg"
            , R.string.tennis
            , R.string.tennisSubtitle
            , TennisSetupActivity.class
            , TennisPlayActivity.class),
    //BADMINTON(1, "images/badminton.jpg", R.string.badminton, R.string.badmintonSubtitle, BadmintonSetupActivity.class),
    ;

    public static final Sport DEFAULT = TENNIS;

    public final int value;
    public final String imageFilename;
    public final int titleResId;
    public final int subtitleResId;
    public final Class<? extends Activity> setupActivityClass;
    public final Class<? extends Activity> playActivityClass;

    private String resolvedTitle = null;
    private String resolvedSubtitle = null;

    Sport(int value, String imageFilename,
          int titleResId, int subtitleResId,
          Class<? extends Activity> setupActivityClass,
          Class<? extends Activity> playActivityClass) {
        this.value = value;
        this.imageFilename = imageFilename;
        this.titleResId = titleResId;
        this.subtitleResId = subtitleResId;
        this.setupActivityClass = setupActivityClass;
        this.playActivityClass = playActivityClass;
    }

    public static Match CreateMatch(Sport sport, Context context) {
        switch (sport) {
            case TENNIS:
                return new TennisMatch(context);
            case POINTS:
                return new PointsMatch(context);
            default:
                return null;
        }
    }

    public static Sport[] GetSports() {
        return values();
    }

    public static void ResolveSportTitles(Context context) {
        // set the string members on all the sports from the context strings
        for (Sport sport : values()) {
            sport.getTitle(context);
            sport.getSubtitle(context);
        }
    }

    public Match createMatch(Context context) {
        return CreateMatch(this, context);
    }

    public static Sport from(int value) {
        for (Sport sport : values()) {
            if (sport.value == value) {
                return sport;
            }
        }
        // return the default
        return Sport.DEFAULT;
    }

    public String getTitle(Context context) {
        if (null == this.resolvedTitle) {
            this.resolvedTitle = context.getString(this.titleResId);
        }
        return this.resolvedTitle;
    }

    public String getSubtitle(Context context) {
        if (null == this.resolvedSubtitle) {
            this.resolvedSubtitle = context.getString(this.subtitleResId);
        }
        return this.resolvedSubtitle;
    }

    public static Sport from(String string, Context context) {
        for (Sport sport : values()) {
            if (sport.getTitle(context).equals(string)) {
                return sport;
            }
        }
        // return the default
        return Sport.DEFAULT;
    }

    @Override
    public String toString() {
        if (null != this.resolvedTitle) {
            return this.resolvedSubtitle;
        }
        else {
            return super.toString();
        }
    }
}
