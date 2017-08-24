package net.alloyggp.research.strategy

import net.alloyggp.research.MemorylessTurnTakingPlayer
import net.alloyggp.research.MemorylessTurnTakingStrategy
import net.alloyggp.research.Move
import net.alloyggp.research.Strategy
import net.alloyggp.research.StrategyProvider
import net.alloyggp.research.TurnTakingGameState
import net.alloyggp.research.strategy.parameter.StrategyParameterDescription
import net.alloyggp.research.strategy.parameter.StrategyParameters
import net.alloyggp.research.strategy.util.StateNode
import net.alloyggp.research.strategy.util.SumAndCountArray
import java.util.ArrayList
import java.util.LinkedHashMap
import java.util.Random

/**
 * A modification of UCT.
 *
 * "Wins first": In the selection phase, when comparing explored and unexplored nodes, this
 * variant prefers explored nodes with an average of 1 to any other nodes, including unexplored nodes.
 * If there are somehow multiple explored nodes with an average of 1, it chooses one with the highest
 * number of visits so far.
 *
 * This version runs a fixed number of iterations of the algorithm ("rollouts") per move.
 * It discards its game tree after each move and does not use transposition tables. It
 * adds at most one node to its game tree per rollout.
 *
 * Original publication:
 * Kocsis, Levente, and Csaba Szepesvári. "Bandit based monte-carlo planning." ECML. Vol. 6. 2006.
 * Available from:
 * http://ggp.stanford.edu/readings/uct.pdf
 * https://pdfs.semanticscholar.org/a441/488e8fe40370b7f5f99eb5a1659d93fb7091.pdf
 */
class MemorylessUCTWinsFirstStrategyProvider: StrategyProvider {
    companion object {
        @JvmStatic
        val C_P: StrategyParameterDescription<Double> = StrategyParameterDescription.forDouble("c_p").min(0.0).defaultValue(Math.sqrt(2.0)).build()
        @JvmStatic
        val ITERATION_COUNT: StrategyParameterDescription<Int> = StrategyParameterDescription.forInt("iterationCount").min(0).build()
    }

    override fun getName(): String {
        return "UCTWinsFirst"
    }

    override fun getParameters(): List<StrategyParameterDescription<*>> {
        return listOf(C_P, ITERATION_COUNT)
    }

    override fun get(parameters: StrategyParameters): Strategy {
        return Strategy.wrap(object: MemorylessTurnTakingStrategy {
            override fun getPlayer(roleIndex: Int, random: Random): MemorylessTurnTakingPlayer {
                val iterationsPerTurn = parameters[ITERATION_COUNT]
                val c_p = parameters[C_P]

                return MemorylessUCTWinsFirstPlayer(iterationsPerTurn, c_p, roleIndex, random)
            }
        })
    }
}

class MemorylessUCTWinsFirstPlayer(val iterationsPerTurn: Int, val c_p: Double, val roleIndex: Int, val random: Random): MemorylessTurnTakingPlayer {
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
        
        var hasExpanded = false

        loop@ while (true) {
            nodesVisited.add(curNode)
            val moveChosen = chooseMoveToExplore(curNode, random)
            movesChosen.add(moveChosen)

            curState = curState.getNextState(moveChosen)
            val nextNode: StateNode = if (curNode.children.containsKey(moveChosen)) {
                curNode.children[moveChosen]!!
            } else if (!hasExpanded) {
                val newNode = makeNode(curState)
                curNode.children[moveChosen] = newNode
                hasExpanded = true
                newNode
            } else {
                // Fast-forward to the end of the rollout
                getNodeAtEndOfRandomSelections(curState)
            }
            
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
    
    private fun getNodeAtEndOfRandomSelections(initialState: TurnTakingGameState): StateNode.TerminalNode {
        var curState = initialState
        while (!curState.isTerminal) {
            curState = curState.getRandomNextState(random)
        }
        return StateNode.TerminalNode(curState.getOutcomes())
    }

    private fun chooseMoveToExplore(curNode: StateNode.NonTerminalNode, random: Random): Move {
        // Start with exploring unvisited states
        val untriedMoves = ArrayList<Move>()
        val winningMovesWithHighestCount = ArrayList<Move>()
        var highestWinningMoveCount = 0L
        for ((move, stats) in curNode.childStats.entries) {
            if (stats.getCount() == 0L) {
                untriedMoves.add(move)
            } else if (stats.getAverage(curNode.activeRole) >= 1.0) {
                val moveCount = stats.getCount()
                if (moveCount > highestWinningMoveCount) {
                    winningMovesWithHighestCount.clear()
                    winningMovesWithHighestCount.add(move)
                    highestWinningMoveCount = moveCount
                } else if (moveCount == highestWinningMoveCount) {
                    winningMovesWithHighestCount.add(move)
                }
            }
        }
        if (winningMovesWithHighestCount.isNotEmpty()) {
            return MoveUtils.pickAtRandom(winningMovesWithHighestCount, random)
        }
        if (untriedMoves.isNotEmpty()) {
            return MoveUtils.pickAtRandom(untriedMoves, random)
        }

        val timesAnythingChosen = curNode.childStats.values.map(SumAndCountArray::getCount).sum().toDouble()

        return MoveUtils.pickThingWithHighestScore(curNode.childStats.keys, { move ->
            val stats = curNode.childStats[move]!!
            // Compute the UCT score of the move
            // A typical UCT formula is vi + c*sqrt(log np / ni). vi here is the average reward for that state. c is an arbitrary constant. np is the total number of times the state's parent was picked. ni is the number of times this particular state was picked.
            val averageScore = stats.getAverage(curNode.activeRole)
            val timesThisChosen = stats.getCount().toDouble()
            averageScore + c_p * Math.sqrt(Math.log(timesAnythingChosen) / timesThisChosen)
        }, random)
    }
}

private fun makeNode(currentState: TurnTakingGameState): StateNode {
    if (currentState.isTerminal) {
        return StateNode.TerminalNode(currentState.getOutcomes())
    }

    val initialChildStats = LinkedHashMap<Move, SumAndCountArray>()
    for (move in currentState.possibleMoves) {
        initialChildStats[move] = SumAndCountArray.create(currentState.numRoles)
    }
    return StateNode.NonTerminalNode(childStats = initialChildStats,
            children = LinkedHashMap<Move, StateNode>(),
            activeRole = currentState.roleToMove)
}
