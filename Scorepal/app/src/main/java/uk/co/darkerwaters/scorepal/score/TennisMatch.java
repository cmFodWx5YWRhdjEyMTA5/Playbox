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
}
