package uk.co.darkerwaters.scorepal.score;

import android.content.Context;
import android.content.res.Resources;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

public class MatchTest {

    @Mock
    Context mockContext;

    @Mock
    Log mockLogger;

    private boolean isMessageLogged = false;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        // be sure we are getting the right names
        doReturn("playerNameOne")
                .when(mockContext)
                .getString(R.string.default_playerOneName);
        doReturn("playerNameTwo")
                .when(mockContext)
                .getString(R.string.default_playerTwoName);
        doReturn("playerNameOnePartner")
                .when(mockContext)
                .getString(R.string.default_playerOnePartnerName);
        doReturn("playerNameTwoPartner")
                .when(mockContext)
                .getString(R.string.default_playerTwoPartnerName);

        /*
        // check the error logger
        doReturn(true).when(mockLogger).isLogging();
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                isMessageLogged = true;
                return null;
            }
        }).when(mockLogger)
                .error(any(String.class));
                */
    }

    private boolean wasErrorLogged() {
        boolean wasLogged = isMessageLogged;
        this.isMessageLogged = false;
        return wasLogged;
    }

    @Test
    public void playersCreation() {
        Match match = ScoreFactory.CreateMatchFromMode(mockContext, ScoreFactory.ScoreMode.K_TENNIS);

        assertEquals("player created ok", "playerNameOne", match.getPlayerOne().getPlayerName());
        assertEquals("player created ok", "playerNameOnePartner", match.getPlayerOnePartner().getPlayerName());
        assertEquals("player created ok", "playerNameTwo", match.getPlayerTwo().getPlayerName());
        assertEquals("player created ok", "playerNameTwoPartner", match.getPlayerTwoPartner().getPlayerName());

        assertTrue("team one contains player one", match.getTeamOne().isPlayerInTeam(match.getPlayerOne()));
        assertTrue("team one contains player one p", match.getTeamOne().isPlayerInTeam(match.getPlayerOnePartner()));
        assertTrue("team one contains player two", match.getTeamTwo().isPlayerInTeam(match.getPlayerTwo()));
        assertTrue("team one contains player two p", match.getTeamTwo().isPlayerInTeam(match.getPlayerTwoPartner()));

        match.setPlayerOne(new Player("newPlayer1"));
        assertEquals("player created ok", "newPlayer1", match.getPlayerOne().getPlayerName());

        match.setPlayerOnePartner(new Player("newPlayer1b"));
        assertEquals("player created ok", "newPlayer1b", match.getPlayerOnePartner().getPlayerName());

        match.setPlayerTwo(new Player("newPlayer2"));
        assertEquals("player created ok", "newPlayer2", match.getPlayerTwo().getPlayerName());

        match.setPlayerTwoPartner(new Player("newPlayer2b"));
        assertEquals("player created ok", "newPlayer2b", match.getPlayerTwoPartner().getPlayerName());

    }

    @Test
    public void dataSetting() {
        Match match = ScoreFactory.CreateMatchFromMode(mockContext, ScoreFactory.ScoreMode.K_POINTS);

        assertEquals("score mode", ScoreFactory.ScoreMode.K_POINTS, match.getScoreMode());
        assertNotNull("score ok", match.getScore());
    }

    @Test
    public void dateSetting() {
        Match match = ScoreFactory.CreateMatchFromMode(mockContext, ScoreFactory.ScoreMode.K_POINTS);

        Date now = new Date();
        match.setMatchPlayedDate(now);
        assertTrue("date ok", Match.isFileDatesSame(now, match.getMatchPlayedDate()));
        assertNotNull("id ok", match.getMatchId());
    }

    @Test
    public void matchPlaying() {
        Match match = ScoreFactory.CreateMatchFromMode(mockContext, ScoreFactory.ScoreMode.K_POINTS);

        assertFalse("not started", match.isMatchStarted());
        match.incrementPoint(match.getTeamOne());
        assertTrue("now started", match.isMatchStarted());
        assertEquals("undone returning correct team", match.getTeamOne(), match.undoLastPoint());
        assertFalse("not started", match.isMatchStarted());
    }

    @Test
    public void matchUnDoing() {

    }
}