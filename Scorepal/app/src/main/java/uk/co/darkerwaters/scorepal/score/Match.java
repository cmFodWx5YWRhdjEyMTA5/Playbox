package uk.co.darkerwaters.scorepal.score;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;

public class Match<T extends Score> implements Score.ScoreListener {

    private static final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    public static class PointChange {
        public final Team team;
        public final int level;
        public final int point;
        PointChange(Team team, int level, int point) {
            this.team = team;
            this.level = level;
            this.point = point;
        }
    }

    private String description;
    private boolean isDoubles;
    private String matchPlayedDate;
    private int matchMinutesPlayed;
    private final Team[] teams;
    private Sport sport = Sport.TENNIS;

    private final T score;
    private final Stack<Team> pointHistory;

    private boolean isReadOnly = false;

    private Team startingTeam;
    private Player[] startingServers;
    private CourtPosition[] startingEnds;

    private ArrayList<PointChange> pointLevelsChanged;

    private boolean isDataPersisted = false;

    public enum MatchChange {
        RESET, INCREMENT, DECREMENT, STARTED, DOUBLES_SINGLES, GOAL, PLAYERS, ENDS, SERVER, DECIDING_POINT, BREAK_POINT, BREAK_POINT_CONVERTED;
    }

    public interface MatchListener {
        void onMatchChanged(MatchChange type);
        void onMatchPointsChanged(PointChange[] levelsChanged);
    }

    private final List<MatchListener> listeners;

    public Match(Context context, Sport sport) {
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
        // no minutes so far
        this.matchMinutesPlayed = 0;
        this.listeners = new ArrayList<MatchListener>();
        this.pointLevelsChanged = null;
        // set the description
        this.description = "A match played";
        // create the score here
        this.score = createScore(teams);
        // listen to this score to pass on the information to our listeners
        this.score.addListener(this);
        // we will also store the entire history played
        this.pointHistory = new Stack<Team>();
        // setup the starting server
        this.startingServers = new Player[this.teams.length];
        Arrays.fill(this.startingServers, null);
        // and the starting ends
        this.startingEnds = new CourtPosition[this.teams.length];
        Arrays.fill(this.startingEnds, null);
        // find the default starting team to start with each time
        Player startingServer = score.getServer();
        this.startingTeam = null;
        for (Team team : this.teams) {
            if (team.isPlayerInTeam(startingServer)) {
                // this is the starting team
                this.startingTeam = team;
            }
        }
        // this data is not saved yet
        this.isDataPersisted = false;
    }

    public String getMatchSummary(Context context) {
        //TODO show the time we play etc then add the score summary
        return this.score.getScoreSummary(context);
    }

    public boolean isMatchOver() {
        return this.score.isMatchOver();
    }

    public void resetMatch() {

        // reset the score
        resetScoreToStartingPosition();
        // and clear the history
        this.pointHistory.clear();
        // and the time played
        this.matchMinutesPlayed = 0;
        // this data is not saved yet
        this.isDataPersisted = false;
    }

    public boolean setDataToJson(JSONObject obj) {
        boolean isSuccess = false;
        try {
            // do all the house keeping
            obj.put("description", this.description);
            obj.put("doubles", this.isDoubles);
            obj.put("played_date", this.matchPlayedDate);
            obj.put("match_minutes", this.matchMinutesPlayed);
            obj.put("sport", this.sport.toString());

            // do the team names
            obj.put("team_one_name", getTeamOne().getTeamName());
            obj.put("team_two_name", getTeamTwo().getTeamName());

            // do the player names
            obj.put("player_one_name", getPlayerOne().getPlayerName());
            obj.put("partner_one_name", getPlayerOnePartner().getPlayerName());
            obj.put("player_two_name", getPlayerTwo().getPlayerName());
            obj.put("partner_two_name", getPlayerTwoPartner().getPlayerName());
            // starting servers
            obj.put("team_one_starting_server", getTeamOne().toPlayerString(this.startingServers[0]));
            obj.put("team_two_starting_server", getTeamTwo().toPlayerString(this.startingServers[1]));
            // starting ends
            obj.put("team_one_starting_end", CourtPosition.toString(this.startingEnds[0]));
            obj.put("team_two_starting_end", CourtPosition.toString(this.startingEnds[1]));

            // and do the starting team as a nice number to
            if (this.startingTeam == getTeamOne()) {
                obj.put("starting_team", "Team1");
            }
            else if (this.startingTeam == getTeamTwo()) {
                obj.put("starting_team", "Team2");
            }
            else {
                obj.put("starting_team", "none");
            }
            // create a child for the score
            JSONObject scoreObj = new JSONObject();
            // add any data from the score
            this.score.setDataToJson(scoreObj);
            // and, perhaps most importantly - the score history
            scoreObj.put("score_history", getPointHistoryAsString());
            // being sure to add this to the root
            obj.put("score", scoreObj);

            // if here then everything is ok
            isSuccess = true;
        }
        catch (JSONException e) {
            Log.error("Failed to write something to JSON", e);
        }
        return isSuccess;
    }

    public boolean setDataFromJson(JSONObject obj, Context context) {
        boolean isSuccess = false;

        try {
            // do all the house keeping
            this.description = obj.getString("description");
            this.isDoubles = obj.getBoolean("doubles");
            this.matchPlayedDate = obj.getString("played_date");
            this.matchMinutesPlayed = obj.getInt("match_minutes");
            this.sport = Sport.from(obj.getString("sport"), context);

            // do the team names
            setTeamOneName(obj.getString("team_one_name"));
            setTeamTwoName(obj.getString("team_two_name"));

            // do the player names
            setPlayerOneName(obj.getString("player_one_name"));
            setPlayerOnePartnerName(obj.getString("partner_one_name"));
            setPlayerTwoName(obj.getString("player_two_name"));
            setPlayerTwoPartnerName(obj.getString("partner_two_name"));
            // starting servers
            this.startingServers[0] = getTeamOne().fromPlayerString(obj.getString("team_one_starting_server"));
            this.startingServers[1] = getTeamOne().fromPlayerString(obj.getString("team_two_starting_server"));
            // starting ends
            this.startingEnds[0] = CourtPosition.fromString(obj.getString("team_one_starting_end"));
            this.startingEnds[1] = CourtPosition.fromString(obj.getString("team_two_starting_end"));

            // and do the starting team
            switch (obj.getString("starting_team")) {
                case "Team1" :
                    this.startingTeam = getTeamOne();
                    break;
                case "Team2" :
                    this.startingTeam = getTeamTwo();
                    break;
                default :
                    this.startingTeam = null;
                    break;
            }

            // find the child that is the score
            JSONObject scoreObj = obj.getJSONObject("score");
            // add any data from the score
            this.score.setDataFromJson(scoreObj);
            // and, perhaps most importantly - the score history
            StringBuilder scoreHistory = new StringBuilder(scoreObj.getString("score_history"));
            // and set the history from this
            restorePointHistoryFromString(scoreHistory);
            // finally - LAST THING TO DO - restore our state from this history
            restorePointHisory();

            // and if all this is done, it was a success
            isSuccess = true;
        }
        catch (JSONException e) {
            Log.error("Failed to get something from JSON", e);
            isSuccess = false;
        }
        return isSuccess;
    }

    public String getPointHistoryAsString() {
        StringBuilder recDataString = new StringBuilder();
        int noHistoricPoints = this.pointHistory.size();
        // first write the number of historic points we are going to store
        recDataString.append(noHistoricPoints);
        recDataString.append(':');
        // and then all the historic points we have
        int bitCounter = 0;
        int dataPacket = 0;
        Team teamOne = getTeamOne();
        for (int i = 0; i < noHistoricPoints; ++i) {
            // get the team as a binary value
            int binaryValue;
            if (this.pointHistory.get(i) == teamOne) {
                // this is team one, so this is zero
                binaryValue = 0;
            }
            else {
                // it can only be team two
                binaryValue = 1;
            }
            // add this value to the data packet
            dataPacket |= binaryValue << bitCounter;
            // and increment the counter, sending as radix32 number means we can store 10 bits of data (up to 1023 base 10)
            if (++bitCounter >= 10) {
                // exceeded the size for next time, send this packet
                if (dataPacket < 32) {
                    // this will be print as '0' up to 'F' but we need it to be '0F' as expecting a fixed length...
                    // this is true for hex - who knows how a radix32 number is printed - but whatever nice that we get 10 values
                    recDataString.append('0');
                }
                recDataString.append(Integer.toString(dataPacket, 32));
                // and reset the counter and data
                bitCounter = 0;
                dataPacket = 0;
            }
        }
        if (bitCounter > 0) {
            // there was data we failed to send, only partially filled - send this anyway
            if (dataPacket < 32) {
                // this will be print as '0' up to 'F' but we need it to be '0F' as expecting a fixed length...
                // this is true for hex - who knows how a radix64 number is printed - but whatever nice that we get 10 values
                recDataString.append('0');
            }
            recDataString.append(Integer.toString(dataPacket, 32));
        }
        return recDataString.toString();
    }

    private void restorePointHistoryFromString(StringBuilder pointHistoryString) {
        // the value before the colon is the number of subsequent values
        int noHistoricPoints = extractValueToColon(pointHistoryString);
        int dataCounter = 0;
        Team teamOne = getTeamOne();
        Team teamTwo = getTeamTwo();
        while (dataCounter < noHistoricPoints) {
            // while there are points to get, get them
            int dataReceived = extractHistoryValue(pointHistoryString);
            // this char contains somewhere between one and eight values all bit-shifted, extract them now
            int bitCounter = 0;
            while (bitCounter < 10 && dataCounter < noHistoricPoints) {
                int bitValue = 1 & (dataReceived >> bitCounter++);
                // add this to the list of value received and inc the counter of data
                switch (bitValue) {
                    case 0 :
                        this.pointHistory.push(teamOne);
                        break;
                    case 1:
                        this.pointHistory.push(teamTwo);
                        break;
                }
                // increment the counter
                ++dataCounter;
            }
        }
    }

    private int extractHistoryValue(StringBuilder recDataString) {
        // get the string as a double char value
        String hexString = extractChars(2, recDataString);
        return Integer.parseInt(hexString, 32);
    }

    private int extractValueToColon(StringBuilder recDataString) {
        int colonIndex = recDataString.indexOf(":");
        if (colonIndex == -1) {
            throw new StringIndexOutOfBoundsException();
        }
        // extract this data as a string
        String extracted = extractChars(colonIndex, recDataString);
        // and the colon
        recDataString.delete(0, 1);
        // return the data as an integer
        return Integer.parseInt(extracted);
    }

    private String extractChars(int charsLength, StringBuilder recDataString) {
        String extracted = "";
        if (recDataString.length() >= charsLength) {
            extracted = recDataString.substring(0, charsLength);
        }
        else {
            throw new StringIndexOutOfBoundsException();
        }
        recDataString.delete(0, charsLength);
        return extracted;
    }

    public String getDescriptionShort(Context context) {
        // return a nice description
        int minutesPlayed = getMatchMinutesPlayed();
        int hoursPlayed = (int)(minutesPlayed / 60f);
        minutesPlayed = minutesPlayed - (hoursPlayed * 60);
        Date matchPlayedDate = getMatchPlayedDate();
        String description = String.format(context.getString(R.string.match_description)
                , getMatchWinner().getTeamName()
                , isMatchOver() ? context.getString(R.string.match_beat) : context.getString(R.string.match_beating)
                , getOtherTeam(getMatchWinner()).getTeamName()
                , String.format("%d",hoursPlayed)
                , String.format("%02d",minutesPlayed)
                , DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                , DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
        // and return this
        return description;
    }

    public String getDescriptionLong(Context context) {
        // return a nice description
        return getDescriptionShort(context);
    }

    public boolean addListener(MatchListener listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    public boolean removeListener(MatchListener listener) {
        synchronized (this.listeners) {
            return this.listeners.remove(listener);
        }
    }

    public void informListeners(MatchChange type) {
        if (null == this.score || this.score.isInformActive()) {
            synchronized (this.listeners) {
                for (MatchListener listener : this.listeners) {
                    listener.onMatchChanged(type);
                }
            }
            // this data is not saved yet
            this.isDataPersisted = false;
        }
    }

    private void informListenersOfPointChange() {
        if (null != this.pointLevelsChanged
                && (null == this.score || this.score.isInformActive())) {
            synchronized (this.listeners) {
                for (MatchListener listener : this.listeners) {
                    listener.onMatchPointsChanged(this.pointLevelsChanged.toArray(new PointChange[0]));
                }
            }
            // clear the list of levels that changed
            this.pointLevelsChanged = null;
            // this data is not saved yet
            this.isDataPersisted = false;
        }
    }

    public void setTeamStarting(Team startingTeam) {
        if (isReadOnly) {
            Log.error("cannot edit a match once it is started!");
        }
        else {
            // set the starting team to start serving
            this.startingTeam = startingTeam;
            // this sets the starting server to the server from that team
            // so change the server on the score to be the one from the starting team
            this.score.changeServer(this.startingTeam.getServingPlayer());
            // this data is not saved yet
            this.isDataPersisted = false;
        }
    }

    public Team getTeamStarting() {
        return this.startingTeam;
    }

    public Team getTeamServing() {
        Team servingTeam = null;
        Player currentServer = this.getCurrentServer();
        for (Team team : this.teams) {
            if (team.isPlayerInTeam(currentServer)) {
                // this is the serving team
                servingTeam = team;
                break;
            }
        }
        return servingTeam;
    }

    public void cycleTeamStartingEnds() {
        if (isReadOnly) {
            Log.error("cannot edit a match once it is started!");
        }
        else {
            // set the starting ends for each team
            for (int i = 0; i < this.teams.length; ++i) {
                CourtPosition newPosition = this.teams[i].getCourtPosition().getNext();
                this.teams[i].setInitialCourtPosition(newPosition);
                // and remember this for when we have to reset back to the start of the match
                this.startingEnds[i] = newPosition;
            }
            // and refresh the ends on the score
            this.score.refreshTeamEnds();
            // this data is not saved yet
            this.isDataPersisted = false;
        }
    }

    public void setTeamStartingServer(Player startingServer) {
        // set the starting server for their team
        //TODO you can only do this on the first game of each set, choose who starts.
        if (isReadOnly) {
            Log.error("cannot edit a match once it is started - need to allowing editing team servers on first game!");
        }
        else {
            for (int i = 0; i < this.teams.length; ++i) {
                if (this.teams[i].isPlayerInTeam(startingServer)) {
                    // found their team, set their starting server
                    this.teams[i].setServingPlayer(startingServer);
                    // also remember this for when we have to reset back to the start of the match
                    this.startingServers[i] = startingServer;
                    break;
                }
            }
            Team servingTeam = getTeamServing();
            if (null != servingTeam && servingTeam.isPlayerInTeam(startingServer)) {
                // the starting server for the serving team has changed, this is the new server
                this.score.changeServer(startingServer);
            }
            // this data is not saved yet
            this.isDataPersisted = false;
        }
    }

    public void addMatchMinutesPlayed(int minutesPlayed) {
        this.matchMinutesPlayed += minutesPlayed;
        // this data is not saved yet
        this.isDataPersisted = false;
    }

    public int getMatchMinutesPlayed() {
        return this.matchMinutesPlayed;
    }

    boolean isDataPersisted() { return this.isDataPersisted; }
    void setDataPersisted() { this.isDataPersisted = true; }

    @Override
    public void onScoreChanged(Team team, int level, int newPoint) {
        // this is an actual change in score, store all the changes so we can inform
        // in a nice informative batch of levels that changed during an increment of score
        if (null != this.pointLevelsChanged) {
            this.pointLevelsChanged.add(new PointChange(team, level, newPoint));
        }
        // this data is not saved yet
        this.isDataPersisted = false;
    }

    @Override
    public void onScoreChanged(Score.ScoreChange type) {
        // pass on the interesting changes to our listener
        switch (type) {
            case POINTS_SET:
            case INCREMENT:
            case GOAL:
            case RESET:
                // none of this is interesting, we did all that from here and informed correctly
                break;
            case BREAK_POINT:
                // pass this on
                informListeners(MatchChange.BREAK_POINT);
                break;
            case BREAK_POINT_CONVERTED:
                // pass this on
                informListeners(MatchChange.BREAK_POINT_CONVERTED);
                break;
            case DECIDING_POINT:
                // this is interesting, someone will want to know this
                informListeners(MatchChange.DECIDING_POINT);
                break;
            case ENDS :
                // this is interesting, someone will want to know this
                informListeners(MatchChange.ENDS);
                break;
            case SERVER:
                // this is interesting too, pass it on
                informListeners(MatchChange.SERVER);
                break;
        }
        // this data is not saved yet
        this.isDataPersisted = false;
    }

    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    private void resetScoreToStartingPosition() {
        // reset the score to the starting state in order to reset properly
        this.score.resetScore();
        // this is no longer read-only all reset to zero
        this.isReadOnly = false;
        // need to re-establish the starting servers for each team that was decided on
        // at match startup
        for (int i = 0; i < this.startingServers.length; ++i) {
            if (null != this.startingServers[i]) {
                // there was one decided on for this team, set this
                this.teams[i].setServingPlayer(this.startingServers[i]);
            }
        }
        // also we need to do the ends
        for (int i = 0; i < this.startingEnds.length; ++i) {
            if (null != this.startingEnds[i]) {
                // there was one decided on for this team, set this
                this.teams[i].setInitialCourtPosition(this.startingEnds[i]);
                // as we are in the reset stage then this sets the current position too
                this.teams[i].setCourtPosition(this.startingEnds[i]);
            }
        }
        // change the server on the score to be from the starting team
        setTeamStarting(this.startingTeam);
        // inform listeners so they can set the player who is starting serve, location etc.
        informListeners(MatchChange.RESET);
    }

    public int incrementPoint(Team team) {
        // before we increment, create the list that will be added to as points change
        this.pointLevelsChanged = new ArrayList<PointChange>();
        // just add a point to the base level
        int pointToReturn = this.score.incrementPoint(team);
        // and store the history of this action in this scorer
        this.pointHistory.push(team);
        // from this point forward we cannot change anything on this match's data
        // else changing the starting position etc can really mess us up
        this.isReadOnly = true;
        // inform listeners of this change to the score
        informListeners(MatchChange.INCREMENT);
        // now we will have gathered all the changes, inform the listeners
        informListenersOfPointChange();
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
            // pop the last point from the history
            teamThatWonPoint = this.pointHistory.pop();
            // and restore the history that remains
            restorePointHisory();
            // inform listeners of this change to the score
            informListeners(MatchChange.DECREMENT);
        }
        // return the team who's point was popped
        return teamThatWonPoint;
    }

    private void restorePointHisory() {
        // stop the score from sending out update messages while we reconstruct it
        this.score.silenceInformers(true);
        // reset the score
        resetScoreToStartingPosition();
        // and restore the rest
        for (Team scoringTeam : this.pointHistory) {
            this.score.incrementPoint(scoringTeam);
        }
        // stop the score from sending out update messages while we reconstruct it
        this.score.silenceInformers(false);
        // we are read only if there are points in the history
        this.isReadOnly = false == this.pointHistory.isEmpty();
    }

    public T getScore() {
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

    public String getMatchId(Context context) {
        return this.matchPlayedDate + "_" + this.sport.getTitle(context);
    }

    public static boolean isMatchIdValid(String matchId) {
        boolean isValid = false;
        try {
            fileDateFormat.parse(matchId);
            isValid = true;
        } catch (ParseException e) {
            // whatever, just isn't valid is all
        }
        return isValid;
    }

    public String createScoreDataMessage() {
        return "{" + new ScoreData(this) + "}";
    }

    public Sport getSport() {
        return this.score.getSport();
    }

    protected T createScore(Team[] teams) {
        return null;
    }

    public Player getCurrentServer() {
        return this.score.getServer();
    }

    public Date getMatchPlayedDate() {
        Date played = null;
        if (null != this.matchPlayedDate) {
            try {
                played = fileDateFormat.parse(this.matchPlayedDate);
            } catch (ParseException e) {
                Log.error("Failed to create the match date", e);
            }
        }
        return played;
    }

    public void setMatchPlayedDate(Date date) {
        if (isReadOnly) {
            Log.error("cannot edit a match once it is started!");
        }
        else {
            this.matchPlayedDate = fileDateFormat.format(date);
            // inform listeners of this change to the score
            informListeners(MatchChange.STARTED);
        }
    }

    public void setIsDoubles(boolean isDoubles) {
        if (isReadOnly) {
            Log.error("cannot edit a match once it is started!");
        }
        else {
            this.isDoubles = isDoubles;
        }
        // inform listeners of this change to the score
        informListeners(MatchChange.DOUBLES_SINGLES);
    }

    public int getScoreGoal() {
        return this.score.getScoreGoal();
    }

    public void setScoreGoal(int goal) {
        this.score.setScoreGoal(goal);
        // inform listeners of this change to the score
        informListeners(MatchChange.GOAL);
    }

    public int getPointsTotal(int level, int teamIndex) {
        // count all the points in the levels
        return getPointsTotal(level, this.teams[teamIndex]);
    }

    public int getPointsTotal(int level, Team team) {
        // count all the points in the levels
        return this.score.getPointsTotal(level, team);
    }

    public Team getMatchWinner() {
        return this.score.getWinner(this.score.getLevels() - 1);
    }

    public Team getOtherTeam(Team team) {
        if (team == getTeamOne()) {
            return getTeamTwo();
        }
        else {
            return getTeamOne();
        }
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

    public void setTeamOneName(String name) {
        // can change names whenever we like... just change it
        this.teams[0].setTeamName(name);
    }

    public void setTeamTwoName(String name) {
        // can change names whenever we like... just change it
        this.teams[1].setTeamName(name);
    }

    public void setPlayerOneName(String name) {
        // can change names whenever we like... just change it
        this.teams[0].getPlayers()[0].setPlayerName(name);
    }

    public void setPlayerOnePartnerName(String name) {
        // can change names whenever we like... just change it
        this.teams[0].getPlayers()[1].setPlayerName(name);
    }

    public void setPlayerTwoName(String name) {
        // can change names whenever we like... just change it
        this.teams[1].getPlayers()[0].setPlayerName(name);
    }

    public void setPlayerTwoPartnerName(String name) {
        // can change names whenever we like... just change it
        this.teams[1].getPlayers()[1].setPlayerName(name);
    }

    public void setPlayerOne(Player player) {
        if (isReadOnly) {
            Log.error("cannot edit a match once it is started!");
        }
        else {
            this.teams[0].getPlayers()[0] = player;
            // inform listeners of this change to the score
            informListeners(MatchChange.PLAYERS);
        }
    }

    public void setPlayerTwo(Player player) {
        if (isReadOnly) {
            Log.error("cannot edit a match once it is started!");
        }
        else {
            this.teams[1].getPlayers()[0] = player;
            // inform listeners of this change to the score
            informListeners(MatchChange.PLAYERS);
        }
    }

    public void setPlayerOnePartner(Player player) {
        if (isReadOnly) {
            Log.error("cannot edit a match once it is started!");
        }
        else {
            this.teams[0].getPlayers()[1] = player;
            // inform listeners of this change to the score
            informListeners(MatchChange.PLAYERS);
        }
    }

    public void setPlayerTwoPartner(Player player) {
        if (isReadOnly) {
            Log.error("cannot edit a match once it is started!");
        }
        else {
            this.teams[1].getPlayers()[1] = player;
            // inform listeners of this change to the score
            informListeners(MatchChange.PLAYERS);
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
