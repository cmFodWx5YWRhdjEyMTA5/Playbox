package uk.co.darkerwaters.scorepal.matches;

public class PointsScore extends Score {

    private static final int K_POINTS_LEVEL = 1;

    private final int pointsToPlayTo;

    public PointsScore(Team[] teams) {
        this(teams, -1);
    }

    public PointsScore(Team[] teams, int pointsToPlayTo) {
        super(teams, K_POINTS_LEVEL);

        this.pointsToPlayTo = pointsToPlayTo;
    }

    public int getPointsToPlayTo() {
        return this.pointsToPlayTo;
    }

    private int getPlayedPoints() {
        int playedPoints = 0;
        for (Team team : getTeams()) {
            playedPoints += getPoint(0, team);
        }
        return playedPoints;
    }

    public int getPoints(Team team) {
        return super.getPoint(0, team);
    }

    public String getPointString(Team team) {
        return super.getPointString(0, team);
    }

    @Override
    public int incrementPoint(Team team) {
        // add one to the point already stored
        int point = super.incrementPoint(team);

        if (this.pointsToPlayTo <= 0 || point < this.pointsToPlayTo) {
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
    public boolean isMatchOver() {
        boolean isMatchOver = false;
        if (this.pointsToPlayTo > 0) {
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
