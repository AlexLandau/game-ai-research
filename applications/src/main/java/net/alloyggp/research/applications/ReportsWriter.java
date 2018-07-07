package net.alloyggp.research.applications;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import net.alloyggp.research.Experiment;
import net.alloyggp.research.MatchResult;

public class ReportsWriter {

    public static void main(String[] args) throws IOException {
        List<Experiment> experiments = ExperimentRegistry.getExperiments();

        File reportsDir = Files.getReportsDirectory().getCanonicalFile();

        for (Experiment experiment : experiments) {
            long startTime = System.currentTimeMillis();
            List<MatchResult> results = MatchResults.loadAllResults(experiment.getName());
            System.out.println("Loaded all results for experiment " + experiment.getName() + " in " + (System.currentTimeMillis() - startTime) + " ms");
            startTime = System.currentTimeMillis();
            String html = experiment.writeHtmlOutput(results);

            File reportFile = new File(reportsDir, getExperimentFilename(experiment.getName()));
            Files.writeStringToFile(reportFile, html);
            System.out.println("Wrote report for experiment " + experiment.getName() + " in " + (System.currentTimeMillis() - startTime) + " ms");
        }

        File indexFile = new File(reportsDir, "index.html");
        String indexHtml = writeIndexHtml(experiments);
        Files.writeStringToFile(indexFile, indexHtml);

        System.out.println("Reports available at: " + indexFile.toURI());
    }

    private static String getExperimentFilename(String name) {
        String cleanedName = name.replace("/", "_")
                .replace("\\", "_")
                .toLowerCase();
        if (cleanedName.equalsIgnoreCase("index")) {
            throw new IllegalArgumentException("Shouldn't have an experiment named 'index'!");
        }
        return cleanedName + ".html";
    }

    private static String writeIndexHtml(List<Experiment> experiments) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><head><title>Game AI Research - Results</title></head>\n");
        sb.append("<body>\n");

        sb.append("<p>");
        sb.append("Reports last updated: " + LocalDateTime.now());
        sb.append("</p>\n");

        sb.append("<ul>\n");
        for (Experiment experiment : experiments) {
            String filename = getExperimentFilename(experiment.getName());
            sb.append("  <li>");
            sb.append("<a href=\"").append(filename).append("\">");
            sb.append(experiment.getName());
            sb.append("</a></li>\n");
        }
        sb.append("</ul>\n");

        sb.append("</body></html>\n");

        return sb.toString();
    }
}
