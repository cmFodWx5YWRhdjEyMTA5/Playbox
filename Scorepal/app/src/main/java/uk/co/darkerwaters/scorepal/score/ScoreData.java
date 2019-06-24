package uk.co.darkerwaters.scorepal.score;

import android.util.Pair;

import java.util.ArrayList;

import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;

public class ScoreData {
    /*
        {		— as the first char
        u		— for sending score
        1 or 0	— current server
        1 or 0	— current north player
        n:		— no sets player one (base 10)
        n:		— no sets player two (base 10)
        for no sets
            n:		— no games player one (base 10)
            n:		— no games player two (base 10)
        n:		— no points player one (base 10)
        n:		— no points player two (base 10)
        n:		— total points player one (base 10)
        n:		— total points player two (base 10)
        n:		— total number historic points (base 10)
        for total history
            1 or 0	— historic winner
        }		— as the last char

        expecting ‘r’ in response to data received
     */

    public int currentServer = 0;
    public int currentNorth = 0;
    public Sport currentScoreMode = Sport.POINTS;
    public int currentSetsOption = 0;
    public boolean isInTieBreak = false;
    public Integer matchWinner = null;
    public Pair<Integer, Integer> sets;
    public ArrayList<Pair<Integer, Integer>> previousSets;
    public Pair<Integer, Integer> points;
    public Pair<Integer, Integer> games;
    public Pair<Integer, Integer> totalPoints;
    public int noHistoricPoints;
    public int[] historicPoints;

    public int dataCode = 0;
    public char dataVersion = 0;
    public int secondsStartTime = 0;
    public int secondsGameDuration = 0;
    public String dataCommand = "u";

    public String filename = null;

    public ScoreData() {
        this.currentServer = 0;
        this.currentNorth = 0;
        this.currentScoreMode = Sport.POINTS;
        this.isInTieBreak = false;
        this.matchWinner = null;
        this.sets = new Pair<Integer, Integer>(0, 0);
        this.previousSets = new ArrayList<Pair<Integer, Integer>>();
        this.points = new Pair<Integer, Integer>(0, 0);
        this.games = new Pair<Integer, Integer>(0, 0);
        this.totalPoints = new Pair<Integer, Integer>(0, 0);
        this.noHistoricPoints = 0;
        this.historicPoints = new int[0];
    }

    public ScoreData(Match match) {
        // we are in the same package so have access to the score direct
        Score score = match.getScore();
        // build this data from the current score, first find the team that is serving
        Player server = score.getServer();
        Team[] teams = score.getTeams();
        Team matchWinner = null;
        int scoreLevels = score.getLevels();
        if (score.isMatchOver()) {
            // remember the winner
            matchWinner = score.getWinner(scoreLevels - 1);
        }
        for (int i = 0; i < teams.length; ++i) {
            // is the server in this team?
            if (teams[i].isPlayerInTeam(server)) {
                // the server is in this team
                this.currentServer = i;
            }
            // is this team north?
            if (teams[i].getCourtPosition() == CourtPosition.NORTH) {
                this.currentNorth = i;
            }
            if (teams[i] == matchWinner) {
                // this is the match winner
                this.matchWinner = new Integer(i);
            }
        }
        // what score mode is this?
        this.currentScoreMode = score.getSport();

        // some things for the data are score specific
        TennisScore tennisScore = null;
        if (score instanceof TennisScore) {
            tennisScore = (TennisScore) score;
            // tennis specific
            if (null != tennisScore) {
                // are we in a tie break ?
                this.isInTieBreak = tennisScore.isInTieBreak();
            }
        }
        // do the points now, for this there are two teams / players. Get them here
        Team playerOne = null, playerTwo = null;
        if (teams.length > 0) {
            playerOne = teams[0];
        }
        else {
            // make not null for simple code
            playerOne = new Team(new Player[0], CourtPosition.GetDefault());
        }
        if (teams.length > 1) {
            playerTwo = teams[0];
        }
        else {
            // make not null for simple code
            playerTwo = new Team(new Player[0], CourtPosition.GetDefault());
        }
        // store the score of the match in our members here
        switch (scoreLevels) {
            case 3:
                // there is a third level - store this as sets
                this.sets = new Pair<Integer, Integer>(
                        score.getPoint(2, playerOne),
                        score.getPoint(2, playerTwo));
                // also have to do the previous sets here
                this.previousSets.clear();
                for (int[] setHistory : score.getPointHistory(2)) {
                    // add the history from the score to the list we will store in this data
                    this.previousSets.add(new Pair<Integer, Integer>(
                            setHistory[0], setHistory[1]
                    ));
                }
                // break; - fall through
            case 2:
                // there is a second level - store this as games
                this.games = new Pair<Integer, Integer>(
                        score.getPoint(1, playerOne),
                        score.getPoint(1, playerTwo));
                // break; - fall through
            case 1:
                // there is a level one - store this as points
                this.points = new Pair<Integer, Integer>(
                        score.getPoint(0, playerOne),
                        score.getPoint(0, playerTwo));
                this.totalPoints = new Pair<Integer, Integer>(
                        score.getPointsTotal(0, playerOne),
                        score.getPointsTotal(0, playerTwo));
                // this is the end, no levels do nothing
                break;
        }
        // and we also want to do the history of the entire match
        Team[] pointWinners = match.getWinnersHistory();
        this.noHistoricPoints = pointWinners.length;
        this.historicPoints = new int[this.noHistoricPoints];
        for (int i = 0; i < this.noHistoricPoints; ++i) {
            // each time a team wins, put it's index in the list
            if (pointWinners[i] == playerOne) {
                // player one won this
                this.historicPoints[i] = 0;
            }
            else {
                // player two won it
                this.historicPoints[i] = 1;
            }
        }
    }

    @Override
    public String toString() {
        try {
            // return all the data in this class as a properly formatted data string
            StringBuilder recDataString = new StringBuilder();
            // now write all the data, first comes the command character
            writeChar(this.dataCommand, recDataString);
            // then the version
            writeChar(this.dataVersion, recDataString);
            // then the data code, this has a colon
            writeStringWithColon(dataCode, recDataString);
            // now the start and duration timers which also have colons
            writeStringWithColon(secondsStartTime, recDataString);
            writeStringWithColon(secondsGameDuration, recDataString);
            // now the active mode
            writeChar(this.currentScoreMode.value, recDataString);
            // the match winner
            if (this.matchWinner == null) {
                // no match winner
                writeChar(9, recDataString);
            } else {
                // there is a winner, write it
                writeChar(this.matchWinner, recDataString);
            }
            // are we in a tie
            writeChar(this.isInTieBreak ? 1 : 0, recDataString);
            // the server
            writeChar(this.currentServer, recDataString);
            writeChar(this.currentNorth, recDataString);
            // now the sets
            writeStringWithColon(sets.first, recDataString);
            writeStringWithColon(sets.second, recDataString);
            // now the games for each set player
            for (Pair<Integer, Integer> pair : previousSets) {
                writeStringWithColon(pair.first, recDataString);
                writeStringWithColon(pair.second, recDataString);
            }
            // points
            writeStringWithColon(points.first, recDataString);
            writeStringWithColon(points.second, recDataString);
            // games
            writeStringWithColon(games.first, recDataString);
            writeStringWithColon(games.second, recDataString);
            // total points
            writeStringWithColon(totalPoints.first, recDataString);
            writeStringWithColon(totalPoints.second, recDataString);
            // no historic points
            writeStringWithColon(noHistoricPoints, recDataString);
            // and then all the historic points we have
            int bitCounter = 0;
            int dataPacket = 0;
            for (int i = 0; i < noHistoricPoints; ++i) {
                // add this value to the data packet
                dataPacket |= historicPoints[i] << bitCounter;
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
            // and return the results
            return recDataString.toString();
        }
        catch (Exception e) {
            Log.error("Failed to write the data string", e);
            return null;
        }
    }

    public ScoreData(StringBuilder recDataString) {
        if (null == recDataString) {
            // there is no data here, fine just leave all as default
        }
        else {
            // remove the first char, which should be the command
            dataCommand = extractChars(1, recDataString);
            if (dataCommand.equals("u")) {
                // first get the version - one char only but representing the number of it
                dataVersion = extractChars(1, recDataString).charAt(0);
                // now process the data for this version
                switch(dataVersion) {
                    case 'a':
                        parseVersionOneScoreData(recDataString);
                        break;
                    default:
                        //TODO something more serious - inform the user this isn't possible?
                        Log.error("unsupported data version: '" + dataVersion + "'");
                        break;
                }
            }
        }
    }

    private void parseVersionOneScoreData(StringBuilder recDataString) {
        // get the code that we need to respond with
        dataCode = extractValueToColon(recDataString);
        // get the start and duration timers
        secondsStartTime = extractValueToColon(recDataString);
        secondsGameDuration = extractValueToColon(recDataString);
        // get the active mode
        currentScoreMode = Sport.from(extractValueToColon(recDataString));
        currentSetsOption = extractValueToColon(recDataString);
        // and the winner
        String matchWinnerData = extractChars(1, recDataString);
        if (matchWinnerData != null && !matchWinnerData.isEmpty()) {
            // there is a winner, 0 or 1 - set this
            matchWinner = Integer.parseInt(matchWinnerData);
        }
        isInTieBreak = Integer.parseInt(extractChars(1, recDataString)) == 1;
        // get the current server
        currentServer = Integer.parseInt(extractChars(1, recDataString));
        currentNorth = Integer.parseInt(extractChars(1, recDataString));
        sets = new Pair<Integer, Integer>(extractValueToColon(recDataString), extractValueToColon(recDataString));
        // do the games for each set played
        int totalSets = extractValueToColon(recDataString);
        previousSets = new ArrayList<Pair<Integer, Integer>>(totalSets);
        for (int i = 0; i < totalSets; ++i) {
            previousSets.add(new Pair<Integer, Integer>(extractValueToColon(recDataString), extractValueToColon(recDataString)));
        }
        points = new Pair<Integer, Integer>(extractValueToColon(recDataString), extractValueToColon(recDataString));
        games = new Pair<Integer, Integer>(extractValueToColon(recDataString), extractValueToColon(recDataString));
        totalPoints = new Pair<Integer, Integer>(extractValueToColon(recDataString), extractValueToColon(recDataString));
        // now do all the historic points
        noHistoricPoints = extractValueToColon(recDataString);
        historicPoints = new int[noHistoricPoints];
        int dataCounter = 0;
        while (dataCounter < noHistoricPoints) {
            // while there are points to get, get them
            int dataReceived = extractHistoryValue(recDataString);
            // this char contains somewhere between one and eight values all bit-shifted, extract them now
            int bitCounter = 0;
            while (bitCounter < 10 && dataCounter < noHistoricPoints) {
                int bitValue = 1 & (dataReceived >> bitCounter++);
                // add this to the list of value received and inc the counter of data
                historicPoints[dataCounter++] = bitValue;
            }
        }
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

    private int extractHistoryValue(StringBuilder recDataString) {
        // get the string as a double char value
        String hexString = extractChars(2, recDataString);
        return Integer.parseInt(hexString, 32);
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

    private void writeChar(String data, StringBuilder recDataString) {
        if (data.length() != 1) {
            // oops
            throw new StringIndexOutOfBoundsException();
        }
        else {
            recDataString.append(data);
        }
    }

    private void writeChar(char data, StringBuilder recDataString) {
        // append this char to the string builder
        recDataString.append(Character.toString(data));
    }

    private void writeChar(int data, StringBuilder recDataString) {
        if (data > 9 || data < 0) {
            // oops
            throw new StringIndexOutOfBoundsException();
        }
        else {
            recDataString.append(Integer.toString(data));
        }
    }

    private void writeStringWithColon(int data, StringBuilder recDataString) {
        recDataString.append(Integer.toString(data));
        recDataString.append(':');
    }
}
