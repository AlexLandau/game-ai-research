package net.alloyggp.research.applications;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.alloyggp.research.MatchResult;

public class MatchResults {
    private MatchResults() {
        // Not instantiable
    }

    public static List<MatchResult> loadAllResults(String experimentName) throws IOException {
        File resultsDirectory = Files.getExperimentResultsDirectory(experimentName);
        // ...
        File resultsFile = new File(resultsDirectory, "results");
        if (!resultsFile.exists()) {
            return ImmutableList.of();
        }
        ObjectMapper objectMapper = createObjectMapper();

        List<MatchResult> results = Lists.newArrayList();
        try (BufferedReader reader = new BufferedReader(new FileReader(resultsFile))) {
            return reader.lines()
                .map(line -> {
                    try {
                        return objectMapper.readValue(line, MatchResult.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static void saveResult(MatchResult result) throws IOException {
        File resultsDirectory = Files.getExperimentResultsDirectory(result.getSpec().getExperimentName());

        // TODO: Better naming scheme?
        File resultsFile = new File(resultsDirectory, "results");
        ObjectMapper objectMapper = createObjectMapper();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultsFile, true))) {
            writer.write(objectMapper.writeValueAsString(result));
            writer.newLine();
        }
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new GuavaModule());
        return objectMapper;
    }
}
