package uk.co.darkerwaters.staveinvaders.actvities.handlers;

import android.content.Context;
import android.media.Image;
import android.support.v4.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import uk.co.darkerwaters.staveinvaders.R;

public class InputOptionSettingsHandler extends ActionProvider {

    private final Context parent;

    public InputOptionSettingsHandler(Context context) {
        super(context);
        this.parent = context;
    }

    @Override
    public View onCreateActionView() {
        // Inflate the action provider to be shown on the action bar.
        LayoutInflater layoutInflater = LayoutInflater.from(parent);
        View providerView = layoutInflater.inflate(R.layout.input_option_provider, null);

        return providerView;
    }

    public void setOnClickListener(View parentView, View.OnClickListener listener) {
        // get the button
        ImageButton button = (ImageButton) parentView.findViewById(R.id.imageButton);
        button.setOnClickListener(listener);
    }

    public void showProgress(View parentView) {
        ImageView progressView = (ImageView) parentView.findViewById(R.id.progressImage);
        progressView.setVisibility(View.VISIBLE);
    }

    public void hideProgress(View parentView) {
        ImageView progressView = (ImageView) parentView.findViewById(R.id.progressImage);
        progressView.setVisibility(View.INVISIBLE);
    }
}
