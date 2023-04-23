package nl.applesaph.battleships.game;

public enum GameState {
    // Waiting in purgatory, why is no one online?
    LOBBY,
    // Running away or running game
    RUNNING,
    // When you're dead, you're [BLANK] :eyes:
    FINISHED
}
