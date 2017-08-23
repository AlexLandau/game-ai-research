package net.alloyggp.research;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public interface TurnTakingGameState {
    // TODO: Passing this in here is redundant
    Move getMoveWithName(int role, String name);
    int getRoleToMove();
    List<Move> getPossibleMoves();
    TurnTakingGameState getNextState(Move moveTaken);
    boolean isTerminal();
    double getOutcomeForRole(int role);
    int getNumRoles();

    default List<TurnTakingGameState> getPossibleNextStates() {
        return getPossibleMoves().stream()
                .map(this::getNextState)
                .collect(Collectors.toList());
    }

    default List<Double> getOutcomes() {
        return IntStream.range(0, getNumRoles())
                .mapToDouble(this::getOutcomeForRole)
                .boxed()
                .collect(Collectors.toList());
    }

    default TurnTakingGameState getRandomNextState(Random random) {
        List<Move> possibleMoves = getPossibleMoves();
        int index = random.nextInt(possibleMoves.size());
        Move chosenMove = possibleMoves.get(index);
        return getNextState(chosenMove);
    }

    public static TurnTakingGameState wrap(GameState delegate) {
        return new TurnTakingGameState() {
            @Override
            public boolean isTerminal() {
                return delegate.isTerminal();
            }

            private transient int cachedRoleToMove = -1;
            @Override
            public int getRoleToMove() {
                if (this.cachedRoleToMove == -1) {
                    int roleToMove = -1;
                    for (int roleIndex : new int[] {0, 1}) {
                        List<Move> possibleMoves = delegate.getPossibleMovesForRole(roleIndex);
                        if (possibleMoves.size() > 1) {
                            if (roleToMove != -1) {
                                throw new RuntimeException("Multiple roles have legal moves, but we are using a turn-taking strategy!");
                            }
                            roleToMove = roleIndex;
                        }
                    }
                    if (roleToMove == -1) {
                        // By convention, if no one has a choice of moves, say it's the first player
                        roleToMove = 0;
                    }
                    this.cachedRoleToMove = roleToMove;
                }
                return this.cachedRoleToMove;
            }

            @Override
            public List<Move> getPossibleMoves() {
                int roleToMove = getRoleToMove();
                return delegate.getPossibleMovesForRole(roleToMove);
            }

            @Override
            public double getOutcomeForRole(int role) {
                return delegate.getOutcomeForRole(role);
            }

            @Override
            public TurnTakingGameState getNextState(Move moveTaken) {
                int roleToMove = getRoleToMove();
                List<Move> movesTaken = Lists.newArrayList();

                for (int roleIndex : new int[] {0, 1}) {
                    if (roleIndex == roleToMove) {
                        movesTaken.add(moveTaken);
                    } else {
                        movesTaken.add(Iterables.getOnlyElement(
                                delegate.getPossibleMovesForRole(roleIndex)));
                    }
                }

                return TurnTakingGameState.wrap(delegate.getNextState(movesTaken));
            }

            @Override
            public Move getMoveWithName(int role, String name) {
                return delegate.getMoveWithName(role, name);
            }

            @Override
            public int getNumRoles() {
                return delegate.getNumRoles();
            }
        };
    }


}
