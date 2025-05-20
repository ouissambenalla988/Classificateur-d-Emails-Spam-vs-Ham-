package com.emailclassifier.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import opennlp.tools.doccat.*;
import opennlp.tools.util.*;
import opennlp.tools.util.model.ModelUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * Main classifier for email spam detection using OpenNLP.
 */
public class EmailClassifier {
    private static final Logger logger = LogManager.getLogger(EmailClassifier.class);

    private DoccatModel model;
    private DocumentCategorizerME categorizer;
    private final EmailPreprocessor preprocessor;

    public EmailClassifier() {
        this.preprocessor = new EmailPreprocessor();
    }

    public boolean isModelTrained() {
        return model != null;
    }

    public DoccatModel getModel() {
        return model;
    }

    public double train(Map<String, String[]> dataset, Consumer<Double> progressCallback) throws IOException {
        logger.info("Starting model training...");

        List<DocumentSample> samples = new ArrayList<>();
        int totalDocs = dataset.values().stream().mapToInt(arr -> arr.length).sum();
        int processed = 0;
        double lastProgress = 0;

        for (Map.Entry<String, String[]> entry : dataset.entrySet()) {
            for (String text : entry.getValue()) {
                String preprocessed = preprocessor.preprocess(text);
                String[] tokens = preprocessor.tokenize(preprocessed);
                samples.add(new DocumentSample(entry.getKey(), tokens));
                processed++;

                double currentProgress = processed / (double) totalDocs * 0.5;
                if (currentProgress - lastProgress >= 0.05) {
                    lastProgress = currentProgress;
                    progressCallback.accept(currentProgress);
                }
            }
        }

        progressCallback.accept(0.5);

        Collections.shuffle(samples);
        int split = (int) (samples.size() * 0.8);
        List<DocumentSample> trainSet = samples.subList(0, split);
        List<DocumentSample> evalSet = samples.subList(split, samples.size());

        TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
        params.put(TrainingParameters.ITERATIONS_PARAM, 100);
        params.put(TrainingParameters.CUTOFF_PARAM, 3);

        try (ObjectStream<DocumentSample> stream = ObjectStreamUtils.createObjectStream(trainSet)) {
            model = DocumentCategorizerME.train("en", stream, params, new DoccatFactory());
        }

        this.categorizer = new DocumentCategorizerME(model);

        progressCallback.accept(0.9);
        double accuracy = evaluateModel(evalSet);
        progressCallback.accept(1.0);

        logger.info("Training completed with accuracy: {}", accuracy);
        return accuracy;
    }

    private double evaluateModel(List<DocumentSample> evalSamples) {
        if (categorizer == null) {
            logger.error("Categorizer is not initialized.");
            return 0;
        }

        int correct = 0;

        for (DocumentSample sample : evalSamples) {
            String expected = sample.getCategory();
            String[] tokens = sample.getText();
            String predicted = categorizer.getBestCategory(categorizer.categorize(tokens));
            if (expected.equals(predicted)) correct++;
        }

        return correct / (double) evalSamples.size();
    }

    public Map<String, Double> classify(String emailContent) {
        if (categorizer == null) {
            logger.error("Cannot classify. Model not loaded or trained.");
            throw new IllegalStateException("Model not trained or loaded.");
        }

        String text = preprocessor.preprocess(emailContent);
        String[] tokens = preprocessor.tokenize(text);
        double[] probs = categorizer.categorize(tokens);

        Map<String, Double> results = new LinkedHashMap<>();
        for (int i = 0; i < categorizer.getNumberOfCategories(); i++) {
            results.put(categorizer.getCategory(i), probs[i]);
        }

        logger.info("Classification result: {}", results);
        return results;
    }

    public void loadModel(String modelPath) throws IOException {
        logger.info("Attempting to load model from: {}", modelPath);

        Path path = Paths.get(modelPath);
        if (!Files.exists(path)) {
            logger.error("Model file not found: {}", modelPath);
            throw new IOException("Model file does not exist: " + modelPath);
        }

        try (InputStream in = Files.newInputStream(path)) {
            model = new DoccatModel(in);
            categorizer = new DocumentCategorizerME(model);
            logger.info("Model loaded successfully.");
        }
    }

    /**
     * Lists available models in the "models" directory and lets the user choose one.
     *
     * @param modelsDir Path to the directory containing model files
     * @return Path to the selected model file
     * @throws IOException if listing fails
     */
    public static String chooseModelInteractively(String modelsDir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(modelsDir), "*.bin")) {
            List<String> availableModels = new ArrayList<>();
            for (Path path : stream) {
                availableModels.add(path.toString());
            }

            if (availableModels.isEmpty()) {
                throw new IOException("No model found in directory: " + modelsDir);
            }

            if (availableModels.size() == 1) {
                return availableModels.get(0); // one model only, load it directly
            }

            // Multiple models: ask user
            System.out.println("Available models:");
            for (int i = 0; i < availableModels.size(); i++) {
                System.out.printf("[%d] %s%n", i + 1, availableModels.get(i));
            }

            Scanner scanner = new Scanner(System.in);
            int choice;
            do {
                System.out.print("Choose a model to load (1-" + availableModels.size() + "): ");
                while (!scanner.hasNextInt()) {
                    System.out.print("Invalid input. Enter a number: ");
                    scanner.next();
                }
                choice = scanner.nextInt();
            } while (choice < 1 || choice > availableModels.size());

            return availableModels.get(choice - 1);
        }
    }
}
