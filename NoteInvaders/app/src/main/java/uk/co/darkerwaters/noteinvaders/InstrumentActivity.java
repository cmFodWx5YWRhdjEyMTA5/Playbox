package uk.co.darkerwaters.noteinvaders;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.selectables.Instrument;


public class InstrumentActivity extends SelectableItemActivity {

    @Override
    protected int getContentViewRes() {
        return R.layout.activity_instrument;
    }

    @Override
    protected List<Instrument> getItemList() {
        List<Instrument> instruments = new ArrayList<Instrument>();
        for (int i = 0; i < NoteInvaders.getAppContext().getAvailableInstrumentCount(); ++i) {
            instruments.add(NoteInvaders.getAppContext().getAvailableInstrument(i));
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
            Log.e(NoteInvaders.K_APPTAG,"Selected instrument is not an instrument, it is a " + item);
        }
        else {
            NoteInvaders.getAppContext().setInstrument((Instrument) item);
        }
        // close this activity
        finish();
    }

    @Override
    protected int getSpan() {
        return 2;
    }
}