package uk.co.darkerwaters.scorepal.score;

enum Sport {
    TENNIS;

    @Override
    public String toString() {
        switch (this) {
            case TENNIS:
                return "Tennis";
            default:
                return "none";
        }
    }
    public static Sport fromString(String string) {
        switch (string) {
            case "Tennis":
                return TENNIS;
            default:
                return null;
        }
    }
}
