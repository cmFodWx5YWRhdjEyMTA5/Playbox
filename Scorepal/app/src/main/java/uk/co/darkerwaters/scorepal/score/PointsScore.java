package uk.co.darkerwaters.scorepal.score;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.darkerwaters.scorepal.players.Team;

class PointsScore extends Score {

    private static final int K_POINTS_LEVEL = 1;

    PointsScore(Team[] teams) {
        this(teams, -1);
    }

    PointsScore(Team[] teams, int pointsToPlayTo) {
        super(teams, K_POINTS_LEVEL, Sport.POINTS);
        // remember our goal here
        setScoreGoal(pointsToPlayTo);
    }

    @Override
    void resetScore() {
        // let the super reset their data
        super.resetScore();
        // and reset any of our member data here
    }

    @Override
    void setDataToJson(JSONObject json) throws JSONException {
        super.setDataToJson(json);
        // put any extra data we need to this JSON file, very little on this
        // as this can entirely be reconstructed from the history
    }

    @Override
    void setDataFromJson(JSONObject json) throws JSONException {
        super.setDataFromJson(json);
        // get any data we put on the JSON back off it, again little here
    }

    private int getPlayedPoints() {
        int playedPoints = 0;
        for (Team team : getTeams()) {
            playedPoints += getPoint(0, team);
        }
        return playedPoints;
    }

    Point getDisplayPoint(Team team) {
        return super.getDisplayPoint(0, team);
    }

    int getPoints(Team team) {
        return super.getPoint(0, team);
    }

    @Override
    int incrementPoint(Team team) {
        // add one to the point already stored
        int point = super.incrementPoint(team);

        int pointsToPlayTo = getScoreGoal();
        if (pointsToPlayTo <= 0 || point < pointsToPlayTo) {
            // they have not finished, could we swap ends / servers?
            // play a bit like a tie-break, after the first, and subsequent two points,
            // we have to change servers
            int playedPoints = getPlayedPoints();
            if ((playedPoints - 1) % 2 == 0) {
                // we are at point 1, 3, 5, 7 etc - change server
                changeServer();
            }
            // also change ends every 6 points
            if (playedPoints % 6 == 0) {
                // the set ended with
                changeEnds();
            }
        }
        return point;
    }

    @Override
    boolean isMatchOver() {
        boolean isMatchOver = false;
        int pointsToPlayTo = getScoreGoal();
        if (pointsToPlayTo > 0) {
            // return if a player has reached the points to play to
            for (Team team : getTeams()) {
                if (getPoint(0, team) >= pointsToPlayTo) {
                    // this team has reached the limit
                    isMatchOver = true;
                }
            }
        }
        return isMatchOver;
    }
}
