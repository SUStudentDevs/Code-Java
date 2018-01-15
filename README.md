# ÉNUMÉRATION

### MISE EN SITUATION
(UPMC, année scolaire 2017/2018, L3 S6 mono informatique, logiciel utilisé : Novotip).

Ayant seulement à disposition l'ensemble des créneaux horaires des 15 UE disponibles (cf https://github.com/UPMCStudentDevs/Code-Java/blob/master/edt%20S6.png), nous devons choisir 5 UE à suivre.

Théoriquement, il y a 5 parmi 15 soit 3003 combinaisons possibles.

En pratique, seulement 1066 combinaisons fonctionnent, __soit 65% de chance qu'une demande naïve de 5 UE soit refusée.__

Considérons maintenant le choix des groupes. Une fois nos 5 UE choisies, il faut choisir un groupe de TD pour chaque UE. (Le nombre de groupe varie selon les UE).

Théoriquement, pour une combinaison de 5 UE, le nombre de possibilités est égal au produit du nombre de groupe de chaque UE. Dans notre cas, cela donne 135 368 possibilités.
En pratique, il y en a seulement 4635 (3,4%).


Algorithme à appliquer dans le cas où des combinaisons de matières ne sont pas possibles ("chevauchement").


## ENTRÉE : 
- Liste des UE
- Pour chaque UE, créneaux horaires de son amphi et de ses groupes de TD


## SORTIE :
- Liste des combinaisons de 5 UE, telles que pour toute UE, on puisse assister à son amphi et appartenir à au moins un groupe de TD
- Pour chaque combinaison d'UE, choix possibles de groupe de TD


#### REMARQUES :
- Tous les groupes de TD sont supposés libres.
- Il est donc inutile (mais pas incorrect) de fournir en entrée deux TD de la même UE ayant lieu au même instant.
- Dans un deuxième temps, étant donné la liste exhaustive des TD et des amphis ainsi que les listes de choix des étudiants, un deuxième algorithme retournera le voeu accordé à chaque étudiant.


#### TODO :
- Permettre à l'utilisateur d'ordonner les résultats par ordre de préférence d'UE
- Gérer des UE à 3 ECTS
- Gérer les étudiants en UE projet
- Gérer des UE possédant plusieurs créneaux pour leur amphi
- Gérer des groupes de TD pour lesquels les créneaux ne sont pas consécutifs
- Gérer les majeur, mineur, double majeur
