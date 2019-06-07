package uk.co.darkerwaters.scorepal.matches;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TennisScoreTest extends ScoreTest {

    @Test
    public void settingTennisValues() {
        // create a series of games
        TennisScore score = new TennisScore(this.teams, TennisScore.Sets.FIVE);
        for (int iSets = 0; iSets < 3; ++iSets) {
            for (int iGames = 0; iGames < 6; ++iGames) {
                // set the points to player one wins
                for (int iPoints = 0; iPoints < 4; ++iPoints) {
                    score.incrementPoint(this.teams[0]);
                }
                // set the game won
                assertEquals("Points should be zero after winning game", 0, score.getPoints(teams[0]));
                assertEquals("Games should be " + (iGames + 1), iGames + 1, score.getGames(teams[0], iSets));
            }
            // set should be won
            assertEquals("Games should be six", 6, score.getGames(teams[0], iSets));
        }

        // so the points are nothing because we won the game
        assertEquals("Points should be zero after winning set", 0, score.getPoints(teams[0]));
        assertEquals("Sets should be 3 after winning set", 3, score.getSets(teams[0]));

        assertEquals("Match should be over", true, score.isMatchOver());
        assertEquals("Match should won by team 1", teams[0], score.getWinner(2));
    }

    @Test
    public void scoreBasicString() {
        Score score = new TennisScore(this.teams, TennisScore.Sets.ONE);
        for (int i = 0; i < 4; ++i) {
            score.setPoint(0, this.teams[0], i);
            assertEquals("team one pt string", TennisScore.POINTS_STRINGS[i], score.getPointString(0, this.teams[0]));
        }
        // check the invalid number
        score.setPoint(0, this.teams[0], 6);
        score.setPoint(0, this.teams[1], 7);
        assertEquals("team one pt string", "6", score.getPointString(0, this.teams[0]));
        assertEquals("team two pt string", "7", score.getPointString(0, this.teams[1]));
    }

    @Test
    public void scoreString() {
        Score score = new TennisScore(this.teams, TennisScore.Sets.ONE);

        // LOVE all
        score.setPoint(0, this.teams[0], 0);
        score.setPoint(0, this.teams[0], 0);
        assertEquals("team one pt string", TennisScore.STR_LOVE, score.getPointString(0, this.teams[0]));
        assertEquals("team two pt string", TennisScore.STR_LOVE, score.getPointString(0, this.teams[0]));

        // 15 love
        score.setPoint(0, this.teams[0], 1);
        assertEquals("team one pt string", TennisScore.STR_FIFTEEN, score.getPointString(0, this.teams[0]));

        // 30 love
        score.setPoint(0, this.teams[0], 2);
        assertEquals("team one pt string", TennisScore.STR_THIRTY, score.getPointString(0, this.teams[0]));

        // 40 love
        score.setPoint(0, this.teams[0], 3);
        assertEquals("team one pt string", TennisScore.STR_FORTY, score.getPointString(0, this.teams[0]));

        // Game love
        score.setPoint(0, this.teams[0], 4);
        assertEquals("team one pt string", TennisScore.STR_GAME, score.getPointString(0, this.teams[0]));

        // Deuce
        score.setPoint(0, this.teams[0], 3);
        score.setPoint(0, this.teams[1], 3);
        assertEquals("team one pt string", TennisScore.STR_DEUCE, score.getPointString(0, this.teams[0]));

        // ad 40
        score.setPoint(0, this.teams[0], 4);
        assertEquals("team one pt string", TennisScore.STR_ADVANTAGE, score.getPointString(0, this.teams[0]));

        // Game 40
        score.setPoint(0, this.teams[0], 5);
        assertEquals("team one pt string", TennisScore.STR_GAME, score.getPointString(0, this.teams[0]));

        // 40 ad
        score.setPoint(0, this.teams[0], 3);
        score.setPoint(0, this.teams[1], 4);
        assertEquals("team two pt string", TennisScore.STR_ADVANTAGE, score.getPointString(0, this.teams[1]));

        // 40 Game
        score.setPoint(0, this.teams[1], 5);
        assertEquals("team two pt string", TennisScore.STR_GAME, score.getPointString(0, this.teams[1]));
    }

    private void winPoints(TennisScore score, Team team, int points) {
        for (int i = 0; i < points; ++i) {
            score.incrementPoint(team);
        }
    }

    private void winGame(TennisScore score, Team team, int games) {
        winPoints(score, team, 4 * games);
    }

    @Test
    public void tieBreakScore() {
        // in a tie the points are 0, 1, 2, 3 etc
        // wins the game ('game') when 7 points and 2 ahead

        TennisScore score = new TennisScore(teams, TennisScore.Sets.THREE);
        // quickly put is into a tie break
        winGame(score, teams[0], 5);
        winGame(score, teams[1], 5);
        // 5 - 5, so win one each to get to a tie-break
        winGame(score, teams[0], 1);
        winGame(score, teams[1], 1);

        assertEquals("in tie", true, score.isInTieBreak());
        // check the score from now on is output as numbers
        for (int i = 0; i < 6; ++i) {
            // should be 0, 1, 2...
            assertEquals("in tie point number", Integer.toString(i), score.getPointString(0, teams[0]));
            assertEquals("in tie point number", Integer.toString(i), score.getPointsString(teams[1]));
            // add a point to each
            score.incrementPoint(teams[0]);
            score.incrementPoint(teams[1]);
        }

        // we are at 6 - 6 now, add one, shouldn't win
        score.incrementPoint(teams[0]);
        assertEquals("7-6 doesn't win", "7", score.getPointString(0, teams[0]));
        assertEquals("7-6 doesn't win", "6", score.getPointString(0, teams[1]));
        // but going to 8 - 6 should win
        score.incrementPoint(teams[0]);
        assertEquals("player one won the tie", 0, score.getPoints(teams[0]));
        assertEquals("player one won the tie", 7, score.getGames(teams[0], 0));
        assertEquals("player two lost the tie", 6, score.getGames(teams[1], 0));
        assertEquals("player one won the tie set", 1, score.getSets(teams[0]));
        assertEquals("player two lost the tie set", 0, score.getSets(teams[1]));

    }

    @Test
    public void tieBreakServing() {
        // player who's turn to serve starts the tie
        TennisScore score = new TennisScore(teams, TennisScore.Sets.THREE);
        // player one should server
        Player firstServer = score.getServer();
        Player secondServer = firstServer == teams[0].getPlayers()[0] ? teams[1].getPlayers()[0] : teams[0].getPlayers()[0];

        // play the first game and we should swap servers
        winGame(score, teams[0], 1);
        assertEquals("expected server", secondServer, score.getServer());
        // play another one
        winGame(score, teams[1], 1);
        assertEquals("expected server", firstServer, score.getServer());
        // play a third
        winGame(score, teams[0], 1);
        assertEquals("expected server", secondServer, score.getServer());

        // we are at 2 - 1 games, let's add another two
        winGame(score, teams[1], 2);
        assertEquals("expected server", secondServer, score.getServer());
        // we are at 2 - 3, add another two
        winGame(score, teams[0], 2);
        assertEquals("expected server", secondServer, score.getServer());
        // we are at 4 - 3, add another two
        winGame(score, teams[1], 2);
        assertEquals("expected server", secondServer, score.getServer());
        // we are at 4 - 5, add another two
        winGame(score, teams[0], 2);
        assertEquals("expected server", secondServer, score.getServer());
        // we are at 6 - 5, add another one and we enter a tie-break
        winGame(score, teams[1], 1);
        assertEquals("in tie", true, score.isInTieBreak());
        assertEquals("expected server", firstServer, score.getServer());

        // in a 6-6 tie break now
        Player tieServer = firstServer;
        // server changes after 1 point
        winPoints(score, teams[0], 1);
        assertEquals("expected server", secondServer, score.getServer());

        // server changes after 2 points
        winPoints(score, teams[0], 2);
        assertEquals("expected server", firstServer, score.getServer());

        // we are at 3-0 in the tie
        winPoints(score, teams[0], 2);
        assertEquals("expected server", secondServer, score.getServer());

        // we are at 5-0 in the tie
        winPoints(score, teams[0], 2);
        // player one won the game
        assertEquals("player one won the tie", 7, score.getGames(teams[0], 0));
        assertEquals("player two lost the tie", 6, score.getGames(teams[1], 0));

        // tie is over, the first player is expected to receive despite the fact they would normally be next
        assertEquals("expected server", secondServer, score.getServer());
        assertNotEquals("tie server receiving", tieServer, score.getServer());

        // doubles - the player of the opposing team due to serve next

        // alternate every 2 points (doubles as normal in set)

        // the player whose turn it was to serve first in tie will be the receiver in the first
        // game of the following set

    }

    @Test
    public void tieBreakServingDoubles() {
        // player who's turn to serve starts the tie
        TennisScore score = new TennisScore(doubles, TennisScore.Sets.THREE);
        // player one should serve
        Player playerOneA = doubles[0].getPlayers()[0];
        Player playerOneB = doubles[0].getPlayers()[1];
        Player playerTwoA = doubles[1].getPlayers()[0];
        Player playerTwoB = doubles[1].getPlayers()[1];
        assertEquals("team server first player", playerOneA, doubles[0].getServingPlayer());
        assertEquals("team server first player", playerTwoA, doubles[1].getServingPlayer());


        // check the servers cycle in doubles
        assertEquals("first server", playerOneA, score.getServer());
        // play the first game and we should swap servers
        winGame(score, doubles[0], 1);
        assertEquals("expected server", playerTwoA, score.getServer());
        // play another one
        winGame(score, doubles[1], 1);
        assertEquals("expected server", playerOneB, score.getServer());
        // play a third
        winGame(score, doubles[0], 1);
        assertEquals("expected server", playerTwoB, score.getServer());
        // and a forth
        winGame(score, doubles[0], 1);
        assertEquals("expected server", playerOneA, score.getServer());

        // we are at 3 - 1 games, let's add another two
        winGame(score, doubles[1], 2);
        assertEquals("expected server", playerOneB, score.getServer());
        // we are at 3 - 3, add another two
        winGame(score, doubles[0], 2);
        assertEquals("expected server", playerOneA, score.getServer());
        // we are at 5 - 3, add another three
        winGame(score, doubles[1], 3);
        assertEquals("expected server", playerTwoB, score.getServer());
        // we are at 5 - 6, add another one and we enter a tie-break
        winGame(score, doubles[0], 1);
        assertEquals("in tie", true, score.isInTieBreak());
        assertEquals("expected server", playerOneA, score.getServer());

        // in a 6-6 tie break now
        Player tieServer = playerOneA;
        // server changes after 1 point
        winPoints(score, doubles[0], 1);
        assertEquals("expected server", playerTwoA, score.getServer());

        // server changes after 2 points
        winPoints(score, doubles[0], 2);
        assertEquals("expected server", playerOneB, score.getServer());

        // we are at 3-0 in the tie
        winPoints(score, doubles[0], 2);
        assertEquals("expected server", playerTwoB, score.getServer());

        // we are at 5-0 in the tie
        winPoints(score, doubles[1], 2);
        assertEquals("expected server", playerOneA, score.getServer());

        // we are at 5-2 in the tie
        winPoints(score, doubles[1], 2);
        assertEquals("expected server", playerTwoA, score.getServer());

        // we are at 5-4 in the tie
        winPoints(score, doubles[1], 2);
        assertEquals("expected server", playerOneB, score.getServer());

        // we are at 5-6 in the tie
        winPoints(score, doubles[0], 3);

        // player one won the game
        assertEquals("team one won the tie", 7, score.getGames(doubles[0], 0));
        assertEquals("team two lost the tie", 6, score.getGames(doubles[1], 0));

        // tie is over, the next server is the one after the player that started
        assertEquals("expected server", playerTwoA, score.getServer());
    }

    @Test
    public void doublesServing() {
        // the players in a team cycle alternately
        TennisScore score = new TennisScore(doubles, TennisScore.Sets.THREE);
        // player one should serve
        Player playerOneA = doubles[0].getPlayers()[0];
        Player playerOneB = doubles[0].getPlayers()[1];
        Player playerTwoA = doubles[1].getPlayers()[0];
        Player playerTwoB = doubles[1].getPlayers()[1];
        assertEquals("team server first player", playerOneA, doubles[0].getServingPlayer());
        assertEquals("team server first player", playerTwoA, doubles[1].getServingPlayer());


        // check the servers cycle in doubles
        assertEquals("first server", playerOneA, score.getServer());
        // play the first game and we should swap servers
        winGame(score, doubles[0], 1);
        assertEquals("expected server", playerTwoA, score.getServer());
        // play another one
        winGame(score, doubles[1], 1);
        assertEquals("expected server", playerOneB, score.getServer());
        // play a third
        winGame(score, doubles[0], 1);
        assertEquals("expected server", playerTwoB, score.getServer());
        // and a forth
        winGame(score, doubles[0], 1);
        assertEquals("expected server", playerOneA, score.getServer());

        // we are at 3 - 1 games, let's add another two
        winGame(score, doubles[1], 2);
        assertEquals("expected server", playerOneB, score.getServer());
        // we are at 3 - 3, add another two
        winGame(score, doubles[0], 2);
        assertEquals("expected server", playerOneA, score.getServer());
        // we are at 5 - 3, add another three
        winGame(score, doubles[1], 3);
        assertEquals("expected server", playerTwoB, score.getServer());
        // we are at 5 - 6, add another one to win
        winGame(score, doubles[1], 1);
        assertEquals("expected server", playerOneA, score.getServer());

        // player two won the game
        assertEquals("team one lost the set", 5, score.getGames(doubles[0], 0));
        assertEquals("team two won the set", 7, score.getGames(doubles[1], 0));

    }

    @Test
    public void tieBreakFinalSet() {
        // 6 games all triggers a tie in all sets but last
        TennisScore score = new TennisScore(doubles, TennisScore.Sets.THREE);
        winGame(score, doubles[0], 5);
        winGame(score, doubles[1], 5);
        winGame(score, doubles[0], 1);
        winGame(score, doubles[1], 1);
        assertEquals("in tie", true, score.isInTieBreak());
        winPoints(score, doubles[0], 7);
        assertEquals("sets won", 1, score.getSets(doubles[0]));
        assertEquals("games won", 7, score.getGames(doubles[0], 0));

        // win the second set on a tie too
        winGame(score, doubles[0], 5);
        winGame(score, doubles[1], 5);
        winGame(score, doubles[0], 1);
        winGame(score, doubles[1], 1);
        assertEquals("in tie", true, score.isInTieBreak());
        winPoints(score, doubles[1], 7);
        assertEquals("sets won", 1, score.getSets(doubles[0]));
        assertEquals("sets won", 1, score.getSets(doubles[1]));
        assertEquals("games won", 7, score.getGames(doubles[1], 1));

        // final set wll not be a tie
        winGame(score, doubles[0], 5);
        winGame(score, doubles[1], 5);
        winGame(score, doubles[0], 1);
        winGame(score, doubles[1], 1);
        assertEquals("no tie", false, score.isInTieBreak());
        winGame(score, doubles[0], 1);
        // 7-6 still no winner, not enough games ahead
        assertEquals("sets won", 1, score.getSets(doubles[0]));
        assertEquals("sets won", 1, score.getSets(doubles[1]));
        assertEquals("games won", 7, score.getGames(doubles[0], 2));
        // move ahead 2
        winGame(score, doubles[1], 2);
        // 7-8 still no winner, not enough games ahead
        assertEquals("sets won", 1, score.getSets(doubles[0]));
        assertEquals("sets won", 1, score.getSets(doubles[1]));
        assertEquals("games won", 7, score.getGames(doubles[0], 2));
        // move ahead 2
        winGame(score, doubles[0], 2);
        // 9-8 still no winner, not enough games ahead
        assertEquals("sets won", 1, score.getSets(doubles[0]));
        assertEquals("sets won", 1, score.getSets(doubles[1]));
        assertEquals("games won", 9, score.getGames(doubles[0], 2));
        // move ahead 1 to win
        winGame(score, doubles[0], 1);
        // 10-8 and we have ended the match
        assertEquals("sets won", 2, score.getSets(doubles[0]));
        assertEquals("sets won", 1, score.getSets(doubles[1]));
        assertEquals("games won", 10, score.getGames(doubles[0], 2));
        assertEquals("match won", true, score.isMatchOver());
        assertEquals("match winner", doubles[0], score.getWinner(2));
    }

    @Test
    public void tieBreakInFinalSet() {
        // 6 games all triggers a tie in all sets
        TennisScore score = new TennisScore(doubles, TennisScore.Sets.THREE);
        score.setIsFinalSetTieBreaker(true);

        winGame(score, doubles[0], 5);
        winGame(score, doubles[1], 5);
        winGame(score, doubles[0], 1);
        winGame(score, doubles[1], 1);
        assertEquals("in tie", true, score.isInTieBreak());
        winPoints(score, doubles[0], 7);
        assertEquals("set won", 1, score.getSets(doubles[0]));
        assertEquals("games won", 7, score.getGames(doubles[0], 0));

        // win the second set on a tie too
        winGame(score, doubles[0], 5);
        winGame(score, doubles[1], 5);
        winGame(score, doubles[0], 1);
        winGame(score, doubles[1], 1);
        assertEquals("in tie", true, score.isInTieBreak());
        winPoints(score, doubles[1], 7);
        assertEquals("set won", 1, score.getSets(doubles[1]));
        assertEquals("games won", 7, score.getGames(doubles[1], 1));

        // final set wll be a tie too
        winGame(score, doubles[0], 5);
        winGame(score, doubles[1], 5);
        winGame(score, doubles[0], 1);
        winGame(score, doubles[1], 1);
        assertEquals("in tie", true, score.isInTieBreak());
        winPoints(score, doubles[0], 7);

        // 7-0 and we have ended the match
        assertEquals("sets won", 2, score.getSets(doubles[0]));
        assertEquals("games won", 7, score.getGames(doubles[0], 2));
        assertEquals("match won", true, score.isMatchOver());
        assertEquals("match winner", doubles[0], score.getWinner(2));
    }

    @Test
    public void setsToWin() {
        // best of 1, 3 or 5
        TennisScore score = new TennisScore(doubles, TennisScore.Sets.ONE);
        // win this set
        winGame(score, doubles[0], 6);
        assertEquals("set won", 1, score.getSets(doubles[0]));
        assertEquals("match won", true, score.isMatchOver());

        // win a three setter
        score = new TennisScore(doubles, TennisScore.Sets.THREE);
        // do a straight win in 2 sets
        winGame(score, doubles[0], 12);
        assertEquals("set won", 2, score.getSets(doubles[0]));
        assertEquals("match won", true, score.isMatchOver());

        // win a five setter
        score = new TennisScore(doubles, TennisScore.Sets.FIVE);
        // do a straight win in 3 sets
        winGame(score, doubles[0], 18);
        assertEquals("sets won", 3, score.getSets(doubles[0]));
        assertEquals("match won", true, score.isMatchOver());

        // win a three setter, closer
        score = new TennisScore(doubles, TennisScore.Sets.THREE);
        // do a win in the whole sets
        winGame(score, doubles[1], 9);
        winGame(score, doubles[0], 12);
        assertEquals("set won", 2, score.getSets(doubles[0]));
        assertEquals("match won", true, score.isMatchOver());

        // win a five setter
        score = new TennisScore(doubles, TennisScore.Sets.FIVE);
        // do a win in the whole sets
        winGame(score, doubles[1], 15);
        winGame(score, doubles[0], 18);
        assertEquals("set won", 3, score.getSets(doubles[0]));
        assertEquals("match won", true, score.isMatchOver());


    }

    @Test
    public void changingEnds() {
        // we want to change ends at the correct time
        TennisScore score = new TennisScore(teams, TennisScore.Sets.THREE);

        // just track the position of team zero, 1 should be opposite
        CourtPosition startingPosition = teams[0].getCourtPosition();
        CourtPosition oppositePosition = startingPosition.getNext();
        assertNotEquals(startingPosition, oppositePosition);

        // play the first game and we should swap ends
        winGame(score, teams[0], 1);
        assertEquals("expected end", oppositePosition, teams[0].getCourtPosition());
        // play another one and we don't swap
        winGame(score, teams[1], 1);
        assertEquals("expected end", oppositePosition, teams[0].getCourtPosition());
        // play a third - we do swap back to the first
        winGame(score, teams[0], 1);
        assertEquals("expected end", startingPosition, teams[0].getCourtPosition());

        // we are at 2 - 1 games, let's add another two
        winGame(score, teams[1], 2);
        assertEquals("expected end", oppositePosition, teams[0].getCourtPosition());
        // we are at 2 - 3, add another two
        winGame(score, teams[0], 2);
        assertEquals("expected end", startingPosition, teams[0].getCourtPosition());
        // we are at 4 - 3, add another two
        winGame(score, teams[1], 2);
        assertEquals("expected end", oppositePosition, teams[0].getCourtPosition());
        // we are at 4 - 5, add another two
        winGame(score, teams[0], 2);
        assertEquals("expected end", startingPosition, teams[0].getCourtPosition());
        // we are at 6 - 5, add another one and we enter a tie-break
        winGame(score, teams[1], 1);
        assertEquals("expected end", startingPosition, teams[0].getCourtPosition());

        // in a 6-6 tie break now
        // end changes end after 6 points
        winPoints(score, teams[0], 6);
        assertEquals("expected end", oppositePosition, teams[0].getCourtPosition());

        // end changes end after 6 points
        winPoints(score, teams[1], 6);
        assertEquals("expected end", startingPosition, teams[0].getCourtPosition());

        // we are at 6-6 in the tie
        winPoints(score, teams[0], 2);
        assertEquals("expected end", startingPosition, teams[0].getCourtPosition());

        // we are at 8-6 in the tie
        assertEquals("player one won the tie", 7, score.getGames(teams[0], 0));
        assertEquals("player two lost the tie", 6, score.getGames(teams[1], 0));

        // check this tie result in the history
        List<int[]> pointHistory = score.getPointHistory(0);
        int[] tieResults = pointHistory.get(pointHistory.size() - 1);
        assertEquals("tie result", 8, tieResults[0]);
        assertEquals("tie result", 6, tieResults[1]);


        // shouldn't have changed ends (not even number of games in set)
        assertEquals("expected end", startingPosition, teams[0].getCourtPosition());
        winGame(score, teams[0], 1);
        // still change ends after the first game
        assertEquals("expected end", oppositePosition, teams[0].getCourtPosition());
        // and the next two
        winGame(score, teams[0], 2);
        assertEquals("expected end", startingPosition, teams[0].getCourtPosition());

        // 3 - 0, let player two win
        winGame(score, teams[1], 6);
        // and shouldn't swap ends as was odd
        assertEquals("expected end", startingPosition, teams[0].getCourtPosition());
        winGame(score, teams[0], 1);
        // still change ends after the first game
        assertEquals("expected end", oppositePosition, teams[0].getCourtPosition());
        // and the next two
        winGame(score, teams[0], 2);
        assertEquals("expected end", startingPosition, teams[0].getCourtPosition());
    }
}
