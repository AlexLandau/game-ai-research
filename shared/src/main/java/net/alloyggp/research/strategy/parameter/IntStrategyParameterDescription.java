package net.alloyggp.research.strategy.parameter;

import org.immutables.value.Value;

@Value.Immutable
public interface IntStrategyParameterDescription extends StrategyParameterDescriptionWithDefault<Integer> {
    @Value.Default default int getMin() {
        return Integer.MIN_VALUE;
    }
    @Value.Default default int getMax() {
        return Integer.MAX_VALUE;
    }

    @Override
    default void validate(Integer value) {
        if (value == null) {
            throw new NullPointerException();
        }
        if (value < getMin()) {
            throw new IllegalArgumentException("Parameter " + getName() + " must have value at least " +
                    getMin() + ", but was " + value);
        }
        if (value > getMax()) {
            throw new IllegalArgumentException("Parameter " + getName() + " must have value at most " +
                    getMax() + ", but was " + value);
        }
    }

    @Override
    default String toString(Integer value) {
        return value.toString();
    }
    @Override
    default Integer parse(String value) {
        return Integer.parseInt(value);
    }
}
