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
│   ├── hdfs-client-pod.yaml   # Pod client HDFS pour tests
│   ├── hdfs-namespace.yaml    # Namespace Kubernetes pour Hadoop
│   ├── hdfs-persistentvolume.yaml  # Volumes persistants pour HDFS
│   ├── hdfs-service.yaml      # Service Kubernetes pour HDFS
│   ├── scala-app-deployment.yaml   # Déploiement de l'application Scala
│   └── simple-hdfs.yaml       # Déploiement simplifié de HDFS
├── scripts/                   # Scripts d'automatisation
│   ├── cleanup-minikube.sh    # Nettoyage de l'environnement Minikube
│   ├── deploy-minikube.sh     # Déploiement sur Minikube
│   └── wait-for-hdfs.sh       # Script d'attente pour HDFS
├── src/                       # Code source Scala
│   └── main/
│       ├── resources/         # Fichiers de ressources
│       │   ├── application.properties  # Configuration de l'application
│       │   ├── test_file.csv  # Données de test CSV
│       │   ├── test_file.json # Données de test JSON
│       │   └── test_file_sep_diff.csv  # Données CSV avec séparateur différent
│       └── scala/fr/mosef/scala/template/
│           ├── config/        # Configuration
│           ├── hdfs/          # Connecteur HDFS
│           ├── job/           # Définition des jobs
│           ├── processor/     # Traitement des données
│           ├── reader/        # Lecture des fichiers
│           └── writer/        # Écriture des fichiers
├── docker-compose.yml         # Configuration Docker Compose
├── Dockerfile                 # Configuration Docker pour l'application
├── pom.xml                    # Configuration Maven
└── README.md                  # Ce fichier
```

## Build du projet
- Aller dans Maven > Lifecycle > package pour générer le .jar
- Penser à mettre à jour la version dans le fichier pom.xml avant de packager (version actuelle: 2.0.9)

## Déploiement via GitHub
- Pousser les modifications sur GitHub (dans ce cadre sur la branche main)
- GitHub Actions lancera automatiquement le pipeline de build
- Le .jar sera ensuite disponible en artifact téléchargeable

## Tester le .jar avec le système de fichiers local

### Depuis le GitHub
1. Cloner le repo et télécharger le .jar dans le dossier target du Repo
2. Via le terminal de commande, se placer dans le dossier target
3. Lancer la commande :
```
java -cp scala_template-2.0.9-jar-with-dependencies.jar fr.mosef.scala.template.Main local ../src/main/resources/test_file.csv ../src/main/output "," sum
```
   - Attention à bien choisir la version et les arguments à sélectionner (séparateur, transformation)

4. Si ça a bien fonctionné, dans le dossier output de resources, devrait apparaître le fichier demandé dans le format indiqué dans le `application.properties`

### En local
Prérequis : Dans un dossier local de test, vous devez avoir :
- Le .jar (ex: `scala_template-2.0.9-jar-with-dependencies.jar`)
- Un dossier `input/` contenant un fichier comme `test_file.csv`
- Un dossier `output/` vide

Exemple de commande :
```
java -cp scala_template-2.0.9-jar-with-dependencies.jar fr.mosef.scala.template.Main local input/test_file.csv output "," sum
```

Paramètres modifiables :
- `scala_template-2.0.9-jar-with-dependencies.jar` → adapter selon la version du .jar
- `","` → séparateur CSV (ex : ;, |, etc.)
- `sum` → transformation à appliquer (sum, groupby, sort, groupby_avg)

Résultat attendu :  
À la fin de l'exécution, le dossier output/ contiendra les fichiers correspondant à la transformation demandée dans le format indiqué dans `application.properties`.

## Utilisation avec HDFS

### Prérequis
- Docker installé sur votre machine

### Démarrer HDFS localement
1. Lancez les conteneurs HDFS avec Docker Compose :
```
docker compose up -d
```
2. Vérifiez que les conteneurs sont en cours d'exécution :
```
docker ps
```
3. Créez un répertoire et copiez les données de test dans HDFS :
```
docker exec hdfs-namenode hdfs dfs -mkdir -p /test
docker cp src/main/resources/test_file.csv hdfs-namenode:/tmp/
docker exec hdfs-namenode hdfs dfs -put /tmp/test_file.csv /test/
```

### Exécuter l'application avec HDFS
```
java -cp scala_template-2.0.9-jar-with-dependencies.jar fr.mosef.scala.template.Main local hdfs://localhost:9000/test/test_file.csv hdfs://localhost:9000/test/output "," sum true hdfs://localhost:9000
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

## Déploiement avec Kubernetes (Minikube)

### Prérequis
- Docker installé
- Minikube installé
- kubectl installé
- Maven installé

### Démarrer le cluster et déployer l'application
Utilisez le script de déploiement :
```
./scripts/deploy-minikube.sh
```
Ce script effectue automatiquement :
- Démarre Minikube
- Crée le namespace Hadoop
- Déploie un HDFS simplifié (`k8s/simple-hdfs.yaml`)
- Compile l'application Scala
- Construit l'image Docker
- Déploie l'application Scala (`k8s/scala-app-deployment.yaml`)
- Prépare un pod client HDFS et charge les données
- Affiche les logs de l'application

### Pour accéder à l'interface web HDFS
```
minikube service hdfs-simple -n hadoop
```

### Pour interagir avec l'application
```
kubectl exec -n hadoop -it $(kubectl get pod -n hadoop -l app=scala-app -o jsonpath='{.items[0].metadata.name}') -- bash
```

### Nettoyer l'environnement Kubernetes
```
./scripts/cleanup-minikube.sh
```

## Notes importantes

### Versions
- Version actuelle: 2.0.9
- À mettre à jour dans :
  - pom.xml
  - Dockerfile
  - Scripts et documentation

### Configuration
- Paramètres HDFS dans `application.properties`
- HDFS désactivé par défaut (`hdfs.enabled=false`)

### Kubernetes vs Docker Compose
- Pour faire des tests rapides: Docker Compose
- Pour un environnement type production: Kubernetes / Minikube
