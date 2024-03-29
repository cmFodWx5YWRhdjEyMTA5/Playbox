package uk.co.darkerwaters.staveinvaders.activities.handlers;

import android.media.midi.MidiDeviceInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.input.InputMidi;
import uk.co.darkerwaters.staveinvaders.R;

public class UsbItemAdapter extends RecyclerView.Adapter<UsbItemAdapter.ViewHolder> {

    private final List<MidiDeviceInfo> itemList;
    private final Application application;
    private final MidiListListener listener;

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title, subtitle;
        final ImageView thumbnail;
        final ImageView selected;
        MidiDeviceInfo item;

        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title_text);
            subtitle = view.findViewById(R.id.subtitle_text);
            thumbnail = view.findViewById(R.id.instrument_image);
            selected = view.findViewById(R.id.midi_selected);
        }
    }

    public interface MidiListListener {
        void onMidiListItemClicked(MidiDeviceInfo item);
    }

    public UsbItemAdapter(List<MidiDeviceInfo> itemList, Application application, MidiListListener listener) {
        this.itemList = itemList;
        this.application = application;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemIndex) {
        // create the view
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_midi_instrument, viewGroup, false);
        // and return the holder for the view
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder midiDeviceInfo, int i) {
        // setup the holder
        midiDeviceInfo.item = this.itemList.get(i);
        midiDeviceInfo.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // inform the listener of this click
                if (null != UsbItemAdapter.this.listener) {
                    UsbItemAdapter.this.listener.onMidiListItemClicked(midiDeviceInfo.item);
                }
            }
        });
        // and refresh the content
        refreshContent(midiDeviceInfo);
    }

    private void refreshContent(ViewHolder holder) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Bundle properties = holder.item.getProperties();
            String manufacturer = properties.getString(MidiDeviceInfo.PROPERTY_MANUFACTURER);
            String name = properties.getString(MidiDeviceInfo.PROPERTY_PRODUCT);
            if (manufacturer != null && false == manufacturer.isEmpty()) {
                // show the manufacturer and the name
                holder.title.setText(manufacturer);
                holder.subtitle.setText(name);
            }
            else {
                // show the ID we will use
                holder.title.setText(InputMidi.GetMidiDeviceId(holder.item));
                holder.subtitle.setText("");
            }
            // see if this is the selected device and show that we are connected / connecting?
            if (InputMidi.GetMidiDeviceId(holder.item).equals(this.application.getSettings().getLastConnectedBtDevice())) {
                holder.selected.startAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in));
            }
            else {
                holder.selected.startAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade));
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }
}
