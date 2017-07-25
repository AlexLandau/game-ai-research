package net.alloyggp.research;

import java.util.List;

public interface Player {
    void initializeGameState(GameState initialState);
    void advanceGameState(List<Move> moves, GameState newGameState);
    Move getMove();
}
