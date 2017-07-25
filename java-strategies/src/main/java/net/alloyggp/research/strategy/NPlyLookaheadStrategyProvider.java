package net.alloyggp.research.strategy;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.alloyggp.research.Strategy;
import net.alloyggp.research.StrategyProvider;
import net.alloyggp.research.strategy.parameter.StrategyParameterDescription;
import net.alloyggp.research.strategy.parameter.StrategyParameters;

public class NPlyLookaheadStrategyProvider implements StrategyProvider {
    @Override
    public String getName() {
        return "NPlyLookahead";
    }

    public static final StrategyParameterDescription<Integer> PLIES_TO_LOOK_AHEAD =
            StrategyParameterDescription.forInt("pliesToLookAhead")
                .min(1)
                .build();
    public static final StrategyParameterDescription<Double> DEFAULT_OUTCOME =
            StrategyParameterDescription.forDouble("defaultOutcome")
                .min(0.0)
                .max(1.0)
                .build();

    @Override
    public List<StrategyParameterDescription<?>> getParameters() {
        return ImmutableList.of(PLIES_TO_LOOK_AHEAD, DEFAULT_OUTCOME);
    }

    @Override
    public Strategy get(StrategyParameters parameters) {
        int pliesToLookAhead = parameters.get(PLIES_TO_LOOK_AHEAD);
        double defaultOutcome = parameters.get(DEFAULT_OUTCOME);
        return Strategy.wrap(new NPlyLookaheadStrategy(pliesToLookAhead, defaultOutcome));
    }
}
