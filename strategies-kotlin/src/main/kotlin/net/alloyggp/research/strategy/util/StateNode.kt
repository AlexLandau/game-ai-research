package net.alloyggp.research.strategy.util

import net.alloyggp.research.Move
import net.alloyggp.research.strategy.MoveUtils
import java.util.Random

sealed class StateNode {
    data class TerminalNode(val outcomes: List<Double>): StateNode()
    data class NonTerminalNode(val childStats: Map<Move, SumAndCountArray>,
                               val children: MutableMap<Move, StateNode>,
                               val activeRole: Int): StateNode() {
        fun getBestMove(roleIndex: Int, random: Random): Move {
            return MoveUtils.pickThingWithHighestScore(childStats.entries,
                    { (_, sumAndCount) -> sumAndCount.getAverage(roleIndex) }, random).key
        }
    }
}
