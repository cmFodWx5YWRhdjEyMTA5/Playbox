package uk.co.darkerwaters.staveinvaders.actvities.handlers;

import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import uk.co.darkerwaters.staveinvaders.R;

public class InputOptionSettingsHandler extends ActionProvider {

    private final Context parent;
    private ImageButton button;
    private ImageView progressView;

    public InputOptionSettingsHandler(Context context) {
        super(context);
        this.parent = context;
    }

    @Override
    public View onCreateActionView() {
        // Inflate the action provider to be shown on the action bar.
        LayoutInflater layoutInflater = LayoutInflater.from(parent);
        View providerView = layoutInflater.inflate(R.layout.input_option_provider, null);

        this.button = (ImageButton) providerView.findViewById(R.id.imageButton);
        this.progressView = (ImageView) providerView.findViewById(R.id.progressImage);

        return providerView;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.button.setOnClickListener(listener);
    }

    public void showProgress() {
        this.progressView.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        this.progressView.setVisibility(View.INVISIBLE);
    }
}
