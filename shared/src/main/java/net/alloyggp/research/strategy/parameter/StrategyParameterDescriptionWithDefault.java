package net.alloyggp.research.strategy.parameter;

import java.util.Optional;

public interface StrategyParameterDescriptionWithDefault<T> extends StrategyParameterDescription<T> {
    Optional<T> getDefaultValue();
}
