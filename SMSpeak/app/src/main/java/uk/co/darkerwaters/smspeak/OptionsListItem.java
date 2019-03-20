package uk.co.darkerwaters.smspeak;

import android.graphics.Path;

public abstract class OptionsListItem {
    final String name;
    final int nameId;
    final int iconId;
    final OptionsListAdapter.OptionsListAdapterListener listener;

    OptionsListItem(OptionsListAdapter.OptionsListAdapterListener listener, String name, int iconId) {
        this.listener = listener;
        this.name = name;
        this.nameId = 0;
        this.iconId = iconId;
    }

    OptionsListItem(OptionsListAdapter.OptionsListAdapterListener listener, int name, int iconId) {
        this.listener = listener;
        this.nameId = name;
        this.name = null;
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

    boolean isBluetooth() {
        return false;
    }
}
