package net.alloyggp.research;

import java.util.List;

public interface Experiment {
    List<MatchSpec> getMatchesToRun(List<MatchResult> matchesAlreadyRun, List<MatchResult> matchesInProgress);
}
