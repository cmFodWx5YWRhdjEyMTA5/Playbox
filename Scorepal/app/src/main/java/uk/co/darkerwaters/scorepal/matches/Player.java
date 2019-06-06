package uk.co.darkerwaters.scorepal.matches;

public class Player {

    private boolean isServing;
    private String playerName;

    public Player(String playerName) {
        this.playerName = playerName;
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
