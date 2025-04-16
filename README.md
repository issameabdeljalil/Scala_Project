# SCALA PROJECT

## Fonctionnalités
Ce projet Scala permet de traiter des données via Apache Spark avec diverses transformations (somme, groupement, tri). Il prend en charge plusieurs formats de fichiers (CSV, JSON, Parquet) et fonctionne avec:
- Système de fichiers local
- HDFS (Hadoop Distributed File System)

## Architecture du projet
```
Scala_Project/
├── .github/workflows/         # Workflows CI/CD
├── docker/                    # Configuration Docker pour HDFS
├── k8s/                       # Définitions Kubernetes
├── src/                       # Code source Scala
│   └── main/
│       ├── resources/         # Fichiers de ressources
│       └── scala/fr/mosef/scala/template/
│           ├── config/        # Configuration
│           ├── hdfs/          # Connecteur HDFS
│           ├── job/           # Définition des jobs
│           ├── processor/     # Traitement des données
│           ├── reader/        # Lecture des fichiers
│           └── writer/        # Écriture des fichiers
├── docker-compose.yml         # Configuration Docker Compose
├── pom.xml                    # Configuration Maven
└── README.md                  # Ce fichier
```

## Build du projet
* Aller dans Maven > Lifecycle > package pour générer le .jar
* Penser à mettre à jour la version dans le fichier pom.xml avant de packager (version actuelle: 2.0.7)

## Déploiement via GitHub
1. Pousser les modifications sur GitHub (dans ce cadre sur la branche main)
2. GitHub Actions lancera automatiquement le pipeline de build
3. Le .jar sera ensuite disponible en artifact téléchargeable

## Tester le .jar avec le système de fichiers local

### Depuis le GitHub
1. Cloner le repo et télécharger le .jar dans le dossier target du Repo
2. Via le terminal de commande, se placer dans le dossier target
3. Lancer la commande : 
```
java -cp scala_template-2.0.7-jar-with-dependencies.jar fr.mosef.scala.template.Main local ../src/main/resources/test_file.csv ../src/main/output "," sum
```
   * Attention à bien choisir la version et les arguments à sélectionner (séparateur, transformation)
4. Si ça a bien fonctionné, dans le dossier output de resources, devrait apparaître le fichier demandé dans le format indiqué dans le application.properties

### En local
Prérequis: Dans un dossier local de test, vous devez avoir :
* Le .jar (ex: scala_template-2.0.7-jar-with-dependencies.jar)
* Un dossier input/ contenant un fichier comme test_file.csv (extrait du dossier resources)
* Un dossier output/ vide

Exemple de commande: 
```
java -cp scala_template-2.0.7-jar-with-dependencies.jar fr.mosef.scala.template.Main local input/test_file.csv output "," sum
```

Paramètres modifiables :
* scala_template-2.0.7-jar-with-dependencies.jar → adapter selon la version du .jar
* "," → séparateur CSV (ex : ;, |, etc.)
* sum → transformation à appliquer (sum, groupby, sort, groupby_avg)

Résultat attendu :
À la fin de l'exécution, le dossier output/ contiendra les fichiers correspondant à la transformation demandée dans le format indiqué dans application.properties.

## Utilisation avec HDFS

### Prérequis
- Docker installé sur votre machine

### Démarrer HDFS localement
1. Lancez les conteneurs HDFS avec Docker Compose:
```
docker compose up -d
```

2. Vérifiez que les conteneurs sont en cours d'exécution:
```
docker ps
```

3. Créez un répertoire et copiez les données de test dans HDFS:
```
docker exec hdfs-namenode hdfs dfs -mkdir -p /test
docker cp src/main/resources/test_file.csv hdfs-namenode:/tmp/
docker exec hdfs-namenode hdfs dfs -put /tmp/test_file.csv /test/
```

### Exécuter l'application avec HDFS
```
java -cp scala_template-2.0.7-jar-with-dependencies.jar fr.mosef.scala.template.Main local hdfs://localhost:9000/test/test_file.csv hdfs://localhost:9000/test/output "," sum true hdfs://localhost:9000
```

### Vérifier les résultats dans HDFS
```
docker exec hdfs-namenode hdfs dfs -ls /test/output
docker exec hdfs-namenode hdfs dfs -cat /test/output/part*
```

### Arrêter l'environnement HDFS
```
docker compose down
```

## Prochaines étapes
- **Déploiement Kubernetes** : Les définitions Kubernetes (dans le dossier k8s/) peuvent être utilisées pour déployer l'application sur un cluster Kubernetes existant avec la commande `kubectl apply -f k8s/`.
- **Intégration CI/CD complète** : Étendre le workflow GitHub Actions pour déployer automatiquement sur un cluster Kubernetes.

## Notes sur les versions du package
- Version actuelle: 2.0.7
- Assurez-vous de mettre à jour la version dans pom.xml avant chaque nouvelle release.
