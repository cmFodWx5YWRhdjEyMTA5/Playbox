package uk.co.darkerwaters.staveinvaders.actvities.handlers;

import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;

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
        /*ImageButton button = (ImageButton) providerView.findViewById(R.id.imageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something...
                Toast.makeText(parent, "hello", Toast.LENGTH_LONG).show();
            }
        });*/
        return providerView;
    }
}
