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
 * TODO: Add a Gradle task for running this.
 * TODO: Pick a better name for this.
 */
public class OngoingMatchRunner {
    public static void main(String[] args) throws IOException {
        final int numThreads;
        if (args.length > 0) {
            numThreads = Integer.parseInt(args[0]);
        } else {
            numThreads = 1;
        }
        System.out.println("Running with a thread count of " + numThreads + ".");

        BlockingQueue<WorkItem> workQueue = Queues.newLinkedBlockingQueue();
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
                        MatchRunner.run(spec);
                    } catch (IOException e) {
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
            workQueue.offer(WorkItem.stopper());
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

    private static void enqueueMatchesForExperiment(Experiment experiment, BlockingQueue<WorkItem> workQueue) throws IOException {
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
                    workQueue.offer(WorkItem.of(spec));
                }
            }
        }
    }
}
