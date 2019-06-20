package uk.co.darkerwaters.scorepal.score;

import android.content.Context;

public interface Point {

    int val();

    String displayString(Context context);
    String speakString(Context context);
    String speakAllString(Context context);
}
