package uk.co.darkerwaters.noteinvaders;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.selectables.Instrument;
import uk.co.darkerwaters.noteinvaders.state.State;

public class InputActivity extends SelectableItemActivity {

    private List<Instrument> inputList;
    private Instrument parentInstrument = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        if(null != bundle) {
            String instrumentName = bundle.getString("instrument");
            // get the actual instrument for this
            for (int i = 0; i < State.getInstance().getAvailableInstrumentCount(); ++i) {
                Instrument instrument = State.getInstance().getAvailableInstrument(i);
                if (instrument.getName().equals(instrumentName)) {
                    // this is it
                    this.parentInstrument = instrument;
                    break;
                }
            }
        }
        // and create the view now
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentViewRes() {
        return R.layout.activity_input;
    }

    @Override
    protected int getTitleImageRes() {
        if (null == this.parentInstrument) {
            return R.drawable.instruments;
        }
        else {
            return this.parentInstrument.getThumbnail();
        }
    }

    @Override
    protected List<Instrument> getItemList() {

        this.inputList = new ArrayList<Instrument>();
        this.inputList.add(new Instrument(this, getString(R.string.microphone), R.drawable.microphone));
        this.inputList.add(new Instrument(this, getString(R.string.usb_midi), R.drawable.usb));
        this.inputList.add(new Instrument(this, getString(R.string.bt_midi), R.drawable.bt_usb));

        return this.inputList;
    }

    @Override
    public void onSelectableItemClicked(SelectableItem item) {
        // start the selection activity
        if (item.getName().equals(getString(R.string.microphone))) {
            // user selected to use the microphone
            Intent myIntent = new Intent(this, MicrophoneSetupActivity.class);
            //myIntent.putExtra("instrument", item.getName()); //Optional parameters
            this.startActivity(myIntent);
        }

    }

    @Override
    protected int getSpan() {
        int span = 1;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            // if we are landscape then we can show 2
            span = 2;
        }
        return span;
    }
}