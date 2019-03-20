package uk.co.darkerwaters.smspeak;

import android.graphics.Path;

public abstract class OptionsListItem {
    final int name;
    final int iconId;
    final OptionsListAdapter.OptionsListAdapterListener listener;

    OptionsListItem(OptionsListAdapter.OptionsListAdapterListener listener, int name, int iconId) {
        this.listener = listener;
        this.name = name;
        this.iconId = iconId;
    }

    boolean isActive() {
        return false;
    }

    boolean isSelected() {
        return false;
    }

    void setSelected(boolean isSelected) {}

    boolean isHeadphones() {
        return false;
    }
}
