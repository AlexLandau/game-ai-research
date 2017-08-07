package net.alloyggp.research.experiments;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

import net.alloyggp.research.Experiment;
import net.alloyggp.research.ImmutableMatchSpec;
import net.alloyggp.research.MatchResult;
import net.alloyggp.research.MatchSpec;
import net.alloyggp.research.applications.Game;
import net.alloyggp.research.applications.MatchResults;

// TODO: Add information about error counts
public class ABTestExperiment implements Experiment {
    private final String experimentName;
    private final ImmutableList<String> strategyIds;
    private final ImmutableList<Game> games;
    private final int iterationsPerConfiguration;

    public ABTestExperiment(String experimentName,
            ImmutableList<String> strategyIds, ImmutableList<Game> games,
            int iterationsPerConfiguration) {
        this.experimentName = experimentName;
        this.strategyIds = strategyIds;
        this.games = games;
        this.iterationsPerConfiguration = iterationsPerConfiguration;
    }

    @Override
    public String getName() {
        return experimentName;
    }

    @Override
    public Multiset<MatchSpec> getMatchesToRun() {
        Multiset<MatchSpec> matches = HashMultiset.create();
        for (Game game : games) {
            for (int i = 0; i < strategyIds.size(); i++) {
                for (int j = i + 1; j < strategyIds.size(); j++) {
                    matches.add(ImmutableMatchSpec.builder()
                            .experimentName(experimentName)
                            .addStrategyIds(strategyIds.get(i), strategyIds.get(j))
                            .gameId(game.getId())
                            .build(),
                            iterationsPerConfiguration);
                    matches.add(ImmutableMatchSpec.builder()
                            .experimentName(experimentName)
                            .addStrategyIds(strategyIds.get(j), strategyIds.get(i))
                            .gameId(game.getId())
                            .build(),
                            iterationsPerConfiguration);
                }
            }
        }
        return matches;
    }

    @Override
    public String writeHtmlOutput(List<MatchResult> results) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><head><title>").append(experimentName).append("</title></head>\n");
        sb.append("<body>");

        writeBodyContents(sb, results);

        sb.append("</body></html>\n");
        return sb.toString();
    }

    private void writeBodyContents(StringBuilder sb,
            List<MatchResult> allResults) {
        ListMultimap<String, MatchResult> resultsByGameId = MatchResults.groupByGameId(allResults);

        sb.append("<h1>Overall results</h1>\n");
        writeLeaderboardAndComparisonTables(sb, allResults);

        for (Game game : games) {
            List<MatchResult> resultsForGame = resultsByGameId.get(game.getId());
            sb.append("<h2>Game: " + game.getDisplayName() + "</h2>\n");
            writeLeaderboardAndComparisonTables(sb, resultsForGame);
        }
    }

    private void writeLeaderboardAndComparisonTables(StringBuilder sb,
            List<MatchResult> results) {
        // Do computations for these tables
        Map<String, SummaryStatistics> strategyStats = Maps.newHashMap();
        Map<List<String>, SummaryStatistics> matchupStats = Maps.newHashMap();
        for (String strategyId : strategyIds) {
            strategyStats.put(strategyId, new SummaryStatistics());
            for (String otherStrategyId : strategyIds) {
                if (!strategyId.equals(otherStrategyId)) {
                    matchupStats.put(ImmutableList.of(strategyId, otherStrategyId), new SummaryStatistics());
                }
            }
        }

        for (MatchResult result : results) {
            if (result.hadError()) {
                continue;
            }
            if (!result.getSpec().getStrategyIds().stream().allMatch(strategyIds::contains)) {
                // We no longer care about a strategy used in this match
                continue;
            }
            for (int playerId : Arrays.asList(0, 1)) {
                String strategyId = result.getSpec().getStrategyIds().get(playerId);
                SummaryStatistics stats = strategyStats.get(strategyId);
                stats.addValue(result.getOutcomes().get().get(playerId));
            }
            matchupStats.get(result.getSpec().getStrategyIds())
                   .addValue(result.getOutcomes().get().get(0));
            matchupStats.get(result.getSpec().getStrategyIds().reverse())
                   .addValue(result.getOutcomes().get().get(1));
        }

        List<String> sortedStrategies = Lists.newArrayList(strategyIds);
        sortedStrategies.removeIf(strategy -> strategyStats.get(strategy).getN() == 0);
        sortedStrategies.sort(Comparator.comparing(strategy -> strategyStats.get(strategy).getMean()).reversed());

        NumberFormat numberFormat = getThreeDigitDecimalFormat();

        // Write the leaderboard table
        sb.append("<table>\n");
        for (String strategy : sortedStrategies) {
            double meanOutcome = strategyStats.get(strategy).getMean();
            sb.append(" <tr>\n");
            sb.append("  <td>" + strategy + "</td>\n");
            sb.append("  <td>" + numberFormat.format(meanOutcome) + "</td>\n");
            sb.append(" </tr>\n");
        }
        sb.append("</table>\n");

        // Write the strategy-vs-strategy comparison table
        sb.append("<table>\n");
        sb.append(" <tr><td></td>\n");
        for (String colStrat : sortedStrategies) {
            sb.append("  <td>" + colStrat + "</td>\n");
        }
        sb.append(" </tr>\n");
        for (String rowStrat : sortedStrategies) {
            sb.append(" <tr>\n");
            sb.append("  <td>" + rowStrat + "</td>\n");
            for (String colStrat : sortedStrategies) {
                if (rowStrat.equals(colStrat)) {
                    sb.append("  <td>N/A</td>\n");
                } else {
                    SummaryStatistics stats = matchupStats.get(ImmutableList.of(rowStrat, colStrat));
                    double mean = stats.getMean();
                    sb.append("  <td>" + numberFormat.format(mean) + "</td>\n");
                }
            }
            sb.append(" </tr>\n");
        }
        sb.append("</table>\n");
    }

    private static NumberFormat getThreeDigitDecimalFormat() {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(3);
        return numberFormat;
    }
}
