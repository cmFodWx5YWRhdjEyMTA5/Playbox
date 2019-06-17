package uk.co.darkerwaters.scorepal.score;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;

public class Match {

    private static final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    private String description;
    private boolean isDoubles;
    private String matchPlayedDate;
    private final Team[] teams;
    private Sport sport = Sport.TENNIS;

    private final Score score;
    private final Stack<Team> pointHistory;

    private boolean isReadOnly = false;

    public Match(Context context, ScoreFactory.ScoreMode scoreMode) {
        // setup the teams, will replace players in the structures as required
        this.teams = new Team[] {
                new Team(new Player[] {
                        new Player(context.getString(R.string.default_playerOneName)),
                        new Player(context.getString(R.string.default_playerOnePartnerName))
                }, CourtPosition.GetDefault()),
                new Team(new Player[] {
                        new Player(context.getString(R.string.default_playerTwoName)),
                        new Player(context.getString(R.string.default_playerTwoPartnerName))
                }, CourtPosition.GetDefault().getNext()),
        };
        // create the score here
        this.score = ScoreFactory.CreateScore(teams, scoreMode);
        // we will also store the entire history played
        this.pointHistory = new Stack<Team>();
    }

    private void resetScoreToStartingPosition() {
        // reset the score to the starting state in order to reset properly
        this.score.resetScore();
        // set the player who is starting serve, location etc.
    }

    public int incrementPoint(Team team) {
        // just add a point to the base level
        int pointToReturn = this.score.incrementPoint(team);
        // and store the history of this action in this scorer
        this.pointHistory.push(team);
        // from this point forward we cannot change anything on this match's data
        // else changing the starting position etc can really mess us up
        this.isReadOnly = true;
        // and return the new points for the team
        return pointToReturn;
    }

    public Team undoLastPoint() {
        // we want to remove the last point, this can be tricky as can effect an awful lot of things
        // like are we in a tie-break, serving end, number of sets games, etc. This is hard to undo
        // so instead we are being lazy and using the power of the device you are on. ie, reset
        // the score and re-populate based on the history
        Team teamThatWonPoint = null;
        if (false == this.pointHistory.isEmpty()) {
            // reset the score
            resetScoreToStartingPosition();
            // pop the last point from the history
            teamThatWonPoint = this.pointHistory.pop();
            // and restore the rest
            for (Team scoringTeam : this.pointHistory) {
                this.score.incrementPoint(scoringTeam);
            }
        }
        if (this.pointHistory.isEmpty()) {
            // there are no points played, we can edit again
            this.isReadOnly = false;
        }
        // return the team who's point was popped
        return teamThatWonPoint;
    }

    Score getScore() {
        return this.score;
    }

    public Team[] getWinnersHistory() {
        // return the history as an array of which team won each point
        return this.pointHistory.toArray(new Team[0]);
    }

    public boolean isMatchStarted() {
        // match is started when there are points in the history
        return false == this.pointHistory.empty();
    }

    public String getMatchId() {
        return this.matchPlayedDate;
    }

    public String createScoreDataMessage() {
        return "{" + new ScoreData(this) + "}";
    }

    public ScoreFactory.ScoreMode getScoreMode() {
        return this.score.getScoreMode();
    }

    public Date getMatchPlayedDate() {
        Date played = null;
        try {
            played = fileDateFormat.parse(this.matchPlayedDate);
        } catch (ParseException e) {
            Log.error("Failed to create the match date", e);
        }
        return played;
    }

    public void setMatchPlayedDate(Date date) {
        if (isReadOnly) {
            Log.error("cannot edit a match once it is started!");
        }
        else {
            this.matchPlayedDate = fileDateFormat.format(date);
        }
    }

    public void setIsDoubles(boolean isDoubles) {
        this.isDoubles = isDoubles;
    }

    public int getScoreGoal() {
        return this.score.getScoreGoal();
    }

    public void setScoreGoal(int goal) {
        this.score.setScoreGoal(goal);
    }

    public boolean getIsDoubles() {
        return this.isDoubles;
    }

    public Team getTeamOne() {
        return this.teams[0];
    }

    public Team getTeamTwo() {
        return this.teams[1];
    }

    public Player getPlayerOne() {
        return this.teams[0].getPlayers()[0];
    }

    public Player getPlayerTwo() {
        return this.teams[1].getPlayers()[0];
    }

    public Player getPlayerOnePartner() {
        return this.teams[0].getPlayers()[1];
    }

    public Player getPlayerTwoPartner() {
        return this.teams[1].getPlayers()[1];
    }

    public void setPlayerOne(Player player) {
        if (isReadOnly) {
            Log.error("cannot edit a match once it is started!");
        }
        else {
            this.teams[0].getPlayers()[0] = player;
        }
    }

    public void setPlayerTwo(Player player) {
        if (isReadOnly) {
            Log.error("cannot edit a match once it is started!");
        }
        else {
            this.teams[1].getPlayers()[0] = player;
        }
    }

    public void setPlayerOnePartner(Player player) {
        if (isReadOnly) {
            Log.error("cannot edit a match once it is started!");
        }
        else {
            this.teams[0].getPlayers()[1] = player;
        }
    }

    public void setPlayerTwoPartner(Player player) {
        if (isReadOnly) {
            Log.error("cannot edit a match once it is started!");
        }
        else {
            this.teams[1].getPlayers()[1] = player;
        }
    }

    public static boolean isFileDatesSame(Date fileDate1, Date fileDate2) {
        // compare only up to seconds as only up to seconds stored in the filename
        // for simplicities sake we can use the same formatter we use for the filename and compare strings
        if (fileDate1 != null && fileDate2 == null) {
            return false;
        }
        else if (fileDate1 == null && fileDate2 != null) {
            return false;
        }
        else if (fileDate1 == null && fileDate2 == null) {
            return true;
        }
        else {
            // do the actual comparing then
            String stringDate1 = fileDateFormat.format(fileDate1);
            String stringDate2 = fileDateFormat.format(fileDate2);
            return stringDate1.equals(stringDate2);
        }
    }
}
