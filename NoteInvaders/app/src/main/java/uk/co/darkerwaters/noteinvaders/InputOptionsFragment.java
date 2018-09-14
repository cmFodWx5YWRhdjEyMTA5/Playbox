package uk.co.darkerwaters.noteinvaders;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.co.darkerwaters.noteinvaders.state.State;


public class InputOptionsFragment extends Fragment {

    private InputOptionsListener listener;

    private FloatingActionButton inputFab;
    private FloatingActionButton manualFab;
    private FloatingActionButton micFab;
    private FloatingActionButton usbFab;
    private FloatingActionButton btFab;

    private boolean isFabsShown = false;


    public InputOptionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.inputFab = (FloatingActionButton) view.findViewById(R.id.input_action_button);
        this.manualFab = (FloatingActionButton) view.findViewById(R.id.input_action_1);
        this.micFab = (FloatingActionButton) view.findViewById(R.id.input_action_2);
        this.usbFab = (FloatingActionButton) view.findViewById(R.id.input_action_3);
        this.btFab = (FloatingActionButton) view.findViewById(R.id.input_action_4);

        // set the correct current icon
        setInputIcon();

        // set the icons on these buttons
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
    }

    private void changeInputType(State.InputType input) {
        // set the input to manual
        State.getInstance().setSelectedInput(input);
        // and shrink the selection
        toggleInputFabs();
        // and update the icon
        setInputIcon();
        // and inform the listener
        this.listener.onInputOptionSelected(input);
    }

    private void setInputIcon() {
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

    private void toggleInputFabs() {
        if (this.isFabsShown) {
            manualFab.animate().translationY(0);
            micFab.animate().translationY(0);
            usbFab.animate().translationY(0);
            btFab.animate().translationY(0);
            this.isFabsShown = false;
        }
        else {
            manualFab.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
            micFab.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
            usbFab.animate().translationY(-getResources().getDimension(R.dimen.standard_155));
            btFab.animate().translationY(-getResources().getDimension(R.dimen.standard_205));
            this.isFabsShown = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_input_options, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InputOptionsListener) {
            listener = (InputOptionsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement InputOptionsListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface InputOptionsListener {
        void onInputOptionSelected(State.InputType input);
    }
}
