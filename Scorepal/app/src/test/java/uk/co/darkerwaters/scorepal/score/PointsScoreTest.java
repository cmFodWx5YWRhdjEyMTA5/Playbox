package uk.co.darkerwaters.scorepal.score;

import org.junit.Test;

import java.util.Random;

import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PointsScoreTest extends ScoreTest {

    @Test
    public void playShortPointsGame() {
        // create a short game
        PointsScore score = new PointsScore(this.teams, 10);
        assertEquals("mode", ScoreFactory.ScoreMode.K_POINTS, score.getScoreMode());
        assertEquals("levels", 1, score.getLevels());

        assertEquals("points to play to", 10, score.getScoreGoal());
        for (int i = 0; i < 9; ++i) {
            score.incrementPoint(teams[0]);
            assertEquals("point", i + 1, score.getPoints(teams[0]));
            assertEquals("point string", Integer.toString(i + 1), score.getDisplayPoint(teams[0]).displayString(null));
        }
        for (int i = 0; i < 9; ++i) {
            score.incrementPoint(teams[1]);
            assertEquals("point", i + 1, score.getPoints(teams[1]));
            assertEquals("point string", Integer.toString(i + 1), score.getDisplayPoint(teams[1]).displayString(null));
        }
        // not over
        assertEquals("not won", false, score.isMatchOver());
        score.incrementPoint(teams[0]);
        assertEquals("point", 10, score.getPoints(teams[0]));
        assertEquals("point string", "10", score.getDisplayPoint(teams[0]).displayString(null));
        assertEquals("match won", true, score.isMatchOver());
    }

    @Test
    public void playNeverEndingPointsGame() {
        PointsScore score = new PointsScore(this.teams);
        Random random = new Random();

        for (int i = 0; i < 900; ++i) {
            if (random.nextFloat() <= 0.5f) {
                score.incrementPoint(teams[0]);
            }
            else {
                score.incrementPoint(teams[1]);
            }
            assertEquals("not won", false, score.isMatchOver());
        }
    }

    @Test
    public void serving() {
        // player who's turn to serve starts the tie
        PointsScore score = new PointsScore(this.teams, 7);
        // player one should server
        Player firstServer = score.getServer();
        Player secondServer = firstServer == teams[0].getPlayers()[0] ? teams[1].getPlayers()[0] : teams[0].getPlayers()[0];

        // server changes after 1 point
        score.incrementPoint(teams[0]);
        assertEquals("expected server", secondServer, score.getServer());

        // server changes after 2 points
        score.incrementPoint(teams[0]);
        score.incrementPoint(teams[0]);
        assertEquals("expected server", firstServer, score.getServer());

        // we are at 3-0 in the tie
        score.incrementPoint(teams[0]);
        score.incrementPoint(teams[0]);
        assertEquals("expected server", secondServer, score.getServer());

        // we are at 5-0 in the tie
        score.incrementPoint(teams[0]);
        score.incrementPoint(teams[0]);
        // player one won the game
        assertEquals("player one won the tie", 7, score.getPoints(teams[0]));
        assertEquals("player two lost the tie", 0, score.getPoints(teams[1]));
    }

    private void winPoints(PointsScore score, Team team, int points) {
        for (int i = 0; i < points; ++i) {
            score.incrementPoint(team);
        }
    }

    @Test
    public void servingDoubles() {
        // player who's turn to serve starts the tie
        PointsScore score = new PointsScore(this.doubles, 0);
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
        winPoints(score, doubles[0], 1);
        assertEquals("expected server", playerTwoA, score.getServer());
        // play another two
        winPoints(score, doubles[1], 2);
        assertEquals("expected server", playerOneB, score.getServer());
        // again
        winPoints(score, doubles[0], 2);
        assertEquals("expected server", playerTwoB, score.getServer());
        // again
        winPoints(score, doubles[0], 2);
        assertEquals("expected server", playerOneA, score.getServer());
    }

    @Test
    public void changingEnds() {
        // we want to change ends at the correct time
        PointsScore score = new PointsScore(this.teams, 0);

        // just track the position of team zero, 1 should be opposite
        CourtPosition startingPosition = teams[0].getCourtPosition();
        CourtPosition oppositePosition = startingPosition.getNext();
        assertNotEquals(startingPosition, oppositePosition);

        // just swapping every 6 points
        winPoints(score, teams[0], 6);
        assertEquals("expected end", oppositePosition, teams[0].getCourtPosition());
        // play another one and we don't swap
        winPoints(score, teams[1], 1);
        assertEquals("expected end", oppositePosition, teams[0].getCourtPosition());
        // play another 5
        winPoints(score, teams[0], 5);
        assertEquals("expected end", startingPosition, teams[0].getCourtPosition());

        winPoints(score, teams[0], 6);
        assertEquals("expected end", oppositePosition, teams[0].getCourtPosition());
    }
}
