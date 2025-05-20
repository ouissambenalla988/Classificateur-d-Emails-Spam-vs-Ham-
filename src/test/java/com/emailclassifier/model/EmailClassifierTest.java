package com.emailclassifier.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmailClassifier.
 */
public class EmailClassifierTest {
    private EmailClassifier classifier;
    
    @BeforeEach
    public void setUp() {
        classifier = new EmailClassifier();
    }
    
    @Test
    public void testModelTrainedFlag() {
        // Initially the model should not be trained
        assertFalse(classifier.isModelTrained());
    }
    
    @Test
    public void testSimpleTrainAndClassify() throws IOException {
        // Create a simple test dataset
        Map<String, String[]> dataset = new HashMap<>();
        
        // Spam examples
        dataset.put("spam", new String[] {
            "Buy now! Limited offer on viagra and other pills.",
            "Congratulations! You've won $1,000,000 in lottery.",
            "URGENT: Your bank account will be suspended.",
            "Amazing investment opportunity, 500% return guaranteed!",
            "Free money, click here to claim your prize now!"
        });
        
        // Ham examples
        dataset.put("ham", new String[] {
            "Meeting scheduled for tomorrow at 10 AM.",
            "Please review the attached document and provide feedback.",
            "Your monthly invoice is attached for review.",
            "Hello, how are you doing? Let's catch up sometime.",
            "The project deadline has been extended to next Friday."
        });
        
        // Train model with progress tracking
        double accuracy = classifier.train(dataset, progress -> {
            // Just assert that progress values are within expected range
            assertTrue(progress >= 0.0 && progress <= 1.0);
        });
        
        // After training, the model should be trained
        assertTrue(classifier.isModelTrained());
        
        // Accuracy should be reasonable for this simple dataset
        // Since we're using a very small dataset, we don't expect
        // extremely high accuracy but it should be better than random
        assertTrue(accuracy > 0.5, "Accuracy should be better than random guessing");
        
        // Test classification of a typical spam message
        Map<String, Double> spamResult = classifier.classify("Congratulations! You won a free prize worth $10,000");
        assertTrue(spamResult.containsKey("spam"));
        assertTrue(spamResult.containsKey("ham"));
        assertTrue(spamResult.get("spam") > spamResult.get("ham"), "Spam message should be classified as spam");
        
        // Test classification of a typical ham message
        Map<String, Double> hamResult = classifier.classify("Hi John, can we schedule a meeting for tomorrow?");
        assertTrue(hamResult.containsKey("spam"));
        assertTrue(hamResult.containsKey("ham"));
        assertTrue(hamResult.get("ham") > hamResult.get("spam"), "Ham message should be classified as ham");
    }
}
