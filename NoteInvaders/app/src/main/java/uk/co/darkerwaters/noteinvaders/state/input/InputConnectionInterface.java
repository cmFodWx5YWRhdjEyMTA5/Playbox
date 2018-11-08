package uk.co.darkerwaters.noteinvaders.state.input;

import uk.co.darkerwaters.noteinvaders.state.Note;

public interface InputConnectionInterface {
    void onNoteDetected(Note note, boolean isDetection, float probability, int frequency);
}
