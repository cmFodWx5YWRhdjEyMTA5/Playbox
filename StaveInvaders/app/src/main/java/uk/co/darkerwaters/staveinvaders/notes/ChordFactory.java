package uk.co.darkerwaters.staveinvaders.notes;

import java.util.HashMap;
import java.util.Map;

public class ChordFactory {

    public static void InitialiseOffkeyCreation(Chords singleChords) {
        // to use this, we want to get a list of notes to use as our basis of comparison
        // just the middle C group of notes will do
        ReferenceNotes = new HashMap<>(12);
        int iStart = singleChords.getChordIndex("A3");
        for (int i = iStart; i < iStart + 12; ++i) {
            // we are getting 12 notes to act as references for everything
            for (Note note : singleChords.getChord(i).notes) {
                // put each in the map
                ReferenceNotes.put(GetOffkeyNoteName(note), note);
            }
        }
    }
    private static Map<String, Note> ReferenceNotes = null;

    public static void ClearOffkeyCreation() {
        ReferenceNotes = null;
    }

    private static String GetOffkeyNoteName(Note note) {
        // just use the title and the sharp / flat qualifier
        String name = Character.toString(note.getNotePrimitive()) + note.getNoteQualifier();
        return name.trim();
    }

    private static Note[] SanitiseNotes(Note[] notes) {
        if (null == ReferenceNotes) {
            throw new RuntimeException("Cannot create a 'ChordOffkey' without initialisation");
        }
        // we want to replace all the notes specified with the reference ones to
        // properly compare like with like
        Note[] sanitised = new Note[notes.length];
        for (int i = 0; i < notes.length; ++i) {
            // instead of the original, replace with the reference note
            sanitised[i] = ReferenceNotes.get(GetOffkeyNoteName(notes[i]));
        }
        return sanitised;
    }

    public static boolean IsOffKeyCreation() {
        return null != ReferenceNotes;
    }

    static int CompareNoteFrequencies(Note note1, Note note2) {
        if (IsOffKeyCreation()) {
            note1 = ReferenceNotes.get(GetOffkeyNoteName(note1));
            note2 = ReferenceNotes.get(GetOffkeyNoteName(note2));
        }
        return (int)((note1 == null ? 0 : note1.frequency * 10000) -
                     (note2 == null ? 0 : note2.frequency * 10000));
    }
}
