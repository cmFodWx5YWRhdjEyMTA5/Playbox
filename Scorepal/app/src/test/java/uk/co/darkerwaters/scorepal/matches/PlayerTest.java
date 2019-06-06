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
}