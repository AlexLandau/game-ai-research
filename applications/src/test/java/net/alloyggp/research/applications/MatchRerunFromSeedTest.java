package net.alloyggp.research.applications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.alloyggp.research.ImmutableMatchSpec;
import net.alloyggp.research.MatchResult;
import net.alloyggp.research.MatchSpec;
import net.alloyggp.research.StrategyProvider;
import net.alloyggp.research.applications.MatchRunner;
import net.alloyggp.research.game.Game;
import net.alloyggp.research.strategy.MemorylessUCTRecordAllNodesStrategyProvider;
import net.alloyggp.research.strategy.RandomStrategyProvider;
import net.alloyggp.research.strategy.StrategyRegistry;
import net.alloyggp.research.strategy.parameter.StrategyParameters;

public class MatchRerunFromSeedTest {

    // TODO: Parameterize this over possible games, strategies, and/or seeds
    @Test
    public void testRerunningMatchFromKnownSeed() {
        MatchSpec matchSpec = createMatchSpec();
        MatchResult result = MatchRunner.runWithoutSaving(matchSpec);

        assertTrue(result.getOutcomes().isPresent());
        assertFalse(result.getErrorString().isPresent());

        MatchResult rerunResult = MatchRunner.runFromSeedWithoutSaving(matchSpec, result.getSeed());

        assertEquals(result.getSpec(), rerunResult.getSpec());
        assertEquals(result.getSeed(), rerunResult.getSeed());
        assertEquals(result.getMoveHistory(), rerunResult.getMoveHistory());
        assertEquals(result.getOutcomes(), rerunResult.getOutcomes());
    }

    private static MatchSpec createMatchSpec() {
        StrategyProvider strategy1 = new RandomStrategyProvider();
        StrategyParameters params1 = StrategyParameters.empty();
//        StrategyProvider strategy2 = new NPlyLookaheadStrategyProvider();
//        StrategyParameters params2 = StrategyParameters.builder()
//                .put(NPlyLookaheadStrategyProvider.PLIES_TO_LOOK_AHEAD, 9)
//                .put(NPlyLookaheadStrategyProvider.DEFAULT_OUTCOME, 0.5)
//                .build();
//        StrategyProvider strategy2 = new RandomStrategyProvider();
//        StrategyParameters params2 = StrategyParameters.empty();

        // TODO: Get these (and other strategies) working; currently they don't
        StrategyProvider strategy2 = new MemorylessUCTRecordAllNodesStrategyProvider();
        StrategyParameters params2 = StrategyParameters.builder()
                .put(MemorylessUCTRecordAllNodesStrategyProvider.getITERATION_COUNT(), 50)
                .build();

        return ImmutableMatchSpec.builder()
            .experimentName("SingleMatchRunnerTest" + System.currentTimeMillis())
            .addStrategyIds(StrategyRegistry.getId(strategy1, params1),
                    StrategyRegistry.getId(strategy2, params2))
            .gameId(Game.TIC_TAC_TOE.getId())
            .build();
    }
}
