package uk.co.darkerwaters.staveinvaders.games;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Note;
import uk.co.darkerwaters.staveinvaders.notes.Notes;
import uk.co.darkerwaters.staveinvaders.notes.Range;

public class Game {

    public final Game parent;
    public final String id;
    public final String name;
    public final String description;
    public final String image;
    public final String gameClass;
    public final GameEntry[] entries;

    public class GameEntry {
        public final String name;
        public final Chord chord;
        public final String fingering;
        public final boolean isTreble;
        public final boolean isBass;

        GameEntry(Notes notes, String fileData) {
            // create the entry from the JSON data
            String[] entries = fileData.split(":");
            if (entries[0].equals("t")) {
                // this is treble
                isTreble = true;
                isBass = false;
            }
            else {
                isTreble = false;
                isBass = true;
            }
            this.chord = createChord(notes, entries[1]);
            if (entries.length > 2) {
                this.name = entries[2];
            }
            else {
                this.name = this.chord.getTitle();
            }
            if (entries.length > 3) {
                this.fingering = entries[3];
            }
            else {
                this.fingering = "";
            }
        }

        private Chord createChord(Notes notes, String noteName) {
            String[] noteNames = noteName.split(",");
            Chord createdChord;
            if (noteNames == null || noteNames.length == 0) {
                // just use the name
                createdChord = new Chord(noteName, new Note[] {notes.getNote(noteName)});
            }
            else if (noteNames.length == 1) {
                // use the first note title
                createdChord = new Chord(noteName, new Note[] {notes.getNote(noteNames[0])});
            }
            else {
                // this is a chord
                Note[] notesToAdd = new Note[noteNames.length];
                for (int i = 0; i < noteNames.length; ++i) {
                    notesToAdd[i] = notes.getNote(noteNames[i]);
                }
                // and create the chord for thest notes, with the correct name
                createdChord = new Chord(name, notesToAdd);
            }
            // return the created chord
            return createdChord;
        }
    }

    public final Game[] children;

    Game(Application application, Game parent, JSONObject fileSource) throws JSONException {
        // setup the parent of this game (if there is one)
        this.parent = parent;
        // create the game object from the JSON object passed in
        this.id = fileSource.getString("id");
        this.name = fileSource.getString("name");
        this.description = getJsonStringOptional(fileSource,"description");
        this.image = getJsonStringOptional(fileSource, "image");
        this.gameClass = getJsonStringOptional(fileSource, "class");

        // get the notes we can search for each correct note to play
        Notes notes = application.getNotes();

        // load in all the notes that we should play
        JSONArray jsonData = getJsonArrayOptional(fileSource,"notes");
        this.entries = new GameEntry[jsonData == null ? 0 : jsonData.length()];
        for (int i = 0; i < this.entries.length; ++i) {
            // create each entry
            String noteData = jsonData.getString(i);
            this.entries[i] = new GameEntry(notes, noteData);
        }

        // and the children of this game
        JSONArray jsonChildren = getJsonArrayOptional(fileSource,"children");
        this.children = new Game[jsonChildren == null? 0 : jsonChildren.length()];
        for (int i = 0; i < this.children.length; ++i) {
            // for each level, load them in
            this.children[i] = new Game(application, this, jsonChildren.getJSONObject(i));
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

    public float getGameProgress() {
        //TODO return the current progress acheived by this game
        return (float) Math.random();
    }

    public boolean getIsGamePassed() {
        //TODO return if this game progress is enough to move on
        int i = 0;
        if (this.parent != null) {
            for (Game sibling : this.parent.children) {
                if (this == sibling) {
                    break;
                } else {
                    // move the counter
                    ++i;
                }
            }
            return i <= (int) (this.parent.children.length * 0.5f);
        }
        else {
            return false;
        }
    }

    public Range getNoteRange() {
        // go through all the notes to find the lowest and highest of them all
        Range range = new Range((Chord)null, (Chord)null);
        for (int i = 0; i < this.entries.length; ++i) {
            Chord playable= this.entries[i].chord;
            Note lowest = playable.getLowest();
            if (range.getStart() == null || lowest.getFrequency() < range.getStart().getHighest().getFrequency()) {
                // this is before the current start
                range.setStart(new Chord(new Note[]{lowest}));
            }
            Note highest = playable.getHighest();
            if (range.getEnd() == null || highest.getFrequency() > range.getEnd().getHighest().getFrequency()) {
                // this is after the current end
                range.setEnd(new Chord(new Note[]{highest}));
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
                Log.error("Loaded class of " + className + " is not a GamePlayer");
            }
        }
        return null;
    }

    private String getGameClass() {
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

    public String getFullName() {
        String fulltitle = new String(this.name);
        Game gameParent = this.parent;
        while (null != gameParent) {
            fulltitle = gameParent.name + " -- " + fulltitle;
            gameParent = gameParent.parent;
        }
        return fulltitle;
    }
}
