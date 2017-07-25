package net.alloyggp.research;

import java.util.Arrays;
import java.util.List;

// TODO: Consider parameterizing in terms of Move?
// TODO: Add something in the framework to allow transposition tables
public interface GameState {
    Move getMoveWithName(String name);
    List<Move> getPossibleMovesForRole(int role);
    GameState getNextState(List<Move> movesTaken);
    boolean isTerminal();
    double getOutcomeForRole(int role);

    default List<Double> getOutcomes() {
        return Arrays.asList(getOutcomeForRole(0), getOutcomeForRole(1));
    }
}
