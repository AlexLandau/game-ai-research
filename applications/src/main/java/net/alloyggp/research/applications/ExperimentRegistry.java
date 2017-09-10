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
                        .putUnparsedParameterValuesByGame(Game.CEPHALOPOD_3x3,        "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120")
                        .putUnparsedParameterValuesByGame(Game.CONNECT_FOUR_8x6,      "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240", "20480")
                        .putUnparsedParameterValuesByGame(Game.CONNECT_FOUR_9x6,      "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240", "20480")
                        .putUnparsedParameterValuesByGame(Game.DOTS_AND_BOXES,        "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120")
                        .putUnparsedParameterValuesByGame(Game.DOTS_AND_BOXES_MISERE, "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120")
                        .putUnparsedParameterValuesByGame(Game.ENGLISH_DRAUGHTS,      "10", "20", "40", "80", "160", "320", "640", "1280", "2560")
                        .putUnparsedParameterValuesByGame(Game.GOMOKU_11x11,          "10", "20", "40", "80", "160", "320", "640", "1280", "2560")
                        .putUnparsedParameterValuesByGame(Game.GOMOKU_15x15,          "10", "20", "40", "80", "160", "320", "640", "1280")
                        .putUnparsedParameterValuesByGame(Game.GOMOKU_SWAP2_11x11,    "10", "20", "40", "80", "160", "320", "640", "1280", "2560")
                        .putUnparsedParameterValuesByGame(Game.GOMOKU_SWAP2_15x15,    "10", "20", "40", "80", "160", "320", "640", "1280")
                        .putUnparsedParameterValuesByGame(Game.BREAKTHROUGH,          "10", "20", "40", "80", "160", "320", "640", "1280", "2560")
                        .putUnparsedParameterValuesByGame(Game.BREAKTHROUGH_6x6,      "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240")
                        .putUnparsedParameterValuesByGame(Game.HEX,                   "10", "20", "40", "80", "160", "320", "640")
                        .putUnparsedParameterValuesByGame(Game.HEX_PIE,               "10", "20", "40", "80", "160", "320", "640")
                        .putUnparsedParameterValuesByGame(Game.MAJORITIES,            "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120")
                        .putUnparsedParameterValuesByGame(Game.PENTAGO,               "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120")
                        .putUnparsedParameterValuesByGame(Game.PENTAGO_MISERE,        "10", "20", "40", "80", "160", "320", "640", "1280", "2560")
                        .putUnparsedParameterValuesByGame(Game.QUARTO,                "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240")
                        .putUnparsedParameterValuesByGame(Game.QUARTO_MISERE,         "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240")
                        .putUnparsedParameterValuesByGame(Game.REVERSI,               "10", "20", "40", "80", "160", "320", "640", "1280", "2560")
                        .putUnparsedParameterValuesByGame(Game.REVERSI_MISERE,        "10", "20", "40", "80", "160", "320", "640", "1280")
                        .putUnparsedParameterValuesByGame(Game.SHEEP_AND_WOLF,        "10", "20", "40", "80", "160", "320", "640", "1280", "2560", "5120", "10240", "20480", "40960")
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
                                "Random"
                                ),
                        ImmutableList.of(Game.TIC_TAC_TOE,
                                Game.BREAKTHROUGH,
                                Game.DOTS_AND_BOXES,
                                Game.ENGLISH_DRAUGHTS,
                                Game.CONNECT_FOUR_8x6,
                                Game.CONNECT_FOUR_9x6,
                                Game.PENTAGO),
                        500)

        );
    }

}
