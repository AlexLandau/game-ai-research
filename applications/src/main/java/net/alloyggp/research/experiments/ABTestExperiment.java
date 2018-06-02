package net.alloyggp.research.experiments;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hipparchus.stat.descriptive.DescriptiveStatistics;
import org.hipparchus.stat.inference.AlternativeHypothesis;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

import net.alloyggp.research.Experiment;
import net.alloyggp.research.ImmutableMatchSpec;
import net.alloyggp.research.MatchResult;
import net.alloyggp.research.MatchSpec;
import net.alloyggp.research.applications.MatchResults;
import net.alloyggp.research.experiments.shared.Colorer;
import net.alloyggp.research.game.Game;

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
        ImmutableMultiset.Builder<MatchSpec> matches = ImmutableMultiset.builder();
        for (Game game : games) {
            for (int i = 0; i < strategyIds.size(); i++) {
                for (int j = i + 1; j < strategyIds.size(); j++) {
                    matches.addCopies(ImmutableMatchSpec.builder()
                            .experimentName(experimentName)
                            .addStrategyIds(strategyIds.get(i), strategyIds.get(j))
                            .gameId(game.getId())
                            .build(),
                            iterationsPerConfiguration);
                    matches.addCopies(ImmutableMatchSpec.builder()
                            .experimentName(experimentName)
                            .addStrategyIds(strategyIds.get(j), strategyIds.get(i))
                            .gameId(game.getId())
                            .build(),
                            iterationsPerConfiguration);
                }
            }
        }
        return matches.build();
    }

    @Override
    public String writeHtmlOutput(List<MatchResult> results) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><head><title>").append(experimentName).append("</title>");
        sb.append("<style>.sig { color: black; } .nonsig { color: gray; }</style></head>\n");
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

        writeAveragePerGameTimes(sb, resultsByGameId);

        sb.append("<p>Notes about p-values: The p-values listed are for a two-sided binomial test treating "
                + "matches as win or loss events based on aggregate scores, ignoring the nuance of possible "
                + "values between 0 and 1. Note that stopping an experiment early based on preliminary results "
                + "or extending it beyond the initial number of tests may invalidate the resulting p-values for "
                + "the purposes of testing statistical significance.</p>\n");
    }

    private void writeAveragePerGameTimes(StringBuilder sb,
            ListMultimap<String, MatchResult> resultsByGameId) {
        sb.append("<p>Average time to run a match of each game:</p>\n");
        sb.append("<table>\n");
        for (Game game : games) {
            List<MatchResult> list = resultsByGameId.get(game.getId());
            double averageTime = getAverageTimeSeconds(list);
            sb.append("<tr><td>" + game.getDisplayName() + "</td><td>" + averageTime + " s</td></tr>\n");
        }
        sb.append("</table>\n");
    }

    private double getAverageTimeSeconds(List<MatchResult> list) {
        double totalTimeSeconds = 0.0;
        long matchCount = 0L;
        for (MatchResult result : list) {
            if (!result.hadError()) {
                totalTimeSeconds += (result.getMillisecondsElapsed() / 1000.0);
                matchCount++;
            }
        }
        if (matchCount == 0L) {
            return Double.NaN;
        } else {
            return totalTimeSeconds / matchCount;
        }
    }

    private void writeLeaderboardAndComparisonTables(StringBuilder sb,
            List<MatchResult> results) {
        // Do computations for these tables
        Map<String, DescriptiveStatistics> strategyStats = Maps.newHashMap();
        Map<List<String>, DescriptiveStatistics> matchupStats = Maps.newHashMap();
        for (String strategyId : strategyIds) {
            strategyStats.put(strategyId, new DescriptiveStatistics());
            for (String otherStrategyId : strategyIds) {
                if (!strategyId.equals(otherStrategyId)) {
                    matchupStats.put(ImmutableList.of(strategyId, otherStrategyId), new DescriptiveStatistics());
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
                DescriptiveStatistics stats = strategyStats.get(strategyId);
                stats.addValue(result.getOutcomes().get().get(playerId));
            }
            matchupStats.get(result.getSpec().getStrategyIds())
                   .addValue(result.getOutcomes().get().get(0));
            matchupStats.get(result.getSpec().getStrategyIds().reverse())
                   .addValue(result.getOutcomes().get().get(1));
        }

        MinAndMax minAndMax = getMinAndMaxCountsForMatchups(matchupStats);
        String nMatches = (minAndMax.min == minAndMax.max)
                ? minAndMax.min + " matches"
                : "between " + minAndMax.min + " and " + minAndMax.max + " matches" ;
        sb.append("<p>Sample size for each cell is ");
        sb.append(nMatches);
        sb.append(".</p>\n");

        List<String> sortedStrategies = Lists.newArrayList(strategyIds);
        sortedStrategies.removeIf(strategy -> strategyStats.get(strategy).getN() == 0);
        sortedStrategies.sort(Comparator.comparing(strategy -> strategyStats.get(strategy).getMean()).reversed());

        Map<String, String> strategyShorthands = getStrategyShorthands(sortedStrategies);

        NumberFormat numberFormat = getThreeDigitDecimalFormat();
        Colorer colorer = Colorer.MUTED_RED_WHITE_BLUE;

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
            sb.append("  <td>" + strategyShorthands.get(colStrat) + "</td>\n");
        }
        sb.append(" </tr>\n");
        for (String rowStrat : sortedStrategies) {
            sb.append(" <tr>\n");
            sb.append("  <td>" + strategyShorthands.get(rowStrat) + " " + rowStrat + "</td>\n");
            for (String colStrat : sortedStrategies) {
                if (rowStrat.equals(colStrat)) {
                    sb.append("  <td>N/A</td>\n");
                } else {
                    DescriptiveStatistics stats = matchupStats.get(ImmutableList.of(rowStrat, colStrat));
                    double mean = stats.getMean();
                    /*
                     * As also explained in the output HTML: The p-values listed are for a two-sided binomial
                     * test treating matches as win or loss events based on aggregate scores, ignoring the nuance
                     * of possible values between 0 and 1.
                     */
                    double pValue = new org.hipparchus.stat.inference.BinomialTest().binomialTest((int) stats.getN(), (int) stats.getSum(), 0.5, AlternativeHypothesis.TWO_SIDED);
                    String titleText = "count: " + stats.getN() + "; p: " + pValue;
                    String tagClass = (pValue < 0.05) ? "sig" : "nonsig";
                    Color color = colorer.getColor(mean);

                    sb.append("  <td style='background-color:"+Reports.getCssRgbString(color)+"' class=\"" + tagClass + "\" title=\"" + titleText + "\">" + numberFormat.format(mean) + "</td>\n");
                }
            }
            sb.append(" </tr>\n");
        }
        sb.append("</table>\n");
    }

    private Map<String, String> getStrategyShorthands(List<String> sortedStrategies) {
        Map<String, String> results = new HashMap<>();
        for (int i = 0; i < sortedStrategies.size(); i++) {
            String strategy = sortedStrategies.get(i);
            String shorthand = "(" + (i + 1) + ")";
            results.put(strategy,  shorthand);
        }
        return results;
    }

    private MinAndMax getMinAndMaxCountsForMatchups(Map<List<String>, DescriptiveStatistics> matchupStats) {
        long minSeen = Integer.MAX_VALUE;
        long maxSeen = 0;
        for (String strategy1 : strategyIds) {
            for (String strategy2 : strategyIds) {
                if (!strategy1.equals(strategy2)) {
                    long count = matchupStats.get(ImmutableList.of(strategy1, strategy2)).getN();
                    if (count < minSeen) {
                        minSeen = count;
                    }
                    if (count > maxSeen) {
                        maxSeen = count;
                    }
                }
            }
        }
        return new MinAndMax(minSeen, maxSeen);
    }

    private static NumberFormat getThreeDigitDecimalFormat() {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(3);
        return numberFormat;
    }

    // TODO: Utility?
    public final class MinAndMax {
        public final long min;
        public final long max;

        public MinAndMax(long min, long max) {
            this.min = min;
            this.max = max;
        }
    }

}
