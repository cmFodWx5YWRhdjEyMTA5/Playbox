package uk.co.darkerwaters.smspeak;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class OptionsListAdapter extends RecyclerView.Adapter<OptionsListItemView> {

    private final ArrayList<OptionsListItem> mDataset;

    public interface OptionsListAdapterListener {
        public void onOptionsListChanged(OptionsListItem item);
    }

    public OptionsListAdapter(Context context, OptionsListAdapterListener listener) {
        mDataset = new ArrayList<OptionsListItem>();
        final State state = State.GetInstance(context);
        // add the options
        Resources resources = context.getResources();
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
            void setSelected(boolean isSelected) {
                state.setIsTalkBluetooth(isSelected);
                // inform the listener of this change
                listener.onOptionsListChanged(this);
            }
        });
        // and all the registered bluetooth devices we can find on this phone
    }

    private void addItem(OptionsListItem item) {
        mDataset.add(item);
    }

    public OptionsListItemView[] getDataset() {
        return mDataset.toArray(new OptionsListItemView[mDataset.size()]);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public OptionsListItemView onCreateViewHolder(ViewGroup parent, int viewType) {
        // create the holder here
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item, parent, false);
        return new OptionsListItemView(layout);
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
