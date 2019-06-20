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
import uk.co.darkerwaters.scorepal.activities.animation.ChangeEndsTextAnimation;
import uk.co.darkerwaters.scorepal.activities.animation.ChangeServerTextAnimation;
import uk.co.darkerwaters.scorepal.activities.animation.GameOverTextAnimation;
import uk.co.darkerwaters.scorepal.activities.animation.TextViewAnimation;
import uk.co.darkerwaters.scorepal.score.Point;

public class FragmentScore extends Fragment {

    private final static int K_NO_TEAMS = 2;
    private final static int K_NO_LEVELS = 3;

    public interface FragmentScoreInteractionListener {
        void onAttachFragment(FragmentScore fragment);
        void onFragmentScorePointsClick(int teamIndex);
    }

    public enum ScoreState {
        COMPLETED,
        CHANGE_ENDS,
        CHANGE_SERVER
    }

    private FragmentScoreInteractionListener listener;

    private TextViewAnimation informationAnimator = null;
    private TextView informationText;

    private TextSwitcher[][] switchers = new TextSwitcher[K_NO_TEAMS][K_NO_LEVELS];
    private ViewSwitcher.ViewFactory switcherFactory;

    public FragmentScore() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_score, container, false);

        this.informationText = mainView.findViewById(R.id.information_textView);

        // find all the text switchers here for team one
        this.switchers[0][0] = mainView.findViewById(R.id.sets_teamOne);
        this.switchers[0][1] = mainView.findViewById(R.id.games_teamOne);
        this.switchers[0][2] = mainView.findViewById(R.id.points_teamOne);
        // and team two
        this.switchers[1][0] = mainView.findViewById(R.id.sets_teamTwo);
        this.switchers[1][1] = mainView.findViewById(R.id.games_teamTwo);
        this.switchers[1][2] = mainView.findViewById(R.id.points_teamTwo);

        // listen for clicks on the points to change the points
        this.switchers[0][2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                informListenerOfPointsClick(0);
            }
        });
        this.switchers[1][2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                informListenerOfPointsClick(1);
            }
        });

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
                t.setTextColor(context.getColor(R.color.primaryTextColor));
                return t;
            }
        };

        // load an animation by using AnimationUtils class
        // set this factory for all the switchers
        Animation in = AnimationUtils.loadAnimation(context, R.anim.slide_in_top);
        Animation out = AnimationUtils.loadAnimation(context, R.anim.slide_out_bottom);
        for (int j = 0; j < K_NO_LEVELS; ++j) {
            // set the factory
            this.switchers[0][j].setFactory(this.switcherFactory);
            // and the animations
            this.switchers[0][j].setInAnimation(in);
            this.switchers[0][j].setOutAnimation(out);
        }
        // do the bottom row the other way in / out
        in = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
        out = AnimationUtils.loadAnimation(context, R.anim.slide_out_top);
        for (int j = 0; j < K_NO_LEVELS; ++j) {
            // set the factory
            this.switchers[1][j].setFactory(this.switcherFactory);
            // and the animations
            this.switchers[1][j].setInAnimation(in);
            this.switchers[1][j].setOutAnimation(out);
        }

        // and return the constructed parent view
        return mainView;
    }

    public String getMatchState() {
        // return the active match state we are animating
        String state = null;
        if (null != this.informationAnimator) {
            state = this.informationAnimator.getAnimatedText();
        }
        return state;
    }

    public void cancelMatchState() {
        // stop showing any messages
        if (null != this.informationAnimator) {
            this.informationAnimator.cancel();
            this.informationAnimator = null;
        }
    }

    public void showMatchState(ScoreState state) {
        // show the state as a nice animation of text that scales up and slides away
        // first cancel any active one
        cancelMatchState();
        // if we are not attached to a context, ignore this attempt to show things
        if (!this.isDetached()) {
            switch (state) {
                case COMPLETED:
                    this.informationAnimator = new GameOverTextAnimation(getActivity(), this.informationText);
                    break;
                case CHANGE_ENDS:
                    this.informationAnimator = new ChangeEndsTextAnimation(getActivity(), this.informationText);
                    break;
                case CHANGE_SERVER:
                    this.informationAnimator = new ChangeServerTextAnimation(getActivity(), this.informationText);
                    break;
            }
        }
    }

    private void informListenerOfPointsClick(int teamIndex) {
        this.listener.onFragmentScorePointsClick(teamIndex);
    }

    public void setSetValue(int teamIndex, String value) {
        setSwitcherText(this.switchers[teamIndex][0], value);
    }

    public void setGamesValue(int teamIndex, String value) {
        setSwitcherText(this.switchers[teamIndex][1], value);
    }

    public void setPointsValue(int teamIndex, Point value) {
        setSwitcherText(this.switchers[teamIndex][2], value.displayString(getContext()));
    }

    private void setSwitcherText(TextSwitcher switcher, String content) {
        // if we are not attached to a context, ignore this attempt to show things
        if (false == this.isDetached() && switcher.getCurrentView() instanceof TextView) {
            TextView currentTextView = (TextView) switcher.getCurrentView();
            CharSequence text = currentTextView.getText();
            if (null == text || false == text.toString().equals(content)) {
                // this is different, set it to that passed
                switcher.setText(content);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentScoreInteractionListener) {
            listener = (FragmentScoreInteractionListener) context;
            // and inform this listener of our attachment
            listener.onAttachFragment(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentScoreInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
