package net.alloyggp.research.applications;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

import net.alloyggp.research.Experiment;
import net.alloyggp.research.MatchResult;
import net.alloyggp.research.MatchSpec;

/**
 * This continually looks for matches that should be run for experiments, then runs them.
 *
 * TODO: Enable multithreading.
 * TODO: Add a Gradle task for running this.
 * TODO: Pick a better name for this.
 */
public class OngoingMatchRunner {
    public static void main(String[] args) throws IOException {
        List<Experiment> allExperiments = ExperimentRegistry.getExperiments();

        for (Experiment experiment : allExperiments) {
            runMatchesForExperiment(experiment);
        }
    }

    private static void runMatchesForExperiment(Experiment experiment) throws IOException {
        Multiset<MatchSpec> desiredSpecCounts = experiment.getMatchesToRun();

        List<MatchResult> existingResults = MatchResults.loadAllResults(experiment.getName());
        Multiset<MatchSpec> existingSpecCounts = existingResults.stream()
                .map(result -> result.getSpec())
                .collect(ImmutableMultiset.toImmutableMultiset());

        // We note that partial results are often more interesting to look at when
        // iterations are spread evenly across different possibilities, as opposed to
        // collecting all at once.
        int maxDesiredCount = desiredSpecCounts.entrySet().stream().mapToInt(Multiset.Entry::getCount).max().getAsInt();
        for (int i = 0; i < maxDesiredCount; i++) {
            for (MatchSpec spec : desiredSpecCounts.elementSet()) {
                int desiredCount = desiredSpecCounts.count(spec);
                int existingCount = existingSpecCounts.count(spec);
                if (existingCount <= i && desiredCount > i) {
                    System.out.println("Running a match of " + spec.getGameId() + " for experiment " + spec.getExperimentName());
                    MatchRunner.run(spec);
                }
            }
        }
    }
}
