package uk.co.darkerwaters.scorepal.score;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;

public class TennisMatch extends Match<TennisScore> {

    public TennisMatch(Context context) {
        super(context, Sport.TENNIS);
    }

    @Override
    protected TennisScore createScore(Team[] teams) {
        return new TennisScore(teams, TennisSets.FIVE);
    }

    @Override
    public String getDescriptionLong(Context context) {
        // get the basic description
        StringBuilder stringBuilder = new StringBuilder(super.getDescriptionLong(context));
        // and we want to add a breakdown of the score here
        stringBuilder.append("\n\n");

        Team winner = getMatchWinner();
        Team loser = getOtherTeam(winner);
        TennisScore score = getScore();
        int totalSets = score.getPlayedSets();
        for (int i = 0; i < totalSets; ++i) {
            int winnerGames = score.getGames(winner, i);
            int loserGames = score.getGames(loser, i);
            stringBuilder.append(winnerGames);
            stringBuilder.append("-");
            stringBuilder.append(loserGames);
            if (score.isSetTieBreak(i)) {
                int[] tiePoints = score.getPoints(i, winnerGames + loserGames - 1);
                stringBuilder.append( "(" + tiePoints[0] + "-" + tiePoints[1] + ")");
            }
            stringBuilder.append("\t");
        }
        // and return the string
        return stringBuilder.toString();
    }
}
