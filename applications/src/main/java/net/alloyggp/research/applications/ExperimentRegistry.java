package net.alloyggp.research.applications;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.alloyggp.research.Experiment;
import net.alloyggp.research.experiments.ABTestExperiment;
import net.alloyggp.research.experiments.ParameterChartExperiment;
import net.alloyggp.research.strategy.MemorylessUCTStrategyProvider;
import net.alloyggp.research.strategy.NPlyLookaheadStrategyProvider;
import net.alloyggp.research.strategy.parameter.StrategyParameters;

// TODO: Make this based on persisted state that can be edited by other tools (?)
public class ExperimentRegistry {

    public static List<Experiment> getExperiments() {
        return ImmutableList.of(

                // Sample experiment 1
                ParameterChartExperiment.create(
                        "TestUctChartExperiment",
                        new MemorylessUCTStrategyProvider(),
                        StrategyParameters.empty(),
                        MemorylessUCTStrategyProvider.getITERATION_COUNT(),
                        ImmutableList.of("10", "20", "40", "80", "160", "320", "640", "1280", "2560"),
                        ImmutableList.of(Game.TIC_TAC_TOE),
                        50),

                // Sample experiment 2
                ParameterChartExperiment.create(
                        "TestDFSChartExperiment",
                        new NPlyLookaheadStrategyProvider(),
                        StrategyParameters.builder()
                            .put(NPlyLookaheadStrategyProvider.DEFAULT_OUTCOME, 0.5)
                            .build(),
                        NPlyLookaheadStrategyProvider.PLIES_TO_LOOK_AHEAD,
                        ImmutableList.of("1", "2", "3", "4", "5", "6", "7", "8", "9"),
                        ImmutableList.of(Game.TIC_TAC_TOE),
                        30),

                // Sample A/B test experiment
                new ABTestExperiment(
                        "TestABExperiment",
                        ImmutableList.of(
                                "UCT:iterationCount=20",
                                "UCT:iterationCount=200",
                                "UCT:iterationCount=2000",
                                "Random",
                                "NPlyLookahead:defaultOutcome=0.5:pliesToLookAhead=1",
                                "NPlyLookahead:defaultOutcome=0.5:pliesToLookAhead=5",
                                "NPlyLookahead:defaultOutcome=0.5:pliesToLookAhead=6"
                                ),
                        ImmutableList.of(Game.TIC_TAC_TOE),
                        100)
        );
    }

}
