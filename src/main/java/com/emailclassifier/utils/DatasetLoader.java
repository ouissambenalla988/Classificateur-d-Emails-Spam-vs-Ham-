package com.emailclassifier.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for loading email datasets from directories.
 * Handles both spam and ham (non-spam) emails for training and testing purposes.
 */
public class DatasetLoader {
    private static final Logger logger = LogManager.getLogger(DatasetLoader.class);

    /**
     * Loads an email dataset from spam and ham directories.
     *
     * @param spamDir Directory containing spam emails
     * @param hamDir  Directory containing ham (non-spam) emails
     * @return A map where keys are categories ("spam", "ham") and values are arrays of email contents
     * @throws IOException If reading files fails
     */
    public Map<String, String[]> loadFromDirectories(File spamDir, File hamDir) throws IOException {
        logger.info("Loading dataset from directories: spam={}, ham={}",
                spamDir.getAbsolutePath(), hamDir.getAbsolutePath());

        Map<String, String[]> dataset = new HashMap<>();

        dataset.put("spam", loadEmailsFromDirectory(spamDir));
        logger.info("Loaded {} spam emails", dataset.get("spam").length);

        dataset.put("ham", loadEmailsFromDirectory(hamDir));
        logger.info("Loaded {} ham emails", dataset.get("ham").length);

        return dataset;
    }

    /**
     * Loads all email contents from a given directory.
     *
     * @param directory Directory containing email files
     * @return Array of email contents
     * @throws IOException If reading files fails
     */
    private String[] loadEmailsFromDirectory(File directory) throws IOException {
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Invalid directory: " + directory.getAbsolutePath());
        }

        List<String> emails = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files == null || files.length == 0) {
            logger.warn("No files found in directory: {}", directory.getAbsolutePath());
            return new String[0];
        }

        for (File file : files) {
            if (file.isFile()) {
                try {
                    String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                    emails.add(content);
                } catch (IOException e) {
                    logger.warn("Failed to read file: {}", file.getAbsolutePath(), e);
                }
            }
        }

        return emails.toArray(new String[0]);
    }

    /**
     * Checks whether a directory contains at least a minimum number of email samples.
     *
     * @param directory  Directory to check
     * @param minSamples Minimum required number of email files
     * @return true if directory contains enough files, false otherwise
     */
    public boolean hasEnoughSamples(File directory, int minSamples) {
        if (!directory.exists() || !directory.isDirectory()) {
            return false;
        }

        File[] files = directory.listFiles();
        return files != null && files.length >= minSamples;
    }
}
