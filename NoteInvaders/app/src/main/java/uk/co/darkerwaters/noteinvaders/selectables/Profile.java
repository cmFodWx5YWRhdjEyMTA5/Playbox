package uk.co.darkerwaters.noteinvaders.selectables;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import uk.co.darkerwaters.noteinvaders.MicrophoneSetupActivity;
import uk.co.darkerwaters.noteinvaders.R;
import uk.co.darkerwaters.noteinvaders.SelectableItem;
import uk.co.darkerwaters.noteinvaders.SelectableItemActivity;
import uk.co.darkerwaters.noteinvaders.SelectableItemAdapter;
import uk.co.darkerwaters.noteinvaders.state.State;

public class Profile extends SelectableItem {

    private FloatingActionButton btnMicrophone;
    private FloatingActionButton btnUsbMidi;
    private FloatingActionButton btnBtMidi;

    public Profile(Activity context) {
        super(context,"temp", R.drawable.piano);
    }

    @Override
    public String getSubtitle() {
        return "subtitle";
    }

    @Override
    public void onBindViewHolder(final SelectableItemActivity context, SelectableItemAdapter.MyViewHolder holder) {
        super.onBindViewHolder(context, holder);

        // in here, called each time the activity is shown now, we can set the data on the profile card according
        // to our latest data from the state class
        holder.title.setText(State.getInstance().getInstrument().getName());
        Glide.with(context).load(State.getInstance().getInstrument().getThumbnail()).into(holder.thumbnail);

        // let's show the buttons as we want to
        this.btnMicrophone = (FloatingActionButton) holder.itemView.findViewById(R.id.button_card1);
        this.btnUsbMidi = (FloatingActionButton) holder.itemView.findViewById(R.id.button_card2);
        this.btnBtMidi = (FloatingActionButton) holder.itemView.findViewById(R.id.button_card3);

        // setup the microphone button
        this.btnMicrophone.setVisibility(View.VISIBLE);
        this.btnMicrophone.setImageResource(R.drawable.ic_baseline_mic_24px);
        if (false == State.getInstance().isInputAvailable(State.InputType.microphone)) {
            this.btnUsbMidi.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
        }
        this.btnMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // user selected to setup the microphone, show this activity now
                Intent myIntent = new Intent(context, MicrophoneSetupActivity.class);
                //myIntent.putExtra("instrument", item.getName()); //Optional parameters
                context.startActivity(myIntent);
            }
        });

        // setup the USB MIDI button
        this.btnUsbMidi.setVisibility(View.VISIBLE);
        this.btnUsbMidi.setImageResource(R.drawable.ic_baseline_usb_24px);
        // set background colour to show it isn't USB connected
        this.btnUsbMidi.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context,R.color.colorPrimaryDark)));
        if (false == State.getInstance().isInputAvailable(State.InputType.usb)) {
            this.btnUsbMidi.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
        }
        this.btnUsbMidi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // user selected to setup the USB MIDI
                Toast.makeText(context, "TODO THE MIDI CONNECTION", Toast.LENGTH_LONG).show();
            }
        });

        // setup the BT MIDI button
        this.btnBtMidi.setVisibility(View.VISIBLE);
        this.btnBtMidi.setImageResource(R.drawable.ic_baseline_bluetooth_audio_24px);
        // set background colour to show it isn't BT connected
        this.btnBtMidi.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context,R.color.colorPrimaryDark)));
        if (false == State.getInstance().isInputAvailable(State.InputType.bt)) {
            this.btnBtMidi.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
        }
        this.btnBtMidi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // user selected to setup the BT MIDI
                Toast.makeText(context, "TODO THE BT CONNECTION", Toast.LENGTH_LONG).show();
            }
        });
    }
}
