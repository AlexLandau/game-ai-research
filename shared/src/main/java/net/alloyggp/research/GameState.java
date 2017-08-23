package net.alloyggp.research;

import java.util.List;

import com.google.common.collect.ImmutableList;

// TODO: Consider parameterizing in terms of Move?
// TODO: Add something in the framework to allow transposition tables
public interface GameState {
    Move getMoveWithName(int roleIndex, String name);
    List<Move> getPossibleMovesForRole(int role);
    GameState getNextState(List<Move> movesTaken);
    boolean isTerminal();
    double getOutcomeForRole(int role);

    default ImmutableList<Double> getOutcomes() {
        return ImmutableList.of(getOutcomeForRole(0), getOutcomeForRole(1));
    }
}
