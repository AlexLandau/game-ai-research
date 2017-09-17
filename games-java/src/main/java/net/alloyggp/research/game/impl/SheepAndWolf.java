package net.alloyggp.research.game.impl;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.alloyggp.research.GameState;
import net.alloyggp.research.GameTreeProvider;
import net.alloyggp.research.Move;

public class SheepAndWolf implements GameTreeProvider {
    @Override
    public GameState getInitialState() {
        return new SheepAndWolfGameState(true,
                ImmutableList.of(
                        new Position(1, 1),
                        new Position(3, 1),
                        new Position(5, 1),
                        new Position(7, 1)),
                new Position(4, 8));
    }

    private static class Position {
        public final int x;
        public final int y;
        private Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + y;
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Position other = (Position) obj;
            if (x != other.x)
                return false;
            if (y != other.y)
                return false;
            return true;
        }
        @Override
        public String toString() {
            return "Position [x=" + x + ", y=" + y + "]";
        }

        public @Nullable Position adjust(int xDiff, int yDiff) {
            int newX = x + xDiff;
            if (newX < 1 || newX > 8) {
                return null;
            }
            int newY = y + yDiff;
            if (newY < 1 || newY > 8) {
                return null;
            }
            return new Position(newX, newY);
        }
    }

    private static final Move NOOP_MOVE = new Move() {
        @Override
        public String getName() {
            return "noop";
        }

        @Override
        public int hashCode() {
            return 490583057;
        }

        @Override
        public boolean equals(Object other) {
            return other == this;
        }
    };

    private static class SheepAndWolfMove extends Move {
        private final Position from;
        private final Position to;
        private SheepAndWolfMove(Position from, Position to) {
            this.from = from;
            this.to = to;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((from == null) ? 0 : from.hashCode());
            result = prime * result + ((to == null) ? 0 : to.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SheepAndWolfMove other = (SheepAndWolfMove) obj;
            if (from == null) {
                if (other.from != null)
                    return false;
            } else if (!from.equals(other.from))
                return false;
            if (to == null) {
                if (other.to != null)
                    return false;
            } else if (!to.equals(other.to))
                return false;
            return true;
        }
        @Override
        public String toString() {
            return "SheepAndWolfMove [from=" + from + ", to=" + to + "]";
        }
        @Override
        public String getName() {
            return "( move c" + from.x + " c" + from.y + " c" + to.x + " c" + to.y + " )";
        }

    }

    private static class SheepAndWolfGameState implements GameState {
        private static final ImmutableList<Integer> DIFFS = ImmutableList.of(-1, 1);
        private static final ImmutableList<Move> NOOP_MOVE_LIST = ImmutableList.of(NOOP_MOVE);
        private final boolean wolfToMove;
        private final ImmutableList<Position> sheepPositions;
        private final Position wolfPosition;

        private SheepAndWolfGameState(boolean wolfToMove, ImmutableList<Position> sheepPositions,
                Position wolfPosition) {
            this.wolfToMove = wolfToMove;
            this.sheepPositions = sheepPositions;
            this.wolfPosition = wolfPosition;
        }

        @Override
        public Move getMoveWithName(int roleIndex, String name) {
            if (name.equals("noop")) {
                return NOOP_MOVE;
            }
            name.charAt(7);
            int fromX = Integer.parseInt(name.substring(8, 9));
            int fromY = Integer.parseInt(name.substring(11, 12));
            int toX = Integer.parseInt(name.substring(14, 15));
            int toY = Integer.parseInt(name.substring(17, 18));
            return new SheepAndWolfMove(new Position(fromX, fromY), new Position(toX, toY));
        }

        @Override
        public List<Move> getPossibleMovesForRole(int role) {
            if (wolfToMove && role == 0) {
                return getWolfMoves();
            } else if (!wolfToMove && role == 1) {
                return getSheepMoves();
            } else {
                return NOOP_MOVE_LIST;
            }
        }

        private List<Move> getSheepMoves() {
            List<Move> moves = Lists.newArrayListWithCapacity(8);
            for (int sheep = 0; sheep < 4; sheep++) {
                Position sheepPosition = sheepPositions.get(sheep);
                for (int xDiff : DIFFS) {
                    @Nullable Position newSheepPosition = sheepPosition.adjust(xDiff, 1);
                    if (newSheepPosition != null
                            && !occupied(newSheepPosition)) {
                        moves.add(new SheepAndWolfMove(sheepPosition, newSheepPosition));
                    }
                }
            }
            return moves;
        }

        private List<Move> getWolfMoves() {
            List<Move> moves = Lists.newArrayListWithCapacity(4);
            for (int xDiff : DIFFS) {
                for (int yDiff : DIFFS) {
                    @Nullable Position newWolfPosition = wolfPosition.adjust(xDiff, yDiff);
                    if (newWolfPosition != null
                            && !occupied(newWolfPosition)) {
                        moves.add(new SheepAndWolfMove(wolfPosition, newWolfPosition));
                    }
                }
            }
            return moves;
        }

        private boolean occupied(Position position) {
            if (position.equals(wolfPosition)) {
                return true;
            }
            for (Position sheepPosition : sheepPositions) {
                if (position.equals(sheepPosition)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public GameState getNextState(List<Move> movesTaken) {
            final ImmutableList<Position> newSheepPositions;
            final Position newWolfPosition;
            if (wolfToMove) {
                SheepAndWolfMove move = (SheepAndWolfMove) movesTaken.get(0);
                newSheepPositions = sheepPositions;
                newWolfPosition = move.to;
            } else {
                SheepAndWolfMove move = (SheepAndWolfMove) movesTaken.get(1);
                newWolfPosition = wolfPosition;
                ImmutableList.Builder<Position> newSheepPositionsBuilder = ImmutableList.builder();
                for (Position sheepPosition : sheepPositions) {
                    if (sheepPosition.equals(move.from)) {
                        newSheepPositionsBuilder.add(move.to);
                    } else {
                        newSheepPositionsBuilder.add(sheepPosition);
                    }
                }
                newSheepPositions = newSheepPositionsBuilder.build();
            }
            boolean newWolfToMove = !wolfToMove;
            return new SheepAndWolfGameState(newWolfToMove, newSheepPositions, newWolfPosition);
        }

        @Override
        public boolean isTerminal() {
            return wolfHasNoMoves() || isWolfPastSheep() || sheepHaveNoMoves();
        }

        private boolean sheepHaveNoMoves() {
            return getSheepMoves().isEmpty();
        }

        private boolean wolfHasNoMoves() {
            return getWolfMoves().isEmpty();
        }

        private boolean isWolfPastSheep() {
            int wolfY = wolfPosition.y;
            for (Position sheepPosition : sheepPositions) {
                if (sheepPosition.y < wolfY) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public double getOutcomeForRole(int role) {
            boolean won = (role == 1) == wolfHasNoMoves();
            if (won) {
                return 1.0;
            } else {
                return 0.0;
            }
        }

        @Override
        public String toString() {
            return "SheepAndWolfGameState [wolfToMove=" + wolfToMove + ", sheepPositions=" + sheepPositions
                    + ", wolfPosition=" + wolfPosition + "]";
        }

        @Override
        public int getNumRoles() {
            return 2;
        }
    }
}
