package uk.co.darkerwaters.scorepal.activities.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.Settings;

public class FragmentTeam extends Fragment {

    private static final long K_ANIMATION_DURATION = 1000;
    private static final String K_NAME_SEPERATOR = " / ";

    private Application application;

    public enum TeamNamingMode {
        ROLE,
        SURNAME_INITIAL,
        FIRST_NAME,
        FULL_NAME;

        TeamNamingMode next() {
            int i = 0;
            TeamNamingMode[] modes = values();
            boolean foundThis = false;
            for (i = 0; i < modes.length; ++i) {
                if (foundThis) {
                    // this is the next one
                    return modes[i];
                }
                else if (modes[i] == this) {
                    foundThis = true;
                }
            }
            // if here then overflowed the list
            return ROLE;
        }
    };

    private TextView title;
    private ImageView titleModeButton;
    private CardView teamCard;
    private AutoCompleteTextView playerName;
    private AutoCompleteTextView partnerName;
    private CursorAdapter adapter;

    private float cardHeight = 0f;
    private boolean currentlyDoubles = true;
    private float animationAmount = 0f;

    private TeamNamingMode currentMode = TeamNamingMode.SURNAME_INITIAL;

    private ObjectAnimator animation = null;

    public interface FragmentTeamInteractionListener {
        // the listener to set the data from this fragment
        void onAttachFragment(FragmentTeam fragmentTeam);
        void onAnimationUpdated(Float value);
        void onTeamNameChanged(FragmentTeam fragmentTeam);
    }
    
    private FragmentTeamInteractionListener listener;

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

        this.application = (Application) getActivity().getApplication();

        this.title = parent.findViewById(R.id.titleText);
        this.titleModeButton = parent.findViewById(R.id.teamTitleModeButton);
        this.playerName = parent.findViewById(R.id.playerAutoTextView);
        this.partnerName = parent.findViewById(R.id.playerPartnerAutoTextView);
        this.teamCard = parent.findViewById(R.id.team_card);

        // listen for changes in each name to construct the title from them
        //this.playerName.setOnItemSelectedListener(createSelectedItemListener());
        //this.partnerName.setOnItemSelectedListener(createSelectedItemListener());
        this.playerName.addTextChangedListener(createTextChangeLisener());
        this.partnerName.addTextChangedListener(createTextChangeLisener());

        this.titleModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change the mode
                currentMode = currentMode.next();
                createTeamName();
            }
        });

        // set our labels
        setLabels(this.teamNumber);
        // setup our adapters here
        setupAdapters();
        return parent;
    }

    @Override
    public void onDestroy() {
        Settings settings = this.application.getSettings();
        // these are the new defaults to use the next time we come here
        settings.setPlayerName(this.playerName.getText().toString(), this.teamNumber - 1, 0);
        settings.setPlayerName(this.partnerName.getText().toString(), this.teamNumber - 1, 1);
        // and destroy this
        super.onDestroy();
    }

    private TextWatcher createTextChangeLisener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // nothing
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // update the team name
                createTeamName();
            }
            @Override
            public void afterTextChanged(Editable editable) {
                // nothing
            }
        };
    }

    private AdapterView.OnItemSelectedListener createSelectedItemListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // user selected a contact to use as a name, setup the name
                createTeamName();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // nothing selected, default team name will be chosen
                createTeamName();
            }
        };
    }

    public TeamNamingMode getTeamNameMode() {
        return this.currentMode;
    }

    public void setTeamNameMode(TeamNamingMode mode) {
        // only change if changing as will send a modification message
        if (mode != this.currentMode) {
            this.currentMode = mode;
            // update the team name
            createTeamName();
        }
    }

    public void setIsReadOnly(boolean readOnly) {
        this.playerName.setEnabled(!readOnly);
        this.partnerName.setEnabled(!readOnly);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentTeamInteractionListener) {
            listener = (FragmentTeamInteractionListener) context;
            // and inform this listener of our attachment
            listener.onAttachFragment(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentTeamInteractionListener");
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
        // and update the team name
        createTeamName();
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

    private void createTeamName() {
        // sort out what we are doing with our names, by default in doubles
        // we are a team, in singles we are player one
        String teamName = "";
        // combine the name in the correct chosen way
        switch (this.currentMode) {
            case ROLE:
                teamName = createRoleTeamName();
                break;
            case SURNAME_INITIAL:
                teamName = createSurnameTeamName();
                break;
            case FIRST_NAME:
                teamName = createFirstNameTeamName();
                break;
            case FULL_NAME:
                teamName = createFullnameTeamName();
                break;
        }
        if (null == teamName || teamName.isEmpty()) {
            // there is no team name, use the default
            switch (this.teamNumber) {
                case 1:
                    if (currentlyDoubles) {
                        teamName = getContext().getString(R.string.team_one_title);
                    }
                    else {
                        teamName = getContext().getString(R.string.default_playerOneName);
                    }
                    break;
                case 2:
                    if (currentlyDoubles) {
                        teamName = getContext().getString(R.string.team_two_title);
                    }
                    else {
                        teamName = getContext().getString(R.string.default_playerTwoName);
                    }
                    break;
            }
        }
        // set the title
        this.title.setText(teamName);
        // inform the listener of this
        if (null != this.listener) {
            this.listener.onTeamNameChanged(this);
        }
    }

    private String createRoleTeamName() {
        Context context = this.getContext();
        return combineTwoNames(context.getString(R.string.server), context.getString(R.string.receiver));
    }

    private String createSurnameTeamName() {
        if (this.currentlyDoubles) {
            return combineTwoNames(splitSurname(getPlayerName()), splitSurname(getPlayerPartnerName()));
        }
        else {
            return splitSurname(getPlayerName());
        }
    }

    private String createFirstNameTeamName() {
        if (this.currentlyDoubles) {
            return combineTwoNames(splitFirstName(getPlayerName()), splitFirstName(getPlayerPartnerName()));
        }
        else {
            return splitFirstName(getPlayerName());
        }
    }

    private String createFullnameTeamName() {
        if (this.currentlyDoubles) {
            return combineTwoNames(getPlayerName(), getPlayerPartnerName());
        }
        else {
            return getPlayerName();
        }
    }

    private String splitFirstName(String fullName) {
        String[] parts = fullName.split(" ");
        if (parts == null || parts.length <= 1) {
            // no good
            return fullName;
        }
        else {
            // there are a number of parts, just use the first name
            return parts[0];
        }
    }

    private String splitSurname(String fullName) {
        String[] parts = fullName.split(" ");
        if (parts == null || parts.length <= 1) {
            // no good
            return fullName;
        }
        else {
            // there are a number of parts, get all the initials
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < parts.length - 1; ++i) {
                // just append the first initial
                builder.append(parts[i].charAt(0));
                // append a dot after it
                builder.append('.');
            }
            // after the initials, we want a space
            builder.append(' ');
            // and finally the surname
            builder.append(parts[parts.length - 1]);
            // and return the string
            return builder.toString();
        }
    }

    private String combineTwoNames(String name1, String name2) {
        if (null == name1 || name1.isEmpty()) {
            // need to just use name 2
            return name2;
        }
        else if (null == name2 || name2.isEmpty()) {
            // need to just use name 1
            return name1;
        }
        else {
            // combine the two strings with a nice seperator
            StringBuilder builder = new StringBuilder();
            builder.append(name1);
            builder.append(K_NAME_SEPERATOR);
            builder.append(name2);
            // return this string
            return builder.toString();
        }
    }

    public String getTeamName() {
        return this.title.getText().toString();
    }

    public String getPlayerName() {
        String name = this.playerName.getText().toString();
        if (null == name || name.isEmpty()) {
            // get the default (the hint)
            name = this.playerName.getHint().toString();
        }
        return name;
    }

    public String getPlayerPartnerName() {
        String name = this.partnerName.getText().toString();
        if (null == name || name.isEmpty()) {
            // get the default (the hint)
            name = this.partnerName.getHint().toString();
        }
        return name;
    }

    public void setLabels(int teamNumber) {
        this.teamNumber = teamNumber;

        // set the names and hints to use
        if (null != this.title) {
            int playerHint = 0;
            int partnerHint = 0;
            int colorResId = 0;
            switch (this.teamNumber) {
                case 1:
                    colorResId = R.color.teamOneColor;
                    playerHint = R.string.default_playerOneName;
                    partnerHint = R.string.default_playerOnePartnerName;

                    break;
                case 2:
                    colorResId = R.color.teamTwoColor;
                    playerHint = R.string.default_playerTwoName;
                    partnerHint = R.string.default_playerTwoPartnerName;
                    break;
            }
            // set the hints
            this.playerName.setHint(playerHint);
            this.partnerName.setHint(partnerHint);
            // set the colours
            int color = getContext().getColor(colorResId);
            this.title.setTextColor(color);
            this.playerName.setTextColor(color);
            this.partnerName.setTextColor(color);
            // and do the team title
            createTeamName();

            // we can get the default names from the application too
            Settings settings = this.application.getSettings();
            // set the main player's name
            String defaultName = this.playerName.getText().toString();
            this.playerName.setText(settings.getPlayerName(teamNumber - 1, 0, defaultName));
            // and set the partner's name
            defaultName = this.partnerName.getText().toString();
            this.partnerName.setText(settings.getPlayerName(teamNumber - 1, 1, defaultName));
        }
    }
}
