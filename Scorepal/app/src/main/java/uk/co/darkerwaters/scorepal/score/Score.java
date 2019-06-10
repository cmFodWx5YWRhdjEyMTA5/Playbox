package uk.co.darkerwaters.scorepal.score;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;

class Score {

    static final int INVALID_POINT = -1;
    static final int CLEAR_POINT = 0;

    private final Team[] teams;
    private final int[][] points;
    private final List<int[]>[] pointsHistory;
    private final ScoreFactory.ScoreMode scoreMode;

    private final Player[] players;

    // default access, make the users go through a scorer class to store history of the process
    Score(Team[] teams, int pointsLevels, ScoreFactory.ScoreMode mode) {
        this.teams = teams;
        this.points = new int[pointsLevels][teams.length];
        this.pointsHistory = new List[pointsLevels];
        this.scoreMode = mode;
        // also, we will use the players so much, store them in their own list
        List<Player> playerList = new ArrayList<Player>();
        for (Team team : this.teams) {
            // add the players to the global list of players
            playerList.addAll(Arrays.asList(team.getPlayers()));
        }
        // and set the players on this class
        this.players = playerList.toArray(new Player[0]);
        // make sure everything starts off the same each time
        resetScore();
    }

    void resetScore() {
        // set all the points to zero
        for (int[] teamPoints : this.points) {
            for (int i = 0; i < teamPoints.length; ++i) {
                teamPoints[i] = 0;
            }
        }
        // clear the history lists
        for (int i = 0; i < this.pointsHistory.length; ++i) {
            this.pointsHistory[i] = null;
        }
        // reset all the player data (server etc)
        for (Player player : this.players) {
            player.resetPlayer();
        }
        CourtPosition courtPosition = CourtPosition.GetDefault();
        for (Team team : this.teams) {
            // reset the data on the team here
            team.resetTeam();
            // while we are here set the court position for the team
            team.setCourtPosition(courtPosition);
            // and use the next for the next team
            courtPosition = courtPosition.getNext();
        }
        // initialise the server for the first team
        if (this.teams.length > 0) {
            // and initialise the server on the players
            changeServer(this.teams[0].getNextServer());
        }
    }

    ScoreFactory.ScoreMode getScoreMode() {
        return this.scoreMode;
    }

    int getLevels() {
        // return the number of levels we store the points at
        return this.points.length;
    }

    int incrementPoint(Team team) {
        // just add a point to the base level
        int point = getPoint(0, team) + 1;
        setPoint(0, team, point);
        return point;
    }

    void setPoint(int level, Team team, int point) {
        int teamIndex = getTeamIndex(team);
        this.points[level][teamIndex] = point;
    }

    Player[] getPlayers() {
        return Arrays.copyOf(this.players, this.players.length);
    }

    Team[] getTeams() {
        return Arrays.copyOf(this.teams, this.teams.length);
    }

    void changeServer(Player server) {
        for (Player player : this.players) {
            // set the server correctly for all players
            player.setIsServing(player == server);
        }
    }

    Player getServer() {
        Player server = null;
        for (Player player : this.players) {
            // set the server correctly for all players
            if (player.getIsServing()) {
                server = player;
            }
        }
        return server;
    }

    void clearLevel(int level) {
        // we just set the points for a level that is not the bottom, we want to store
        // the points that were the level below in the history and clear them here
        storeHistory(level, this.points[level]);
        // clear this data
        for (int i = 0; i < this.teams.length; ++i) {
            this.points[level][i] = CLEAR_POINT;
        }
    }

    int getPoint(int level, Team team) {
        return this.points[level][getTeamIndex(team)];
    }

    String getPointString(int level, Team team) {
        // just return the point as a string
        return Integer.toString(getPoint(level, team));
    }

    private void storeHistory(int level, int[] toStore) {
        List<int[]> points = this.pointsHistory[level];
        if (points == null) {
            points = new ArrayList<int[]>();
            this.pointsHistory[level] = points;
        }
        // create the array of points we currently have and add to the list
        points.add(Arrays.copyOf(toStore, toStore.length));
    }

    List<int[]> getPointHistory(int level) {
        List<int[]> history = this.pointsHistory[level];
        if (null != history) {
            List<int[]> toReturn = new ArrayList<int[]>();
            for (int[] points : history) {
                if (null != points) {
                    toReturn.add(Arrays.copyOf(points, points.length));
                }
            }
            return toReturn;
        }
        else {
            // no history for this
            return null;
        }
    }

    protected void changeEnds() {
        // cycle each team's court position
        for (Team team : this.teams) {
            // set the court position to the next position in the list
            team.setCourtPosition(team.getCourtPosition().getNext());
        }
    }

    boolean isMatchOver() { return false; }

    protected int getTeamIndex(Team team) {
        for (int i = 0; i < this.teams.length; ++i) {
            if (this.teams[i] == team) {
                // this is the team
                return i;
            }
        }
        Log.error("Searching for a team in score that is not in the list");
        return 0;
    }

    protected Team getOtherTeam(Team team) {
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

    protected Team getWinner(int level) {
        int topPoints = INVALID_POINT;
        int topTeam = 0;
        for (int i = 0; i < this.points[level].length; ++i) {
            if (this.points[level][i] > topPoints) {
                // this is the top team
                topPoints = this.points[level][i];
                topTeam = i;
            }
        }
        return this.teams[topTeam];
    }

    protected void changeServer() {
        // the current server must yield now to the new one
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

    int getPointsTotal(int level, Team team) {
        // add all the points for this team
        int iTeam = getTeamIndex(team);
        int total = getPoint(level, team);
        List<int[]> history = getPointHistory(level);
        if (null != history) {
            // add all this history to the total
            for (int[] points : history) {
                total += points[iTeam];
            }
        }
        return total;
    }
}