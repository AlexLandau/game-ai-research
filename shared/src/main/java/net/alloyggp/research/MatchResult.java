package net.alloyggp.research;

import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;

// TODO: Consider moving this to the applications project
@Value.Immutable
@JsonSerialize(as = ImmutableMatchResult.class)
@JsonDeserialize(builder = ImmutableMatchResult.Builder.class)
public interface MatchResult {
    MatchSpec getSpec();
    // TODO: Replace this with ImmutableIntArray when jackson-datatype-guava supports it
    ImmutableList<Integer> getSeed();
    boolean hadError();
    Optional<String> getErrorString();
    Optional<ImmutableList<Double>> getOutcomes();
    ImmutableList<ImmutableList<String>> getMoveHistory();
    long getMillisecondsElapsed();
}
