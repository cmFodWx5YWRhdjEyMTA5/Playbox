package uk.co.darkerwaters.scorepal.activities.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.util.Arrays;
import java.util.Calendar;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.score.TennisSets;

public class FragmentTime extends Fragment {

    private final static int K_NO_TIMES = 2;
    private final static int K_NO_DIGITS = 4;

    public interface FragmentTimeInteractionListener {
        void onAttachFragment(FragmentTime fragment);
    }

    private FragmentTimeInteractionListener listener;

    private TextSwitcher[][] switchers = new TextSwitcher[K_NO_TIMES][K_NO_DIGITS];
    private ViewSwitcher.ViewFactory switcherFactory;

    private BroadcastReceiver timeChangedReceiver;

    private final int[] timeDigits;
    private final int[] matchDigits;

    public FragmentTime() {
        // Required empty public constructor
        this.timeDigits = new int[K_NO_DIGITS];
        this.matchDigits = new int[K_NO_DIGITS];
        // fill with unset numbers
        Arrays.fill(this.timeDigits, -1);
        Arrays.fill(this.matchDigits, -1);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_previous_sets, container, false);

        // find all the text switchers here for team one
        this.switchers[0][0] = mainView.findViewById(R.id.time_digitOne);
        this.switchers[0][1] = mainView.findViewById(R.id.time_digitTwo);
        this.switchers[0][2] = mainView.findViewById(R.id.time_digitThree);
        this.switchers[0][3] = mainView.findViewById(R.id.time_digitFour);
        // and team two
        this.switchers[1][0] = mainView.findViewById(R.id.matchTime_digitOne);
        this.switchers[1][1] = mainView.findViewById(R.id.matchTime_digitTwo);
        this.switchers[1][2] = mainView.findViewById(R.id.matchTime_digitThree);
        this.switchers[1][3] = mainView.findViewById(R.id.matchTime_digitFour);

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
        for (int j = 0; j < K_NO_DIGITS; ++j) {
            // set the factory
            this.switchers[0][j].setFactory(this.switcherFactory);
            // and the animations
            this.switchers[0][j].setInAnimation(in);
            this.switchers[0][j].setOutAnimation(out);
        }
        // do the bottom row the other way in / out
        in = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
        out = AnimationUtils.loadAnimation(context, R.anim.slide_out_top);
        for (int j = 0; j < K_NO_DIGITS; ++j) {
            // set the factory
            this.switchers[1][j].setFactory(this.switcherFactory);
            // and the animations
            this.switchers[1][j].setInAnimation(in);
            this.switchers[1][j].setOutAnimation(out);
        }

        // need to listen for changes in time
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        // create the receiver
        this.timeChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(Intent.ACTION_TIME_CHANGED) ||
                        action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                    // update the time
                    onTimeChanged();
                }
            }
        };
        // and register it
        getActivity().registerReceiver(timeChangedReceiver, intentFilter);

        // and return the constructed parent view
        return mainView;
    }

    @Override
    public void onDestroy() {
        // detach the time change receiver
        getActivity().unregisterReceiver(timeChangedReceiver);
        super.onDestroy();
    }

    private void onTimeChanged() {
        Calendar rightNow = Calendar.getInstance();
        int[] newDigits = splitTimeToDigits(rightNow);
        for (int i = 0; i < K_NO_DIGITS; ++i) {
            if (timeDigits[i] != newDigits[i]) {
                // this is different
                timeDigits[i] = newDigits[i];
                this.switchers[0][i].setText(getTimeAsString(i, timeDigits[i]));
            }
        }
    }

    private void setMatchTime(int minutes) {
        //TODO set the match time
    }

    private String getTimeAsString(int position, int timeDigit) {
        String digit;
        if (timeDigit < 0 || (position == 0 && timeDigit <= 0)) {
            // should be blank
            digit = "";
        }
        else {
            // use as a number
            digit = Integer.toString(timeDigit);
        }
        return digit;
    }

    private int[] splitTimeToDigits(Calendar time) {
        // do the hours
        int[] newDigits = new int[K_NO_DIGITS];
        int currentHours = time.get(Calendar.HOUR_OF_DAY);
        newDigits[0] = currentHours % 10;
        newDigits[1] = currentHours - (newDigits[0] * 10);
        // do the minutes too
        int currentMinutes = time.get(Calendar.MINUTE);
        newDigits[2] = currentMinutes % 10;
        newDigits[3] = currentMinutes - (newDigits[2] * 10);
        // and return this
        return newDigits;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentTimeInteractionListener) {
            listener = (FragmentTimeInteractionListener) context;
            // and inform this listener of our attachment
            listener.onAttachFragment(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentTimeInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
