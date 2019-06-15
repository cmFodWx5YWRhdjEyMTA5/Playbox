package uk.co.darkerwaters.scorepal.activities.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;

public class FragmentTeam extends Fragment {

    private static final long K_ANIMATION_DURATION = 1000;

    private TextView title;
    private CardView teamCard;
    private AutoCompleteTextView playerName;
    private AutoCompleteTextView partnerName;
    private CursorAdapter adapter;

    private float cardHeight = 0f;
    private boolean currentlyDoubles = true;
    private float animationAmount = 0f;

    private ObjectAnimator animation = null;

    public interface OnFragmentInteractionListener {
        // the listener to set the data from this fragment
        void onAttachFragment(FragmentTeam fragmentTeam);
        void onAnimationUpdated(Float value);
    }
    
    private OnFragmentInteractionListener listener;

    private int teamNumber = 0;

    public FragmentTeam() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parent = inflater.inflate(R.layout.fragment_team, container, false);

        this.title = parent.findViewById(R.id.titleText);
        this.playerName = parent.findViewById(R.id.playerAutoTextView);
        this.partnerName = parent.findViewById(R.id.playerPartnerAutoTextView);
        this.teamCard = parent.findViewById(R.id.team_card);

        // set our labels
        setLabels(this.teamNumber);
        // setup our adapters here
        setupAdapters();
        return parent;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
            // and inform this listener of our attachment
            listener.onAttachFragment(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void setAutoCompleteAdapter(CursorAdapter adapter) {
        this.adapter = adapter;
        // setup our adapters here
        setupAdapters();
    }

    private void animatePartnerName(final float animValue) {
        if (this.cardHeight <= 0f) {
            this.cardHeight = this.teamCard.getHeight();
        }
        if (null != this.animation) {
            this.animation.end();
        }
        this.animation = ObjectAnimator.ofFloat(this.partnerName, "translationY", animValue);
        this.animation.setDuration(K_ANIMATION_DURATION);
        this.animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                partnerName.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animator animator) {
                if (animValue < 0f) {
                    partnerName.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onAnimationCancel(Animator animator) {
            }
            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        this.animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // get the value the interpolator is a
                Float value = (Float) animation.getAnimatedValue();
                partnerName.setAlpha(1 - (value / animationAmount));
                // I'm going to set the layout's height 1:1 to the tick
                teamCard.getLayoutParams().height = (int)(cardHeight + value);
                teamCard.requestLayout();
                // and inform the listener of this change
                FragmentTeam.this.listener.onAnimationUpdated(value);
            }
        });
        this.animation.start();
    }

    public void setIsDoubles(boolean isDoubles, boolean instantChange) {
        if (instantChange) {
            // just hide / show the controls
            this.partnerName.setVisibility(isDoubles ? View.VISIBLE : View.GONE);
        }
        else {
            // do the animation
            if (isDoubles) {
                if (!this.currentlyDoubles) {
                    // are not currently doubles, put the parner name back in place
                    animatePartnerName(0f);
                }
            } else if (this.currentlyDoubles) {
                // are currently doubles, put the partner name up under the player name
                this.animationAmount = this.partnerName.getY() - this.playerName.getY();
                //animate the right amount (shrinking the parent will adjust this)
                this.animationAmount *= -0.7f;
                animatePartnerName(this.animationAmount);
            }
        }
        // remember where we are
        this.currentlyDoubles = isDoubles;
    }

    private void setupAdapters() {
        if (null != this.playerName) {
            this.playerName.setAdapter(this.adapter);
        }
        if (null != this.partnerName) {
            this.partnerName.setAdapter(this.adapter);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void setLabels(int teamNumber) {
        this.teamNumber = teamNumber;

        if (null != this.title) {
            switch (this.teamNumber) {
                case 1:
                    this.title.setText(R.string.team_one_title);
                    this.playerName.setHint(R.string.default_playerOneName);
                    this.partnerName.setHint(R.string.default_playerOnePartnerName);
                    break;
                case 2:
                    this.title.setText(R.string.team_two_title);
                    this.playerName.setHint(R.string.default_playerTwoName);
                    this.partnerName.setHint(R.string.default_playerTwoPartnerName);
                    break;
            }
        }
    }
}
