package uk.co.darkerwaters.scorepal.matches;

public enum CourtPosition {
    /*for now we are doing north and south, but technically we could have more?*/
    NORTH(),
    SOUTH();

    CourtPosition() {

    }

    public static CourtPosition GetDefault() {
        return NORTH;
    }

    public CourtPosition getNext() {
        int index = this.ordinal() + 1;
        if (index >= CourtPosition.values().length) {
            index = 0;
        }
        return CourtPosition.values()[index];
    }

}
