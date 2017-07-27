package net.alloyggp.research;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;

@Value.Immutable
@JsonSerialize(as = ImmutableMatchSpec.class)
@JsonDeserialize(builder = ImmutableMatchSpec.Builder.class)
public interface MatchSpec {
    String getExperimentName();
    String getGameId();
    ImmutableList<String> getStrategyIds();
}
