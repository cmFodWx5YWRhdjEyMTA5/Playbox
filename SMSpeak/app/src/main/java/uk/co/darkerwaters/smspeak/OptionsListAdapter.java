package uk.co.darkerwaters.smspeak;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OptionsListAdapter extends RecyclerView.Adapter<OptionsListItemView> {

    private final ArrayList<OptionsListItem> mDataset;
    private final ArrayList<OptionsListItemView> mViewset;

    public interface OptionsListAdapterListener {
        public void onOptionsListChanged(OptionsListItem item);
    }

    public OptionsListAdapter(Context context, OptionsListAdapterListener listener) {
        mDataset = new ArrayList<OptionsListItem>();
        mViewset = new ArrayList<OptionsListItemView>();
        final State state = State.GetInstance(context);
        // add the options
        addItem(new OptionsListItem(listener, R.string.everything, R.drawable.ic_baseline_access_time_24px) {
            @Override
            boolean isActive() {
                return false;
            }
            @Override
            boolean isSelected() {
                return state.isTalkAlways();
            }
            @Override
            void setSelected(boolean isSelected) {
                state.setIsTalkAlways(isSelected);
                // inform the listener of this change
                listener.onOptionsListChanged(this);
            }
        });
        addItem(new OptionsListItem(listener, R.string.headphones, R.drawable.ic_baseline_headset_24px) {
            @Override
            boolean isActive() {
                return false;
            }
            @Override
            boolean isSelected() {
                return state.isTalkHeadphones();
            }
            @Override
            void setSelected(boolean isSelected) {
                state.setIsTalkHeadphones(isSelected);
                // inform the listener of this change
                listener.onOptionsListChanged(this);
            }
            @Override
            boolean isHeadphones() {
                return true;
            }
        });
        addItem(new OptionsListItem(listener, R.string.headset, R.drawable.ic_baseline_headset_mic_24px) {
            @Override
            boolean isActive() {
                return false;
            }
            @Override
            boolean isSelected() {
                return state.isTalkHeadset();
            }
            @Override
            void setSelected(boolean isSelected) {
                state.setIsTalkHeadset(isSelected);
                // inform the listener of this change
                listener.onOptionsListChanged(this);
            }
            @Override
            boolean isHeadphones() {
                return true;
            }
        });
        addItem(new OptionsListItem(listener, R.string.bluetooth, R.drawable.ic_baseline_bluetooth_audio_24px) {
            @Override
            boolean isActive() {
                return false;
            }
            @Override
            boolean isSelected() {
                return state.isTalkBluetooth();
            }
            @Override
            void setSelected(final boolean isSelected) {
                state.setIsTalkBluetooth(isSelected);
                // also turn on / off all the children
                for (OptionsListItemView view : mViewset) {
                    if (view.getData() != this && view.getData().isBluetooth()) {
                        // set this to match us
                        state.setIsTalkBtDevice(view.getData().name, isSelected);
                        view.updateState();
                        // and update the state
                        listener.onOptionsListChanged(view.getData());
                    }
                }
                // inform the listener of this change
                listener.onOptionsListChanged(this);
            }
            @Override
            boolean isBluetooth() {
                return true;
            }
        });
        // and all the registered bluetooth devices we can find on this phone
        addBtDevices(context, listener);
    }

    private void addBtDevices(final Context context, OptionsListAdapterListener listener) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (null != bluetoothAdapter) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            final State state = State.GetInstance(context);
            for (BluetoothDevice bt : pairedDevices) {
                // for each device, add to the list they can choose from
                final String btName = bt.getName();
                addItem(new OptionsListItem(listener, btName, R.drawable.ic_baseline_bluetooth_24px) {
                    @Override
                    boolean isActive() {
                        return isSelected() && state.getConnectedBtDevice(context).equals(btName);
                    }

                    @Override
                    boolean isSelected() {
                        return state.isTalkBtDevice(btName);
                    }

                    @Override
                    void setSelected(boolean isSelected) {
                        state.setIsTalkBtDevice(btName, isSelected);
                        // inform the listener of this change
                        listener.onOptionsListChanged(this);
                    }

                    @Override
                    boolean isBluetooth() {
                        return true;
                    }
                });
            }
        }
    }

    private void addItem(OptionsListItem item) {
        mDataset.add(item);
    }

    public OptionsListItemView[] getDataset() {
        return mViewset.toArray(new OptionsListItemView[mViewset.size()]);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public OptionsListItemView onCreateViewHolder(ViewGroup parent, int viewType) {
        // create the holder here
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item, parent, false);
        OptionsListItemView view = new OptionsListItemView(layout);
        mViewset.add(view);
        return view;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(OptionsListItemView holder, int position) {
        holder.bindViewData(position, mDataset.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
