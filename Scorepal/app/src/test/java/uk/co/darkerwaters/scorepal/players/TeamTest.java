package uk.co.darkerwaters.scorepal.players;

import org.junit.Test;

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
    public void resetTeam() {
        Player playerOne = new Player("playerOne");
        Player playerTwo = new Player("playerOne");
        Team teamOne = new Team(new Player[]{playerOne, playerTwo}, CourtPosition.NORTH);
        // test default
        assertEquals("server", CourtPosition.NORTH, teamOne.getCourtPosition());
        assertEquals("court position", playerOne, teamOne.getServingPlayer());

        // change
        teamOne.setCourtPosition(CourtPosition.SOUTH);
        assertEquals("server", playerOne, teamOne.getServingPlayer());
        assertEquals("court position", CourtPosition.SOUTH, teamOne.getCourtPosition());

        // reset
        teamOne.resetTeam();
        assertEquals("server", playerOne, teamOne.getNextServer());
        assertEquals("server", playerTwo, teamOne.getNextServer());
        assertEquals("court position", CourtPosition.NORTH, teamOne.getCourtPosition());
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

    @Test
    public void playerInTeam() {
        Player playerOne = new Player("playerOne");
        Player playerTwo = new Player("playerOne");
        Team teamOne = new Team(new Player[]{playerOne, playerTwo}, CourtPosition.NORTH);

        assertEquals("player in team", true, teamOne.isPlayerInTeam(playerOne));
        assertEquals("player in team", true, teamOne.isPlayerInTeam(playerTwo));
        assertEquals("player not in team", false, teamOne.isPlayerInTeam(new Player("unknown")));
    }
}