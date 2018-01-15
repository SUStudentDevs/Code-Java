# ÉNUMÉRATION

### EXEMPLE
UPMC, année scolaire 2017/2018, L3 mono informatique. Des 15 UE proposées, nous de vons en choisir 5.
Algorithme à appliquer dans le cas où des combinaisons de matières ne sont pas possibles ("chevauchement").
![alt text](https://github.com/UPMCStudentDevs/Code-Java/blob/master/edt%20S6.png)


<img align="left" width="100" height="100" src="https://github.com/UPMCStudentDevs/Code-Java/blob/master/edt%20S6.png">


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
