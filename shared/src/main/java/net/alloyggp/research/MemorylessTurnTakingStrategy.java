package net.alloyggp.research;

import java.util.Random;

public interface MemorylessTurnTakingStrategy {
    MemorylessTurnTakingPlayer getPlayer(int roleIndex, Random random);
}
