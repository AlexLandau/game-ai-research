package net.alloyggp.research.applications;

import java.io.File;

public class Files {
    private Files() {
        // Not instantiable
    }

    public static File getExperimentResultsDirectory(String experimentName) {
        File gitRoot = findGitRoot();
        File resultsDir = new File(gitRoot, "results");
        ensureDirectoryExists(resultsDir);
        File experimentResultsDir = new File(resultsDir, experimentName);
        ensureDirectoryExists(experimentResultsDir);
        return experimentResultsDir;
    }

    private static File findGitRoot() {
        File curDir = new File(".");
        while (curDir.exists()) {
            File gitDir = new File(curDir, ".git");
            if (gitDir.isDirectory()) {
                return curDir;
            }
            curDir = new File(curDir, "..");
        }
        throw new IllegalStateException("Couldn't find a .git folder indicating the location of the git repository root in the ancestors of: " + new File(".").getAbsolutePath());
    }

    private static void ensureDirectoryExists(File dir) {
        String name = dir.getName();
        String parent = dir.getAbsoluteFile().getParent();
        if (!dir.exists()) {
            boolean success = dir.mkdir();
            if (!success) {
                throw new RuntimeException("Failed to make the '" + name + "' directory in: " + parent);
            }
        } else if (!dir.isDirectory()) {
            throw new RuntimeException("Can't make the '" + name + "' directory in " + parent + " because a non-directory file already exists there for some reason.");
        }
    }

}
