package net.alloyggp.research;

public abstract class Move {
    public abstract String getName();

    // Implementations must implement hashCode() and equals(), to ensure that
    // gamer implementations may use Moves as keys in Maps.
    @Override
    public abstract int hashCode();
    @Override
    public abstract boolean equals(Object other);
}
