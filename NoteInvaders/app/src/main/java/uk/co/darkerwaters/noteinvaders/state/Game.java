package uk.co.darkerwaters.noteinvaders.state;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.games.GamePlayer;

public class Game {

    public final Game parent;
    public final String name;
    public final String image;
    public final String gameClass;
    public final Note[] treble_clef;
    public final Note[] bass_clef;

    public final Game[] children;

    Game(Game parent, JSONObject fileSource) throws JSONException {
        // setup the parent of this game (if there is one)
        this.parent = parent;
        // create the game object from the JSON object passed in
        this.name = fileSource.getString("name");
        this.image = fileSource.getString("image");
        this.gameClass = getJsonStringOptional(fileSource, "class");

        Notes notes = Notes.instance();
        // notes for the treble clef
        JSONArray jsonNotes = getJsonArrayOptional(fileSource,"treble_clef");
        this.treble_clef = new Note[jsonNotes == null ? 0 : jsonNotes.length()];
        for (int i = 0; i < this.treble_clef.length; ++i) {
            // for each note, find the right one and put it in our array
            String noteName = jsonNotes.getString(i);
            this.treble_clef[i] = notes.getNote(noteName);
        }
        // and the bass clef
        jsonNotes = getJsonArrayOptional(fileSource,"bass_clef");
        this.bass_clef = new Note[jsonNotes == null ? 0 : jsonNotes.length()];
        for (int i = 0; i < this.bass_clef.length; ++i) {
            // for each note, find the right one and put it in our array
            String noteName = jsonNotes.getString(i);
            this.bass_clef[i] = notes.getNote(noteName);
        }

        // and the children of this game
        JSONArray jsonChildren = getJsonArrayOptional(fileSource,"children");
        this.children = new Game[jsonChildren == null? 0 : jsonChildren.length()];
        for (int i = 0; i < this.children.length; ++i) {
            // for each level, load them in
            this.children[i] = new Game(this, jsonChildren.getJSONObject(i));
        }
    }

    private String getJsonStringOptional(JSONObject source, String name) {
        String result = null;
        try {
            result =  source.getString(name);
        }
        catch (JSONException e) {
            // fine, it doesn't have to be there
        }
        return result;
    }

    private JSONArray getJsonArrayOptional(JSONObject source, String name) {
        JSONArray result = null;
        try {
            result =  source.getJSONArray(name);
        }
        catch (JSONException e) {
            // fine, it doesn't have to be there
        }
        return result;
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
                games.add(new Game(null, new JSONObject(new String(buffer, "UTF-8"))));

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

    public boolean isTreble() {
        return null != this.treble_clef && this.treble_clef.length > 0;
    }

    public boolean isBass() {
        return null != this.bass_clef && this.bass_clef.length > 0;
    }

    public boolean isPlayable() {
        // return if this game can be played
        return (isBass() || isTreble());
    }

    public NoteRange getNoteRange() {
        // go through all the notes to find the lowest and highest of them all
        NoteRange range = new NoteRange(null, null);
        for (int i = 0; i < this.treble_clef.length + this.bass_clef.length; ++i) {
            Note note;
            if (i >= this.treble_clef.length) {
                // get the note from the bass clef
                note = this.bass_clef[i - this.treble_clef.length];
            }
            else {
                // get the note from the treble clef
                note = this.treble_clef[i];
            }
            if (range.getStart() == null || note.frequency < range.getStart().getFrequency()) {
                // this is before the current start
                range.setStart(note);
            }
            if (range.getEnd() == null || note.frequency > range.getEnd().getFrequency()) {
                // this is after the current end
                range.setEnd(note);
            }
        }
        return range;
    }

    public GamePlayer getGamePlayer() {
        String className = getGameClass();
        if (className != null && className.isEmpty() == false) {
            Class<?> loadedClass = null;
            try {
                loadedClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Object classCreated = null;
            if (null != loadedClass) {
                try {
                    classCreated = loadedClass.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            if (null != classCreated && classCreated instanceof GamePlayer) {
                return (GamePlayer) classCreated;
            }
            else {
                Log.e(State.K_APPTAG, "Loaded class of " + className + " is not a GamePlayer");
            }
        }
        return null;
    }

    public String getGameClass() {
        if (null == this.gameClass || this.gameClass.isEmpty()) {
            // there is no game class
            if (null == this.parent) {
                // and no parent
                return null;
            }
            else {
                // use the class from the parent
                return this.parent.getGameClass();
            }
        }
        else {
            // this is the game class
            return this.gameClass;
        }
    }
}
