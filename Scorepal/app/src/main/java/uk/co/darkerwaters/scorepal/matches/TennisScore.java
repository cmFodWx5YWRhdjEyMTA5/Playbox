package uk.co.darkerwaters.scorepal.matches;

import java.util.List;

import uk.co.darkerwaters.scorepal.application.Log;

public class TennisScore extends Score {

    public final static int POINT = 0;
    public final static int GAME = 1;
    public final static int SET = 2;

    private final static int K_LEVELS = 3;

    public TennisScore(Team[] teams) {
        super(teams, K_LEVELS);
    }

    public int getPoints(Team team) {
        return super.getPoint(POINT, team);
    }

    public int getGames(Team team, int setIndex) {
        // get the games for the set index specified
        int toReturn = INVALID_POINT;
        List<int[]> gameResults = super.getPointHistory(GAME - 1);
        if (null == gameResults || setIndex >= gameResults.size()) {
            // there is no history for this set, return the current games instead
            toReturn = super.getPoint(GAME, team);
        }
        else {
            int[] setGames = gameResults.get(setIndex);
            toReturn = setGames[getTeamIndex(team)];
        }
        return toReturn;
    }

    public int getSets(Team team) {
        // get the history of sets to get the last one
        List<int[]> setResults = super.getPointHistory(SET - 1);
        int toReturn = INVALID_POINT;
        if (null != setResults && false == setResults.isEmpty()) {
            int[] setGames = setResults.get(setResults.size() - 1);
            toReturn = setGames[getTeamIndex(team)];
        }
        else {
            // return the running set count
            toReturn = super.getPoint(SET, team);
        }
        return toReturn;
    }

    public int incrementPoint(Team team) {
        // add one to the point already stored
        int point = super.getPoint(POINT, team) + 1;
        // set this back on the score
        super.setPoint(POINT, team, point);
        return point;
    }

    public int incrementGame(Team team) {
        // add one to the game already stored
        int point = super.getPoint(GAME, team) + 1;
        // set this back on the score
        super.setPoint(GAME, team, point);
        // also clear the points
        super.clearLevel(POINT);
        // and return the games we just set
        return point;
    }

    public int incrementSet(Team team) {
        // add one to the set already stored
        int point = super.getPoint(SET, team) + 1;
        // set this back on the score
        super.setPoint(SET, team, point);
        // also clear the games
        super.clearLevel(GAME);
        // and return the games we just set
        return point;
    }

    public void endGame() {
        // clear the sets to end this and wipe current scores
        super.clearLevel(SET);
    }
}
