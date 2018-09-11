package uk.co.darkerwaters.noteinvaders;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.selectables.Instrument;
import uk.co.darkerwaters.noteinvaders.state.State;

public class InstrumentActivity extends SelectableItemActivity {

    @Override
    protected int getContentViewRes() {
        return R.layout.activity_instrument;
    }

    @Override
    protected List<Instrument> getItemList() {
        List<Instrument> instruments = new ArrayList<Instrument>();
        for (int i = 0; i < State.getInstance().getAvailableInstrumentCount(); ++i) {
            instruments.add(State.getInstance().getAvailableInstrument(i));
        }
        return instruments;
    }

    @Override
    protected int getTitleImageRes() {
        return R.drawable.instruments;
    }

    @Override
    public void onSelectableItemClicked(SelectableItem item) {
        // set the selected instrument on our state
        if (false == item instanceof Instrument) {
            Log.e(State.K_APPTAG,"Selected instrument is not an instrument, it is a " + item);
        }
        else {
            State.getInstance().setInstrument((Instrument) item);
        }
        // close this activity
        finish();
    }

    @Override
    protected int getSpan() {
        return 2;
    }
}