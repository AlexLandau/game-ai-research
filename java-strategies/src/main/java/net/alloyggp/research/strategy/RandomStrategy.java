package net.alloyggp.research.strategy;

import java.util.List;
import java.util.Random;

import net.alloyggp.research.GameState;
import net.alloyggp.research.Move;
import net.alloyggp.research.Player;
import net.alloyggp.research.Strategy;

/**
 * A strategy that selects its move uniformly at random from the moves available.
 */
public final class RandomStrategy implements Strategy {
    public final class RandomPlayer implements Player {
        private final int roleIndex;
        private final Random random;
        private GameState currentState;

        private RandomPlayer(int roleIndex, Random random) {
            this.roleIndex = roleIndex;
            this.random = random;
            this.currentState = null;
        }

        @Override
        public void initializeGameState(GameState initialState) {
            this.currentState = initialState;
        }

        @Override
        public void advanceGameState(List<Move> moves, GameState newGameState) {
            this.currentState = newGameState;
        }

        @Override
        public Move getMove() {
            if (this.currentState == null) {
                throw new IllegalStateException("The current state wasn't set!");
            }
            List<Move> possibleMoves = this.currentState.getPossibleMovesForRole(roleIndex);
            int chosenIndex = random.nextInt(possibleMoves.size());
            return possibleMoves.get(chosenIndex);
        }
    }

    @Override
    public Player getPlayer(int roleIndex, Random random) {
        return new RandomPlayer(roleIndex, random);
    }
}
