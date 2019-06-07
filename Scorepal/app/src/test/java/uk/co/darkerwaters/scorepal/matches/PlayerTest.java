package uk.co.darkerwaters.scorepal.matches;

import org.junit.Test;

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
}