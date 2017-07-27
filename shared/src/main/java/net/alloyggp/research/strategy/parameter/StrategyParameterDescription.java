package net.alloyggp.research.strategy.parameter;

import org.immutables.value.Value;

public interface StrategyParameterDescription<T> {
    @Value.Parameter
    public String getName();

    public static ImmutableIntStrategyParameterDescription.Builder forInt(String name) {
        return ImmutableIntStrategyParameterDescription.builder().name(name);
    }

    public static ImmutableDoubleStrategyParameterDescription.Builder forDouble(String name) {
        return ImmutableDoubleStrategyParameterDescription.builder().name(name);
    }

    public void validate(T value);

    public String toString(T value);
    public T parse(String value);
}
