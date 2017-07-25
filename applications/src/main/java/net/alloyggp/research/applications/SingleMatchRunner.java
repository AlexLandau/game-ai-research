package net.alloyggp.research.applications;

import net.alloyggp.research.ImmutableMatchSpec;
import net.alloyggp.research.MatchSpec;
import net.alloyggp.research.StrategyProvider;
import net.alloyggp.research.strategy.NPlyLookaheadStrategyProvider;
import net.alloyggp.research.strategy.RandomStrategyProvider;
import net.alloyggp.research.strategy.parameter.StrategyParameters;

// This is essentially used as a simple test of the framework.
public class SingleMatchRunner {

    public static void main(String[] args) {
        MatchSpec spec = createMatchSpec();
        MatchRunner.run(spec);
        // TODO: Read the records created this way.
    }

    private static MatchSpec createMatchSpec() {
        StrategyProvider strategy1 = new RandomStrategyProvider();
        StrategyParameters params1 = StrategyParameters.empty();
        StrategyProvider strategy2 = new NPlyLookaheadStrategyProvider();
        StrategyParameters params2 = StrategyParameters.builder()
                .put(NPlyLookaheadStrategyProvider.PLIES_TO_LOOK_AHEAD, 9)
                .put(NPlyLookaheadStrategyProvider.DEFAULT_OUTCOME, 0.5)
                .build();

        return ImmutableMatchSpec.builder()
            .experimentName("SingleMatchRunnerTest" + System.currentTimeMillis())
            .addStrategyIds(StrategyRegistry.getId(strategy1, params1),
                    StrategyRegistry.getId(strategy2, params2))
            .gameId(Game.TIC_TAC_TOE.getId())
            .build();
    }

}
