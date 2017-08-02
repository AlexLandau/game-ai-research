package net.alloyggp.research.strategy

import net.alloyggp.research.MemorylessTurnTakingPlayer
import net.alloyggp.research.MemorylessTurnTakingStrategy
import net.alloyggp.research.Move
import net.alloyggp.research.Strategy
import net.alloyggp.research.StrategyProvider
import net.alloyggp.research.TurnTakingGameState
import net.alloyggp.research.strategy.parameter.StrategyParameterDescription
import net.alloyggp.research.strategy.parameter.StrategyParameters
import java.util.ArrayList
import java.util.LinkedHashMap
import java.util.Random

/**
 * An implementation of the well-known UCT algorithm.
 *
 * This version runs a fixed number of iterations of the algorithm ("rollouts") per move.
 * It discards its game tree after each move and does not use transposition tables. It
 * adds every node visited in a rollout to its game tree.
 *
 * Original publication:
 * Kocsis, Levente, and Csaba Szepesvári. "Bandit based monte-carlo planning." ECML. Vol. 6. 2006.
 * Available from:
 * http://ggp.stanford.edu/readings/uct.pdf
 * https://pdfs.semanticscholar.org/a441/488e8fe40370b7f5f99eb5a1659d93fb7091.pdf
 */
class MemorylessUCTStrategyProvider: StrategyProvider {
    companion object {
        @JvmStatic
        val C_P: StrategyParameterDescription<Double> = StrategyParameterDescription.forDouble("c_p").min(0.0).defaultValue(Math.sqrt(2.0)).build()
        @JvmStatic
        val ITERATION_COUNT: StrategyParameterDescription<Int> = StrategyParameterDescription.forInt("iterationCount").min(0).build()
    }

    override fun getName(): String {
        return "UCT"
    }

    override fun getParameters(): List<StrategyParameterDescription<*>> {
        return listOf(C_P, ITERATION_COUNT)
    }

    override fun get(parameters: StrategyParameters): Strategy {
        return Strategy.wrap(object: MemorylessTurnTakingStrategy {
            override fun getPlayer(roleIndex: Int, random: Random): MemorylessTurnTakingPlayer {
                val iterationsPerTurn = parameters[ITERATION_COUNT]
                val c_p = parameters[C_P]

                return MemorylessUCTPlayer(iterationsPerTurn, c_p, roleIndex, random)
            }
        })
    }
}

class MemorylessUCTPlayer(val iterationsPerTurn: Int, val c_p: Double, val roleIndex: Int, val random: Random): MemorylessTurnTakingPlayer {
    override fun getMove(currentState: TurnTakingGameState): Move {
        val rootNode = makeNode(currentState) as? StateNode.NonTerminalNode ?: error("Did not expect to ask for moves to make in a terminal state")
        for (i in 1..iterationsPerTurn) {
            doRollout(rootNode, currentState)
        }
        return rootNode.getBestMove(roleIndex, random)
    }

    private fun doRollout(rootNode: StateNode.NonTerminalNode, rootState: TurnTakingGameState) {
        var curNode = rootNode
        var curState = rootState

        val nodesVisited = ArrayList<StateNode.NonTerminalNode>()
        val movesChosen = ArrayList<Move>()

        loop@ while (true) {
            nodesVisited.add(curNode)
            val moveChosen = chooseMoveToExplore(curNode, random)
            movesChosen.add(moveChosen)

            curState = curState.getNextState(moveChosen)
            val nextNode: StateNode = curNode.children[moveChosen] ?: {
                val newNode = makeNode(curState)
                curNode.children[moveChosen] = newNode
                newNode
            }() // silly Kotlin
            when (nextNode) {
                is StateNode.NonTerminalNode -> {
                    curNode = nextNode
                }
                is StateNode.TerminalNode -> {
                    // TODO: Clean up here
                    val outcomes = nextNode.outcomes
                    for ((nodeVisited, moveChosen) in nodesVisited.zip(movesChosen)) {
                        nodeVisited.childStats[moveChosen]!!.addValues(outcomes)
                    }
                    break@loop
                }
            }
        }
    }

    private fun chooseMoveToExplore(curNode: StateNode.NonTerminalNode, random: Random): Move {
        // Start with exploring unvisited states
        val untriedMoves = ArrayList<Move>()
        for ((move, stats) in curNode.childStats.entries) {
            if (stats.getCount() == 0L) {
                untriedMoves.add(move)
            }
        }
        if (untriedMoves.isNotEmpty()) {
            return MoveUtils.pickAtRandom(untriedMoves, random)
        }

        val timesAnythingChosen = curNode.childStats.values.map(SumAndCountArray::getCount).sum()

        return MoveUtils.pickThingWithHighestScore(curNode.childStats.keys, { move ->
            val stats = curNode.childStats[move]!!
            // Compute the UCT score of the move
            // A typical UCT formula is vi + c*sqrt(log np / ni). vi here is the average reward for that state. c is an arbitrary constant. np is the total number of times the state's parent was picked. ni is the number of times this particular state was picked.
            val averageScore = stats.getAverage(curNode.activeRole)
            val timesThisChosen = stats.getCount().toDouble()
            averageScore + c_p * Math.sqrt(Math.log(timesThisChosen) / timesAnythingChosen)
        }, random)
    }
}

private fun makeNode(currentState: TurnTakingGameState): StateNode {
    if (currentState.isTerminal) {
        return StateNode.TerminalNode(currentState.getOutcomes(2))
    }

    val initialChildStats = LinkedHashMap<Move, SumAndCountArray>()
    for (move in currentState.possibleMoves) {
        initialChildStats[move] = SumAndCountArray.create(2)
    }
    return StateNode.NonTerminalNode(childStats = initialChildStats,
            children = LinkedHashMap<Move, StateNode>(),
            activeRole = currentState.roleToMove)
}

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

// TODO: Put in utility code file
class SumAndCountArray(private val sums: MutableList<Double>, private var count: Long) {
    companion object {
        fun create(size: Int): SumAndCountArray {
            val sums = ArrayList<Double>()
            for (i in 1..size) {
                sums.add(0.0)
            }
            sums.trimToSize()
            return SumAndCountArray(sums, 0L)
        }
    }

    fun getAverage(roleIndex: Int): Double {
        val sum = sums[roleIndex]
        if (count == 0L) {
            // TODO: Consider making a parameter?
            return -1.0
        }
        return sum / count
    }

    fun getCount(): Long {
        return count
    }

    fun addValues(outcomes: List<Double>) {
        if (outcomes.size != sums.size) {
            error("These should be the same size")
        }
        outcomes.forEachIndexed { index, value ->
            sums[index] += value
        }
        count += 1L
    }
}
