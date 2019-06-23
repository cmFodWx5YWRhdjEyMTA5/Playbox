package uk.co.darkerwaters.scorepal.activities.fragments;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.score.Match;

public abstract class LayoutScoreSummary {

    public abstract View createView(LayoutInflater inflater, ViewGroup container);

    public abstract void setDataFromMatch(Match match);

    protected void setTextViewBold(TextView textView) {
        BaseActivity.SetTextViewBold(textView);
    }

}
