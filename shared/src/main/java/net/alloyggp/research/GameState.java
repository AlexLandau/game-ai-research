package net.alloyggp.research;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// TODO: Consider parameterizing in terms of Move?
// TODO: Add something in the framework to allow transposition tables
public interface GameState {
    Move getMoveWithName(int roleIndex, String name);
    List<Move> getPossibleMovesForRole(int role);
    GameState getNextState(List<Move> movesTaken);
    boolean isTerminal();
    double getOutcomeForRole(int role);
    int getNumRoles();

    default List<Double> getOutcomes() {
        return IntStream.range(0, getNumRoles())
                .mapToDouble(this::getOutcomeForRole)
                .boxed()
                .collect(Collectors.toList());
    }
}
