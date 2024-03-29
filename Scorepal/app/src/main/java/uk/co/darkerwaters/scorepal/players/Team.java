package uk.co.darkerwaters.scorepal.players;

import uk.co.darkerwaters.scorepal.application.Log;

public class Team {

    private final Player[] players;
    private CourtPosition currentPosition;
    private CourtPosition initialPosition;

    private String teamName;

    private Player servingPlayer;

    public Team(Player[] players, CourtPosition initialPosition) {
        this.players = players;
        this.initialPosition = initialPosition;
        this.teamName = "";
        // ensure all our defaults are set here
        resetTeam();
    }

    public void resetTeam() {
        // reset all our data here to the starting data
        this.servingPlayer = null;
        // set their current position too
        this.currentPosition = this.initialPosition;
        //this.teamName -- leave the name alone, this is fine to change (doesn't effect the match)
    }

    public String toPlayerString(Player player) {
        for (int i = 0; i < this.players.length; ++i) {
            if (this.players[i] == player) {
                // this is it
                return "Player" + (i + 1);
            }
        }
        // not found
        return "none";
    }

    public Player fromPlayerString(String string) {
        Player player = null;
        if (string.startsWith("Player")) {
            // remove this
            String numberString = string.replace("Player", "");
            try {
                int playerIndex = Integer.parseInt(numberString);
                if (playerIndex >= 0 && playerIndex < this.players.length) {
                    player = this.players[playerIndex];
                }
            }
            catch (NumberFormatException e) {
                // oops
                Log.error("Player string '" + string + "' invalid", e);
            }
        }
        return player;
    }

    public void setTeamName(String name) {
        this.teamName = name;
    }

    public String getTeamName() {
        return this.teamName;
    }

    public Player[] getPlayers() {
        return this.players;
    }

    public CourtPosition getInitialPosition() { return this.initialPosition; }

    public void setInitialCourtPosition(CourtPosition position) {
        this.initialPosition = position;
    }

    public void setCourtPosition(CourtPosition position) {
        this.currentPosition = position;
    }

    public CourtPosition getCourtPosition() {
        return this.currentPosition;
    }

    public boolean isServingPlayerSet() {
        return null != this.servingPlayer;
    }

    public void setServingPlayer(Player startingServer) {
        // override the member to set the player to start serving
        this.servingPlayer = servingPlayer;
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
