package uk.co.darkerwaters.scorepal.score;

import android.content.Context;

import uk.co.darkerwaters.scorepal.players.Team;

public class PointsMatch extends Match<PointsScore> {

    public PointsMatch(Context context) {
        super(context, Sport.POINTS);
    }

    @Override
    protected PointsScore createScore(Team[] teams) {
        return new PointsScore(teams, -1);
    }
}
