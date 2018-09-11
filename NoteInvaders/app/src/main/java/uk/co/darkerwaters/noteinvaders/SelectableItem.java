package uk.co.darkerwaters.noteinvaders;

public abstract class SelectableItem {
    private String name;
    private String subtitle;
    private int thumbnail;

    public SelectableItem(String name, int thumbnail) {
        this.name = name;
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
    }

    public abstract String getSubtitle();

    public void setName(String name) {
        this.name = name;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }
}
