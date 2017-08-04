package net.alloyggp.research.experiments;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import javax.annotation.Nullable;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import net.alloyggp.research.Experiment;
import net.alloyggp.research.ImmutableMatchSpec;
import net.alloyggp.research.MatchResult;
import net.alloyggp.research.MatchSpec;
import net.alloyggp.research.StrategyProvider;
import net.alloyggp.research.applications.Game;
import net.alloyggp.research.applications.StrategyRegistry;
import net.alloyggp.research.strategy.parameter.StrategyParameterDescription;
import net.alloyggp.research.strategy.parameter.StrategyParameters;

public class ParameterChartExperiment<T> implements Experiment {
    private final String experimentName;
    private final StrategyProvider strategyProvider;
    private final StrategyParameters initialParameters;
    private final StrategyParameterDescription<T> parameterToVary;
    private final ImmutableList<String> unparsedParameterValues;
    private final ImmutableList<Game> games;
    private final int iterationsPerConfiguration;

    private ParameterChartExperiment(String experimentName,
            StrategyProvider strategyProvider,
            StrategyParameters initialParameters,
            StrategyParameterDescription<T> parameterToVary,
            ImmutableList<String> unparsedParameterValues,
            ImmutableList<Game> games, int iterationsPerConfiguration) {
        this.experimentName = experimentName;
        this.strategyProvider = strategyProvider;
        this.initialParameters = initialParameters;
        this.parameterToVary = parameterToVary;
        this.unparsedParameterValues = unparsedParameterValues;
        this.games = games;
        this.iterationsPerConfiguration = iterationsPerConfiguration;
    }

    public static <T> ParameterChartExperiment<T> create(String experimentName,
            StrategyProvider strategyProvider,
            StrategyParameters initialParameters,
            StrategyParameterDescription<T> parameterToVary,
            ImmutableList<String> unparsedParameterValues,
            ImmutableList<Game> games, int iterationsPerConfiguration) {
        return new ParameterChartExperiment<T>(experimentName,
                strategyProvider,
                initialParameters,
                parameterToVary,
                unparsedParameterValues,
                games,
                iterationsPerConfiguration);
    }

    @Override
    public String getName() {
        return experimentName;
    }

    @Override
    public Multiset<MatchSpec> getMatchesToRun() {
        Multiset<MatchSpec> matchSpecs = LinkedHashMultiset.create();

        for (Game game : games) {
            for (String paramValue1 : unparsedParameterValues) {
                for (String paramValue2 : unparsedParameterValues) {
                    String strategyId1 = StrategyRegistry.getId(strategyProvider, initialParameters
                            .withParsed(parameterToVary, paramValue1));
                    String strategyId2 = StrategyRegistry.getId(strategyProvider, initialParameters
                            .withParsed(parameterToVary, paramValue2));

                    matchSpecs.setCount(ImmutableMatchSpec.builder()
                        .experimentName(experimentName)
                        .gameId(game.getId())
                        .addStrategyIds(strategyId1, strategyId2)
                        .build(),
                        iterationsPerConfiguration);
                }
            }
        }

        return matchSpecs;
    }

    private List<String> getUnparsedParameterValues(MatchResult result) {
        return result.getSpec().getStrategyIds().stream()
                .map(this::getParameterStringFromStrategyId)
                .collect(ImmutableList.toImmutableList());
    }

    private String getParameterStringFromStrategyId(String strategyId) {
        String value = StrategyRegistry.getParametersFromId(strategyId).get(parameterToVary.getName());
        if (value == null) {
            throw new NullPointerException("");
        }
        return value;
    }

    @Override
    public String writeHtmlOutput(List<MatchResult> matchResults) {
        return writeHtml(groupResults(matchResults));
    }

    private String writeHtml(Map<String, ListMultimap<List<String>, MatchResult>> groupedResultsByGameId) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>UCT Charts</title><style>.outcomes-table td {width:36px;height:36px;text-align:center;}</style></head><body>\n");

        sb.append("<p>Player 1 is blue, player 2 is red.</p>\n");
        sb.append("<p>Hover over a cell to see player 1's average score on a scale from 0 to 1, followed by the average seconds taken per match.</p>\n");

        for (String gameId : ImmutableSortedSet.copyOf(groupedResultsByGameId.keySet())) {
            sb.append("<h1>" + Game.valueOf(gameId).getDisplayName() + "</h1>\n");
            writeSampleSizeAndTimingNote(sb, groupedResultsByGameId.get(gameId));
            writeTableForResults(sb, groupedResultsByGameId.get(gameId));
            writeTablesForMoveChoices(sb, groupedResultsByGameId.get(gameId));
        }

        sb.append("</body></html>\n");
        return sb.toString();
    }

    private void writeSampleSizeAndTimingNote(StringBuilder sb, ListMultimap<List<String>, MatchResult> listMultimap) {
        sb.append("<h3>");
        int minSampleSize = Integer.MAX_VALUE;
        int maxSampleSize = Integer.MIN_VALUE;
        for (String i : this.unparsedParameterValues) {
            for (String j : this.unparsedParameterValues) {
                ImmutableList<String> key = ImmutableList.of(i, j);
                int size = listMultimap.get(key).size();
                if (minSampleSize > size) {
                    minSampleSize = size;
                }
                if (maxSampleSize < size) {
                    maxSampleSize = size;
                }
            }
        }
        if (maxSampleSize == minSampleSize) {
            sb.append("Sample size for each cell is ").append(maxSampleSize).append(".");
        } else {
            sb.append("Sample size for each cell is between ")
                .append(minSampleSize)
                .append(" and ")
                .append(maxSampleSize)
                .append(".");
        }

        NumberFormat numberFormat = getThreeDigitDecimalFormat();
        SummaryStatistics summaryStatistics = new SummaryStatistics();
        for (MatchResult result : listMultimap.values()) {
            long millisElapsed = result.getMillisecondsElapsed();
            summaryStatistics.addValue(millisElapsed);
        }
        if (summaryStatistics.getN() > 0) {
            long longestTimeTakenSeconds = (long) summaryStatistics.getMax() / 1000;
            sb.append(" The longest time taken to generate a single match was " + longestTimeTakenSeconds + " seconds.");
            double averageSeconds = summaryStatistics.getMean() / 1000.0;
            sb.append(" The mean time was " + numberFormat.format(averageSeconds) + " seconds.");
        }

        sb.append("</h3>\n");
    }

    //TODO: Adjust the cell sizes and label font sizes so the cells are perfect squares, and/or convert to <svg>
    private void writeTableForResults(StringBuilder sb, ListMultimap<List<String>, MatchResult> listMultimap) {

        sb.append("<table class='outcomes-table' style='border-collapse: collapse'>\n");

        // Remember to also change the description at the top of the report
        // if you change the color scheme.
//        Colorer colorer = new GrayscaleColorer();
        Colorer colorer = new RedWhiteBlueColorer();
//        Colorer colorer = new RedGrayBlueColorer();
        {
            //Upper-left corner
            Color color = colorer.getColor(0.5);
            sb.append(" <tr><td style='background-color:"+getCssRgbString(color)+"'/>\n");
        }
        for (int col = 0; col < unparsedParameterValues.size(); col++) {
            String bgColor = getCssRgbString(colorer.getColor(0.0));
            String fontColor = getCssRgbString(colorer.getP2TextColor());
            sb.append("  <td style='background-color:"+bgColor+"; color:"+fontColor+"'>"+unparsedParameterValues.get(col)+"</td>\n");
        }
        sb.append(" </tr>\n");

        NumberFormat numberFormat = getThreeDigitDecimalFormat();
        for (int row = 0; row < unparsedParameterValues.size(); row++) {
            sb.append(" <tr>\n");
            String bgColor = getCssRgbString(colorer.getColor(1.0));
            String fontColor = getCssRgbString(colorer.getP1TextColor());
            sb.append("  <td style='background-color:"+bgColor+"; color:"+fontColor+"'>"+unparsedParameterValues.get(row)+"</td>\n");
            for (int col = 0; col < unparsedParameterValues.size(); col++) {
                List<MatchResult> resultsInBox = listMultimap.get(ImmutableList.of(unparsedParameterValues.get(row), unparsedParameterValues.get(col)));
                double player1AvgVal = averageFirstPlayerScore(resultsInBox);
                boolean isEmpty = resultsInBox.isEmpty();
                @Nullable Double avgTimeTaken = averageSecondsTaken(resultsInBox);

                String player1AvgText = isEmpty ? "" : numberFormat.format(player1AvgVal);
                String timeTakenText = (avgTimeTaken == null) ? "" : " / " + numberFormat.format(avgTimeTaken);
                String hoverText = player1AvgText + timeTakenText;

                Color color = isEmpty ? colorer.getEmptyColor() : colorer.getColor(player1AvgVal);
                sb.append("  <td style='background-color:"+getCssRgbString(color)+"' title='"+hoverText+"'/>\n");
            }
            sb.append(" </tr>\n");
        }
        sb.append("</table>\n");
    }

    private static String getCssRgbString(Color color) {
        return "rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+")";
    }

    private static interface Colorer {
        Color getEmptyColor();
        Color getP1TextColor();
        Color getP2TextColor();
        // Scale of 0.0 to 1.0
        Color getColor(double player1Avg);
    }

    private static class GrayscaleColorer implements Colorer {
        @Override
        public Color getEmptyColor() {
            return Color.RED;
        }

        @Override
        public Color getColor(double player1Avg) {
            int val = (int) (player1Avg * 255.0);
            return new Color(val, val, val);
        }

        @Override
        public Color getP1TextColor() {
            return Color.BLACK;
        }

        @Override
        public Color getP2TextColor() {
            return Color.WHITE;
        }
    }

    private static class RedWhiteBlueColorer implements Colorer {
        @Override
        public Color getEmptyColor() {
            return Color.BLACK;
        }

        @Override
        public Color getColor(double player1Avg) {
            if (player1Avg < 0.5) {
                // 0 at 0, 255 at 0.5
                int val = (int) (player1Avg * 2 * 255.0);
                // red at 0, white at 0.5
                return new Color(255, val, val);
            } else {
                // 255 at 0.5, 0 at 1.0
                int val = (int) ((1.0 - player1Avg) * 2 * 255.0);
                // white at 0.5, blue at 1.0
                return new Color(val, val, 255);
            }
        }

        @Override
        public Color getP1TextColor() {
            return Color.WHITE;
        }

        @Override
        public Color getP2TextColor() {
            return Color.WHITE;
        }
    }

    private static class RedGrayBlueColorer implements Colorer {
        @Override
        public Color getEmptyColor() {
            return Color.BLACK;
        }

        @Override
        public Color getColor(double player1Avg) {
            if (player1Avg < 0.5) {
                // 0 at 0, 255/3 at 0.5
                int val = (int) (player1Avg * 2 * 255.0 / 3.0);
                // red at 0, white at 0.5
                return new Color(255 - (2*val), val, val);
            } else {
                // 255 at 0.5, 0 at 1.0
                double val = ((1.0 - player1Avg) * 2 * 255.0 / 3.0);
                // white at 0.5, blue at 1.0
                return new Color((int) val, (int) val, (int) (255 - (2*val)));
            }
        }

        @Override
        public Color getP1TextColor() {
            return Color.WHITE;
        }

        @Override
        public Color getP2TextColor() {
            return Color.WHITE;
        }
    }

    private static NumberFormat getThreeDigitDecimalFormat() {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(3);
        return numberFormat;
    }

    private static double averageFirstPlayerScore(List<MatchResult> list) {
        double sum = 0.0;
        for (MatchResult result : list) {
            sum += result.getOutcomes().get(0);
        }
        int count = list.size();
        if (count == 0) {
            return 0.0;
        } else {
            return sum / count;
        }
    }

    private static @Nullable Double averageSecondsTaken(List<MatchResult> list) {
        if (list.isEmpty()) {
            return null;
        }
        long sum = 0;
        for (MatchResult result : list) {
            sum += result.getMillisecondsElapsed();
        }
        // Include the milliseconds-to-seconds conversion here
        return sum / (list.size() * 1000.0);
    }

    private Map<String, ListMultimap<List<String>, MatchResult>> groupResults(List<MatchResult> abfResults) {
        Map<String, ListMultimap<List<String>, MatchResult>> results = Maps.newHashMap();

        for (MatchResult entry : abfResults) {
            if (!results.containsKey(entry.getSpec().getGameId())) {
                results.put(entry.getSpec().getGameId(), ArrayListMultimap.create());
            }
            List<String> parameterValues = getUnparsedParameterValues(entry);
            results.get(entry.getSpec().getGameId()).put(parameterValues, entry);
        }
        return results;
    }

    private void writeTablesForMoveChoices(StringBuilder sb,
            ListMultimap<List<String>, MatchResult> results) {
        writeMoveChoiceTableForPlayer(0, sb, results);
        writeMoveChoiceTableForPlayer(1, sb, results);
    }

    private void writeMoveChoiceTableForPlayer(int roleIndex, StringBuilder sb,
            ListMultimap<List<String>, MatchResult> results) {
        NumberFormat numberFormat = getThreeDigitDecimalFormat();
        sb.append("<h3>First move choice for player "+(roleIndex + 1)+"</h3>\n");

        // Rows are possible moves, columns are parameter settings, cells are probabilities
        List<String> allFirstMovesForPlayer = getAllFirstMovesForPlayer(roleIndex, results);
        Map<String, Multiset<String>> countsByMoveByParamValue = getCountsByMoveByParamValue(roleIndex, results);

        sb.append("<table>\n");

        // TODO: Write the first row, where we list our parameter settings
        sb.append(" <tr><td>Move</td>");
        for (String paramValue : unparsedParameterValues) {
            sb.append("<td>");
            sb.append(paramValue);
            sb.append("</td>");
        }
        sb.append("</tr>\n");

        for (String firstMove : allFirstMovesForPlayer) {
            sb.append(" <tr><td>");
            sb.append(firstMove);
            sb.append("</td>\n");
            for (String paramValue : unparsedParameterValues) {
                sb.append("  <td>");
                double percentage = getFirstMovePercentage(firstMove, paramValue, countsByMoveByParamValue);
                sb.append(numberFormat.format(percentage));
                sb.append("</td>\n");
            }
            sb.append("</tr>\n");
        }

        sb.append("</table>\n");
    }

    private double getFirstMovePercentage(String firstMove, String paramValue,
            Map<String, Multiset<String>> countsByMoveByParamValue) {
        Multiset<String> countsByMove = countsByMoveByParamValue.get(paramValue);
        if (countsByMove.size() == 0) {
            return 0.0;
        }
        return countsByMove.count(firstMove) / (double) countsByMove.size();
    }

    private Map<String, Multiset<String>> getCountsByMoveByParamValue(int roleIndex,
            ListMultimap<List<String>, MatchResult> results) {
        Map<String, Multiset<String>> countsByMoveByParamValue = Maps.newHashMap();
        for (String paramValue : unparsedParameterValues) {
            countsByMoveByParamValue.put(paramValue, HashMultiset.create());
        }
        for (Entry<List<String>, MatchResult> entry : results.entries()) {
            String paramValue = entry.getKey().get(roleIndex);
            Multiset<String> countsByMove = countsByMoveByParamValue.get(paramValue);
            String firstMove = entry.getValue().getMoveHistory().get(0).get(roleIndex);
            countsByMove.add(firstMove);
        }
        return countsByMoveByParamValue;
    }

    private List<String> getAllFirstMovesForPlayer(int roleIndex,
            ListMultimap<List<String>, MatchResult> results) {
        SortedSet<String> moves = Sets.newTreeSet();
        for (MatchResult result : results.values()) {
            String firstMove = result.getMoveHistory().get(0).get(roleIndex);
            moves.add(firstMove);
        }
        return ImmutableList.copyOf(moves);
    }
}
