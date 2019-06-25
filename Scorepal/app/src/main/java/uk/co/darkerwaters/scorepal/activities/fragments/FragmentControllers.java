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

public class FragmentControllers extends ExpandingFragment {

    private ImageButton controllersButton;
    private Button tapControllerButton;
    private ImageButton tapControllerSettingsButton;
    private Button btControllerButton;
    private ImageButton btControllerSettingsButton;

    private Settings settings;

    public interface FragmentControllersInteractionListener {
        void onAttachFragment(FragmentControllers fragment);
    }

    private FragmentControllers.FragmentControllersInteractionListener listener;


    public FragmentControllers() {
        super();
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_controllers, container, false);

        // get our controls
        this.controllersButton = mainView.findViewById(R.id.controllersButton);
        this.tapControllerButton = mainView.findViewById(R.id.controllerTap);
        this.tapControllerSettingsButton = mainView.findViewById(R.id.controllerSettingsTap);
        this.btControllerButton = mainView.findViewById(R.id.controllerBt);
        this.btControllerSettingsButton = mainView.findViewById(R.id.controllerSettingsBt);

        // and setup the expansion / collapse of these
        setupExpandingFragment(mainView, this.controllersButton, new View[] {
                mainView.findViewById(R.id.background),
                this.tapControllerButton,
                this.tapControllerSettingsButton,
                this.btControllerButton,
                this.btControllerSettingsButton
        });

        // setup this fragment to respond to clicks etc
        Application application = (Application)getActivity().getApplication();


        // update the buttons from the settings
        updateViewFromData();
        // and return the main view
        return mainView;
    }

    @Override
    protected void updateViewFromData() {
        // set the tint of these new icons
        super.updateViewFromData();

        // setup the data on these buttons from the application / whatever
        Context context = getContext();
        int inactiveColor = context.getColor(R.color.colorPrimary);

        // tap is not enabled
        BaseActivity.SetIconTint(this.tapControllerButton, inactiveColor);
        // BT is not enabled
        BaseActivity.SetIconTint(this.btControllerButton, inactiveColor);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentControllers.FragmentControllersInteractionListener) {
            this.listener = (FragmentControllers.FragmentControllersInteractionListener) context;
            // and inform this listener of our attachment
            this.listener.onAttachFragment(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentControllersInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

}