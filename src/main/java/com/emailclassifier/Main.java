package com.emailclassifier;

import com.emailclassifier.gui.ClassifierGUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

/**
 * Main entry point for the Email Classifier application.
 * This application classifies emails as spam or non-spam using Apache OpenNLP.
 */
public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting Email Classifier application");
        
        // Use the Event Dispatch Thread for Swing applications
        SwingUtilities.invokeLater(() -> {
            try {
                // Set the system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Create and display the main GUI
                ClassifierGUI gui = new ClassifierGUI();
                gui.setVisible(true);
                
                logger.info("Application GUI initialized successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize application", e);
                JOptionPane.showMessageDialog(null, 
                    "Failed to start application: " + e.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
