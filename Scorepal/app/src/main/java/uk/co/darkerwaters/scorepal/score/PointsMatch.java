package uk.co.darkerwaters.scorepal.score;

import android.content.Context;

import uk.co.darkerwaters.scorepal.players.Team;

public class PointsMatch extends Match<PointsScore> {

    public PointsMatch(Context context) {
        this(context, -1);
    }

    public PointsMatch(Context context, int pointsToPlayTo) {
        super(context, CreateScoreFactory(pointsToPlayTo));
    }

    private static ScoreFactory<PointsScore> CreateScoreFactory(final int pointsToPlayTo) {
        return new ScoreFactory<PointsScore>() {
            @Override
            public PointsScore createScore(Team[] teams) {
                return new PointsScore(teams, pointsToPlayTo);
            }
        };
    }
}
