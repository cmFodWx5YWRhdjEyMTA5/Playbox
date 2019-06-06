package uk.co.darkerwaters.scorepal.matches;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ScoreTest {

    protected final Team[] teams = new Team[] {
            new Team(new Player[] {new Player("playerOne")}, CourtPosition.NORTH),
            new Team(new Player[] {new Player("playerTwo")}, CourtPosition.SOUTH),
    };

    private boolean isThrown(Runnable code) {
        boolean isThrown = false;
        try {
            code.run();
        }
        catch (Throwable e) {
            isThrown = true;
        }
        return isThrown;
    }

    @Test
    public void construction() {
        assertEquals("null teams crashes", true, isThrown(new Runnable() {
            @Override
            public void run() {
                new Score(null, 1);
            }
        }));
        assertEquals("zero levels crashes", true, isThrown(new Runnable() {
            @Override
            public void run() {
                new Score(teams, 0);
            }
        }));

        Score score = new Score(this.teams, 1);
        assertEquals("incorrect team", 0, score.getPoint(0, new Team(new Player[0], CourtPosition.GetDefault())));
    }

    @Test
    public void server() {
        Score score = new Score(this.teams, 1);
        assertEquals("default server", this.teams[0].getPlayers()[0], score.getServer());
        for (Player player : score.getPlayers()) {
            score.setServer(player);
            assertEquals("server", player, score.getServer());
        }
    }

    @Test
    public void settingValues() {
        // create a series of games
        Score score = new Score(this.teams, 3);
        for (int iSets = 0; iSets < 3; ++iSets) {
            for (int iGames = 0; iGames < 6; ++iGames) {
                // set the points to player one wins
                for (int iPoints = 0; iPoints < 5; ++iPoints) {
                    score.setPoint(0, this.teams[0], iPoints);
                    score.setPoint(0, this.teams[1], 0);
                }
                // set the game won
                assertEquals("Points should be four to win", 4, score.getPoint(0,teams[0]));
                score.setPoint(1, this.teams[0], iGames + 1);
                score.setPoint(1, this.teams[1], 0);
                score.clearLevel(0);
                assertEquals("Points should be zero after winning game", 0, score.getPoint(0, teams[0]));
            }
            assertEquals("Games should be six", 6, score.getPoint(1, teams[0]));
            // set the set won
            score.setPoint(2, this.teams[0], iSets + 1);
            score.setPoint(2, this.teams[1], 0);
            score.clearLevel(1);
            assertEquals("Games should be zero after winning set", 0, score.getPoint(1, teams[0]));
        }

        // so the points are nothing
        assertEquals("Points should be zero after winning set", 0, score.getPoint(0, teams[0]));
        assertEquals("Games should be zero after winning set", 0, score.getPoint(1, teams[0]));
        assertEquals("Sets should be 3 after winning set", 3, score.getPoint(2, teams[0]));
        score.clearLevel(2);

        // so check the history here
        List<int[]> gameHistory = score.getPointHistory(0);
        // there should be 6 games, all won with 4 points each here
        assertEquals("History should contain 3 sets", 3, gameHistory.size());
        for (int[] gamePoints : gameHistory) {
            // there should be a point per team
            assertEquals("History game should have two points in it", 2, gamePoints.length);
            // each should be 4 points to zero
            assertEquals("History game should be six games for team 1", 6, gamePoints[0]);
            assertEquals("History game should be love games for team 2", 0, gamePoints[1]);
        }

        assertEquals("winner is", this.teams[0], score.getWinner(2));
    }
}