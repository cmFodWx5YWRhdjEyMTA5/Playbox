package uk.co.darkerwaters.staveinvaders.notes;

public enum Clef {
    treble(0, "B4"),
    bass(1, "D3");

    public final int val;
    public final String middleNoteName;

    Clef(int val, String middleNoteName) {
        this.val = val;
        this.middleNoteName = middleNoteName;
    }

    public static Clef get(int val) {
        for (Clef stave : Clef.values()) {
            if (stave.val == val) {
                return stave;
            }
        }
        return null;
    }
}
