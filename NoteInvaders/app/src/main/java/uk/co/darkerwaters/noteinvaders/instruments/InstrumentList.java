package uk.co.darkerwaters.noteinvaders.instruments;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.R;

public class InstrumentList {
    private static InstrumentList INSTANCE = null;
    private final List<Instrument> list;

    public static InstrumentList GET() {
        if (null == INSTANCE) {
            INSTANCE = new InstrumentList();
        }
        return INSTANCE;
    }

    private InstrumentList() {
        this.list = new ArrayList<Instrument>();
        synchronized (this.list) {
            this.list.add(new Instrument("Piano / Keyboard", R.drawable.piano));
            this.list.add(new Instrument("Violin", R.drawable.violin));
        }
    }

    public List<Instrument> toList() {
        synchronized (this.list) {
            return new ArrayList<Instrument>(this.list);
        }
    }

    public Instrument getInstrument(String name) {
        Instrument toReturn = null;
        synchronized (this.list) {
            for (Instrument instrument : this.list) {
                if (instrument.getName().equalsIgnoreCase(name)) {
                    toReturn = instrument;
                    break;
                }
            }
        }
        return toReturn;
    }
}
