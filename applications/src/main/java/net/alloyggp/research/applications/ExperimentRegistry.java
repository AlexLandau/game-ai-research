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
                ParameterChartExperiment.build("TestUctChartExperiment")
                        .strategyProvider(new MemorylessUCTStrategyProvider())
                        .initialParameters(StrategyParameters.empty())
                        .parameterToVary(MemorylessUCTStrategyProvider.getITERATION_COUNT())
                        .putUnparsedParameterValuesByGame(Game.TIC_TAC_TOE, "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120")
                        .putUnparsedParameterValuesByGame(Game.CONNECT_4_8x6, "10", "20", "40", "80", "160", "320", "640", "1280", "2560")
                        .iterationsPerConfiguration(50)
                        .build(),

                // Sample experiment 2
                ParameterChartExperiment.build("TestDFSChartExperiment")
                        .strategyProvider(new NPlyLookaheadStrategyProvider())
                        .initialParameters(StrategyParameters.builder()
                            .put(NPlyLookaheadStrategyProvider.DEFAULT_OUTCOME, 0.5)
                            .build())
                        .parameterToVary(NPlyLookaheadStrategyProvider.PLIES_TO_LOOK_AHEAD)
                        .putUnparsedParameterValuesByGame(Game.TIC_TAC_TOE, "1", "2", "3", "4", "5", "6", "7", "8", "9")
                        .iterationsPerConfiguration(30)
                        .build(),

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
