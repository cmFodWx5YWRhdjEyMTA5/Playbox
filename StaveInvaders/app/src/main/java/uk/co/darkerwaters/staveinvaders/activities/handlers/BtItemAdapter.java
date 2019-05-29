package uk.co.darkerwaters.staveinvaders.activities.handlers;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.input.InputBluetooth;

public class BtItemAdapter extends RecyclerView.Adapter<BtItemAdapter.ViewHolder> {

    private final List<BluetoothDevice> itemList;
    private final BtListListener listener;
    private final Application application;

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title, subtitle;
        final ImageView thumbnail;
        final ImageView selected;
        BluetoothDevice item;

        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title_text);
            subtitle = view.findViewById(R.id.subtitle_text);
            thumbnail = view.findViewById(R.id.instrument_image);
            selected = view.findViewById(R.id.midi_selected);
        }
    }

    public interface BtListListener {
        void onBtListItemClicked(BluetoothDevice item);
    }

    public BtItemAdapter(Application application, BtListListener listener) {
        this.itemList = new ArrayList<>();
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
    public void onBindViewHolder(@NonNull final ViewHolder btDevice, int i) {
        // setup the holder
        btDevice.item = this.itemList.get(i);
        btDevice.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // inform the listener of this click
                if (null != BtItemAdapter.this.listener) {
                    BtItemAdapter.this.listener.onBtListItemClicked(btDevice.item);
                }
            }
        });
        // and refresh the content
        refreshContent(btDevice);
    }

    public void clearList() {
        this.itemList.clear();
    }

    public boolean addDevice(BluetoothDevice device) {
        int i = this.itemList.indexOf(device);
        boolean isSet = false;
        if (i >= 0) {
            // already in the list, replace with the newest
            this.itemList.set(i, device);
            isSet = true;
        }
        else {
            // else add the view
            isSet = this.itemList.add(device);
            notifyDataSetChanged();
        }
        return isSet;
    }

    private void refreshContent(ViewHolder holder) {
        // set the data on the holder
        holder.title.setText(holder.item.getName());
        holder.subtitle.setText(holder.item.getAddress());
        // fade in the holder if selected
        if (InputBluetooth.GetMidiDeviceId(holder.item).equals(this.application.getSettings().getLastConnectedBtDevice())) {
            holder.selected.startAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in));
        }
        else {
            holder.selected.startAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade));
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
