package net.alloyggp.research;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public interface TurnTakingGameState {
    Move getMoveWithName(String name);
    int getRoleToMove();
    List<Move> getPossibleMoves();
    TurnTakingGameState getNextState(Move moveTaken);
    boolean isTerminal();
    double getOutcomeForRole(int role);

    default List<TurnTakingGameState> getPossibleNextStates() {
        return getPossibleMoves().stream()
                .map(move -> this.getNextState(move))
                .collect(Collectors.toList());
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
            public Move getMoveWithName(String name) {
                return delegate.getMoveWithName(name);
            }
        };
    }
}
