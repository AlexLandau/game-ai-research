package net.alloyggp.research.strategy.parameter;

import org.immutables.value.Value;

@Value.Immutable
public interface DoubleStrategyParameterDescription extends StrategyParameterDescriptionWithDefault<Double> {
    @Value.Default default double getMin() {
        return Double.NEGATIVE_INFINITY;
    }
    @Value.Default default double getMax() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    default void validate(Double value) {
        if (value == null) {
            throw new NullPointerException();
        }
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException("Parameters should not have NaN values.");
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
    default String toString(Double value) {
        return value.toString();
    }
    @Override
    default Double parse(String value) {
        return Double.parseDouble(value);
    }
}
