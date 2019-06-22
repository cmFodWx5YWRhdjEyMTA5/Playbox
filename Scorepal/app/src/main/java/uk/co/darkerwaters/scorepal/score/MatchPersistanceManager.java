package uk.co.darkerwaters.scorepal.score;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;

import uk.co.darkerwaters.scorepal.application.Log;

public class MatchPersistanceManager {

    private static final String K_EXT = ".mtch";

    private Match match;
    private File file;
    private final Context context;

    public MatchPersistanceManager(Context context) {
        this.context = context;
        this.match = null;
        this.file = null;
    }

    public Match getMatch() {
        return this.match;
    }

    public boolean loadFromFile(String filename) {
        return loadFromFile(new File(context.getFilesDir(), filename));
    }

    public boolean loadFromFile(File file) {
        StringBuilder json = new StringBuilder();
        this.match = null;
        this.file = file;
        boolean isSuccess = false;
        try {
            FileInputStream stream = new FileInputStream(this.file);
            int size;
            while ((size = stream.available()) > 0) {
                // while there is data, get it out
                byte[] buffer = new byte[size];
                stream.read(buffer);
                // append this buffer to the string builder
                json.append(new String(buffer, "UTF-8"));
            }
            stream.close();

            // now we have ths string, we can create JSON from it
            JSONObject obj = new JSONObject(json.toString());
            // somewhere in this file is the type of game that was played
            ScoreFactory.ScoreMode mode = ScoreFactory.ScoreMode.from(obj.getString("score_mode"));
            // create the match from this
            this.match = ScoreFactory.CreateMatchFromMode(context, mode);
            // load in the data
            this.match.setDataFromJson(obj);
            // success if here
            isSuccess = true;
        } catch (FileNotFoundException e) {
            Log.error("Failed to read the file", e);
        } catch (IOException e) {
            Log.error("Failed to read the JSON", e);
        } catch (JSONException e) {
            Log.error("Failed to create the JSON", e);
        } catch (Throwable e) {
            Log.error("Failed match loading seriously: " + e.getMessage());
        }
        return isSuccess;
    }

    public boolean saveMatchToFile(Match match, String matchId) {
        boolean isSuccess = false;
        this.match = match;
        try {
            this.file = new File(context.getFilesDir(), matchId + K_EXT);
            Writer output = new BufferedWriter(new FileWriter(this.file));
            JSONObject obj = new JSONObject();
            // we have to put the mode in to create the correct match each time
            obj.put("score_mode", this.match.getScoreMode().toString());
            // and the data from the match
            isSuccess = this.match.setDataToJson(obj);
            // close the file
            output.write(obj.toString());
            output.close();

        } catch (Exception e) {
            Log.error("Failed to write the JSON match file", e);
            isSuccess = false;
        }
        return isSuccess;
    }

    public File[] listMatches() {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                boolean isValid = false;
                if (s.endsWith(K_EXT)) {
                    // this is the correct extension
                    String matchId = s.substring(0, s.length() - K_EXT.length());
                    isValid = Match.isMatchIdValid(matchId);
                }
                return isValid;
            }
        };
        // return the files that are valid matches
        return context.getFilesDir().listFiles(filter);
    }
}
