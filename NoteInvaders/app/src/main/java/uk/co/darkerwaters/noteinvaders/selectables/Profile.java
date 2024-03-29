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
import uk.co.darkerwaters.noteinvaders.NoteInvaders;
import uk.co.darkerwaters.noteinvaders.R;
import uk.co.darkerwaters.noteinvaders.SelectableItem;
import uk.co.darkerwaters.noteinvaders.SelectableItemActivity;
import uk.co.darkerwaters.noteinvaders.SelectableItemAdapter;


public class Profile extends SelectableItem {

    private FloatingActionButton btnMicrophone;
    private FloatingActionButton btnUsbMidi;
    private FloatingActionButton btnBtMidi;

    public Profile(Activity context) {
        super(context);
    }

    @Override
    public String getTitle(Activity context) {
        return "temp";
    }

    @Override
    public int getThumbnail() {
        return R.drawable.piano;
    }

    @Override
    public String getSubtitle(Activity context) {
        return "subtitle";
    }

    @Override
    public int getProgress(Activity context) {
        //TODO get the average progress for all the games played
        return -1;
    }

    @Override
    public void onItemRefreshed(final SelectableItemActivity context, SelectableItemAdapter.MyViewHolder holder) {
        super.onItemRefreshed(context, holder);

        // in here, called each time the activity is shown now, we can set the data on the profile card according
        // to our latest data from the state class
        holder.title.setText(NoteInvaders.getAppContext().getInstrument().getTitle(context));
        Glide.with(context).load(NoteInvaders.getAppContext().getInstrument().getThumbnail()).into(holder.thumbnail);

        // let's show the buttons as we want to
        this.btnMicrophone = (FloatingActionButton) holder.itemView.findViewById(R.id.button_card1);
        this.btnUsbMidi = (FloatingActionButton) holder.itemView.findViewById(R.id.button_card2);
        this.btnBtMidi = (FloatingActionButton) holder.itemView.findViewById(R.id.button_card3);

        // setup the microphone button
        this.btnMicrophone.setImageResource(R.drawable.ic_baseline_mic_24px);
        if (false == NoteInvaders.getAppContext().isInputAvailable(NoteInvaders.InputType.microphone)) {
            this.btnMicrophone.setVisibility(View.INVISIBLE);
        }
        else {
            this.btnMicrophone.setVisibility(View.VISIBLE);
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
        this.btnUsbMidi.setImageResource(R.drawable.ic_baseline_usb_24px);
        if (false == NoteInvaders.getAppContext().isInputAvailable(NoteInvaders.InputType.usb)) {
            this.btnUsbMidi.setVisibility(View.INVISIBLE);
        }
        else {
            this.btnUsbMidi.setVisibility(View.VISIBLE);
        }
        // set background colour to show it isn't USB connected
        this.btnUsbMidi.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context,R.color.colorPrimaryDark)));
        if (false == NoteInvaders.getAppContext().isInputAvailable(NoteInvaders.InputType.usb)) {
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
        this.btnBtMidi.setImageResource(R.drawable.ic_baseline_bluetooth_24px);
        if (false == NoteInvaders.getAppContext().isInputAvailable(NoteInvaders.InputType.bt)) {
            this.btnBtMidi.setVisibility(View.INVISIBLE);
        }
        else {
            this.btnBtMidi.setVisibility(View.VISIBLE);
        }
        // set background colour to show it isn't BT connected
        this.btnBtMidi.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context,R.color.colorPrimaryDark)));
        if (false == NoteInvaders.getAppContext().isInputAvailable(NoteInvaders.InputType.bt)) {
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
