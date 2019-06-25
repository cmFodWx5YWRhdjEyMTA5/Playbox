package uk.co.darkerwaters.scorepal.activities.fragments;


import android.content.Context;
import android.os.Bundle;
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

public class FragmentSounds extends Fragment {

    private static final long K_ANIMATION_DURATION = 1000;
    private ImageButton soundsImageButton;
    private Button soundsMuteButton;
    private Button pointsMuteButton;
    private Button messagesMuteButton;

    private boolean isButtonsShown = true;

    private View mainView = null;

    public interface FragmentSoundsInteractionListener {
        void onAttachFragment(FragmentSounds fragment);
        void onTimeChanged();
    }

    private FragmentSounds.FragmentSoundsInteractionListener listener;


    public FragmentSounds() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.mainView = inflater.inflate(R.layout.fragment_sounds, container, false);

        Application application = (Application)getActivity().getApplication();
        final Settings settings = application.getSettings();

        this.soundsImageButton = mainView.findViewById(R.id.soundsImageButton);
        this.soundsMuteButton = mainView.findViewById(R.id.soundsMuteButton);
        this.pointsMuteButton = mainView.findViewById(R.id.pointsMuteButton);
        this.messagesMuteButton = mainView.findViewById(R.id.messagesMuteButton);

        this.soundsImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHideButtons(false);
            }
        });
        this.soundsMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings.setIsMakingSounds(!settings.getIsMakingSounds());
                updateViewFromData(settings);
            }
        });
        this.pointsMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings.setIsSpeakingPoints(!settings.getIsSpeakingPoints());
                updateViewFromData(settings);
            }
        });
        this.messagesMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings.setIsSpeakingMessages(!settings.getIsSpeakingMessages());
                updateViewFromData(settings);
            }
        });
        // update the buttons from the settings
        updateViewFromData(settings);
        // and return the main view
        return this.mainView;
    }

    public void setVisibility(int visibility) {
        if (null != this.mainView) {
            this.mainView.setVisibility(visibility);
        }
    }

    public void hideButtons(boolean isInstant) {
        if (this.isButtonsShown) {
            showHideButtons(isInstant);
        }
    }

    private void updateViewFromData(Settings settings) {
        // set the icons
        this.soundsMuteButton.setCompoundDrawablesWithIntrinsicBounds(getIcon(settings.getIsMakingSounds()), 0, 0, 0);
        this.pointsMuteButton.setCompoundDrawablesWithIntrinsicBounds(getIcon(settings.getIsSpeakingPoints()), 0, 0, 0);
        this.messagesMuteButton.setCompoundDrawablesWithIntrinsicBounds(getIcon(settings.getIsSpeakingMessages()), 0, 0, 0);

        // make the icons lighter
        int color = getContext().getColor(R.color.primaryTextColor);
        BaseActivity.SetIconTint(this.soundsMuteButton, color);
        BaseActivity.SetIconTint(this.pointsMuteButton, color);
        BaseActivity.SetIconTint(this.messagesMuteButton, color);
    }

    private int getIcon(boolean isOn) {
        if (isOn) {
            return R.drawable.ic_baseline_volume_up_24px;
        }
        else {
            return R.drawable.ic_baseline_volume_off_24px;
        }
    }

    private void showHideButtons(boolean isInstant) {
        this.isButtonsShown = !this.isButtonsShown;

        if (!this.isButtonsShown) {
            // and the size we need to be
            float hideWidth = this.soundsImageButton.getWidth();
            float hideHeight = this.soundsImageButton.getHeight();
            // get where we need to go to to hide our button
            float hideXPosition = this.soundsImageButton.getX() - hideWidth;
            float hideYPosition = this.soundsImageButton.getY();

            // animate each button down to this size and position
            hide(this.soundsMuteButton, hideXPosition, hideYPosition, hideWidth, hideHeight, isInstant);
            hide(this.pointsMuteButton, hideXPosition, hideYPosition, hideWidth, hideHeight, isInstant);
            hide(this.messagesMuteButton, hideXPosition, hideYPosition, hideWidth, hideHeight, isInstant);
        }
        else {
            restore(this.soundsMuteButton);
            restore(this.pointsMuteButton);
            restore(this.messagesMuteButton);
        }

    }

    private void hide(Button button, float targetX, float targetY, float targetW, float targetH, boolean isInstant) {
        // calculate the movements
        float movementX = targetX - button.getX();
        float movementY = targetY - button.getY();
        float scaleX = 0f;//targetW / button.getWidth();
        float scaleY = 0f;//targetH / button.getHeight();
        // and animate to here
        button.animate()
                .translationX(movementX)
                .translationY(movementY)
                .scaleX(scaleX)
                .scaleY(scaleY)
                .setDuration(isInstant ? 0 : K_ANIMATION_DURATION)
                .start();
    }

    private void restore(Button button) {
        // animate back
        button.animate()
                .translationX(0f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(K_ANIMATION_DURATION)
                .start();
        BaseActivity.SetIconTint(this.soundsMuteButton, getContext().getColor(R.color.primaryTextColor));
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
