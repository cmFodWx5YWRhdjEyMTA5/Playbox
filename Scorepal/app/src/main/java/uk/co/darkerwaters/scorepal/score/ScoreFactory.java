package uk.co.darkerwaters.scorepal.score;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.Team;

public abstract class ScoreFactory<T extends Score> {

    private static final String K_FILEVERSION = "a";

    public enum ScoreMode {
        K_UNKNOWN(-1, "Unknown"),
        K_POINTS(0, "Points"),
        K_TENNIS(1, "Tennis"),
        K_BADMINTON(2, "Badminton");

        public final int value;
        public final String name;

        ScoreMode(int value, String name) {
            this.value = value; this.name = name;
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
        public static ScoreMode from(String string) {
            for (ScoreMode mode : ScoreMode.values()) {
                if (mode.name.equals(string)) {
                    return mode;
                }
            }
            return K_UNKNOWN;
        }
        @Override
        public String toString() {
            return this.name;
        }
    }

    public static Match<?> CreateMatchFromMode(Context context, ScoreMode mode) {
        switch (mode) {
            case K_TENNIS:
                return new TennisMatch(context);
            case K_POINTS:
                return new PointsMatch(context);
            default:
                return null;
        }
    }

    public abstract T createScore(Team[] teams);
}
