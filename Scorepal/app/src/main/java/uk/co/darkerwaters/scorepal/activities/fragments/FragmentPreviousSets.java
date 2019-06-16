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

public class FragmentPreviousSets extends Fragment {

    private final static int K_NO_TEAMS = 2;
    private final static int K_NO_SETS = 5;

    public interface FragmentPreviousSetsInteractionListener {
        void onAttachFragment(FragmentPreviousSets fragment);
    }

    private FragmentPreviousSetsInteractionListener listener;

    private TextSwitcher[][] switchers = new TextSwitcher[K_NO_TEAMS][K_NO_SETS];
    private ViewSwitcher.ViewFactory switcherFactory;

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

        // make the factory to handle the switching of text here
        final Context context = this.getContext();
        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        this.switcherFactory = new ViewSwitcher.ViewFactory() {
            public View makeView() {
                // create a TextView
                TextView t = new TextView(context);
                // set the gravity of text to top and center horizontal
                t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                // set displayed text size
                t.setTextSize(36);
                return t;
            }
        };

        // load an animation by using AnimationUtils class
        // set this factory for all the switchers
        Animation in = AnimationUtils.loadAnimation(context, R.anim.slide_in_top);
        Animation out = AnimationUtils.loadAnimation(context, R.anim.slide_out_bottom);
        for (int j = 0; j < K_NO_SETS; ++j) {
            // set the factory
            this.switchers[0][j].setFactory(this.switcherFactory);
            // and the animations
            this.switchers[0][j].setInAnimation(in);
            this.switchers[0][j].setOutAnimation(out);
        }
        // do the bottom row the other way in / out
        in = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
        out = AnimationUtils.loadAnimation(context, R.anim.slide_out_top);
        for (int j = 0; j < K_NO_SETS; ++j) {
            // set the factory
            this.switchers[1][j].setFactory(this.switcherFactory);
            // and the animations
            this.switchers[1][j].setInAnimation(in);
            this.switchers[1][j].setOutAnimation(out);
        }

        // and return the constructed parent view
        return mainView;
    }

    public void setSetValue(int teamIndex, int setIndex, int value) {
        this.switchers[teamIndex][setIndex].setText(Integer.toString(value));
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
