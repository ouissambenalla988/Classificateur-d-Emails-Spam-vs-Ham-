#!/bin/bash

# Script pour compiler et lancer l'application de classification d'emails

# Créer les répertoires nécessaires
mkdir -p target
mkdir -p logs

echo "Compilation du projet..."
javac -d target -cp ".:lib/*" $(find src -name "*.java")

if [ $? -eq 0 ]; then
    echo "Compilation réussie! Lancement de l'application..."
    java -cp "target:lib/*" com.emailclassifier.Main
else
    echo "La compilation a échoué. Veuillez vérifier les erreurs ci-dessus."
fi