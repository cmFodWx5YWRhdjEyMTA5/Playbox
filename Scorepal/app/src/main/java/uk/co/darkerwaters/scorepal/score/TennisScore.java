package uk.co.darkerwaters.scorepal.score;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;

public class TennisScore extends Score {

    final static int POINT = 0;
    final static int GAME = 1;
    final static int SET = 2;

    static enum TennisPoint implements Point {
        LOVE(0, R.string.display_love, R.string.speak_love),
        FIFTEEN(1, R.string.display_15, R.string.speak_15),
        THIRTY(2, R.string.display_30, R.string.speak_30),
        FORTY(3, R.string.display_40, R.string.speak_40),
        DEUCE(4, R.string.display_deuce, R.string.speak_deuce),
        ADVANTAGE(5, R.string.display_advantage, R.string.speak_advantage),
        GAME(6, R.string.display_game, R.string.speak_game);

        private final int value;
        private final int displayStrId;
        private final int speakStrId;

        TennisPoint(int value, int displayStrId, int speakStrId) {
            this.value = value;
            this.displayStrId = displayStrId;
            this.speakStrId = speakStrId;
        }
        @Override
        public int val() {
            return this.value;
        }
        @Override
        public String displayString(Context context) {
            if (null != context && 0 != this.displayStrId) {
                return context.getString(this.displayStrId);
            }
            else {
                return Integer.toString(this.value);
            }
        }
        @Override
        public String speakString(Context context) {
            if (null != context && 0 != this.speakStrId) {
                return context.getString(this.speakStrId);
            }
            else {
                return Integer.toString(this.value);
            }
        }

        public static Point fromVal(int points) {
            for (TennisPoint point : TennisPoint.values()) {
                if (point.val() == points) {
                    return point;
                }
            }
            // if here then we don't have a tennis point, return a simple number one
            return new SimplePoint(points);
        }
    }

    private final int POINTS_TO_WIN_GAME = 4;
    private final int POINTS_AHEAD_IN_GAME = 2;

    private final int POINTS_TO_WIN_TIE = 7;
    private final int POINTS_AHEAD_IN_TIE = 2;

    private final int GAMES_TO_WIN_SET = 6;
    private final int GAMES_AHEAD_IN_SET = 2;

    private final static int K_LEVELS = 3;

    private boolean isFinalSetTie;
    private boolean isInTieBreak;
    private Player tieBreakServer;

    private final List<Integer> tieBreakSets;

    TennisScore(Team[] teams, TennisSets setsToPlay) {
        super(teams, K_LEVELS, ScoreFactory.ScoreMode.K_TENNIS);
        this.isFinalSetTie = false;
        this.tieBreakSets = new ArrayList<Integer>();
        // the score goal is the number of sets to play
        setScoreGoal(setsToPlay.val);
    }

    @Override
    void resetScore() {
        // let the base reset
        super.resetScore();
        // and reset our data
        this.isInTieBreak = false;
        this.tieBreakServer = null;
        if (null != this.tieBreakSets) {
            this.tieBreakSets.clear();
        }
    }

    void setIsFinalSetTieBreaker(boolean isTieInFinalSet) {
        this.isFinalSetTie = isTieInFinalSet;
    }

    @Override
    public boolean isMatchOver() {
        boolean isMatchOver = false;
        TennisSets setsToPlay = getSetsToPlay();
        // return if a player has reached the number of sets required (this is just over half)
        for (Team team : getTeams()) {
            if (getSets(team) >= setsToPlay.target) {
                // this team has reached the limit, match is over
                isMatchOver = true;
            }
        }
        return isMatchOver;
    }

    TennisSets getSetsToPlay() {
        // the sets to play are set from the score goal
        return TennisSets.fromValue(getScoreGoal());
    }

    public int getPoints(Team team) {
        return super.getPoint(POINT, team);
    }

    @Override
    Point getDisplayPoint(int level, Team team) {
        Point displayPoint;
        if (this.isInTieBreak) {
            // we are in a tie break, just use the numbers of the points
            displayPoint = super.getDisplayPoint(level, team);
        }
        else {
            switch (level) {
                case POINT:
                    // return the point string
                    displayPoint = getDisplayPoint(team);
                    break;
                default:
                    displayPoint = super.getDisplayPoint(level, team);
                    break;
            }
        }
        return displayPoint;
    }

    public Point getDisplayPoint(Team team) {
        Point displayPoint;
        if (this.isInTieBreak) {
            displayPoint = super.getDisplayPoint(POINT, team);
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
                    displayPoint = TennisPoint.fromVal(points);
                    break;
                case 3:
                    // we have 40, if the other player has 40 too, we are at deuce
                    if (otherPoints == 3) {
                        // this is 40-40
                        displayPoint = TennisPoint.DEUCE;
                    } else {
                        // they have fewer, or advantage, we just have 40
                        displayPoint = TennisPoint.FORTY;
                    }
                    break;
                default:
                    // if we are one ahead we have advantage
                    int delta = points - otherPoints;
                    switch(delta) {
                        case 0 :
                            //this is deuce
                            displayPoint = TennisPoint.DEUCE;
                            break;
                        case 1:
                            // we have ad
                            displayPoint = TennisPoint.ADVANTAGE;
                            break;
                        case -1:
                            // we are disadvantaged
                            displayPoint = TennisPoint.FORTY;
                            break;
                        default:
                            displayPoint = TennisPoint.GAME;
                           break;
                    }
            }
        }
        // return the string
        return displayPoint;
    }

    public int[] getPoints(Team team, int setIndex, int gameIndex) {
        // get the points in the set and games index specified
        int[] toReturn = null;
        // to get the points for this game, we need to find the index of that game
        // so for that we need to add up all the games for all the previous sets
        // before we get to this one
        List<int[]> gameResults = super.getPointHistory(POINT);
        List<int[]> setResults = super.getPointHistory(GAME);
        if (null != setResults && null != gameResults) {
            // there are results for the sets (a record of the games for each)
            // we need to add these up to find the start of the set as a number of games
            int gamesPlayed = 0;
            for (int i = 0; i < setIndex && i < setResults.size(); ++i) {
                for (int games : setResults.get(i)) {
                    gamesPlayed += games;
                }
            }
            // the index into the game results is the games played in previous sets
            // plus the index we are interested in
            gameIndex += gamesPlayed;
            if (gameIndex < gameResults.size()) {
                // this is ok
                toReturn = gameResults.get(gameIndex);
            }
        }
        return toReturn;
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

    public boolean isSetTieBreak(int setIndex) {
        return this.tieBreakSets.contains(new Integer(setIndex));
    }

    @Override
    int incrementPoint(Team team) {
        // add one to the point already stored
        int point = super.incrementPoint(team);
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
        }
        return point;
    }

    public int getPlayedPoints() {
        int playedPoints = 0;
        for (Team team : getTeams()) {
            playedPoints += getPoints(team);
        }
        return playedPoints;
    }

    public int getPlayedGames(int setIndex) {
        int playedGames = 0;
        for (Team team : getTeams()) {
            playedGames += getGames(team, setIndex);
        }
        return playedGames;
    }

    public int getPlayedSets() {
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
                // record that this current set was settled with a tie
                this.tieBreakSets.add(getPlayedSets());
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
        if (getPlayedSets() == getSetsToPlay().val - 1) {
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
            // we want to change ends at the end of any set in which the score wasn't even
            if (gamesPlayed % 2 != 0) {
                // the set ended with odd number of games
                changeEnds();
            }
        }
    }

    @Override
    protected void changeServer() {
        // the current server must yield now to the new one
        if (!this.isInTieBreak && null != this.tieBreakServer) {
            // we were in a tie break, the next server should be the one after the player
            // that started the tie break, set the server back to the player that started it
            changeServer(this.tieBreakServer);
            this.tieBreakServer = null;
        }
        // and let the base change the server
        super.changeServer();
    }
}
