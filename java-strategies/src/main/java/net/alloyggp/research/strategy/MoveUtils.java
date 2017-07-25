package net.alloyggp.research.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import net.alloyggp.research.Move;
import net.alloyggp.research.TurnTakingGameState;

public class MoveUtils {
    private MoveUtils() {
        // Not instantiable
    }

    public static Move pickMoveWithHighestScore(TurnTakingGameState currentState,
            Function<TurnTakingGameState, Double> scoreFunction, Random random) {
        List<Move> movesWithHighestScore = new ArrayList<>();
        double highestScore = Double.NEGATIVE_INFINITY;

        for (Move move : currentState.getPossibleMoves()) {
            double score = scoreFunction.apply(currentState.getNextState(move));
            if (!(score > Double.NEGATIVE_INFINITY)) {
                throw new IllegalStateException("Score was unexpectedly " + score);
            }

            if (score > highestScore) {
                movesWithHighestScore.clear();
                movesWithHighestScore.add(move);
                highestScore = score;
            } else if (score == highestScore) {
                movesWithHighestScore.add(move);
            }
        }
        if (movesWithHighestScore.size() == 1) {
            return movesWithHighestScore.get(0);
        }
        int indexToReturn = random.nextInt(movesWithHighestScore.size());
        return movesWithHighestScore.get(indexToReturn);
    }
}
