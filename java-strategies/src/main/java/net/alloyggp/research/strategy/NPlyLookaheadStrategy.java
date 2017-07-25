package net.alloyggp.research.strategy;

import java.util.Random;

import net.alloyggp.research.MemorylessTurnTakingPlayer;
import net.alloyggp.research.MemorylessTurnTakingStrategy;
import net.alloyggp.research.Move;
import net.alloyggp.research.TurnTakingGameState;

public class NPlyLookaheadStrategy implements MemorylessTurnTakingStrategy {
    /**
     * A 1-ply lookahead means we look at only the immediate consequences of each move, in the
     * event it ends the game on the following turn. A 2-ply lookahead means we look two game
     * state transitions ahead, and so on.
     */
    private final int pliesToLookAhead;
    /**
     * The score we assign to non-terminal game states at the horizon of our search.
     */
    private final double defaultOutcome;

    // TODO: Rearrange classes, return to private visibility
    public NPlyLookaheadStrategy(int pliesToLookAhead, double defaultOutcome) {
        if (pliesToLookAhead < 1) {
            throw new IllegalArgumentException("The number of plies to look ahead must be at least 1, but was " + pliesToLookAhead);
        }
        this.pliesToLookAhead = pliesToLookAhead;
        this.defaultOutcome = defaultOutcome;
    }

    public class NPlyLookaheadPlayer implements MemorylessTurnTakingPlayer {
        private final int myRole;
        private final Random random;

        private NPlyLookaheadPlayer(int myRole, Random random) {
            this.myRole = myRole;
            this.random = random;
        }

        @Override
        public Move getMove(TurnTakingGameState currentState) {
            return MoveUtils.pickMoveWithHighestScore(currentState,
                    state -> getScore(state, pliesToLookAhead - 1),
                    random);
        }

        private double getScore(TurnTakingGameState state, int pliesLeft) {
            if (state.isTerminal()) {
                return state.getOutcomeForRole(myRole);
            }
            if (pliesLeft == 0) {
                return defaultOutcome;
            }

            // Assume zero-sumness for minimax
            if (state.getRoleToMove() == myRole) {
                return state.getPossibleNextStates().stream()
                        .mapToDouble(nextState -> getScore(nextState, pliesLeft - 1))
                        .max().getAsDouble();
            } else {
                return state.getPossibleNextStates().stream()
                        .mapToDouble(nextState -> getScore(nextState, pliesLeft - 1))
                        .min().getAsDouble();
            }
        }
    }

    @Override
    public MemorylessTurnTakingPlayer getPlayer(int roleIndex, Random random) {
        return new NPlyLookaheadPlayer(roleIndex, random);
    }
}