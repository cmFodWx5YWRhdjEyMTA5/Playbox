package uk.co.darkerwaters.smspeak;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.w3c.dom.Text;

public class OptionsListItemView extends RecyclerView.ViewHolder {

    // each data item represents an option
    private final TextView textView;
    private final ImageView stateView;
    private final ImageView iconView;
    private final Switch stateSwitch;
    private final View parent;

    private OptionsListItem data = null;

    public OptionsListItemView(View view) {
        super(view);
        this.parent = view;
        this.textView = (TextView) view.findViewById(R.id.list_item_text);
        this.stateView = (ImageView) view.findViewById(R.id.list_item_check_image);
        this.iconView = (ImageView) view.findViewById(R.id.list_item_icon);
        this.stateSwitch = (Switch) view.findViewById(R.id.list_item_switch);
    }

    public void bindViewData(int position, final OptionsListItem data) {
        // - get element from your dataset at this position
        this.data = data;
        // - replace the contents of the view with that element
        Resources resources = this.parent.getContext().getResources();
        if (data.name == null) {
            this.textView.setText(resources.getString(data.nameId));
        }
        else {
            this.textView.setText(data.name);
        }
        this.iconView.setImageResource(data.iconId);
        // listen for changes in state
        this.stateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // the state has changed, change the state
                data.setSelected(b);
                // and update our state to match
                updateState();
            }
        });
        // update the state to what the data says it is
        updateState();
    }

    public OptionsListItem getData() {
        return this.data;
    }

    public void updateState() {
        // show the state icon
        if (this.data.isActive()) {
            this.stateView.setVisibility(View.VISIBLE);
        }
        else {
            this.stateView.setVisibility(View.INVISIBLE);
        }
        // and set the switch status
        this.stateSwitch.setChecked(this.data.isSelected());
    }
}
