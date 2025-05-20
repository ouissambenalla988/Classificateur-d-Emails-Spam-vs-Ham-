package com.emailclassifier.gui;

import com.emailclassifier.model.EmailClassifier;
import com.emailclassifier.utils.DatasetLoader;
import com.emailclassifier.utils.ModelIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Main application GUI for the Email Classifier.
 * Provides interface for training models, classifying emails,
 * and viewing results.
 */
public class ClassifierGUI extends JFrame {
    private static final Logger logger = LogManager.getLogger(ClassifierGUI.class);
    
    private EmailClassifier classifier;
    private JTabbedPane tabbedPane;
    private JTextArea inputTextArea;
    private JTextArea resultTextArea;
    private JProgressBar trainingProgressBar;
    private JTextField spamFolderField;
    private JTextField hamFolderField;
    private JTextField modelSaveField;
    private JTextField modelLoadField;
    private JButton trainButton;
    private JButton classifyButton;
    private JButton clearButton;
    private JButton loadModelButton;
    private JButton saveModelButton;
    private JLabel accuracyLabel;
    private JLabel statusLabel;
    
    public ClassifierGUI() {
        classifier = new EmailClassifier();
        
        initComponents();
        setupLayout();
        addListeners();
        
        // Set frame properties
        setTitle("Email Spam Classifier");
        setIconImage(AppIcon.getAppIcon());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        tabbedPane = new JTabbedPane();
        
        // Classification panel components
        inputTextArea = new JTextArea();
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        classifyButton = new JButton("Classify");
        clearButton = new JButton("Clear");
        statusLabel = new JLabel("Status: Ready");
        
        // Training panel components
        spamFolderField = new JTextField(30);
        hamFolderField = new JTextField(30);
        modelSaveField = new JTextField(30);
        modelLoadField = new JTextField(30);
        trainButton = new JButton("Train Model");
        loadModelButton = new JButton("Load Model");
        saveModelButton = new JButton("Save Model");
        trainingProgressBar = new JProgressBar(0, 100);
        trainingProgressBar.setStringPainted(true);
        accuracyLabel = new JLabel("Accuracy: N/A");
    }
    
    private void setupLayout() {
        // Classification panel
        JPanel classificationPanel = new JPanel(new BorderLayout(10, 10));
        classificationPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new TitledBorder("Email Content"));
        inputPanel.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);
        
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(new TitledBorder("Classification Result"));
        resultPanel.add(new JScrollPane(resultTextArea), BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(classifyButton);
        controlPanel.add(clearButton);
        controlPanel.add(statusLabel);
        
        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        contentPanel.add(inputPanel);
        contentPanel.add(resultPanel);
        
        classificationPanel.add(contentPanel, BorderLayout.CENTER);
        classificationPanel.add(controlPanel, BorderLayout.SOUTH);
        
        // Training panel
        JPanel trainingPanel = new JPanel(new BorderLayout(10, 10));
        trainingPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel datasetPanel = new JPanel(new GridBagLayout());
        datasetPanel.setBorder(new TitledBorder("Dataset"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        datasetPanel.add(new JLabel("Spam Folder:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        datasetPanel.add(spamFolderField, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JButton spamBrowseButton = new JButton("Browse");
        datasetPanel.add(spamBrowseButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        datasetPanel.add(new JLabel("Ham Folder:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        datasetPanel.add(hamFolderField, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JButton hamBrowseButton = new JButton("Browse");
        datasetPanel.add(hamBrowseButton, gbc);
        
        JPanel modelPanel = new JPanel(new GridBagLayout());
        modelPanel.setBorder(new TitledBorder("Model"));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        modelPanel.add(new JLabel("Save Model To:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        modelPanel.add(modelSaveField, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JButton modelSaveBrowseButton = new JButton("Browse");
        modelPanel.add(modelSaveBrowseButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        modelPanel.add(new JLabel("Load Model From:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        modelPanel.add(modelLoadField, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JButton modelLoadBrowseButton = new JButton("Browse");
        modelPanel.add(modelLoadBrowseButton, gbc);
        
        JPanel trainingControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        trainingControlPanel.add(trainButton);
        trainingControlPanel.add(saveModelButton);
        trainingControlPanel.add(loadModelButton);
        
        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        progressPanel.add(trainingProgressBar, BorderLayout.CENTER);
        progressPanel.add(accuracyLabel, BorderLayout.EAST);
        
        JPanel trainingContentPanel = new JPanel();
        trainingContentPanel.setLayout(new BoxLayout(trainingContentPanel, BoxLayout.Y_AXIS));
        trainingContentPanel.add(datasetPanel);
        trainingContentPanel.add(Box.createVerticalStrut(10));
        trainingContentPanel.add(modelPanel);
        trainingContentPanel.add(Box.createVerticalStrut(10));
        trainingContentPanel.add(trainingControlPanel);
        trainingContentPanel.add(Box.createVerticalStrut(10));
        trainingContentPanel.add(progressPanel);
        
        trainingPanel.add(trainingContentPanel, BorderLayout.NORTH);
        
        // Add action listeners for browse buttons
        spamBrowseButton.addActionListener(e -> browseFolder(spamFolderField));
        hamBrowseButton.addActionListener(e -> browseFolder(hamFolderField));
        modelSaveBrowseButton.addActionListener(e -> browseSaveFile(modelSaveField));
        modelLoadBrowseButton.addActionListener(e -> browseLoadFile(modelLoadField));
        
        // Add panels to tabbed pane
        tabbedPane.addTab("Classify Email", classificationPanel);
        tabbedPane.addTab("Train Model", trainingPanel);
        
        // Add to main frame
        getContentPane().add(tabbedPane);
    }
    
    private void addListeners() {
        classifyButton.addActionListener(e -> classifyEmail());
        clearButton.addActionListener(e -> {
            inputTextArea.setText("");
            resultTextArea.setText("");
        });
        
        trainButton.addActionListener(e -> trainModel());
        loadModelButton.addActionListener(e -> loadModel());
        saveModelButton.addActionListener(e -> saveModel());
    }
    
    private void browseFolder(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void browseSaveFile(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("OpenNLP Model (*.bin)", "bin"));
        int result = fileChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".bin")) {
                path += ".bin";
            }
            textField.setText(path);
        }
    }
    
    private void browseLoadFile(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("OpenNLP Model (*.bin)", "bin"));
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void classifyEmail() {
        String emailContent = inputTextArea.getText().trim();
        
        if (emailContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter the email content to classify.",
                "Empty Input", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!classifier.isModelTrained()) {
            JOptionPane.showMessageDialog(this, 
                "Please train or load a model first.",
                "No Model", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        statusLabel.setText("Status: Classifying...");
        
        // Run classification in a separate thread to keep UI responsive
        SwingWorker<Map<String, Double>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Double> doInBackground() {
                return classifier.classify(emailContent);
            }
            
            @Override
            protected void done() {
                try {
                    Map<String, Double> result = get();
                    StringBuilder sb = new StringBuilder();
                    
                    double spamProb = result.getOrDefault("spam", 0.0);
                    double hamProb = result.getOrDefault("ham", 0.0);
                    
                    String classification = spamProb > hamProb ? "SPAM" : "HAM (Not Spam)";
                    
                    sb.append("Classification: ").append(classification).append("\n\n");
                    sb.append("Confidence scores:\n");
                    sb.append("  SPAM: ").append(String.format("%.2f%%", spamProb * 100)).append("\n");
                    sb.append("  HAM: ").append(String.format("%.2f%%", hamProb * 100));
                    
                    resultTextArea.setText(sb.toString());
                    statusLabel.setText("Status: Ready");
                } catch (Exception e) {
                    logger.error("Classification failed", e);
                    resultTextArea.setText("Classification failed: " + e.getMessage());
                    statusLabel.setText("Status: Error");
                }
            }
        };
        
        worker.execute();
    }
    
    private void trainModel() {
        String spamFolder = spamFolderField.getText().trim();
        String hamFolder = hamFolderField.getText().trim();
        
        if (spamFolder.isEmpty() || hamFolder.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please select both spam and ham folders.",
                "Missing Folders", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        File spamDir = new File(spamFolder);
        File hamDir = new File(hamFolder);
        
        if (!spamDir.exists() || !spamDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, 
                "Spam folder does not exist or is not a directory.",
                "Invalid Folder", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!hamDir.exists() || !hamDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, 
                "Ham folder does not exist or is not a directory.",
                "Invalid Folder", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Disable controls during training
        setTrainingControlsEnabled(false);
        trainingProgressBar.setValue(0);
        accuracyLabel.setText("Accuracy: N/A");
        
        // Run training in a separate thread to keep UI responsive
        SwingWorker<Double, Integer> worker = new SwingWorker<>() {
            @Override
            protected Double doInBackground() {
                try {
                    // Load dataset
                    publish(10);
                    DatasetLoader loader = new DatasetLoader();
                    Map<String, String[]> dataset = loader.loadFromDirectories(spamDir, hamDir);
                    
                    // Train model with progress updates
                    publish(20);
                    double accuracy = classifier.train(dataset, progress -> {
                        // Scale progress from 20-90%
                        publish(20 + (int)(progress * 70));
                    });
                    
                    publish(100);
                    return accuracy;
                } catch (Exception e) {
                    logger.error("Training failed", e);
                    throw new RuntimeException("Training failed: " + e.getMessage(), e);
                }
            }
            
            @Override
            protected void process(java.util.List<Integer> chunks) {
                int latestProgress = chunks.get(chunks.size() - 1);
                trainingProgressBar.setValue(latestProgress);
            }
            
            @Override
            protected void done() {
                try {
                    double accuracy = get();
                    accuracyLabel.setText("Accuracy: " + String.format("%.2f%%", accuracy * 100));
                    JOptionPane.showMessageDialog(ClassifierGUI.this, 
                        "Model training completed successfully!\nAccuracy: " + String.format("%.2f%%", accuracy * 100),
                        "Training Complete", 
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    logger.error("Training failed", e);
                    JOptionPane.showMessageDialog(ClassifierGUI.this, 
                        "Training failed: " + e.getCause().getMessage(),
                        "Training Error", 
                        JOptionPane.ERROR_MESSAGE);
                    trainingProgressBar.setValue(0);
                } finally {
                    setTrainingControlsEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    private void saveModel() {
        String modelPath = modelSaveField.getText().trim();
        
        if (modelPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please specify a path to save the model.",
                "Missing Path", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!classifier.isModelTrained()) {
            JOptionPane.showMessageDialog(this, 
                "Please train a model first.",
                "No Model", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Create directories if they don't exist
            Files.createDirectories(Paths.get(modelPath).getParent());
            
            // Save the model
            ModelIO.saveModel(classifier.getModel(), modelPath);
            
            JOptionPane.showMessageDialog(this, 
                "Model saved successfully!",
                "Model Saved", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            logger.error("Failed to save model", e);
            JOptionPane.showMessageDialog(this, 
                "Failed to save model: " + e.getMessage(),
                "Save Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadModel() {
        String modelPath = modelLoadField.getText().trim();
        
        if (modelPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please specify a path to load the model from.",
                "Missing Path", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        File modelFile = new File(modelPath);
        if (!modelFile.exists() || !modelFile.isFile()) {
            JOptionPane.showMessageDialog(this, 
                "Model file does not exist.",
                "Invalid File", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Load the model
            classifier.loadModel(modelPath);
            
            JOptionPane.showMessageDialog(this, 
                "Model loaded successfully!",
                "Model Loaded", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Update accuracy label if available
            accuracyLabel.setText("Accuracy: N/A (loaded model)");
        } catch (IOException e) {
            logger.error("Failed to load model", e);
            JOptionPane.showMessageDialog(this, 
                "Failed to load model: " + e.getMessage(),
                "Load Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void setTrainingControlsEnabled(boolean enabled) {
        trainButton.setEnabled(enabled);
        saveModelButton.setEnabled(enabled);
        loadModelButton.setEnabled(enabled);
        spamFolderField.setEnabled(enabled);
        hamFolderField.setEnabled(enabled);
        modelSaveField.setEnabled(enabled);
        modelLoadField.setEnabled(enabled);
    }
}
