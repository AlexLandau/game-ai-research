package net.alloyggp.research.applications;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.ggp.base.util.files.FileUtils;

import net.alloyggp.research.Experiment;
import net.alloyggp.research.MatchResult;

// TODO: Add a Gradle task for this.
public class ReportsWriter {

    public static void main(String[] args) throws IOException {
        List<Experiment> experiments = ExperimentRegistry.getExperiments();

        File reportsDir = Files.getReportsDirectory();

        for (Experiment experiment : experiments) {
            List<MatchResult> results = MatchResults.loadAllResults(experiment.getName());
            String html = experiment.writeHtmlOutput(results);

            File reportFile = new File(reportsDir, getExperimentFilename(experiment.getName()));
            FileUtils.writeStringToFile(reportFile, html);
        }

        // TODO: Write an index.html with links to the different experiment reports
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
}
