package uk.co.darkerwaters.scorepal.matches;

import org.junit.Test;

import java.util.List;

import uk.co.darkerwaters.scorepal.matches.CourtPosition;
import uk.co.darkerwaters.scorepal.matches.Player;
import uk.co.darkerwaters.scorepal.matches.Score;
import uk.co.darkerwaters.scorepal.matches.Team;

import static org.junit.Assert.assertEquals;

public class TeamTest {

    @Test
    public void onePlayer() {
        Player playerOne = new Player("playerOne");
        Team teamOne = new Team(new Player[]{playerOne}, CourtPosition.NORTH);

        assertEquals("one player", 1, teamOne.getPlayers().length);
        assertEquals("player", playerOne, teamOne.getPlayers()[0]);
        assertEquals("court position", CourtPosition.NORTH, teamOne.getCourtPosition());
    }

    @Test
    public void courtPosition() {
        Team teamOne = new Team(new Player[]{new Player("playerOne")}, CourtPosition.NORTH);
        assertEquals("court position", CourtPosition.NORTH, teamOne.getCourtPosition());
        for (CourtPosition position : CourtPosition.values()) {
            teamOne.setCourtPosition(position);
            assertEquals("court position", position, teamOne.getCourtPosition());
        }
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