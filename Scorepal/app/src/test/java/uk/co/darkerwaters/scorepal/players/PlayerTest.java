package uk.co.darkerwaters.scorepal.players;

import org.junit.Test;

import uk.co.darkerwaters.scorepal.players.Player;

import static org.junit.Assert.assertEquals;

public class PlayerTest {

    @Test
    public void playerName() {
        Player playerOne = new Player("playerOne");

        assertEquals("player name", "playerOne", playerOne.getPlayerName());
        playerOne.setPlayerName("player1");
        assertEquals("player name", "player1", playerOne.getPlayerName());
    }

    @Test
    public void serving() {
        Player playerOne = new Player("playerOne");

        assertEquals("player serving", false, playerOne.getIsServing());
        playerOne.setIsServing(true);
        assertEquals("player serving", true, playerOne.getIsServing());
    }

    @Test
    public void resetting() {
        Player playerOne = new Player("playerOne");

        assertEquals("player serving", false, playerOne.getIsServing());
        playerOne.setIsServing(true);
        assertEquals("player serving", true, playerOne.getIsServing());

        playerOne.resetPlayer();
        assertEquals("player serving", false, playerOne.getIsServing());
    }
}