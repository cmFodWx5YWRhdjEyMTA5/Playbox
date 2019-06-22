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
        this(context, TennisSets.FIVE);
    }

    public TennisMatch(Context context, TennisSets setsToPlay) {
        super(context, CreateScoreFactory(setsToPlay));
    }

    private static ScoreFactory<TennisScore> CreateScoreFactory(final TennisSets setsToPlay) {
        return new ScoreFactory<TennisScore>() {
            @Override
            public TennisScore createScore(Team[] teams) {
                return new TennisScore(teams, setsToPlay);
            }
        };
    }
}
