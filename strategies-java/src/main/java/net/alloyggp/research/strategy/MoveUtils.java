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

    public static <T> T pickThingWithHighestScore(Iterable<T> possibilities,
                                                  Function<T, Double> scoreFunction,
                                                  Random random) {
        List<T> thingsWithHighestScore = new ArrayList<>();
        double highestScore = Double.NEGATIVE_INFINITY;

        for (T thing : possibilities) {
            double score = scoreFunction.apply(thing);
            if (!(score > Double.NEGATIVE_INFINITY)) {
                throw new IllegalStateException("Score was unexpectedly " + score);
            }

            if (score > highestScore) {
                thingsWithHighestScore.clear();
                thingsWithHighestScore.add(thing);
                highestScore = score;
            } else if (score == highestScore) {
                thingsWithHighestScore.add(thing);
            }
        }
        if (thingsWithHighestScore.size() == 1) {
            return thingsWithHighestScore.get(0);
        }
        return pickAtRandom(thingsWithHighestScore, random);
    }

    public static <T> T pickAtRandom(List<T> list, Random random) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Can only pick at random from a non-empty list");
        }
        int indexToReturn = random.nextInt(list.size());
        return list.get(indexToReturn);
    }

    public static Move pickMoveWithHighestScore(TurnTakingGameState currentState,
            Function<TurnTakingGameState, Double> scoreFunction, Random random) {
        return pickThingWithHighestScore(currentState.getPossibleMoves(), move ->
            scoreFunction.apply(currentState.getNextState(move)), random);
    }
}
