package net.alloyggp.research.strategy.parameter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class StrategyParameters {
    private final ImmutableMap<String, Object> parameters;
    private final ImmutableList<StrategyParameterDescription<?>> parameterDescriptions;

    private StrategyParameters(ImmutableMap<String, Object> parameters,
            ImmutableList<StrategyParameterDescription<?>> parameterDescriptions) {
        this.parameters = parameters;
        this.parameterDescriptions = parameterDescriptions;
    }

    public static StrategyParameters empty() {
        return new StrategyParameters(ImmutableMap.of(), ImmutableList.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public <T> T get(StrategyParameterDescription<T> parameter) {
        @SuppressWarnings("unchecked")
        T value = (T) parameters.get(parameter.getName());
        if (value != null) {
            return value;
        }

        // No value found; return the default if there is one
        if (parameter instanceof StrategyParameterDescriptionWithDefault) {
            Optional<T> possibleDefault = ((StrategyParameterDescriptionWithDefault<T>) parameter).getDefault();
            if (possibleDefault.isPresent()) {
                return possibleDefault.get();
            }
        }
        throw new IllegalStateException("Trying to get a parameter with name " + parameter.getName() +
                ", but no such parameter was provided.");
    }

    public static class Builder {
        private final Map<String, Object> parameters = Maps.newHashMap();
        private final List<StrategyParameterDescription<?>> parameterDescriptions = Lists.newArrayList();

        public <T> Builder put(StrategyParameterDescription<T> parameter, T value) {
            if (parameters.containsKey(parameter.getName())) {
                throw new IllegalArgumentException("Tried to add a parameter with the same name (" +
                        parameter.getName() + ") multiple times");
            }

            parameter.validate(value);
            parameters.put(parameter.getName(), value);
            parameterDescriptions.add(parameter);
            return this;
        }

        public StrategyParameters build() {
            return new StrategyParameters(ImmutableMap.copyOf(parameters),
                    ImmutableList.copyOf(parameterDescriptions));
        }

        public <T> Builder parseAndPut(StrategyParameterDescription<T> parameter, String valueString) {
            T value = parameter.parse(valueString);
            return put(parameter, value);
        }
    }

    public List<StrategyParameterDescription<?>> getParameterDescriptions() {
        return parameterDescriptions;
    }
}
