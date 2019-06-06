package uk.co.darkerwaters.scorepal.matches;

public class Team {

    private final Player[] players;
    private CourtPosition currentPosition;

    public Team(Player[] players, CourtPosition position) {
        this.players = players;
        this.currentPosition = position;
    }

    public Player[] getPlayers() {
        return this.players;
    }

    public void setCourtPosition(CourtPosition position) {
        this.currentPosition = position;
    }

    public CourtPosition getCourtPosition() {
        return this.currentPosition;
    }
}
