package uk.co.darkerwaters.scorepal.matches;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.darkerwaters.scorepal.application.Log;

public class Score {

    public static final int INVALID_POINT = -1;
    public static final int CLEAR_POINT = 0;

    private final Team[] teams;
    private final int[][] points;
    private final List<int[]>[] pointsHistory;

    private final Player[] players;

    public Score(Team[] teams, int pointsLevels) {
        this.teams = teams;
        this.points = new int[pointsLevels][teams.length];
        this.pointsHistory = new List[pointsLevels];

        // also, we will use the players so much, store them in their own list
        List<Player> playerList = new ArrayList<Player>();
        CourtPosition courtPosition = CourtPosition.GetDefault();
        for (Team team : this.teams) {
            // while we are here set the court position for the team
            team.setCourtPosition(courtPosition);
            // and use the next for the next team
            courtPosition = courtPosition.getNext();
            // add the players to the global list of players
            playerList.addAll(Arrays.asList(team.getPlayers()));
        }
        // and set the players on this class
        this.players = playerList.toArray(new Player[0]);
        // initialise the server for the first team
        if (this.teams.length > 0) {
            // and initialise the server on the players
            changeServer(this.teams[0].getNextServer());
        }
    }

    public void setPoint(int level, Team team, int point) {
        int teamIndex = getTeamIndex(team);
        this.points[level][teamIndex] = point;
    }

    public Player[] getPlayers() {
        return Arrays.copyOf(this.players, this.players.length);
    }

    public Team[] getTeams() {
        return Arrays.copyOf(this.teams, this.teams.length);
    }

    public void changeServer(Player server) {
        for (Player player : this.players) {
            // set the server correctly for all players
            player.setIsServing(player == server);
        }
    }

    public Player getServer() {
        Player server = null;
        for (Player player : this.players) {
            // set the server correctly for all players
            if (player.getIsServing()) {
                server = player;
            }
        }
        return server;
    }

    public void clearLevel(int level) {
        // we just set the points for a level that is not the bottom, we want to store
        // the points that were the level below in the history and clear them here
        storeHistory(level, this.points[level]);
        // clear this data
        for (int i = 0; i < this.teams.length; ++i) {
            this.points[level][i] = CLEAR_POINT;
        }
    }

    public int getPoint(int level, Team team) {
        return this.points[level][getTeamIndex(team)];
    }

    public String getPointString(int level, Team team) {
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

    public List<int[]> getPointHistory(int level) {
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

    public boolean isMatchOver() { return false; }

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
}
