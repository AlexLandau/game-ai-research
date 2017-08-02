
package net.alloyggp.research;

import java.util.List;
import java.util.Random;

public interface Strategy {
    Player getPlayer(int roleIndex, Random random);

    public static Strategy wrap(MemorylessTurnTakingStrategy memorylessTurnTakingStrategy) {
        return new Strategy() {
            @Override
            public Player getPlayer(int roleIndex, Random random) {
                MemorylessTurnTakingPlayer delegate = memorylessTurnTakingStrategy.getPlayer(roleIndex, random);
                return new Player() {
                    private GameState currentGameState;
                    @Override
                    public void initializeGameState(GameState initialState) {
                        this.currentGameState = initialState;
                    }

                    @Override
                    public void advanceGameState(List<Move> moves,
                            GameState newGameState) {
                        this.currentGameState = newGameState;
                    }

                    @Override
                    public Move getMove() {
                        List<Move> ourPossibleMoves = currentGameState.getPossibleMovesForRole(roleIndex);
                        if (ourPossibleMoves.size() > 1) {
                            return delegate.getMove(TurnTakingGameState.wrap(this.currentGameState));
                        } else {
                            return ourPossibleMoves.get(0);
                        }
                    }
                };
            }
        };
    }
}
