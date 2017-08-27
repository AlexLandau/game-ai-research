package net.alloyggp.research.applications;

import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import net.alloyggp.research.Strategy;
import net.alloyggp.research.StrategyProvider;
import net.alloyggp.research.strategy.MemorylessUCTOneNodeExpansionStrategyProvider;
import net.alloyggp.research.strategy.MemorylessUCTRecordAllNodesStrategyProvider;
import net.alloyggp.research.strategy.MemorylessUCTWinsFirstStrategyProvider;
import net.alloyggp.research.strategy.NPlyLookaheadStrategyProvider;
import net.alloyggp.research.strategy.RandomStrategyProvider;
import net.alloyggp.research.strategy.parameter.StrategyParameterDescription;
import net.alloyggp.research.strategy.parameter.StrategyParameters;

public class StrategyRegistry {
    private StrategyRegistry() {
        // Not instantiable
    }

    // TODO: Cache the result of this
    private static Map<String, StrategyProvider> collectStrategyProviders() {
        Map<String, StrategyProvider> strategyProviders = Maps.newHashMap();

        add(strategyProviders, new RandomStrategyProvider());
        add(strategyProviders, new NPlyLookaheadStrategyProvider());
        add(strategyProviders, new MemorylessUCTRecordAllNodesStrategyProvider());
        add(strategyProviders, new MemorylessUCTOneNodeExpansionStrategyProvider());
        add(strategyProviders, new MemorylessUCTWinsFirstStrategyProvider());

        return strategyProviders;
    }

    private static void add(Map<String, StrategyProvider> strategyProviders,
            StrategyProvider strategyProvider) {
        String name = strategyProvider.getName();
        if (strategyProviders.containsKey(name)) {
            throw new IllegalArgumentException("Two strategy providers have the same name " + name +
                    "; the classes are " + strategyProviders.get(name).getClass().getName() + " and " + strategyProvider.getClass().getName());
        }
        strategyProviders.put(name, strategyProvider);
    }

    private static final ImmutableSet<String> ENCODING_CHARACTERS = ImmutableSet.of(
            ":", "=");

    public static String getId(StrategyProvider strategy,
            StrategyParameters params) {
        StringBuilder sb = new StringBuilder();

        for (String encodingChar : ENCODING_CHARACTERS) {
            if (strategy.getName().contains(encodingChar)) {
                throw new IllegalArgumentException("Strategy name " + strategy.getName() + " contains a disallowed character: '" + encodingChar + "'");
            }
        }

        sb.append(strategy.getName());

        for (StrategyParameterDescription<?> parameter : params.getParameterDescriptions()) {
            for (String encodingChar : ENCODING_CHARACTERS) {
                if (parameter.getName().contains(encodingChar)) {
                    throw new IllegalArgumentException("Parameter name " + parameter.getName() + " in strategy " + strategy.getName() + " contains a disallowed character: '" + encodingChar + "'");
                }
            }

            sb.append(":");
            sb.append(parameter.getName()).append("=");

            String valueString = writeValue(parameter, params);
            for (String encodingChar : ENCODING_CHARACTERS) {
                if (valueString.contains(encodingChar)) {
                    throw new IllegalArgumentException("Parameter name " + parameter.getName() + " in strategy " + strategy.getName() + " created a value string " + valueString + " containing a disallowed character: '" + encodingChar + "'");
                }
            }
            sb.append(valueString);
        }
        return sb.toString();
    }

    private static <T> String writeValue(StrategyParameterDescription<T> parameter,
            StrategyParameters params) {
        T value = params.get(parameter);
        return parameter.toString(value);
    }

    public static Strategy fromId(String strategyId) {
        String[] components = strategyId.split(":");

        String strategyName = components[0];
        StrategyProvider strategyProvider = collectStrategyProviders().get(strategyName);
        Map<String, StrategyParameterDescription<?>> parameterTypes = index(strategyProvider.getParameters());
        StrategyParameters.Builder builder = StrategyParameters.builder();
        for (int i = 1; i < components.length; i++) {
            String component = components[i];
            String[] parts = component.split("=");
            if (parts.length != 2) {
                throw new IllegalStateException("Malformed component in a strategy ID. ID was " + strategyId + ", component was " + component + ". Was expecting exactly one '='.");
            }
            String key = parts[0];
            StrategyParameterDescription<?> parameterDescription = Preconditions.checkNotNull(parameterTypes.get(key));

            String valueString = parts[1];
            builder.parseAndPut(parameterDescription, valueString);
        }
        StrategyParameters parameters = builder.build();
        return strategyProvider.get(parameters);
    }

    private static Map<String, StrategyParameterDescription<?>> index(
            List<StrategyParameterDescription<?>> parameters) {
        Map<String, StrategyParameterDescription<?>> map = Maps.newHashMap();
        for (StrategyParameterDescription<?> parameter : parameters) {
            if (map.containsKey(parameter.getName())) {
                throw new IllegalArgumentException("Parameter name collision with name " + parameter.getName());
            }
            map.put(parameter.getName(), parameter);
        }
        return map;
    }

    public static Map<String, String> getParametersFromId(String strategyId) {
        String[] components = strategyId.split(":");

        Map<String, String> results = Maps.newHashMap();
        for (int i = 1; i < components.length; i++) {
            String[] parts = components[i].split("=");
            results.put(parts[0], parts[1]);
        }
        return results;
    }
}
