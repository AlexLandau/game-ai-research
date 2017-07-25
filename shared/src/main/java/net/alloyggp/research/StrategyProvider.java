package net.alloyggp.research;

import java.util.List;

import net.alloyggp.research.strategy.parameter.StrategyParameterDescription;
import net.alloyggp.research.strategy.parameter.StrategyParameters;

public interface StrategyProvider {
    String getName();
    List<StrategyParameterDescription<?>> getParameters();
    Strategy get(StrategyParameters parameters);
}
