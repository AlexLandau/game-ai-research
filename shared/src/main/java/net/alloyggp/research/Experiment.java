package net.alloyggp.research;

import java.util.List;

import com.google.common.collect.Multiset;

public interface Experiment {
    String getName();
    /**
     * Note: The count of a MatchSpec determines how many times that type of
     * match should be run.
     */
    Multiset<MatchSpec> getMatchesToRun();
    String writeHtmlOutput(List<MatchResult> results);
}
