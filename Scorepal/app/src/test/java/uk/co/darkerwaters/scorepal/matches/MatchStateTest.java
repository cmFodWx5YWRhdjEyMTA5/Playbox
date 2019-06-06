package uk.co.darkerwaters.scorepal.matches;

import org.junit.Test;

import uk.co.darkerwaters.scorepal.matches.CourtPosition;
import uk.co.darkerwaters.scorepal.matches.Player;
import uk.co.darkerwaters.scorepal.matches.Team;

import static org.junit.Assert.assertEquals;

public class MatchStateTest {

    @Test
    public void onePlayer() {

    }

    @Test
    public void courtPosition() {

    }

    @Test
    public void twoPlayer() {
        Player playerOne = new Player("playerOne");
        Player playerTwo = new Player("playerOne");
        Team teamOne = new Team(new Player[]{playerOne, playerTwo}, CourtPosition.NORTH);

        assertEquals("two player", 2, teamOne.getPlayers().length);
        assertEquals("player one", playerOne, teamOne.getPlayers()[0]);
        assertEquals("player two", playerTwo, teamOne.getPlayers()[1]);
    }
}