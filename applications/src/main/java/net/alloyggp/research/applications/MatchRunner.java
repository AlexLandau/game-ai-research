package net.alloyggp.research.applications;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
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

    public static void run(MatchSpec matchSpec) throws IOException {
        MatchRunner runner = new MatchRunner(matchSpec);
        MatchResult result = runner.run();

        MatchResults.saveResult(result);
    }

    private MatchResult run() {
        int[] seed = getSeed();
        Random random = new RandomAdaptor(new MersenneTwister(seed));
        Game game = getGame();
        List<Player> players = getPlayers(random);

        List<ImmutableList<String>> moveHistory = Lists.newArrayList();

        long startTime = -1;
        try {
            GameState state = game.getInitialState();
            startTime = System.currentTimeMillis();
            for (Player player : players) {
                player.initializeGameState(state);
            }
            while (!state.isTerminal()) {
                List<Move> moves = Lists.newArrayList();
                for (Player player : players) {
                    moves.add(player.getMove());
                }
                moveHistory.add(moves.stream().map(Move::getName).collect(ImmutableList.toImmutableList()));
                state = state.getNextState(moves);
                for (Player player : players) {
                    player.advanceGameState(moves, state);
                }
            }
            ImmutableList<Double> outcome = ImmutableList.copyOf(state.getOutcomes());
            long millisecondsElapsed = System.currentTimeMillis() - startTime;

            return ImmutableMatchResult.builder()
                .spec(matchSpec)
                .addSeed(seed)
                .addAllMoveHistory(moveHistory)
                .hadError(false)
                .outcomes(outcome)
                .millisecondsElapsed(millisecondsElapsed)
                .build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            String errorString = Throwables.getStackTraceAsString(e);

            long millisecondsElapsed = 0;
            if (startTime != -1) {
                millisecondsElapsed = System.currentTimeMillis() - startTime;
            }

            return ImmutableMatchResult.builder()
                    .spec(matchSpec)
                    .addSeed(seed)
                    .addAllMoveHistory(moveHistory)
                    .hadError(true)
                    .errorString(errorString)
                    .millisecondsElapsed(millisecondsElapsed)
                    .build();
        }
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
