package net.alloyggp.research.applications.test;

import java.io.IOException;

import net.alloyggp.research.ImmutableMatchSpec;
import net.alloyggp.research.MatchResult;
import net.alloyggp.research.MatchSpec;
import net.alloyggp.research.StrategyProvider;
import net.alloyggp.research.applications.Game;
import net.alloyggp.research.applications.MatchResults;
import net.alloyggp.research.applications.MatchRunner;
import net.alloyggp.research.applications.StrategyRegistry;
import net.alloyggp.research.strategy.MemorylessUCTRecordAllNodesStrategyProvider;
import net.alloyggp.research.strategy.RandomStrategyProvider;
import net.alloyggp.research.strategy.parameter.StrategyParameters;

// This is a simple test of the framework.
public class SingleMatchRunner {

    public static void main(String[] args) throws IOException {
        MatchSpec spec = createMatchSpec();
        MatchRunner.run(spec);
        MatchRunner.run(spec);

        for (MatchResult result : MatchResults.loadAllResults(spec.getExperimentName())) {
            System.out.println(result);
        }
    }

    private static MatchSpec createMatchSpec() {
        StrategyProvider strategy1 = new RandomStrategyProvider();
        StrategyParameters params1 = StrategyParameters.empty();
//        StrategyProvider strategy2 = new NPlyLookaheadStrategyProvider();
//        StrategyParameters params2 = StrategyParameters.builder()
//                .put(NPlyLookaheadStrategyProvider.PLIES_TO_LOOK_AHEAD, 9)
//                .put(NPlyLookaheadStrategyProvider.DEFAULT_OUTCOME, 0.5)
//                .build();
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
