package uk.co.darkerwaters.noteinvaders.state.input;

import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Playable;

public interface InputConnectionInterface {
    void onNoteDetected(Playable note, boolean isDetection, float probability, int frequency);
}
