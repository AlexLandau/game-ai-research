package net.alloyggp.research;

import java.util.List;

import org.immutables.value.Value;

// TODO: Consider moving this to the applications project
@Value.Immutable
public interface MatchResult {
    MatchSpec getSpec();
    int[] getSeed();
    List<Double> getOutcomes();
    List<List<String>> getMoveHistory();
    long getMillisecondsToRun();
}
