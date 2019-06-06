package uk.co.darkerwaters.scorepal.matches;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TennisScoreTest extends ScoreTest {

    @Test
    public void settingTennisValues() {
        // create a series of games
        TennisScore score =new TennisScore(this.teams);
        for (int iSets = 0; iSets < 3; ++iSets) {
            for (int iGames = 0; iGames < 6; ++iGames) {
                // set the points to player one wins
                for (int iPoints = 0; iPoints < 4; ++iPoints) {
                    score.incrementPoint(this.teams[0]);
                }
                // set the game won
                assertEquals("Points should be four to win", 4, score.getPoints(teams[0]));
                score.incrementGame(this.teams[0]);
                assertEquals("Points should be zero after winning game", 0, score.getPoints(teams[0]));
                assertEquals("Games should be " + (iGames + 1), iGames + 1, score.getGames(teams[0], iSets));
            }
            // set the set won
            score.incrementSet(this.teams[0]);
            assertEquals("Games should be six", 6, score.getGames(teams[0], iSets));
        }

        // so the points are nothing
        assertEquals("Points should be zero after winning set", 0, score.getPoints(teams[0]));
        assertEquals("Sets should be 3 after winning set", 3, score.getSets(teams[0]));

        // win the game
        score.endGame();

        // so the points are nothing
        assertEquals("Points should be zero after winning set", 0, score.getPoints(teams[0]));
        assertEquals("Sets should be 3 after winning set", 3, score.getSets(teams[0]));
    }
}
