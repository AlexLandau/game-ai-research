package net.alloyggp.research.experiments;

import java.util.List;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

import net.alloyggp.research.Experiment;
import net.alloyggp.research.ImmutableMatchSpec;
import net.alloyggp.research.MatchResult;
import net.alloyggp.research.MatchSpec;
import net.alloyggp.research.StrategyProvider;
import net.alloyggp.research.applications.Game;
import net.alloyggp.research.applications.StrategyRegistry;
import net.alloyggp.research.strategy.parameter.StrategyParameterDescription;
import net.alloyggp.research.strategy.parameter.StrategyParameters;

public class ParameterChartExperiment<T> implements Experiment {
    private final String experimentName;
    private final StrategyProvider strategyProvider;
    private final StrategyParameters initialParameters;
    private final StrategyParameterDescription<T> parameterToVary;
    private final ImmutableList<String> unparsedParameterValues;
    private final ImmutableList<Game> games;

    private ParameterChartExperiment(String experimentName,
            StrategyProvider strategyProvider,
            StrategyParameters initialParameters,
            StrategyParameterDescription<T> parameterToVary,
            ImmutableList<String> unparsedParameterValues,
            ImmutableList<Game> games) {
        this.experimentName = experimentName;
        this.strategyProvider = strategyProvider;
        this.initialParameters = initialParameters;
        this.parameterToVary = parameterToVary;
        this.unparsedParameterValues = unparsedParameterValues;
        this.games = games;
    }

    @Override
    public List<MatchSpec> getMatchesToRun(List<MatchResult> matchesAlreadyRun,
            List<MatchResult> matchesInProgress) {
        Multiset<MatchKey> keyCounts = HashMultiset.create();
        for (MatchResult existingResult : Iterables.concat(matchesAlreadyRun, matchesInProgress)) {
            MatchKey key = matchKeyOf(existingResult);
            keyCounts.add(key);
        }

        List<MatchKey> keysToRun = Lists.newArrayList();
        int lowestCount = Integer.MAX_VALUE; // TODO: Upper limit?
        for (Game game : games) {
            for (String paramValue1 : unparsedParameterValues) {
                for (String paramValue2 : unparsedParameterValues) {
                    MatchKey key = new MatchKey(game, paramValue1, paramValue2);
                    int count = keyCounts.count(key);
                    if (count < lowestCount) {
                        keysToRun.clear();
                        keysToRun.add(key);
                        lowestCount = count;
                    } else if (count == lowestCount) {
                        keysToRun.add(key);
                    }
                }
            }
        }

        List<MatchSpec> matchSpecs = Lists.newArrayList();
        for (MatchKey key : keysToRun) {
            String strategyId1 = StrategyRegistry.getId(strategyProvider, initialParameters
                    .withParsed(parameterToVary, key.unparsedParameterValue1));
            String strategyId2 = StrategyRegistry.getId(strategyProvider, initialParameters
                    .withParsed(parameterToVary, key.unparsedParameterValue2));

            matchSpecs.add(ImmutableMatchSpec.builder()
                .experimentName(experimentName)
                .gameId(key.game.getId())
                .addStrategyIds(strategyId1, strategyId2)
                .build());
        }
        return matchSpecs;
    }

    private MatchKey matchKeyOf(MatchResult result) {
        Game game = Game.valueOf(result.getSpec().getGameId());
        String unparsedParameterValue1 = getParameterStringFromStrategyId(result.getSpec().getStrategyIds().get(0));
        String unparsedParameterValue2 = getParameterStringFromStrategyId(result.getSpec().getStrategyIds().get(1));
        return new MatchKey(game, unparsedParameterValue1, unparsedParameterValue2);
    }

    private String getParameterStringFromStrategyId(String strategyId) {
        String value = StrategyRegistry.getParametersFromId(strategyId).get(parameterToVary.getName());
        if (value == null) {
            throw new NullPointerException("");
        }
        return value;
    }

    private static class MatchKey {
        public final Game game;
        public final String unparsedParameterValue1;
        public final String unparsedParameterValue2;
        private MatchKey(Game game, String unparsedParameterValue1,
                String unparsedParameterValue2) {
            this.game = game;
            this.unparsedParameterValue1 = unparsedParameterValue1;
            this.unparsedParameterValue2 = unparsedParameterValue2;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((game == null) ? 0 : game.hashCode());
            result = prime * result + ((unparsedParameterValue1 == null) ? 0
                    : unparsedParameterValue1.hashCode());
            result = prime * result + ((unparsedParameterValue2 == null) ? 0
                    : unparsedParameterValue2.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MatchKey other = (MatchKey) obj;
            if (game != other.game)
                return false;
            if (unparsedParameterValue1 == null) {
                if (other.unparsedParameterValue1 != null)
                    return false;
            } else if (!unparsedParameterValue1
                    .equals(other.unparsedParameterValue1))
                return false;
            if (unparsedParameterValue2 == null) {
                if (other.unparsedParameterValue2 != null)
                    return false;
            } else if (!unparsedParameterValue2
                    .equals(other.unparsedParameterValue2))
                return false;
            return true;
        }
        @Override
        public String toString() {
            return "MatchKey [game=" + game + ", unparsedParameterValue1="
                    + unparsedParameterValue1 + ", unparsedParameterValue2="
                    + unparsedParameterValue2 + "]";
        }
    }
}
