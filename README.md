# Scala Project

## Build du projet

Une fois les codes Scala finalisés :

- Aller dans **Maven > Lifecycle > package** pour générer le `.jar`

- **Penser à mettre à jour la version** dans le fichier `pom.xml` avant de packager.

## Déploiement via GitHub

1. Pousser les modifications sur GitHub (dans ce cadre sur la branche alexis)
2. GitHub Actions lancera automatiquement le pipeline de build
3. Le `.jar` sera ensuite disponible en **artifact téléchargeable**

## Tester le `.jar` localement

### Prérequis

Dans un dossier local de test, tu dois avoir :

- Le `.jar` (ex: `scala_template-2.0.1-jar-with-dependencies.jar`)
- Un dossier `input/` contenant un fichier comme `test_file.csv` (extrait du dossier `resources`)
- Un dossier `output/` vide

### Exemple de commande

```bash
java -cp scala_template-2.0.1-jar-with-dependencies.jar fr.mosef.scala.template.Main local input/test_file.csv output "," sum
```

### Paramètres modifiables :

- `scala_template-2.0.1-jar-with-dependencies.jar` → adapter selon la version du `.jar`
- `","` → séparateur CSV (ex : `;`, `|`, etc.)
- `sum` → transformation à appliquer (`sum`, `count`, etc.)

## Résultat attendu

À la fin de l’exécution, le dossier `output/` contiendra **quatre fichiers** dont celui correspondant à la transformation demandée (ex : `.json`, `.parquet`, etc.)
