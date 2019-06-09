package uk.co.darkerwaters.scorepal.players;

public class Team {

    private final Player[] players;
    private CourtPosition currentPosition;
    private CourtPosition initialPosition;

    private Player servingPlayer;

    public Team(Player[] players, CourtPosition initialPosition) {
        this.players = players;
        this.initialPosition = initialPosition;
        // ensure all our defaults are set here
        resetTeam();
    }

    public void resetTeam() {
        // reset all our data here to the starting data
        this.servingPlayer = null;
        // set their current position too
        this.currentPosition = this.initialPosition;
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

    public Player getServingPlayer() {
        if (null == this.servingPlayer && this.players.length > 0) {
            // no-one served yet, first to start is first in the list
            return this.players[0];
        }
        else {
            return this.servingPlayer;
        }
    }

    public Player getNextServer() {
        if (this.servingPlayer == null) {
            // no-one has served, return the first
            this.servingPlayer = getServingPlayer();
        }
        else {
            // return the next from the last server
            this.servingPlayer = getNextPlayer(this.servingPlayer);
        }
        return this.servingPlayer;
    }

    public Player getNextPlayer(Player player) {
        Player nextPlayer = null;
        boolean isPlayerFound = false;
        for (Player test : getPlayers()) {
            if (nextPlayer == null) {
                // just take the first for now
                nextPlayer = test;
            }
            else if (isPlayerFound) {
                // take this one instead, this is the one after
                nextPlayer = test;
                break;
            }
            // if this player is serving, we want the next one
            if (player == test) {
                isPlayerFound = true;
            }
        }
        return nextPlayer;
    }

    public boolean isPlayerInTeam(Player player) {
        boolean isPlayerFound = false;
        for (Player test : getPlayers()) {
            if (player == test) {
                isPlayerFound = true;
                break;
            }
        }
        return isPlayerFound;
    }
}
