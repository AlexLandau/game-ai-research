package net.alloyggp.research.applications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

import net.alloyggp.research.ImmutableMatchSpec;
import net.alloyggp.research.MatchResult;
import net.alloyggp.research.MatchSpec;
import net.alloyggp.research.StrategyProvider;
import net.alloyggp.research.game.Game;
import net.alloyggp.research.strategy.MemorylessUCTOneNodeExpansionStrategyProvider;
import net.alloyggp.research.strategy.MemorylessUCTRecordAllNodesStrategyProvider;
import net.alloyggp.research.strategy.MemorylessUCTWinsFirstStrategyProvider;
import net.alloyggp.research.strategy.NPlyLookaheadStrategyProvider;
import net.alloyggp.research.strategy.RandomStrategyProvider;
import net.alloyggp.research.strategy.StrategyRegistry;
import net.alloyggp.research.strategy.parameter.StrategyParameters;

public class MatchRerunFromSeedTest {
    private static void testRerunningMatchFromKnownSeed(MatchSpec matchSpec) {
        MatchResult result = MatchRunner.runWithoutSaving(matchSpec);

        assertTrue(result.getOutcomes().isPresent());
        assertFalse(result.getErrorString().isPresent());

        MatchResult rerunResult = MatchRunner.runFromSeedWithoutSaving(matchSpec, result.getSeed());

        assertEquals(result.getSpec(), rerunResult.getSpec());
        assertEquals(result.getSeed(), rerunResult.getSeed());
        assertEquals(result.getMoveHistory(), rerunResult.getMoveHistory());
        assertEquals(result.getOutcomes(), rerunResult.getOutcomes());
    }

    @RunWith(Parameterized.class)
    public static class PerStrategyTest {
        private final String strategyId;
        public PerStrategyTest(String strategyId) {
            this.strategyId = strategyId;
        }

        @Parameters(name = "{0}")
        public static List<Object[]> getParameters() {

            List<String> strategyIds = Lists.newArrayList();
            strategyIds.add(StrategyRegistry.getId(new RandomStrategyProvider(), StrategyParameters.empty()));
            strategyIds.add(StrategyRegistry.getId(new NPlyLookaheadStrategyProvider(),
                    StrategyParameters.builder()
                    .put(NPlyLookaheadStrategyProvider.PLIES_TO_LOOK_AHEAD, 2)
                    .put(NPlyLookaheadStrategyProvider.DEFAULT_OUTCOME, 0.5)
                    .build()));
            strategyIds.add(StrategyRegistry.getId(new MemorylessUCTRecordAllNodesStrategyProvider(),
                    StrategyParameters.builder()
                    .put(MemorylessUCTRecordAllNodesStrategyProvider.getITERATION_COUNT(), 20)
                    .build()));
            strategyIds.add(StrategyRegistry.getId(new MemorylessUCTOneNodeExpansionStrategyProvider(),
                    StrategyParameters.builder()
                    .put(MemorylessUCTOneNodeExpansionStrategyProvider.getITERATION_COUNT(), 20)
                    .build()));
            strategyIds.add(StrategyRegistry.getId(new MemorylessUCTWinsFirstStrategyProvider(),
                    StrategyParameters.builder()
                    .put(MemorylessUCTWinsFirstStrategyProvider.getITERATION_COUNT(), 20)
                    .build()));

            return strategyIds.stream()
                    .map(strategyId -> new Object[] { strategyId })
                    .collect(Collectors.toList());
        }

        @Test
        public void testWithTicTacToe() {
            MatchSpec matchSpec = createMatchSpec(Game.TIC_TAC_TOE, strategyId, getRandomStrategyId());
            testRerunningMatchFromKnownSeed(matchSpec);
        }
    }

    @RunWith(Parameterized.class)
    public static class PerGameTest {
        private final Game game;
        public PerGameTest(Game game) {
            this.game = game;
        }

        @Parameters(name = "{0}")
        public static List<Object[]> getParameters() {
            return Arrays.stream(Game.values())
                    .map(game -> new Object[] { game })
                    .collect(Collectors.toList());
        }

        // This just takes a while due to game engine startup time
        @Ignore("Unfortunately, this currently takes too long to reasonably run other than manually...")
        @Test
        public void testWithRandomPlayers() {
            MatchSpec matchSpec = createMatchSpec(game, getRandomStrategyId(), getRandomStrategyId());
            testRerunningMatchFromKnownSeed(matchSpec);
        }
    }

    private static String getRandomStrategyId() {
        return StrategyRegistry.getId(
                new RandomStrategyProvider(),
                StrategyParameters.empty()
                );
    }

    private static MatchSpec createMatchSpec(Game game, String strategyId1, String strategyId2) {
        return ImmutableMatchSpec.builder()
            .experimentName("MatchRerunFromSeedTest" + System.currentTimeMillis())
            .addStrategyIds(strategyId1,
                    strategyId2)
            .gameId(game.getId())
            .build();
    }
}
