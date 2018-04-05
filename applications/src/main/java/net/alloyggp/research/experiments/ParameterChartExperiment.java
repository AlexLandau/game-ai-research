package net.alloyggp.research.experiments;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.hipparchus.stat.descriptive.DescriptiveStatistics;
import org.immutables.value.Value;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import net.alloyggp.research.Experiment;
import net.alloyggp.research.ImmutableMatchSpec;
import net.alloyggp.research.MatchResult;
import net.alloyggp.research.MatchSpec;
import net.alloyggp.research.StrategyProvider;
import net.alloyggp.research.game.Game;
import net.alloyggp.research.strategy.StrategyRegistry;
import net.alloyggp.research.strategy.parameter.StrategyParameterDescription;
import net.alloyggp.research.strategy.parameter.StrategyParameters;

//TODO: Add information about error counts
@Value.Immutable
public abstract class ParameterChartExperiment implements Experiment {
    protected abstract String experimentName();
    protected abstract StrategyProvider strategyProvider();
    protected abstract StrategyParameters initialParameters();
    protected abstract StrategyParameterDescription<?> parameterToVary();
    protected abstract ImmutableListMultimap<Game, String> unparsedParameterValuesByGame();
    protected abstract int iterationsPerConfiguration();

    @Override
    public String getName() {
        return experimentName();
    }

    private Set<Game> games() {
        return unparsedParameterValuesByGame().keySet();
    }

    @Override
    public Multiset<MatchSpec> getMatchesToRun() {
        Multiset<MatchSpec> matchSpecs = LinkedHashMultiset.create();

        for (Game game : games()) {
            for (String paramValue1 : unparsedParameterValuesByGame().get(game)) {
                for (String paramValue2 : unparsedParameterValuesByGame().get(game)) {
                    String strategyId1 = StrategyRegistry.getId(strategyProvider(), initialParameters()
                            .withParsed(parameterToVary(), paramValue1));
                    String strategyId2 = StrategyRegistry.getId(strategyProvider(), initialParameters()
                            .withParsed(parameterToVary(), paramValue2));

                    matchSpecs.setCount(ImmutableMatchSpec.builder()
                        .experimentName(experimentName())
                        .gameId(game.getId())
                        .addStrategyIds(strategyId1, strategyId2)
                        .build(),
                        iterationsPerConfiguration());
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
        String value = StrategyRegistry.getParametersFromId(strategyId).get(parameterToVary().getName());
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
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/><title>UCT Charts</title>"
                + "<style>.outcomes-table td {width:36px;height:36px;text-align:center;}"
                + ".raw-outcomes-data td,th {border: 2px solid black;}"
                + ".raw-outcomes-data {border-collapse: collapse; border: 2px solid black;}</style>\n");
        writeScripts(sb);
        sb.append("</head><body>\n");

        sb.append("<p>Player 1 is blue, player 2 is red.</p>\n");
        sb.append("<p>Hover over a cell to see player 1's average score on a scale from 0 to 1, followed by the average seconds taken per match.</p>\n");

        for (String gameId : ImmutableSortedSet.copyOf(groupedResultsByGameId.keySet())) {
            Game game = Game.valueOf(gameId);
            sb.append("<h1>" + Game.valueOf(gameId).getDisplayName() + "</h1>\n");
            writeSampleSizeAndTimingNote(sb, groupedResultsByGameId.get(gameId), game);
            writeTableForResults(sb, groupedResultsByGameId.get(gameId), game);
            writeSectionForMoveChoices(sb, groupedResultsByGameId.get(gameId), game);
        }

        sb.append("</body></html>\n");
        return sb.toString();
    }

    private void writeScripts(StringBuilder sb) {
        sb.append("<script>\n");
        sb.append("function setData(componentId, countsArray) {\n");
        sb.append("  for (let i = 0; i < countsArray.length; i++) {\n");
        sb.append("    const cellId = componentId + \"-\" + i;\n");
        sb.append("    document.getElementById(cellId).innerHTML = \"\" + countsArray[i];\n");
        sb.append("  }\n");
        sb.append("}\n");
        sb.append("function showFirstMoves(moveTablesDivId) {\n");
        sb.append("  document.getElementById(moveTablesDivId).style.display = '';\n");
        sb.append("  document.getElementById(moveTablesDivId + '-show').style.display = 'none';\n");
        sb.append("  document.getElementById(moveTablesDivId + '-hide').style.display = '';\n");
        sb.append("}\n");
        sb.append("function hideFirstMoves(moveTablesDivId) {\n");
        sb.append("  document.getElementById(moveTablesDivId).style.display = 'none';\n");
        sb.append("  document.getElementById(moveTablesDivId + '-show').style.display = '';\n");
        sb.append("  document.getElementById(moveTablesDivId + '-hide').style.display = 'none';\n");
        sb.append("}\n");
        sb.append("</script>\n");
    }

    private void writeSampleSizeAndTimingNote(StringBuilder sb, ListMultimap<List<String>, MatchResult> listMultimap, Game game) {
        sb.append("<h3>");
        int minSampleSize = Integer.MAX_VALUE;
        int maxSampleSize = Integer.MIN_VALUE;
        List<String> unparsedParameterValues = unparsedParameterValuesByGame().get(game);
        for (String i : unparsedParameterValues) {
            for (String j : unparsedParameterValues) {
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
        DescriptiveStatistics summaryStatistics = new DescriptiveStatistics();
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
    private void writeTableForResults(StringBuilder sb, ListMultimap<List<String>, MatchResult> resultsByBox, Game game) {
        List<String> unparsedParameterValues = unparsedParameterValuesByGame().get(game);
        List<ImmutableList<Double>> possibleOutcomes = getPossibleOutcomes(resultsByBox.values());
        List<Integer> overallCountsByOutcome = getCountsByOutcome(resultsByBox.values(), possibleOutcomes);
        String numbersDivId = "numbers-" + game.getId();
        // HACK: See note below on our setData inputs
        sb.append("<table class='outcomes-table' style='border-collapse: collapse' onMouseLeave=\"setData('"+numbersDivId+"', "+overallCountsByOutcome+")\">\n");

        // Remember to also change the description at the top of the report
        // if you change the color scheme.
//        Colorer colorer = new GrayscaleColorer();
        Colorer colorer = new RedWhiteBlueColorer();
//        Colorer colorer = new RedGrayBlueColorer();
        {
            //Upper-left corner
            Color color = colorer.getColor(0.5);
            sb.append(" <tr><td style='background-color:"+Reports.getCssRgbString(color)+"'/>\n");
        }
        for (int col = 0; col < unparsedParameterValues.size(); col++) {
            String bgColor = Reports.getCssRgbString(colorer.getColor(0.0));
            String fontColor = Reports.getCssRgbString(colorer.getP2TextColor());
            sb.append("  <td style='background-color:"+bgColor+"; color:"+fontColor+"'>"+unparsedParameterValues.get(col)+"</td>\n");
        }
        sb.append(" </tr>\n");


        NumberFormat numberFormat = getThreeDigitDecimalFormat();
        int errorCount = 0;
        for (int row = 0; row < unparsedParameterValues.size(); row++) {
            sb.append(" <tr>\n");
            String bgColor = Reports.getCssRgbString(colorer.getColor(1.0));
            String fontColor = Reports.getCssRgbString(colorer.getP1TextColor());
            sb.append("  <td style='background-color:"+bgColor+"; color:"+fontColor+"'>"+unparsedParameterValues.get(row)+"</td>\n");
            for (int col = 0; col < unparsedParameterValues.size(); col++) {
                List<MatchResult> resultsInBox = Lists.newLinkedList(resultsByBox.get(ImmutableList.of(unparsedParameterValues.get(row), unparsedParameterValues.get(col))));
                errorCount += removeErrors(resultsInBox);
                List<Integer> countsByOutcome = getCountsByOutcome(resultsInBox, possibleOutcomes);
                double player1AvgVal = averageFirstPlayerScore(resultsInBox);
                boolean isEmpty = resultsInBox.isEmpty();
                @Nullable Double avgTimeTaken = averageSecondsTaken(resultsInBox);

                String player1AvgText = isEmpty ? "" : numberFormat.format(player1AvgVal);
                String timeTakenText = (avgTimeTaken == null) ? "" : " / " + numberFormat.format(avgTimeTaken);
                String hoverText = player1AvgText + timeTakenText;

                Color color = isEmpty ? colorer.getEmptyColor() : colorer.getColor(player1AvgVal);
                sb.append("  <td style='background-color:"+Reports.getCssRgbString(color)+"' title='"+hoverText+"' "
                        // HACK: The countsByOutcome toString() very conveniently gives the JavaScript syntax
                        // for an array of numbers
                        + "onMouseEnter=\"setData('"+numbersDivId+"', "+countsByOutcome+")\"/>\n");
            }
            sb.append(" </tr>\n");
        }
        sb.append("</table>\n");

        writeOutcomesDataTable(sb, possibleOutcomes, numbersDivId);

        sb.append("<script>setData('"+numbersDivId+"', "+overallCountsByOutcome+")</script>\n");

        if (errorCount > 0) {
            sb.append("<p>(In addition, " + errorCount + " matches were run which encountered errors.)</p>\n");
        }
    }

    private List<Integer> getCountsByOutcome(Iterable<MatchResult> results,
            List<ImmutableList<Double>> possibleOutcomes) {
        List<Integer> counts = Lists.newArrayList();
        for (int i = 0; i < possibleOutcomes.size(); i++) {
            counts.add(0);
        }
        for (MatchResult result : results) {
            if (result.getOutcomes().isPresent()) {
                int index = possibleOutcomes.indexOf(result.getOutcomes().get());
                counts.set(index, 1 + counts.get(index));
            }
        }
        return counts;
    }

    private void writeOutcomesDataTable(StringBuilder sb, Collection<ImmutableList<Double>> possibleOutcomes, String numbersDivId) {
        sb.append("<table class='raw-outcomes-data'>\n");
        sb.append(" <caption>Outcome counts</caption>\n");
        sb.append(" <tr>\n");
        for (ImmutableList<Double> outcome : possibleOutcomes) {
            sb.append("  <th>");
            sb.append(styleOutcome(outcome));
            sb.append("</th>\n");
        }
        sb.append(" </tr>\n");

        sb.append(" <tr>\n");
        for (int i = 0; i < possibleOutcomes.size(); i++) {
            String specificId = numbersDivId + "-" + i;
            sb.append("  <td id=\"" + specificId + "\"></td>\n");
        }
        sb.append(" </tr>\n");
        sb.append("</table>\n");
    }

    private String styleOutcome(ImmutableList<Double> outcome) {
        return outcome.stream()
                .map(d -> {
                    if (d == 0.0) {
                        return "0";
                    } else if (d == 1.0) {
                        return "1";
                    } else {
                        return d.toString();
                    }
                })
                .collect(Collectors.joining("-"));
    }

    /**
     * Returns the outcomes sorted in a particular, consistent order.
     */
    private List<ImmutableList<Double>> getPossibleOutcomes(Collection<MatchResult> results) {
        Set<ImmutableList<Double>> outcomes = Sets.newHashSet();
        for (MatchResult result : results) {
            if (result.getOutcomes().isPresent()) {
                outcomes.add(result.getOutcomes().get());
            }
        }
        return ImmutableList.copyOf(sortOutcomes(outcomes));
    }

    private Collection<ImmutableList<Double>> sortOutcomes(Set<ImmutableList<Double>> elementSet) {
        return ImmutableSortedSet.copyOf((outcome1, outcome2) -> {
            if (outcome1.size() != outcome2.size()) {
                throw new IllegalArgumentException();
            }
            for (int i = 0; i < outcome1.size(); i++) {
                // Put first player winning first
                int comparison = -Double.compare(outcome1.get(i), outcome2.get(i));
                if (comparison != 0) {
                    return comparison;
                }
            }
            return 0;
        }, elementSet);
    }
    /**
     * Returns the number of matches removed from the list because they resulted in errors.
     */
    private int removeErrors(List<MatchResult> resultsInBox) {
        int count = 0;
        Iterator<MatchResult> itr = resultsInBox.iterator();
        while (itr.hasNext()) {
            MatchResult result = itr.next();
            if (result.hadError()) {
                itr.remove();
                count++;
            }
        }
        return count;
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
            sum += result.getOutcomes().get().get(0);
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

        Map<String, Set<String>> relevantParametersByGameId = Maps.newHashMap();
        for (Game game : games()) {
            relevantParametersByGameId.put(game.getId(), ImmutableSet.copyOf(unparsedParameterValuesByGame().get(game)));
        }

        for (MatchResult entry : abfResults) {
            if (!relevantParametersByGameId.containsKey(entry.getSpec().getGameId())) {
                continue;
            }
            if (!results.containsKey(entry.getSpec().getGameId())) {
                results.put(entry.getSpec().getGameId(), ArrayListMultimap.create());
            }
            List<String> parameterValues = getUnparsedParameterValues(entry);
            Set<String> relevantParameters = relevantParametersByGameId.get(entry.getSpec().getGameId());
            if (!parameterValues.stream().allMatch(relevantParameters::contains)) {
                // This match involves some parameter value we used to care about but no longer
                // do; exclude it from the beginning here to avoid it showing up in some parts
                // of the report but not others
                continue;
            }
            results.get(entry.getSpec().getGameId()).put(parameterValues, entry);
        }
        return results;
    }

    private void writeSectionForMoveChoices(StringBuilder sb,
            ListMultimap<List<String>, MatchResult> results, Game game) {
        String moveTablesDivId = "firstMoveInfo-" + game.toString();
        sb.append("<p>First move info ");
        sb.append("<span id='"+moveTablesDivId+"-show'><a href=\"javascript:showFirstMoves('"+moveTablesDivId+"')\">Show</a></span>");
        sb.append("<span id='"+moveTablesDivId+"-hide'><a href=\"javascript:hideFirstMoves('"+moveTablesDivId+"')\">Hide</a></span>");
        sb.append("</p>\n");
        sb.append("<div id='"+moveTablesDivId+"'>\n");
        writeMoveChoiceTableForPlayer(0, sb, results, game);
        writeMoveChoiceTableForPlayer(1, sb, results, game);
        sb.append("</div>\n");
        sb.append("<script>hideFirstMoves('"+moveTablesDivId+"');</script>\n");
    }

    private void writeMoveChoiceTableForPlayer(int roleIndex, StringBuilder sb,
            ListMultimap<List<String>, MatchResult> results, Game game) {
        NumberFormat numberFormat = getThreeDigitDecimalFormat();
        List<String> allFirstMovesForPlayer = getAllFirstMovesForPlayer(roleIndex, results);

        if (allFirstMovesForPlayer.size() == 0) {
            return;
        } else if (allFirstMovesForPlayer.size() == 1) {
            sb.append("<h4>First move choice for player "+(roleIndex + 1)+" is always \""+allFirstMovesForPlayer.get(0)+"\"</h4>\n");
            return;
        }

        sb.append("<h3>First move choice for player "+(roleIndex + 1)+"</h3>\n");
        ImmutableList<String> unparsedParameterValues = unparsedParameterValuesByGame().get(game);

        Map<String, Multiset<String>> countsByMoveByParamValue = getCountsByMoveByParamValue(roleIndex, results, game);

        // Rows are possible moves, columns are parameter settings, cells are probabilities
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
            ListMultimap<List<String>, MatchResult> results, Game game) {
        Map<String, Multiset<String>> countsByMoveByParamValue = Maps.newHashMap();
        for (String paramValue : unparsedParameterValuesByGame().get(game)) {
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

    public static ImmutableParameterChartExperiment.Builder build(String experimentName) {
        return ImmutableParameterChartExperiment.builder()
                .experimentName(experimentName);
    }
}
