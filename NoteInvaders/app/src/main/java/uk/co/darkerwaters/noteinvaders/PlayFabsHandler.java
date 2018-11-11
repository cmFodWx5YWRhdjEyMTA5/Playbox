package uk.co.darkerwaters.noteinvaders;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;

import uk.co.darkerwaters.noteinvaders.state.Notes;
import uk.co.darkerwaters.noteinvaders.state.State;

public class PlayFabsHandler implements State.InputChangeListener {

    private final FloatingActionButton inputFab;
    private final FloatingActionButton manualFab;
    private final FloatingActionButton micFab;
    private final FloatingActionButton usbFab;
    private final FloatingActionButton btFab;

    private boolean isFabsShown = false;
    private final Activity context;
    private final State.InputChangeListener changeDeligate;
    private State.InputType currentInput = State.InputType.keyboard;

    public PlayFabsHandler(Activity context, State.InputChangeListener changeListener) {
        this.context = context;
        this.changeDeligate = changeListener;
        this.inputFab = (FloatingActionButton) context.findViewById(R.id.input_action_button);
        this.manualFab = (FloatingActionButton) context.findViewById(R.id.input_action_1);
        this.micFab = (FloatingActionButton) context.findViewById(R.id.input_action_2);
        this.usbFab = (FloatingActionButton) context.findViewById(R.id.input_action_3);
        this.btFab = (FloatingActionButton) context.findViewById(R.id.input_action_4);

        // set the correct current icon
        setInputIcon();

        // set the icons on these buttons
        this.inputFab.setImageResource(R.drawable.ic_baseline_keyboard_24px);
        this.manualFab.setImageResource(R.drawable.ic_baseline_keyboard_24px);
        this.micFab.setImageResource(R.drawable.ic_baseline_mic_24px);
        this.usbFab.setImageResource(R.drawable.ic_baseline_usb_24px);
        this.btFab.setImageResource(R.drawable.ic_baseline_bluetooth_audio_24px);

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

        // listen for changes in input to inform the listener of us
        State.getInstance().addListener(this);
    }

    public void close() {
        // remove us as a listener
        State.getInstance().removeListener(this);
    }

    @Override
    public void onInputTypeChanged(State.InputType type) {
        // the input type has changed, update ourselves here
        setInputIcon();
        // and inform the listener of us
        this.changeDeligate.onInputTypeChanged(type);
    }

    private void setInputIcon() {
        if (null != this.inputFab) {
            switch (State.getInstance().getSelectedInput()) {
                case keyboard:
                    this.inputFab.setImageResource(R.drawable.ic_baseline_keyboard_24px);
                    break;
                case microphone:
                    this.inputFab.setImageResource(R.drawable.ic_baseline_mic_24px);
                    break;
                case usb:
                    this.inputFab.setImageResource(R.drawable.ic_baseline_usb_24px);
                    break;
                case bt:
                    this.inputFab.setImageResource(R.drawable.ic_baseline_bluetooth_audio_24px);
                    break;
            }
        }
    }

    public void setInputAvailability(State.InputType input, boolean state) {
        if (null != this.inputFab) {
            switch (input) {
                case keyboard:
                    setButtonTint(this.manualFab, state);
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
            micFab.animate().translationX(0);
            usbFab.animate().translationX(0);
            btFab.animate().translationX(0);
            this.isFabsShown = false;
        }
        else {
            manualFab.animate().translationX(this.context.getResources().getDimension(R.dimen.standard_55));
            micFab.animate().translationX(this.context.getResources().getDimension(R.dimen.standard_105));
            usbFab.animate().translationX(this.context.getResources().getDimension(R.dimen.standard_155));
            btFab.animate().translationX(this.context.getResources().getDimension(R.dimen.standard_205));
            this.isFabsShown = true;
        }
    }

    private void changeInputType(State.InputType input) {
        // set the input to manual
        this.currentInput = input;
        State.getInstance().setSelectedInput(input);
        // and shrink the selection
        toggleInputFabs();
        // and update the icon
        setInputIcon();
    }
}
