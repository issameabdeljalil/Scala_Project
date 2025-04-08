Scala Project

- Une fois les codes Scala finalisés > Maven > LifeCycle > Package permet de créer le .jar de la version associée (Attention à bien changer la version dans le fichier .pom)

- Quand le .jar est bien dans le target, push sur le GitHub afin de passer par GitHub Action et obtenir les .jar sur GithUB

- Pour tester le .jar, se placer dans un dossier disposant
   - Du .jar
   - D'un dossier input diposant des fichier présent dans resources de ce GitHub
   - D'un dossier output vide

- Ouvrir un terminal dans ce dossier, lancer la commande java -cp scala_template-2.0.1-jar-with-dependencies.jar fr.mosef.scala.template.Main local input/test_file.csv output "," sum
   - On peut adapter la version du .jar en fonction de celle dont on dispose, le séparateur si l'on utilise un csv et la transformation à réaliser
 
- Si tout fonctionne bien, le Dossier Output devrait se remplir de 4 fichier dont celui que l'on a demandé en sortie.
