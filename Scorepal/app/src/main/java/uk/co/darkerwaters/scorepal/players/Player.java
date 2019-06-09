package uk.co.darkerwaters.scorepal.players;

public class Player {

    private boolean isServing;
    private String playerName;

    public Player(String playerName) {
        this.playerName = playerName;
        // ensure all our defaults are set here
        resetPlayer();
    }

    public void resetPlayer() {
        // reset our data to the defaults here
        this.isServing = false;
    }

    public void setPlayerName(String name) {
        this.playerName = name;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setIsServing(boolean isServing) {
        this.isServing = isServing;
    }

    public boolean getIsServing() {
        return this.isServing;
    }
}
