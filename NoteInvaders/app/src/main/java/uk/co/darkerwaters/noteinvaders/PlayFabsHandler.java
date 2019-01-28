package uk.co.darkerwaters.noteinvaders;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import uk.co.darkerwaters.noteinvaders.state.State;

public class PlayFabsHandler implements State.InputChangeListener {

    private final FloatingActionButton inputFab;
    private final FloatingActionButton manualFab;
    private final FloatingActionButton lettersFab;
    private final FloatingActionButton micFab;
    private final FloatingActionButton usbFab;
    private final FloatingActionButton btFab;
    private final FloatingActionButton settingsFab;

    private boolean isFabsShown = false;
    private final Activity context;
    private final State.InputChangeListener changeDeligate;
    private State.InputType currentInput;
    private final Map<State.InputType, Boolean> inputState;

    public enum BtIcon {
        Normal,
        Searching,
        Connected
    }

    private BtIcon lastBtIcon = BtIcon.Normal;

    public PlayFabsHandler(Activity context, State.InputChangeListener changeListener) {
        this.context = context;
        this.changeDeligate = changeListener;
        this.inputFab = (FloatingActionButton) context.findViewById(R.id.input_action_button);
        this.manualFab = (FloatingActionButton) context.findViewById(R.id.input_action_1);
        this.lettersFab = (FloatingActionButton) context.findViewById(R.id.input_action_2);
        this.micFab = (FloatingActionButton) context.findViewById(R.id.input_action_3);
        this.usbFab = (FloatingActionButton) context.findViewById(R.id.input_action_4);
        this.btFab = (FloatingActionButton) context.findViewById(R.id.input_action_5);
        this.settingsFab = (FloatingActionButton) context.findViewById(R.id.input_action_settings);

        // get the current input type to initialise the view
        this.currentInput = State.getInstance().getSelectedInput();

        // set the icons on these buttons
        this.inputFab.setImageResource(R.drawable.ic_baseline_keyboard_24px);
        this.manualFab.setImageResource(R.drawable.ic_baseline_keyboard_24px);
        this.lettersFab.setImageResource(R.drawable.ic_baseline_queue_music_24px);
        this.micFab.setImageResource(R.drawable.ic_baseline_mic_24px);
        this.usbFab.setImageResource(R.drawable.ic_baseline_usb_24px);
        setBtIcon(lastBtIcon);

        // set the correct current icon
        setInputIcon();

        // setup the map of states
        this.inputState = new HashMap<State.InputType, Boolean>();
        for (State.InputType type : State.InputType.values()) {
            // initialise the state to be false
            this.inputState.put(type, new Boolean(false));
            // and initialise the colour of the icon accordingly
            setInputAvailability(type, false);
        }

        this.inputFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // clicked the input FAB, expand or hide the selection
                toggleInputFabs();
            }
        });
        this.manualFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeInputType(State.InputType.keyboard);
            }
        });
        this.lettersFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeInputType(State.InputType.letters);
            }
        });
        this.micFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeInputType(State.InputType.microphone);
            }
        });
        this.usbFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeInputType(State.InputType.usb);
            }
        });
        this.btFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeInputType(State.InputType.bt);
            }
        });
        this.settingsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettings();
            }
        });

        // listen for changes in input to inform the listener of us
        State.getInstance().addListener(this);
    }

    public void close() {
        // remove us as a listener
        State.getInstance().removeListener(this);
    }

    public void setBtIcon(BtIcon icon) {
        switch (icon) {
            case Normal:
                this.btFab.setImageResource(R.drawable.ic_baseline_bluetooth_24px);
                if (this.currentInput == State.InputType.bt) {
                    this.inputFab.setImageResource(R.drawable.ic_baseline_bluetooth_24px);
                }
                break;
            case Searching:
                this.btFab.setImageResource(R.drawable.ic_baseline_bluetooth_searching_24px);
                if (this.currentInput == State.InputType.bt) {
                    this.inputFab.setImageResource(R.drawable.ic_baseline_bluetooth_searching_24px);
                }
                break;
            case Connected:
                this.btFab.setImageResource(R.drawable.ic_baseline_bluetooth_connected_24px);
                if (this.currentInput == State.InputType.bt) {
                    this.inputFab.setImageResource(R.drawable.ic_baseline_bluetooth_connected_24px);
                }
                break;
        }
        // remember this last icon
        this.lastBtIcon = icon;
    }

    private void showSettings() {
        Intent newIntent = null;
        switch (this.currentInput) {
            case keyboard:
                // no settings
                break;
            case letters:
                // no settings
                break;
            case microphone:
                newIntent = new Intent(this.context, MicrophoneSetupActivity.class);
                break;
            case usb:
                newIntent = new Intent(this.context, UsbSetupActivity.class);
                break;
            case bt:
                newIntent = new Intent(this.context, BtSetupActivity.class);
                break;

        }
        if (null != newIntent) {
            this.context.startActivity(newIntent);
        }
    }

    @Override
    public void onInputTypeChanged(State.InputType type) {
        // the input type has changed, update ourselves here
        this.currentInput = type;
        // set the correct icon
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // changing the icon has to happen on the UI
                setInputIcon();
            }
        });
        // and inform the listener of us
        this.changeDeligate.onInputTypeChanged(type);
    }

    private void setInputIcon() {
        if (null != this.inputFab) {
            switch (this.currentInput) {
                case keyboard:
                    this.inputFab.setImageResource(R.drawable.ic_baseline_keyboard_24px);
                    // there are no settings for this input type
                    settingsFab.animate().alpha(0f);
                    break;
                case letters:
                    this.inputFab.setImageResource(R.drawable.ic_baseline_queue_music_24px);
                    // there are no settings for this input type
                    settingsFab.animate().alpha(0f);
                    break;
                case microphone:
                    this.inputFab.setImageResource(R.drawable.ic_baseline_mic_24px);
                    // there are settings for this input type
                    settingsFab.animate().alpha(1f);
                    break;
                case usb:
                    this.inputFab.setImageResource(R.drawable.ic_baseline_usb_24px);
                    // there are settings for this input type
                    settingsFab.animate().alpha(1f);
                    break;
                case bt:
                    setBtIcon(this.lastBtIcon);
                    // there are settings for this input type
                    settingsFab.animate().alpha(1f);
                    break;
            }
            // also need to set the correct colour for the state
            setButtonTint(this.settingsFab, true);
            if (null != inputState) {
                setButtonTint(this.inputFab, this.inputState.get(this.currentInput));
            }
        }
    }

    public void setInputAvailability(State.InputType input, boolean state) {
        // set the state
        this.inputState.put(input, state);
        // and change the tint for this button
        if (null != this.inputFab) {
            switch (input) {
                case keyboard:
                    setButtonTint(this.manualFab, state);
                    break;
                case letters:
                    setButtonTint(this.lettersFab, state);
                    break;
                case microphone:
                    setButtonTint(this.micFab, state);
                    break;
                case usb:
                    setButtonTint(this.usbFab, state);
                    break;
                case bt:
                    setButtonTint(this.btFab, state);
                    break;
            }
            // set the master button if we are changing the state of the current input
            if (this.currentInput == input) {
                setButtonTint(this.inputFab, state);
            }
        }
    }

    private void setButtonTint(FloatingActionButton button, boolean state) {
        if (state) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                button.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.white)));
            }
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                button.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.black)));
            }
        }
    }

    private void toggleInputFabs() {
        if (this.isFabsShown) {
            manualFab.animate().translationX(0);
            lettersFab.animate().translationX(0);
            micFab.animate().translationX(0);
            usbFab.animate().translationX(0);
            btFab.animate().translationX(0);
            settingsFab.animate().translationY(0);
            this.isFabsShown = false;
        }
        else {
            // initialise a translation array for each button in order
            float[] translations = new float[] {
                    this.context.getResources().getDimension(R.dimen.standard_55),
                    this.context.getResources().getDimension(R.dimen.standard_105),
                    this.context.getResources().getDimension(R.dimen.standard_155),
                    this.context.getResources().getDimension(R.dimen.standard_205),
                    this.context.getResources().getDimension(R.dimen.standard_255)
            };
            State state = State.getInstance();
            // always manual and letters
            int iTranslation = 0;
            manualFab.animate().translationX(translations[iTranslation++]);
            lettersFab.animate().translationX(translations[iTranslation++]);
            if (state.isInputAvailable(State.InputType.microphone)) {
                micFab.animate().translationX(translations[iTranslation++]);
            }
            if (state.isInputAvailable(State.InputType.usb)) {
                usbFab.animate().translationX(translations[iTranslation++]);
            }
            if (state.isInputAvailable(State.InputType.bt)) {
                btFab.animate().translationX(translations[iTranslation++]);
            }
            // and reveal the settings
            settingsFab.animate().translationY(translations[0]);
            this.isFabsShown = true;
        }
    }

    private void changeInputType(State.InputType input) {
        // set the input to manual
        this.currentInput = input;
        State.getInstance().setSelectedInput(this.context, input);
        // and update the icon
        setInputIcon();
    }
}
