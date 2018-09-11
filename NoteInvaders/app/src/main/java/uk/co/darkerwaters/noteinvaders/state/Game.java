package uk.co.darkerwaters.noteinvaders.state;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Game {

    public final String name;
    public final String image;
    public final GameLevel[] levels;

    Game(JSONObject fileSource) throws JSONException {
        // create the game object from the JSON object passed in
        this.name = fileSource.getString("name");
        this.image = fileSource.getString("image");
        JSONArray jsonLevels = fileSource.getJSONArray("levels");
        this.levels = new GameLevel[jsonLevels.length()];
        for (int i = 0; i < jsonLevels.length(); ++i) {
            // for each level, load them in
            this.levels[i] = new GameLevel(this, jsonLevels.getJSONObject(i));
        }
    }

    public static class GameLevel {
        public final Game game;
        public final String name;
        public final String image;
        public final Note[] notesApplicable;

        GameLevel(Game parent, JSONObject fileSource) throws JSONException {
            this.game = parent;
            this.name = fileSource.getString("name");
            this.image = fileSource.getString("image");
            JSONArray jsonNotes = fileSource.getJSONArray("notes");
            this.notesApplicable = new Note[jsonNotes.length()];
            Notes notes = Notes.instance();
            for (int i = 0; i < jsonNotes.length(); ++i) {
                // for each note, find the right one and put it in our array
                String noteName = jsonNotes.getString(i);
                this.notesApplicable[i] = notes.getNote(noteName);
            }
        }
    }

    public static List<Game> loadGamesFromAssets(Context context) {
        String json = null;
        // we want to load all the games from our 'games' folder in 'assets'
        int iFileCounter = 0;
        List<Game> games = new ArrayList<Game>();
        do {
            // try to load the next file
            String filename = String.format("games/%03d.json", ++iFileCounter);
            try {
                // open the input stream to this file
                InputStream is = context.getAssets().open(filename);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                // and try to create the Game from this JSON data
                games.add(new Game(new JSONObject(new String(buffer, "UTF-8"))));

            } catch (IOException ex) {
                ex.printStackTrace();
                // out of files, stop looking for them then
                break;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        while (true);
        // return the games we managed to load
        return games;

    }
}
