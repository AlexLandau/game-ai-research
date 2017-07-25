package net.alloyggp.research.applications;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;

import com.google.common.collect.Lists;

import net.alloyggp.research.GameState;
import net.alloyggp.research.ImmutableMatchResult;
import net.alloyggp.research.MatchResult;
import net.alloyggp.research.MatchSpec;
import net.alloyggp.research.Move;
import net.alloyggp.research.Player;
import net.alloyggp.research.Strategy;

public class MatchRunner {
    private final MatchSpec matchSpec;

    private MatchRunner(MatchSpec matchSpec) {
        this.matchSpec = matchSpec;
    }

    public static void run(MatchSpec matchSpec) {
        MatchRunner runner = new MatchRunner(matchSpec);
        MatchResult result = runner.run();
        // TODO: Do something with the result
        System.out.println("Result: " + result);
    }

    private MatchResult run() {
        int[] seed = getSeed();
        Random random = new RandomAdaptor(new MersenneTwister(seed));
        Game game = getGame();
        List<Player> players = getPlayers(random);

        List<List<String>> moveHistory = Lists.newArrayList();

        long startTime = System.currentTimeMillis();
        GameState state = game.getInitialState();
        for (Player player : players) {
            player.initializeGameState(state);
        }
        while (!state.isTerminal()) {
            List<Move> moves = Lists.newArrayList();
            for (Player player : players) {
                moves.add(player.getMove());
            }
            moveHistory.add(moves.stream().map(Move::getName).collect(Collectors.toList()));
            state = state.getNextState(moves);
            for (Player player : players) {
                player.advanceGameState(moves, state);
            }
        }
        List<Double> outcome = state.getOutcomes();
        long millisecondsToRun = System.currentTimeMillis() - startTime;

        return ImmutableMatchResult.builder()
            .spec(matchSpec)
            .seed(seed)
            .addAllMoveHistory(moveHistory)
            .addAllOutcomes(outcome)
            .millisecondsToRun(millisecondsToRun)
            .build();
    }

    private int[] getSeed() {
        return ThreadLocalRandom.current().ints(4).toArray();
    }

    private List<Player> getPlayers(Random random) {
        List<Player> players = Lists.newArrayList();
        int roleIndex = 0;
        for (String strategyId : matchSpec.getStrategyIds()) {
            Strategy strategy = StrategyRegistry.fromId(strategyId);
            players.add(strategy.getPlayer(roleIndex, random));
            roleIndex++;
        }
        return players;
    }

    private Game getGame() {
        String gameId = matchSpec.getGameId();
        return Game.valueOf(gameId);
    }
}
