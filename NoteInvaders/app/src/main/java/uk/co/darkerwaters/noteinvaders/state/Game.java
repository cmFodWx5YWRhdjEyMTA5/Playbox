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
    public final String id;
    public final String name;
    public final String image;
    public final String gameClass;
    public final Playable[] treble_clef;
    public final Playable[] bass_clef;
    public final String[] treble_names;
    public final String[] bass_names;
    public final Annotation[] treble_annotations;
    public final Annotation[] bass_annotations;

    public class Annotation {
        final String name;
        final String[] values;
        Annotation(String name, String[] values) {
            this.name = name;
            this.values = values;
        }
    }

    public final Game[] children;

    Game(Game parent, JSONObject fileSource) throws JSONException {
        // setup the parent of this game (if there is one)
        this.parent = parent;
        // create the game object from the JSON object passed in
        this.id = fileSource.getString("id");
        this.name = fileSource.getString("name");
        this.image = fileSource.getString("image");
        this.gameClass = getJsonStringOptional(fileSource, "class");

        Notes notes = Notes.instance();
        // notes for the treble clef
        JSONArray jsonData = getJsonArrayOptional(fileSource,"treble_clef");
        this.treble_clef = new Playable[jsonData == null ? 0 : jsonData.length()];
        for (int i = 0; i < this.treble_clef.length; ++i) {
            // for each note, find the right one and put it in our array
            String noteName = jsonData.getString(i);
            this.treble_clef[i] = createPlayable(noteName);
        }
        // any names?
        jsonData = getJsonArrayOptional(fileSource,"treble_names");
        this.treble_names = new String[jsonData == null ? 0 : jsonData.length()];
        for (int i = 0; i < this.treble_names.length; ++i) {
            // for each name, put in in the array
            this.treble_names[i] = jsonData.getString(i);
        }

        // and the bass clef
        jsonData = getJsonArrayOptional(fileSource,"bass_clef");
        this.bass_clef = new Playable[jsonData == null ? 0 : jsonData.length()];
        for (int i = 0; i < this.bass_clef.length; ++i) {
            // for each note, find the right one and put it in our array
            String noteName = jsonData.getString(i);
            this.bass_clef[i] = createPlayable(noteName);
        }
        // any names?
        jsonData = getJsonArrayOptional(fileSource,"bass_names");
        this.bass_names = new String[jsonData == null ? 0 : jsonData.length()];
        for (int i = 0; i < this.bass_names.length; ++i) {
            // for each name, put in in the array
            this.bass_names[i] = jsonData.getString(i);
        }

        // what about annotations
        int annotationNumber = 1;
        ArrayList<Annotation> annotationList = new ArrayList<Annotation>();
        while(true) {
            String annotationTitle = "treble_annotation" + Integer.toString(annotationNumber++);
            JSONObject annotation = getJsonObjectOptional(fileSource, annotationTitle);
            if (null != annotation) {
                // there is an annotation, get the data for it
                String annotationName = annotation.getString("name");
                JSONArray values = annotation.getJSONArray("values");
                // put the values in a string array
                String[] annotationValues = new String[values.length()];
                for (int i = 0; i < annotationValues.length; ++i) {
                    annotationValues[i] = values.getString(i);
                }
                // add to the list we are compiling
                annotationList.add(new Annotation(annotationName, annotationValues));
            }
            else {
                // stop looking
                break;
            }
        }
        // set the list of retrieved annotations
        this.treble_annotations = new Annotation[annotationList.size()];
        for (int i = 0; i < this.treble_annotations.length; ++i) {
            this.treble_annotations[i] = annotationList.get(i);
        }
        // and bass
        annotationNumber = 1;
        annotationList.clear();
        while(true) {
            String annotationTitle = "bass_annotation" + Integer.toString(annotationNumber++);
            JSONObject annotation = getJsonObjectOptional(fileSource, annotationTitle);
            if (null != annotation) {
                // there is an annotation, get the data for it
                String annotationName = annotation.getString("name");
                JSONArray values = annotation.getJSONArray("values");
                // put the values in a string array
                String[] annotationValues = new String[values.length()];
                for (int i = 0; i < annotationValues.length; ++i) {
                    annotationValues[i] = values.getString(i);
                }
                // add to the list we are compiling
                annotationList.add(new Annotation(annotationName, annotationValues));
            }
            else {
                // stop looking
                break;
            }
        }
        // set the list of retrieved annotations
        this.bass_annotations = new Annotation[annotationList.size()];
        for (int i = 0; i < this.bass_annotations.length; ++i) {
            this.bass_annotations[i] = annotationList.get(i);
        }


        // and the children of this game
        JSONArray jsonChildren = getJsonArrayOptional(fileSource,"children");
        this.children = new Game[jsonChildren == null? 0 : jsonChildren.length()];
        for (int i = 0; i < this.children.length; ++i) {
            // for each level, load them in
            this.children[i] = new Game(this, jsonChildren.getJSONObject(i));
        }
    }

    private Playable createPlayable(String noteName) {
        String[] noteNames = noteName.split(",");
        Notes notes = Notes.instance();
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
            createdChord = new Chord(noteName);
            for (String name : noteNames) {
                // add all the notes one by one
                createdChord.addNote(notes.getNote(name), name);
            }
        }
        // return the created chord
        return createdChord;
    }

    private JSONObject getJsonObjectOptional(JSONObject source, String name) {
        JSONObject result = null;
        try {
            result =  source.getJSONObject(name);
        }
        catch (JSONException e) {
            // fine, it doesn't have to be there
        }
        return result;
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

    public String getTrebleAnnotations(int index) {
        // return all the annotations at this index
        StringBuilder annotations = new StringBuilder();
        if (null != this.treble_annotations) {
            for (Annotation annotation : this.treble_annotations) {
                if (null != annotation && null != annotation.values && index < annotation.values.length) {
                    // there is an annotation here
                    if (annotations.length() > 0) {
                        annotations.append(',');
                    }
                    // append this annotation
                    annotations.append(annotation.values[index]);
                }
            }
        }
        return annotations.toString();
    }

    public String getBassAnnotations(int index) {
        // return all the annotations at this index
        StringBuilder annotations = new StringBuilder();
        if (null != this.bass_annotations) {
            for (Annotation annotation : this.bass_annotations) {
                if (null != annotation && null != annotation.values && index < annotation.values.length) {
                    // there is an annotation here
                    if (annotations.length() > 0) {
                        annotations.append(',');
                    }
                    // append this annotation
                    annotations.append(annotation.values[index]);
                }
            }
        }
        return annotations.toString();
    }

    public NoteRange getNoteRange() {
        // go through all the notes to find the lowest and highest of them all
        NoteRange range = new NoteRange((Note)null, (Note)null);
        for (int i = 0; i < this.treble_clef.length + this.bass_clef.length; ++i) {
            Playable playable;
            if (i >= this.treble_clef.length) {
                // get the note from the bass clef
                playable = this.bass_clef[i - this.treble_clef.length];
            }
            else {
                // get the note from the treble clef
                playable = this.treble_clef[i];
            }
            Note lowest = playable.getLowest();
            if (range.getStart() == null || lowest.frequency < range.getStart().getFrequency()) {
                // this is before the current start
                range.setStart(lowest);
            }
            Note highest = playable.getHighest();
            if (range.getEnd() == null || highest.frequency > range.getEnd().getFrequency()) {
                // this is after the current end
                range.setEnd(highest);
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
