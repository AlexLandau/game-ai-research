package net.alloyggp.research.applications;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Queues;

import net.alloyggp.research.Experiment;
import net.alloyggp.research.MatchResult;
import net.alloyggp.research.MatchSpec;

/**
 * This continually looks for matches that should be run for experiments, then runs them.
 *
 */
public class ExperimentMatchRunner {
    public static void main(String[] args) throws IOException, InterruptedException {
        final int numThreads;
        if (args.length > 0) {
            numThreads = Integer.parseInt(args[0]);
        } else {
            numThreads = 1;
        }
        System.out.println("Running with a thread count of " + numThreads + ".");

        BlockingQueue<WorkItem> workQueue = Queues.newArrayBlockingQueue(100);
        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                while (true) {
                    WorkItem work;
                    try {
                        work = workQueue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    if (work.shouldStop) {
                        // No more work to do
                        break;
                    }
                    MatchSpec spec = work.matchSpec;
                    System.out.println("Running a match of " + spec.getGameId() + " for experiment " + spec.getExperimentName());
                    try {
                        MatchRunner.runAndSaveResult(spec);
                    } catch (IOException e) {
                        // TODO: We should really record errors, which might indicate invalid results
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        List<Experiment> allExperiments = ExperimentRegistry.getExperiments();
        for (Experiment experiment : allExperiments) {
            enqueueMatchesForExperiment(experiment, workQueue);
        }
        for (int i = 0; i < numThreads; i++) {
            workQueue.put(WorkItem.stopper());
        }
    }

    private static class WorkItem {
        public final boolean shouldStop;
        public final MatchSpec matchSpec;

        private WorkItem(boolean shouldStop, MatchSpec matchSpec) {
            this.shouldStop = shouldStop;
            this.matchSpec = matchSpec;
        }

        public static WorkItem of(MatchSpec spec) {
            return new WorkItem(false, spec);
        }

        public static WorkItem stopper() {
            return new WorkItem(true, null);
        }
    }

    private static void enqueueMatchesForExperiment(Experiment experiment, BlockingQueue<WorkItem> workQueue) throws IOException, InterruptedException {
        Multiset<MatchSpec> desiredSpecCounts = experiment.getMatchesToRun();

        List<MatchResult> existingResults = MatchResults.loadAllResults(experiment.getName());
        Multiset<MatchSpec> existingSpecCounts = existingResults.stream()
                .filter(result -> !result.hadError())
                .map(result -> result.getSpec())
                .collect(ImmutableMultiset.toImmutableMultiset());

        System.out.println("Starting to enqueue matches for experiment " + experiment.getName() + ".");
        System.out.println("Number of matches already done for this experiment: " + existingSpecCounts.size());

        // We note that partial results are often more interesting to look at when
        // iterations are spread evenly across different possibilities, as opposed to
        // collecting all at once.
        int maxDesiredCount = desiredSpecCounts.entrySet().stream().mapToInt(Multiset.Entry::getCount).max().getAsInt();
        for (int i = 0; i < maxDesiredCount; i++) {
            for (MatchSpec spec : desiredSpecCounts.elementSet()) {
                int desiredCount = desiredSpecCounts.count(spec);
                int existingCount = existingSpecCounts.count(spec);
                if (existingCount <= i && desiredCount > i) {
                    workQueue.put(WorkItem.of(spec));
                }
            }
        }
    }
}
