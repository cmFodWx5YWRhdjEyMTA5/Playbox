package uk.co.darkerwaters.scorepal.activities.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.score.TennisScore;
import uk.co.darkerwaters.scorepal.score.TennisSets;

public class FragmentPreviousSets extends Fragment {

    private final static int K_NO_TEAMS = 2;
    private final static int K_NO_SETS = TennisSets.FIVE.val;
    private static final String K_SETS_REFERENCE = "00";

    public interface FragmentPreviousSetsInteractionListener {
        void onAttachFragment(FragmentPreviousSets fragment);
    }

    private FragmentPreviousSetsInteractionListener listener;

    private TextSwitcher[][] switchers = new TextSwitcher[K_NO_TEAMS][K_NO_SETS];
    private ViewSwitcher.ViewFactory[] switcherFactorys;
    private TextView[] tieBreaks = new TextView[K_NO_SETS];

    public FragmentPreviousSets() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_previous_sets, container, false);

        // find all the text switchers here for team one
        this.switchers[0][0] = mainView.findViewById(R.id.previousSet_teamOne_setOne);
        this.switchers[0][1] = mainView.findViewById(R.id.previousSet_teamOne_setTwo);
        this.switchers[0][2] = mainView.findViewById(R.id.previousSet_teamOne_setThree);
        this.switchers[0][3] = mainView.findViewById(R.id.previousSet_teamOne_setFour);
        this.switchers[0][4] = mainView.findViewById(R.id.previousSet_teamOne_setFive);
        // and team two
        this.switchers[1][0] = mainView.findViewById(R.id.previousSet_teamTwo_setOne);
        this.switchers[1][1] = mainView.findViewById(R.id.previousSet_teamTwo_setTwo);
        this.switchers[1][2] = mainView.findViewById(R.id.previousSet_teamTwo_setThree);
        this.switchers[1][3] = mainView.findViewById(R.id.previousSet_teamTwo_setFour);
        this.switchers[1][4] = mainView.findViewById(R.id.previousSet_teamTwo_setFive);

        // don't forget the tie-break scores
        this.tieBreaks[0] = mainView.findViewById(R.id.tieBreak_setOne);
        this.tieBreaks[1] = mainView.findViewById(R.id.tieBreak_setTwo);
        this.tieBreaks[2] = mainView.findViewById(R.id.tieBreak_setThree);
        this.tieBreaks[3] = mainView.findViewById(R.id.tieBreak_setFour);
        this.tieBreaks[4] = mainView.findViewById(R.id.tieBreak_setFive);

        // make the factory to handle the switching of text here
        final Context context = this.getContext();
        this.switcherFactorys = new ViewSwitcher.ViewFactory[2];
        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        this.switcherFactorys[0] = new ViewSwitcher.ViewFactory() {
            public View makeView() {
                // create a TextView
                TextView t = new ResizeTextView(context, K_SETS_REFERENCE);
                t.setGravity(Gravity.CENTER);
                t.setTextColor(context.getColor(R.color.teamOneColor));
                return t;
            }
        };
        this.switcherFactorys[1] = new ViewSwitcher.ViewFactory() {
            public View makeView() {
                // create a TextView
                TextView t = new ResizeTextView(context, K_SETS_REFERENCE);
                t.setGravity(Gravity.CENTER);
                t.setTextColor(context.getColor(R.color.teamTwoColor));
                return t;
            }
        };

        // load an animation by using AnimationUtils class
        // set this factory for all the switchers
        Animation in = AnimationUtils.loadAnimation(context, R.anim.slide_in_top);
        Animation out = AnimationUtils.loadAnimation(context, R.anim.slide_out_bottom);
        for (int j = 0; j < K_NO_SETS; ++j) {
            // set the factory
            this.switchers[0][j].setFactory(this.switcherFactorys[0]);
            // and the animations
            this.switchers[0][j].setInAnimation(in);
            this.switchers[0][j].setOutAnimation(out);
        }
        // do the bottom row the other way in / out
        in = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
        out = AnimationUtils.loadAnimation(context, R.anim.slide_out_top);
        for (int j = 0; j < K_NO_SETS; ++j) {
            // set the factory
            this.switchers[1][j].setFactory(this.switcherFactorys[1]);
            // and the animations
            this.switchers[1][j].setInAnimation(in);
            this.switchers[1][j].setOutAnimation(out);
        }

        for (int i = 0; i < K_NO_SETS; ++i) {
            hideTieBreakResult(i);
        }

        // and return the constructed parent view
        return mainView;
    }

    public void setSets(TennisSets sets) {
        if (!this.isDetached()) {
            for (int i = 0; i < K_NO_SETS; ++i) {
                // set the visibility of each control
                this.switchers[0][i].setVisibility(i < sets.val ? View.VISIBLE : View.GONE);
                this.switchers[1][i].setVisibility(i < sets.val ? View.VISIBLE : View.GONE);
            }
        }
    }

    public void setTieBreakResult(int setIndex, int score1, int score2) {
        if (!this.isDetached()) {
            this.tieBreaks[setIndex].setText("(" + score1 + "-" + score2 + ")");
            this.tieBreaks[setIndex].setVisibility(View.VISIBLE);
        }
    }

    public void hideTieBreakResult(int setIndex) {
        if (!this.isDetached()) {
            this.tieBreaks[setIndex].setVisibility(View.INVISIBLE);
        }
    }

    public void setSetValue(int teamIndex, int setIndex, int value) {
        if (!this.isDetached()) {
            this.switchers[teamIndex][setIndex].setText(Integer.toString(value));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentPreviousSetsInteractionListener) {
            listener = (FragmentPreviousSetsInteractionListener) context;
            // and inform this listener of our attachment
            listener.onAttachFragment(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentPreviousSetsInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
