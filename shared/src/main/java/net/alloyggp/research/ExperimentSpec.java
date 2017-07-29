package net.alloyggp.research;

import org.immutables.value.Value;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

@Value.Immutable
public interface ExperimentSpec {
    String getName();
    String getExperimentTypeId();
    ImmutableSortedSet<String> getGameIds();
    ImmutableList<String> getStrategyIds();
}
