package net.alloyggp.research.applications;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.ggp.base.util.files.FileUtils;

import net.alloyggp.research.Experiment;
import net.alloyggp.research.MatchResult;

public class ReportsWriter {

    public static void main(String[] args) throws IOException {
        List<Experiment> experiments = ExperimentRegistry.getExperiments();

        File reportsDir = Files.getReportsDirectory().getCanonicalFile();

        for (Experiment experiment : experiments) {
            List<MatchResult> results = MatchResults.loadAllResults(experiment.getName());
            String html = experiment.writeHtmlOutput(results);

            File reportFile = new File(reportsDir, getExperimentFilename(experiment.getName()));
            FileUtils.writeStringToFile(reportFile, html);
        }

        File indexFile = new File(reportsDir, "index.html");
        FileUtils.writeStringToFile(indexFile, writeIndexHtml(experiments));

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
