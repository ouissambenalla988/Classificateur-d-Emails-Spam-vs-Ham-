package com.emailclassifier.utils;

import opennlp.tools.doccat.DoccatModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Utilitaire pour sauvegarder et charger des modèles OpenNLP.
 * Correction spéciale pour l'erreur DirectoryNotEmptyException.
 */
public class ModelIO {
    private static final Logger logger = LogManager.getLogger(ModelIO.class);
    
    /**
     * Sauvegarde un modèle DoccatModel dans un fichier.
     *
     * @param model Le modèle à sauvegarder
     * @param modelPath Chemin où le modèle devrait être sauvegardé
     * @throws IOException Si l'écriture échoue
     */
    public static void saveModel(DoccatModel model, String modelPath) throws IOException {
        logger.info("Sauvegarde du modèle dans {}", modelPath);
        
        // Vérifier si le modèle est valide
        if (model == null) {
            throw new IOException("Le modèle est null, impossible de sauvegarder");
        }
        
        // Vérifier si le chemin existe et s'il s'agit d'un répertoire
        File destination = new File(modelPath);
        if (destination.exists() && destination.isDirectory()) {
            // Le chemin spécifié est un répertoire, nous devons ajouter un nom de fichier
            modelPath = modelPath + File.separator + "email_classifier_model.bin";
            destination = new File(modelPath);
            logger.info("Le chemin spécifié est un répertoire, utilisation du chemin modifié: {}", modelPath);
        }
        
        // Créer le dossier parent si nécessaire
        File parentDir = destination.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                logger.warn("Impossible de créer les répertoires parents pour {}", modelPath);
            }
        }
        
        // Écrire directement dans le fichier de destination
        try (OutputStream modelOut = new BufferedOutputStream(new FileOutputStream(destination))) {
            model.serialize(modelOut);
            logger.info("Modèle sauvegardé avec succès dans {}", modelPath);
        } catch (IOException e) {
            logger.error("Échec de sauvegarde du modèle dans {} - {}", modelPath, e.getMessage());
            
            // Vérifier les permissions
            if (destination.exists() && !destination.canWrite()) {
                throw new IOException("Pas de permission d'écriture pour le fichier: " + modelPath, e);
            }
            
            // Vérifier si le dossier parent est accessible en écriture
            if (parentDir != null && !parentDir.canWrite()) {
                throw new IOException("Pas de permission d'écriture pour le répertoire: " + parentDir.getAbsolutePath(), e);
            }
            
            throw e;
        }
    }
    
    /**
     * Tente de sauvegarder un modèle à différents emplacements en cas d'échec.
     * @param model Le modèle à sauvegarder
     * @param preferredPath Chemin préféré pour la sauvegarde
     * @return Le chemin où le modèle a été sauvegardé
     * @throws IOException Si la sauvegarde échoue à tous les emplacements
     */
    public static String saveModelWithFallback(DoccatModel model, String preferredPath) throws IOException {
        // Générer un nom de fichier unique basé sur la date/heure
        String timestamp = String.valueOf(System.currentTimeMillis());
        String baseFileName = "email_classifier_model_" + timestamp + ".bin";
        
        // Essayer le chemin préféré en premier
        try {
            // Si le chemin préféré est un répertoire, nous utiliserons le chemin tel quel
            // La méthode saveModel s'occupera d'ajouter un nom de fichier si nécessaire
            saveModel(model, preferredPath);
            return preferredPath;
        } catch (IOException e) {
            logger.warn("Échec de sauvegarde à l'emplacement préféré: " + e.getMessage() + 
                      ". Tentative dans le dossier utilisateur");
            
            // Essayer le dossier documents de l'utilisateur
            try {
                String userHome = System.getProperty("user.home");
                String documentsPath = userHome + File.separator + "Documents";
                File documentsDir = new File(documentsPath);
                
                // Si Documents n'existe pas, utiliser le dossier utilisateur directement
                if (!documentsDir.exists() || !documentsDir.isDirectory()) {
                    documentsPath = userHome;
                }
                
                String homePath = documentsPath + File.separator + baseFileName;
                saveModel(model, homePath);
                return homePath;
            } catch (IOException e2) {
                logger.warn("Échec de sauvegarde dans le dossier utilisateur: " + e2.getMessage() + 
                          ". Tentative dans le dossier de l'application");
                
                // Essayer le dossier courant de l'application
                try {
                    String currentDirPath = "." + File.separator + baseFileName;
                    saveModel(model, currentDirPath);
                    return currentDirPath;
                } catch (IOException e3) {
                    logger.warn("Échec de sauvegarde dans le dossier de l'application: " + e3.getMessage() + 
                              ". Tentative dans le dossier temporaire");
                    
                    // Dernière tentative: dossier temporaire
                    try {
                        String tempDir = System.getProperty("java.io.tmpdir");
                        String tempPath = tempDir + File.separator + baseFileName;
                        saveModel(model, tempPath);
                        return tempPath;
                    } catch (IOException e4) {
                        logger.error("Échec de toutes les tentatives de sauvegarde", e4);
                        throw new IOException("Impossible de sauvegarder le modèle à aucun emplacement. " +
                                           "Dernière erreur: " + e4.getMessage(), e4);
                    }
                }
            }
        }
    }
    
    /**
     * Vérifie si un fichier modèle existe.
     *
     * @param modelPath Chemin à vérifier
     * @return true si le fichier existe, false sinon
     */
    public static boolean modelExists(String modelPath) {
        File file = new File(modelPath);
        return file.exists() && file.isFile();
    }
    
    /**
     * Charge un modèle DoccatModel à partir d'un fichier.
     *
     * @param modelPath Chemin du fichier modèle
     * @return Le modèle chargé
     * @throws IOException Si le chargement échoue
     */
    public static DoccatModel loadModel(String modelPath) throws IOException {
        logger.info("Chargement du modèle depuis {}", modelPath);
        
        File modelFile = new File(modelPath);
        
        // Vérifier si le chemin est un répertoire
        if (modelFile.exists() && modelFile.isDirectory()) {
            // Chercher un fichier .bin dans ce répertoire
            File[] binFiles = modelFile.listFiles((dir, name) -> name.endsWith(".bin"));
            if (binFiles != null && binFiles.length > 0) {
                // Utiliser le premier fichier .bin trouvé
                modelFile = binFiles[0];
                logger.info("Le chemin est un répertoire, utilisation du fichier: {}", modelFile.getAbsolutePath());
            } else {
                throw new IOException("Le chemin spécifié est un répertoire et ne contient pas de fichier modèle .bin");
            }
        }
        
        // Vérifier si le fichier existe
        if (!modelFile.exists()) {
            throw new IOException("Le fichier modèle n'existe pas: " + modelPath);
        }
        
        // Vérifier les permissions de lecture
        if (!modelFile.canRead()) {
            throw new IOException("Pas de permission de lecture pour le fichier: " + modelPath);
        }
        
        // Charger le modèle
        try (InputStream modelIn = Files.newInputStream(modelFile.toPath())) {
            DoccatModel model = new DoccatModel(modelIn);
            logger.info("Modèle chargé avec succès depuis {}", modelFile.getAbsolutePath());
            return model;
        } catch (IOException e) {
            logger.error("Échec du chargement du modèle depuis {} - {}", modelFile.getAbsolutePath(), e.getMessage());
            throw new IOException("Échec du chargement du modèle: " + e.getMessage(), e);
        }
    }
}