package net.alloyggp.research;

import org.immutables.value.Value;

import com.google.common.collect.ImmutableList;

@Value.Immutable
public interface MatchSpec {
    String getExperimentName();
    String getGameId();
    ImmutableList<String> getStrategyIds();
}
