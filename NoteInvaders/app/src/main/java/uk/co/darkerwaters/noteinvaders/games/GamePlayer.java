package uk.co.darkerwaters.noteinvaders.games;

import uk.co.darkerwaters.noteinvaders.views.MusicView;
import uk.co.darkerwaters.noteinvaders.state.Game;

public abstract class GamePlayer {

    public abstract void addNewNotes(MusicView musicView, Game game);
}
