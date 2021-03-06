package net.alloyggp.research.applications;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.alloyggp.research.Experiment;
import net.alloyggp.research.experiments.ABTestExperiment;
import net.alloyggp.research.experiments.ParameterChartExperiment;
import net.alloyggp.research.game.Game;
import net.alloyggp.research.strategy.MemorylessUCTOneNodeExpansionStrategyProvider;
import net.alloyggp.research.strategy.NPlyLookaheadStrategyProvider;
import net.alloyggp.research.strategy.parameter.StrategyParameters;

// TODO: Make this based on persisted state that can be edited by other tools (?)
public class ExperimentRegistry {

    public static List<Experiment> getExperiments() {
        return ImmutableList.of(

                // Sample experiment 1
                ParameterChartExperiment.build("TestUctChartExperiment")
                        .strategyProvider(new MemorylessUCTOneNodeExpansionStrategyProvider())
                        .initialParameters(StrategyParameters.empty())
                        .parameterToVary(MemorylessUCTOneNodeExpansionStrategyProvider.getITERATION_COUNT())
                        .putUnparsedParameterValuesByGame(Game.TIC_TAC_TOE,           "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240", "20480")
                        .putUnparsedParameterValuesByGame(Game.CEPHALOPOD_3x3,        "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240", "20480")
                        .putUnparsedParameterValuesByGame(Game.CONNECT_FOUR_8x6,      "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240", "20480", "40960", "81920")
                        .putUnparsedParameterValuesByGame(Game.CONNECT_FOUR_9x6,      "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240", "20480", "40960", "81920")
                        .putUnparsedParameterValuesByGame(Game.DOTS_AND_BOXES,        "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240", "20480")
                        .putUnparsedParameterValuesByGame(Game.DOTS_AND_BOXES_MISERE, "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240", "20480")
                        .putUnparsedParameterValuesByGame(Game.ENGLISH_DRAUGHTS,      "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240")
                        .putUnparsedParameterValuesByGame(Game.GOMOKU_11x11,          "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240")
                        .putUnparsedParameterValuesByGame(Game.GOMOKU_15x15,          "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240")
                        .putUnparsedParameterValuesByGame(Game.GOMOKU_SWAP2_11x11,    "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240")
                        .putUnparsedParameterValuesByGame(Game.GOMOKU_SWAP2_15x15,    "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240")
                        .putUnparsedParameterValuesByGame(Game.BREAKTHROUGH,          "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240")
                        .putUnparsedParameterValuesByGame(Game.BREAKTHROUGH_6x6,      "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240", "20480", "40960")
                        .putUnparsedParameterValuesByGame(Game.HEX,                   "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240")
                        .putUnparsedParameterValuesByGame(Game.HEX_PIE,               "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240")
                        .putUnparsedParameterValuesByGame(Game.MAJORITIES,            "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240", "20480")
                        .putUnparsedParameterValuesByGame(Game.PENTAGO,               "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240", "20480")
                        .putUnparsedParameterValuesByGame(Game.PENTAGO_MISERE,        "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240", "20480")
                        .putUnparsedParameterValuesByGame(Game.QUARTO,                "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240", "20480", "40960")
                        .putUnparsedParameterValuesByGame(Game.QUARTO_MISERE,         "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240", "20480", "40960")
                        .putUnparsedParameterValuesByGame(Game.REVERSI,               "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240")
                        .putUnparsedParameterValuesByGame(Game.REVERSI_MISERE,        "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240")
                        .putUnparsedParameterValuesByGame(Game.SHEEP_AND_WOLF,        "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240", "20480", "40960", "81920", "163840", "327680")
                        // TODO: Understand what sample size we want here, in terms of effects on visual "accuracy"
                        .iterationsPerConfiguration(100)
                        .build(),

                // Sample experiment 2
                ParameterChartExperiment.build("TestDFSChartExperiment")
                        .strategyProvider(new NPlyLookaheadStrategyProvider())
                        .initialParameters(StrategyParameters.builder()
                            .put(NPlyLookaheadStrategyProvider.DEFAULT_OUTCOME, 0.5)
                            .build())
                        .parameterToVary(NPlyLookaheadStrategyProvider.PLIES_TO_LOOK_AHEAD)
                        .putUnparsedParameterValuesByGame(Game.TIC_TAC_TOE, "1", "2", "3", "4", "5", "6", "7", "8", "9")
                        .iterationsPerConfiguration(50)
                        .build(),

                // Sample A/B test experiment
                new ABTestExperiment(
                        "TestTicTacToeABExperiment",
                        ImmutableList.of(
                                "UCTRecordAllNodes:iterationCount=20",
                                "UCTRecordAllNodes:iterationCount=200",
                                "UCTOneNodeExpansion:iterationCount=20",
                                "UCTOneNodeExpansion:iterationCount=200",
                                "UCTOneNodeExpansion:iterationCount=2000",
                                "UCTWinsFirst:iterationCount=20",
                                "UCTWinsFirst:iterationCount=200",
                                "UCTWinsFirst:iterationCount=2000",
                                "Random",
                                "NPlyLookahead:defaultOutcome=0.5:pliesToLookAhead=1",
                                "NPlyLookahead:defaultOutcome=0.5:pliesToLookAhead=5",
                                "NPlyLookahead:defaultOutcome=0.5:pliesToLookAhead=6"
                                ),
                        ImmutableList.of(Game.TIC_TAC_TOE),
                        100),

                new ABTestExperiment(
                        "IterationCountAffectsCpComparisonsA",
                        ImmutableList.of(
                                "UCTOneNodeExpansion:iterationCount=200:c_p=0.176777",
                                "UCTOneNodeExpansion:iterationCount=200:c_p=0.707107",
                                "UCTOneNodeExpansion:iterationCount=2000:c_p=0.176777",
                                "UCTOneNodeExpansion:iterationCount=2000:c_p=0.707107"
                                ),
                        ImmutableList.of(Game.BREAKTHROUGH),
                        250),

                new ABTestExperiment(
                        "IterationCountAffectsCpComparisonsB",
                        ImmutableList.of(
                                "UCTOneNodeExpansion:iterationCount=200:c_p=0.176777",
                                "UCTOneNodeExpansion:iterationCount=200:c_p=2.828427",
                                "UCTOneNodeExpansion:iterationCount=2000:c_p=0.176777",
                                "UCTOneNodeExpansion:iterationCount=2000:c_p=2.828427"
                                ),
                        ImmutableList.of(Game.CONNECT_FOUR_9x6),
                        250),

                // Sample A/B test experiment
                new ABTestExperiment(
                        "TestUCT200CpExperiment",
                        ImmutableList.of(
                                "UCTOneNodeExpansion:iterationCount=200:c_p=0.176777",
                                "UCTOneNodeExpansion:iterationCount=200:c_p=0.353553",
                                "UCTOneNodeExpansion:iterationCount=200:c_p=0.707107",
                                "UCTOneNodeExpansion:iterationCount=200:c_p=1.414214",
                                "UCTOneNodeExpansion:iterationCount=200:c_p=2.828427"
                                ),
                        ImmutableList.of(Game.TIC_TAC_TOE,
                                Game.BREAKTHROUGH,
                                Game.DOTS_AND_BOXES,
                                Game.DOTS_AND_BOXES_MISERE,
                                Game.ENGLISH_DRAUGHTS,
                                Game.CONNECT_FOUR_8x6,
                                Game.CONNECT_FOUR_9x6,
                                Game.PENTAGO,
                                Game.REVERSI,
                                Game.CEPHALOPOD_3x3),
                        250),

                // Sample A/B test experiment
                new ABTestExperiment(
                        "TestUCT2000CpExperiment",
                        ImmutableList.of(
                                "UCTOneNodeExpansion:iterationCount=2000:c_p=0.176777",
                                "UCTOneNodeExpansion:iterationCount=2000:c_p=0.353553",
                                "UCTOneNodeExpansion:iterationCount=2000:c_p=0.707107",
                                "UCTOneNodeExpansion:iterationCount=2000:c_p=1.414214",
                                "UCTOneNodeExpansion:iterationCount=2000:c_p=2.828427"
                                ),
                        ImmutableList.of(Game.TIC_TAC_TOE,
                                Game.BREAKTHROUGH,
                                Game.DOTS_AND_BOXES,
                                Game.DOTS_AND_BOXES_MISERE,
                                Game.ENGLISH_DRAUGHTS,
                                Game.CONNECT_FOUR_8x6,
                                Game.CONNECT_FOUR_9x6,
                                Game.PENTAGO,
                                Game.REVERSI,
                                Game.CEPHALOPOD_3x3),
                        250),

                // Sample A/B test experiment
                new ABTestExperiment(
                        "TestUCTWinsFirst200CpExperiment",
                        ImmutableList.of(
                                "UCTWinsFirst:iterationCount=200:c_p=0.176777",
                                "UCTWinsFirst:iterationCount=200:c_p=0.353553",
                                "UCTWinsFirst:iterationCount=200:c_p=0.707107",
                                "UCTWinsFirst:iterationCount=200:c_p=1.414214",
                                "UCTWinsFirst:iterationCount=200:c_p=2.828427"
                                ),
                        ImmutableList.of(Game.TIC_TAC_TOE,
                                Game.BREAKTHROUGH,
                                Game.DOTS_AND_BOXES,
                                Game.DOTS_AND_BOXES_MISERE,
                                Game.ENGLISH_DRAUGHTS,
                                Game.CONNECT_FOUR_8x6,
                                Game.CONNECT_FOUR_9x6,
                                Game.PENTAGO,
                                Game.REVERSI,
                                Game.CEPHALOPOD_3x3),
                        250),

                // Sample A/B test experiment
                new ABTestExperiment(
                        "TestUCTWinsFirst2000CpExperiment",
                        ImmutableList.of(
                                "UCTWinsFirst:iterationCount=2000:c_p=0.176777",
                                "UCTWinsFirst:iterationCount=2000:c_p=0.353553",
                                "UCTWinsFirst:iterationCount=2000:c_p=0.707107",
                                "UCTWinsFirst:iterationCount=2000:c_p=1.414214",
                                "UCTWinsFirst:iterationCount=2000:c_p=2.828427"
                                ),
                        ImmutableList.of(Game.TIC_TAC_TOE,
                                Game.BREAKTHROUGH,
                                Game.DOTS_AND_BOXES,
                                Game.DOTS_AND_BOXES_MISERE,
                                Game.ENGLISH_DRAUGHTS,
                                Game.CONNECT_FOUR_8x6,
                                Game.CONNECT_FOUR_9x6,
                                Game.PENTAGO,
                                Game.REVERSI,
                                Game.CEPHALOPOD_3x3),
                        250),

                // Sample A/B test experiment
                new ABTestExperiment(
                        "TestUCTWinsFirstABExperiment",
                        ImmutableList.of(
                                "UCTRecordAllNodes:iterationCount=20",
                                "UCTRecordAllNodes:iterationCount=200",
                                "UCTOneNodeExpansion:iterationCount=20",
                                "UCTOneNodeExpansion:iterationCount=200",
                                "UCTOneNodeExpansion:iterationCount=2000",
                                "UCTWinsFirst:iterationCount=20",
                                "UCTWinsFirst:iterationCount=200",
                                "UCTWinsFirst:iterationCount=2000",
                                "UCTWinsFirst:iterationCount=2000:c_p=2.828427",
                                "UCTWinsFirst:iterationCount=2000:c_p=0.707107",
                                "UCTWinsFirst:iterationCount=2000:c_p=0.353553",
                                "Random"
                                ),
                        ImmutableList.of(Game.TIC_TAC_TOE,
                                Game.BREAKTHROUGH,
                                Game.DOTS_AND_BOXES,
                                Game.ENGLISH_DRAUGHTS,
                                Game.CONNECT_FOUR_8x6,
                                Game.CONNECT_FOUR_9x6,
                                Game.PENTAGO,
                                Game.REVERSI,
                                Game.CEPHALOPOD_3x3),
                        500)

        );
    }

}
