package com.emailclassifier.gui;

import java.awt.image.BufferedImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.awt.image.BufferedImage;

/**
 * Provides the application icon for the Email Classifier.
 */
public class AppIcon {
    private static final Logger logger = LogManager.getLogger(AppIcon.class);
    private static final String ICON_PATH = "/icon.svg";
    private static Image appIcon;

    /**
     * Gets the application icon image.
     * If the icon file cannot be loaded, returns a default generated icon.
     *
     * @return The application icon as an Image
     */
    public static Image getAppIcon() {
        if (appIcon == null) {
            try {
                URL iconUrl = AppIcon.class.getResource(ICON_PATH);
                if (iconUrl != null) {
                    ImageIcon icon = new ImageIcon(iconUrl);
                    appIcon = icon.getImage();
                } else {
                    // If icon file not found, create a simple generated icon
                    appIcon = createGeneratedIcon();
                    logger.warn("Icon file not found, using generated icon");
                }
            } catch (Exception e) {
                logger.error("Failed to load application icon", e);
                appIcon = createGeneratedIcon();
            }
        }
        return appIcon;
    }

    /**
     * Creates a simple programmatically generated icon if the icon file cannot be
     * loaded.
     *
     * @return A generated icon image
     */
    private static Image createGeneratedIcon() {
        // Create a simple envelope icon
        int size = 64;
        Image image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();

        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2d.setColor(new Color(52, 152, 219)); // Blue
        g2d.fillRoundRect(4, 8, size - 8, size - 16, 6, 6);

        // Envelope fold
        g2d.setColor(new Color(41, 128, 185)); // Darker blue
        int[] xPoints = { 4, size / 2, size - 4 };
        int[] yPoints = { 8, size / 2, 8 };
        g2d.fillPolygon(xPoints, yPoints, 3);

        // Spam indicator
        g2d.setColor(new Color(231, 76, 60)); // Red
        g2d.fillOval(size - 20, 4, 16, 16);

        // Text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2d.drawString("SPAM", 14, 40);

        g2d.dispose();
        return image;
    }
}
