package uk.co.darkerwaters.scorepal.activities.fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.application.Settings;

public class FragmentSounds extends ExpandingFragment {

    private ImageButton soundsImageButton;
    private Button soundsMuteButton;
    private Button pointsMuteButton;
    private Button messagesMuteButton;

    private Settings settings;

    public interface FragmentSoundsInteractionListener {
        void onAttachFragment(FragmentSounds fragment);
    }

    private FragmentSounds.FragmentSoundsInteractionListener listener;


    public FragmentSounds() {
        super();
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_sounds, container, false);

        // get our controls
        this.soundsImageButton = mainView.findViewById(R.id.soundsImageButton);
        this.soundsMuteButton = mainView.findViewById(R.id.soundsMuteButton);
        this.pointsMuteButton = mainView.findViewById(R.id.pointsMuteButton);
        this.messagesMuteButton = mainView.findViewById(R.id.messagesMuteButton);

        // and setup the expansion / collapse of these
        setupExpandingFragment(mainView, this.soundsImageButton, new View[] {
                mainView.findViewById(R.id.background),
                this.soundsMuteButton,
                this.pointsMuteButton,
                this.messagesMuteButton
        });

        // setup this fragment to respond to clicks etc
        Application application = (Application)getActivity().getApplication();
        this.settings = application.getSettings();
        // handle the button clicks here
        this.soundsMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings.setIsMakingSounds(!settings.getIsMakingSounds());
                updateViewFromData();
            }
        });
        this.pointsMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings.setIsSpeakingPoints(!settings.getIsSpeakingPoints());
                updateViewFromData();
            }
        });
        this.messagesMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings.setIsSpeakingMessages(!settings.getIsSpeakingMessages());
                updateViewFromData();
            }
        });
        // update the buttons from the settings
        updateViewFromData();
        // and return the main view
        return mainView;
    }

    @Override
    protected void updateViewFromData() {
        // set the icons
        this.soundsMuteButton.setCompoundDrawablesWithIntrinsicBounds(getIcon(settings.getIsMakingSounds()), 0, 0, 0);
        this.pointsMuteButton.setCompoundDrawablesWithIntrinsicBounds(getIcon(settings.getIsSpeakingPoints()), 0, 0, 0);
        this.messagesMuteButton.setCompoundDrawablesWithIntrinsicBounds(getIcon(settings.getIsSpeakingMessages()), 0, 0, 0);

        // set the tint of these new icons
        super.updateViewFromData();
    }

    private int getIcon(boolean isOn) {
        if (isOn) {
            return R.drawable.ic_baseline_volume_up_24px;
        }
        else {
            return R.drawable.ic_baseline_volume_off_24px;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentSounds.FragmentSoundsInteractionListener) {
            this.listener = (FragmentSounds.FragmentSoundsInteractionListener) context;
            // and inform this listener of our attachment
            this.listener.onAttachFragment(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentSoundsInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

}
