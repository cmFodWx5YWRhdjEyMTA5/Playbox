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

    @Override
    public boolean isMatchOver() {
        boolean isGameOver = false;
        if (this.pointsToPlayTo > 0) {
            // return if a player has reached the points to play to
            for (Team team : getTeams()) {
                if (getPoint(0, team) >= pointsToPlayTo) {
                    // this team has reached the limit
                    isGameOver = true;
                }
            }
        }
        return isGameOver;
    }
}
