package uk.co.darkerwaters.staveinvaders.games;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.application.Log;

public class GameList {

    public static Game[] loadGamesFromAssets(Application application, Context context) {
        String json = null;
        // we want to load all the games from our 'games' folder in 'assets'
        List<Game> games = new ArrayList<Game>();
        try {
            // get the list of files and be sure we alphabetise the list
            String[] fileList = context.getAssets().list("games");
            Arrays.sort(fileList);
            // try to load the next file
            for (String filename : fileList) {
                // get all the game files fr0m here
                if (filename.toLowerCase().endsWith(".json")) {
                    // this is a json file (a game), load it
                    try {
                        // open the input stream to this file
                        InputStream is = context.getAssets().open("games/" + filename);
                        int size = is.available();
                        byte[] buffer = new byte[size];
                        is.read(buffer);
                        is.close();
                        // and try to create the Game from this JSON data
                        games.add(new Game(application, null, new JSONObject(new String(buffer, "UTF-8"))));
                        // done (O;
                    } catch (IOException ex) {
                        Log.error("failed to load the game file", ex);
                    } catch (JSONException e) {
                        Log.error("failed to read the game file", e);
                    }
                }
            }
        }
        catch (IOException e) {
            Log.error("failed to find the game files", e);
        }
        // return the games we managed to load
        return games.toArray(new Game[0]);
    }
}
