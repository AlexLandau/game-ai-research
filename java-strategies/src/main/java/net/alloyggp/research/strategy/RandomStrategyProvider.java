package net.alloyggp.research.strategy;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.alloyggp.research.Strategy;
import net.alloyggp.research.StrategyProvider;
import net.alloyggp.research.strategy.parameter.StrategyParameterDescription;
import net.alloyggp.research.strategy.parameter.StrategyParameters;

// TODO: Fix up the class structure here
public class RandomStrategyProvider implements StrategyProvider {
    @Override
    public String getName() {
        return "Random";
    }

    @Override
    public List<StrategyParameterDescription<?>> getParameters() {
        return ImmutableList.of();
    }

    @Override
    public Strategy get(StrategyParameters parameters) {
        return new RandomStrategy();
    }
}
