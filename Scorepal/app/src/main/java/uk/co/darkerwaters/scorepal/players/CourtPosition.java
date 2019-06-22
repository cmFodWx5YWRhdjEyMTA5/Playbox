package uk.co.darkerwaters.scorepal.players;

public enum CourtPosition {
    /*for now we are doing north and south, but technically we could have more?*/
    NORTH(),
    SOUTH();

    CourtPosition() {

    }

    public static CourtPosition GetDefault() {
        return NORTH;
    }

    public static String toString(CourtPosition position) {
        if (position == NORTH) {
            return "north";
        }
        else if (position == NORTH) {
            return "south";
        }
        else {
            return "none";
        }
    }

    public static CourtPosition fromString(String string) {
        switch (string) {
            case "north" :
                return NORTH;
            case "south" :
                return SOUTH;
            default:
                return null;
        }
    }

    public CourtPosition getNext() {
        int index = this.ordinal() + 1;
        if (index >= CourtPosition.values().length) {
            index = 0;
        }
        return CourtPosition.values()[index];
    }

}
