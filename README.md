Scala Project

================================================================================
Build du projet
================================================================================

Une fois les codes Scala finalisés :

1. Aller dans Maven > Lifecycle > package pour générer le fichier .jar.
2. Mettre à jour la version dans le fichier pom.xml avant de packager.

================================================================================
Déploiement via GitHub
================================================================================

1. Pousser les modifications sur GitHub (dans ce cadre sur la branche alexis).
2. GitHub Actions lancera automatiquement le pipeline de build.
3. Le fichier .jar sera ensuite disponible en artifact téléchargeable.

================================================================================
Tester le .jar directement depuis GitHub
================================================================================

1. Cloner le repo et télécharger le fichier .jar dans le dossier target du repo.
2. Via le terminal de commande, se placer dans le dossier target.
3. Lancer la commande :
   java -cp scala_template-2.0.2-jar-with-dependencies.jar fr.mosef.scala.template.Main local ../src/main/resources/test_file.csv ../src/main/output "," sum
   - Attention : Veillez à bien choisir la version et les arguments à sélectionner (séparateur, transformation).
4. Si tout fonctionne correctement, le fichier demandé apparaîtra dans le dossier output des ressources, dans le format indiqué dans le fichier application.properties.

================================================================================
Tester le .jar localement
================================================================================

Prérequis
---------

Dans un dossier local de test, vous devez avoir :

- Le fichier .jar (ex : scala_template-2.0.1-jar-with-dependencies.jar).
- Un dossier input/ contenant un fichier comme test_file.csv (extrait du dossier resources).
- Un dossier output/ vide.

Exemple de commande
-------------------

java -cp scala_template-2.0.1-jar-with-dependencies.jar fr.mosef.scala.template.Main local input/test_file.csv output "," sum

Paramètres modifiables
----------------------

- scala_template-2.0.1-jar-with-dependencies.jar : Adapter selon la version du fichier .jar.
- "," : Séparateur CSV (ex : ;, |, etc.).
- sum : Transformation à appliquer (sum, count, etc.).

================================================================================
Résultat attendu
================================================================================

À la fin de l’exécution, le dossier output/ contiendra quatre fichiers dont celui correspondant à la transformation demandée (ex : .json, .parquet, etc.).
