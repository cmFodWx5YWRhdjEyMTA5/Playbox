package uk.co.darkerwaters.scorepal.matches;

import java.util.List;

import uk.co.darkerwaters.scorepal.application.Log;

public class TennisScore extends Score {

    public final static int POINT = 0;
    public final static int GAME = 1;
    public final static int SET = 2;

    public enum Sets {
        FIVE(5),
        THREE(3),
        ONE(1);

        final int val;
        final int target;

        Sets(int value) {
            this.val = value;
            this.target = (int)((value + 1f) / 2f);
        }
    }

    public final static String STR_LOVE = "Love";
    public final static String STR_FIFTEEN = "15";
    public final static String STR_THIRTY = "30";
    public final static String STR_FORTY = "40";
    public final static String STR_ADVANTAGE = "Advantage";
    public final static String STR_DEUCE = "Deuce";
    public final static String STR_GAME = "Game";

    public final static String[] POINTS_STRINGS = new String[] {STR_LOVE, STR_FIFTEEN, STR_THIRTY, STR_FORTY, STR_ADVANTAGE, STR_GAME};

    private final int POINTS_TO_WIN_GAME = 4;
    private final int POINTS_AHEAD_IN_GAME = 2;

    private final int POINTS_TO_WIN_TIE = 7;
    private final int POINTS_AHEAD_IN_TIE = 2;

    private final int GAMES_TO_WIN_SET = 6;
    private final int GAMES_AHEAD_IN_SET = 2;

    private final static int K_LEVELS = 3;

    private final Sets setsToPlay;

    private boolean isFinalSetTie = false;
    private boolean isInTieBreak = false;
    private Player tieBreakServer = null;

    public TennisScore(Team[] teams, Sets setsToPlay) {
        super(teams, K_LEVELS);
        this.setsToPlay = setsToPlay;
        // initialise the server

    }

    public void setIsFinalSetTieBreaker(boolean isTieInFinalSet) {
        this.isFinalSetTie = isTieInFinalSet;
    }

    @Override
    public boolean isMatchOver() {
        boolean isMatchOver = false;
        // return if a player has reached the number of sets required (this is just over half)
        for (Team team : getTeams()) {
            if (getSets(team) >= this.setsToPlay.target) {
                // this team has reached the limit, match is over
                isMatchOver = true;
            }
        }
        return isMatchOver;
    }

    public int getPoints(Team team) {
        return super.getPoint(POINT, team);
    }

    @Override
    public String getPointString(int level, Team team) {
        String pointsString;
        if (this.isInTieBreak) {
            // we are in a tie break, just use the numbers of the points
            pointsString = super.getPointString(level, team);
        }
        else {
            switch (level) {
                case POINT:
                    // return the point string
                    pointsString = getPointsString(team);
                    break;
                default:
                    pointsString = super.getPointString(level, team);
                    break;
            }
        }
        return pointsString;
    }

    public String getPointsString(Team team) {
        String pointsString;
        if (this.isInTieBreak) {
            pointsString = super.getPointString(POINT, team);
        }
        else {
            // not in a tie, show the points string correctly
            int points = getPoints(team);
            Team opposition = getOtherTeam(team);
            int otherPoints = getPoints(opposition);
            switch (points) {
                case 0: // love
                case 1: // 15
                case 2: // 30
                    // we are less than 40, just return the string from the array
                    pointsString = POINTS_STRINGS[points];
                    break;
                case 3:
                    // we have 40, if the other player has 40 too, we are at deuce
                    if (otherPoints == 3) {
                        // this is 40-40
                        pointsString = STR_DEUCE;
                    } else {
                        // they have fewer, or advantage, we just have 40
                        pointsString = STR_FORTY;
                    }
                    break;
                case 4:
                    // either we have advantage, if they have 40, or we have game if they have less
                    if (otherPoints == 3) {
                        // they have 40, so we have advantage
                        pointsString = STR_ADVANTAGE;
                    } else {
                        // they have fewer as we have 4, we must have won already
                        pointsString = STR_GAME;
                    }
                    break;
                case 5:
                    // we have the game
                    pointsString = STR_GAME;
                    break;
                default:
                    // this is wrong as we shouldn't have this many
                    Log.error("A player in tennis score has too many points with '" + points + "' points");
                    pointsString = super.getPointString(POINT, team);
                    break;
            }
        }
        // return the string
        return pointsString;
    }

    private Team getGameWinner() {
        // return the winner of the current game, or null if not yet over
        for (Team team : getTeams()) {
            if (getPointsString(team) == STR_GAME) {
                // this team has 'game' so this is the winner
                return team;
            }
        }
        // if no team has game then no team is the winner
        return null;
    }

    private Team getOtherTeam(Team team) {
        for (Team other : getTeams()) {
            if (other != team) {
                // this is the one
                return other;
            }
        }
        // quite serious this (only one team?)
        Log.error("There are not enough teams when trying to find the other...");
        return team;
    }

    public int getGames(Team team, int setIndex) {
        // get the games for the set index specified
        int toReturn = INVALID_POINT;
        List<int[]> gameResults = super.getPointHistory(GAME);
        if (null == gameResults || setIndex < 0 || setIndex >= gameResults.size()) {
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
        List<int[]> setResults = super.getPointHistory(SET);
        int toReturn;
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

    public void incrementPoint(Team team) {
        // add one to the point already stored
        int point = super.getPoint(POINT, team) + 1;
        // set this back on the score
        super.setPoint(POINT, team, point);
        int otherPoint = getPoints(getOtherTeam(team));
        // has this team won the game with this new point addition (can't be the other)
        if (false == this.isInTieBreak) {
            if (point >= POINTS_TO_WIN_GAME && point - otherPoint >= POINTS_AHEAD_IN_GAME) {
                // not in a tie and we have enough points (and enough ahead) to win the game
                incrementGame(team);
            }
        }
        else {
            // are in a tie
            if (point >= POINTS_TO_WIN_TIE && point - otherPoint >= POINTS_AHEAD_IN_TIE) {
                // in a tie and we have enough points (and enough ahead) to win the game
                incrementGame(team);
            }
            else {
                // we are in a tie-break, after the first, and subsequent two points, we have to
                // change servers
                int playedPoints = getPlayedPonts();
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
        }
    }

    private int getPlayedPonts() {
        int playedPoints = 0;
        for (Team team : getTeams()) {
            playedPoints += getPoints(team);
        }
        return playedPoints;
    }

    private int getPlayedGames(int setIndex) {
        int playedGames = 0;
        for (Team team : getTeams()) {
            playedGames += getGames(team, setIndex);
        }
        return playedGames;
    }

    private int getPlayedSets() {
        int playedSets = 0;
        for (Team team : getTeams()) {
            playedSets += getSets(team);
        }
        return playedSets;
    }

    private void incrementGame(Team team) {
        // add one to the game already stored
        int point = super.getPoint(GAME, team) + 1;
        // set this back on the score
        super.setPoint(GAME, team, point);
        // also clear the points
        super.clearLevel(POINT);

        boolean isSetChanged = false;
        if (point >= GAMES_TO_WIN_SET) {
            // this team have enough games to win, as long as the other don't have too many...
            Team other = getOtherTeam(team);
            int otherPoints = getGames(other, -1);
            if ((this.isInTieBreak && point != otherPoints)
                    || point - otherPoints >= GAMES_AHEAD_IN_SET) {
                // they are enough games ahead (2) so they have won
                incrementSet(team);
                // won the set, this is the end of the tie break
                this.isInTieBreak = false;
                isSetChanged = true;
            }
            else if (isTieBreakSet() && otherPoints >= GAMES_TO_WIN_SET){
                // we are not ahead enough, we both have more than 6 and are in a tie break set
                // time to initiate a tie break
                this.isInTieBreak = true;
            }
        }
        if (false == isSetChanged) {
            // we want to change ends at the end of the first, 3, 5 (every odd game) of each set
            if ((getPlayedGames(-1) - 1) % 2 == 0) {
                // this is an odd game, change ends
                changeEnds();
            }
        }
        if (false == isMatchOver()) {
            // every game we alternate the server
            changeServer();
            if (this.isInTieBreak && null == this.tieBreakServer) {
                // we just set the server to serve but we need to remember who starts
                this.tieBreakServer = getServer();
            }
        }
    }

    public boolean isInTieBreak() {
        return this.isInTieBreak;
    }

    private boolean isTieBreakSet() {
        // we are in a tie break set if not the final set, or the final set is a tie-break set
        if (getPlayedSets() == this.setsToPlay.val - 1) {
            // we are playing the final set
            return this.isFinalSetTie;
        }
        else {
            // not the final set, this is a tie
            return true;
        }
    }

    private void incrementSet(Team team) {
        // add one to the set already stored
        int point = super.getPoint(SET, team) + 1;
        // set this back on the score
        super.setPoint(SET, team, point);
        // remember how many games were played before we clear then
        int gamesPlayed = getPlayedGames(-1);
        // also clear the games
        super.clearLevel(GAME);
        if (isMatchOver()) {
            // clear the sets to end this and wipe current scores
            super.clearLevel(SET);
        }
        else {
            // we want to change ends at the end of any set in which the score was even
            if (gamesPlayed % 2 == 0) {
                // the set ended with
                changeEnds();
            }
        }
    }

    private void changeServer() {
        // the current server must yield now to the new one
        if (!this.isInTieBreak && null != this.tieBreakServer) {
            // we were in a tie break, the next server should be the one after the player
            // that started the tie break, set the server back to the player that started it
            changeServer(this.tieBreakServer);
            this.tieBreakServer = null;
        }
        // find the team that is serving at the moment
        Team servingTeam = null;
        for (Team team : getTeams()) {
            // check the players
            for (Player player : team.getPlayers()) {
                // if this player is serving, we found the serving team
                if (player.getIsServing()) {
                    servingTeam = team;
                    break;
                }
            }
            if (null != servingTeam) {
                break;
            }
        }
        if (null != servingTeam) {
            // we have a serving team, change team, and not the player that was last serving
            Team otherTeam = getOtherTeam(servingTeam);
            Player newServer = otherTeam.getNextServer();
            // change the server to this new player
            changeServer(newServer);
        }
    }
}
