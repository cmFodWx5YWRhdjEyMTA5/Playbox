package uk.co.darkerwaters.scorepal.score;

import java.util.HashMap;
import java.util.Map;

import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.Team;

public class ScoreFactory {

    public enum ScoreMode {
        K_UNKNOWN(-1),
        K_POINTS(0),
        K_TENNIS(1),
        K_BADMINTON(2);

        public final int value;

        ScoreMode(int value) {
            this.value = value;
        }
        // Mapping enum to id
        private static final Map<Integer, ScoreMode> valueMap = new HashMap<Integer, ScoreMode>();
        // initialise the map in a static global function
        static {
            for (ScoreMode mode : ScoreMode.values())
                valueMap.put(mode.value, mode);
        }
        public static ScoreMode from(int value) {
            ScoreMode mode = valueMap.get(value);
            if (null == mode) {
                return ScoreMode.K_POINTS;
            }
            else {
                return mode;
            }
        }
    }

    public static Score CreateScore(Team[] teams, ScoreMode mode) {
        switch (mode) {
            case K_BADMINTON:
                Log.error("Have not implemented BADMINTON yet...");
                return null;
            case K_POINTS:
                return new PointsScore(teams);
            case K_TENNIS:
                return new TennisScore(teams, TennisScore.Sets.FIVE);
            default:
                Log.error("Have not implemented this type of score yet " + mode.toString());
                return null;
        }
    }
}
