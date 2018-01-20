package com.sustudentdevs.algoues.edtTravaille;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import com.sustudentdevs.algoues.edtBrut.*;

public class Tri {
	private static final int NB_UE = 5;

	/**
	 * Si on permet � l'utilisateur d'obtenir cette r�f�rence, un changement
	 * des param�tres du tri (plages libres p.ex) rend l'arbre invalide
	 * jusqu'au prochain tri
	 */
	private TreeSet<UE[]> arbre;

	private boolean groupe;
	/**
	 * Les r�sultats sont plus pr�cis si le boolean est vrai
	 */
	public void differencieSelonGroupe(boolean b) {
		groupe=b;
	}

	private final LinkedList<Creneau> plages = new LinkedList<>();
	/**
	 * Emp�che les enseignements d'avoir lieu pendant ces plages
	 */
	public void ajoutPlageHoraire(int jour, int debut, int fin) {
		Creneau n = new Creneau(jour, debut, fin);
		for(Creneau c:plages)
			if (Creneau.gene(c, n))
				throw new RuntimeException("les plages horaires se chevauchent");

		plages.add(n);
	}

	/**
	 * Enl�ve les restrictions de plages horaires
	 */
	public void resetPlages() {
		plages.clear();
	}

	private final LinkedList<Integer> listeContenue = new LinkedList<>(); 
	private final LinkedList<Integer> listeNonContenue = new LinkedList<>(); 

	/**
	 * Force les r�sultats � contenir les UE choisies
	 */
	public void prendreUE(Integer id) {
		prendre(id, listeContenue, listeNonContenue);
	}

	/**
	 * Force les r�sultats � ne pas contenir les UE choisies
	 */
	public void nePasPrendre(Integer id) {
		prendre(id, listeNonContenue, listeContenue);
	}

	private static void prendre(Integer id, LinkedList<Integer> add, LinkedList<Integer> remove) {
		if(!UE.listeId.contains(id))
			throw new RuntimeException("n'existe pas");
		if(add.contains(id))
			throw new RuntimeException("d�j� prise");
		add.add(id);

		//attention � ne pas appeler remove(int) mais remove(Object);
		remove.remove(id);
	}

	/**
	 * Les r�sultats peuvent de nouveau contenir ou non cette UE
	 */
	public void resetUE(Integer id) {
		//attention � ne pas appeler remove(int) mais remove(Object);
		listeNonContenue.remove(id);
		listeContenue.remove(id);
	}

	/**
	 * Les r�sultats peuvent de nouveau contenir ou non toutes les UE
	 */
	public void resetAllUE() {
		listeContenue.clear();
		listeNonContenue.clear();
	}

	/**
	 * Prend en compte les options des variables membres
	 * et r�alise le tri des UE selon compUE
	 * puis g�n�re et trie les combinaisons selon compTab.
	 * A la fin, restore les bases de donn�es pour ne pas les corrompre
	 */
	private TreeSet<UE[]> tri(Comparator<Integer> compUE, Comparator<UE[]> compTab) {
		//enl�ve les UE non souhait�es de listeId seulement (et pas de listeUE)
		UE.listeId.removeAll(listeNonContenue);

		//tri listeId par l'ordre
		if(compUE==null)
			//naturel (croissant)
			Collections.sort(UE.listeId);
		else
			//fourni en argument
			UE.listeId.sort(compUE);

		//enl�ve les cr�neaux interf�rant avec nos plages libres.
		//en garde une r�f�rence pour les restaurer � la fin
		LinkedList<LinkedList<HashSet<Creneau>>> l = null;
		if(!plages.isEmpty()) {
			l = new LinkedList<>();
			for (Creneau c : plages) {
				l.add(UE.laisserLibre(c));
			}
		}

		//cr�e l'arbre avec le comparateur donn� en argument
		arbre = new TreeSet<>(compTab);

		//calcul la liste des solutions
		enumeration();

		//enl�ve les combinaisons ne poss�dant pas les UE souhait�es.

		Iterator<UE[]> it = arbre.iterator();
		//pour chaque combinaison
		while (it.hasNext()) {
			UE[] tabU = it.next();

			//pour chaque UE souhait�e
			for(Integer id : listeContenue) {
				boolean trouve = false;

				//la cherche dans le tableau
				for (UE ue : tabU) {
					if(ue.getId()==id) {
						trouve=true;
						break;
					}
				}

				//si non trouv�e, enl�ve la combinaison
				if(!trouve) {
					it.remove();
					break;
				}
			}
		}


		//restaure les cr�neaux chevauchant les plages libres
		if(!plages.isEmpty()) {
			for (LinkedList<HashSet<Creneau>> ll : l) {
				UE.undoLaisserLibre(ll);
			}
		}

		//restaure les UE non d�sir�es
		UE.listeId.addAll(listeNonContenue);

		return arbre;
	}


	/**
	 * Tri les UE par ordre croissant de leur id
	 */
	public TreeSet<UE[]> triParId() {

		//la variable membre afficheGroupe n'est pas d�finie pour le nouvel objet cr�� mais elle peut quand m�me �tre utilis�e cf:
		//https://en.wikipedia.org/wiki/Closure_(computer_programming)#Local_classes_and_lambda_functions_(Java)
		Comparator<UE[]> comparatorTab = new Comparator<UE[]>() {

			@Override
			public int compare(UE[] o1, UE[] o2) {

				//compare les id une par une et retourne � la premi�re diff�rence. Comme dans un vrai dictionnaire
				for (int i = 0; i < o1.length; i++) {
					final int dif = o1[i].getId() - o2[i].getId();
					if(dif!=0) {
						return dif;
					}
				}

				//compare les groupes si demand�
				if(groupe/*acces a group possible*/) {
					for (int i = 0; i < o1.length; i++) {
						final int difG = o1[i].getTdChoisi()-o2[i].getTdChoisi();
						if(difG!=0) {
							return difG;
						}
					}
				}

				//sinon, traiter les deux r�sulats comme les m�mes
				//(donc ne pas ajouter le deuxi�me � l'ensemble des r�sulats)
				return 0;
			}
		};

		return tri(null, comparatorTab);
	}


	/**
	 * voir {@link #triParUEPrioritaire(List)}
	 * prend les id dans un tableau
	 */
	public TreeSet<UE[]> triParUEPrioritaire(int[] a) {
		LinkedList<Integer> l = new LinkedList<>();
		for (int i : a) {
			l.add(i);
		}
		return triParUEPrioritaire(l);
	}

	/**
	 * voir {@link #triParUEPrioritaire(List)}
	 * prend les int les uns � la suite des autres
	 */
	public TreeSet<UE[]> triParUEPrioritaire(Integer... a) {
		return triParUEPrioritaire(Arrays.asList(a));
	}

	/**
	 * En ordonnant les id comme dans la liste pass�e en param�tre
	 * La liste est compl�t�e en ins�rant � la fin les id non renseign�s par ordre croissant
	 */
	public TreeSet<UE[]> triParUEPrioritaire(List<Integer> pref) {
		//tri les UE par leur index dans pref puis par id
		Comparator<Integer> comparatorId = new Comparator<Integer>() {
			@Override
			public int compare(Integer id1, Integer id2) {
				int ind1 = pref.indexOf(id1);
				int ind2 = pref.indexOf(id2);

				ind1=ind1==-1?Integer.MAX_VALUE:ind1;
				ind2=ind2==-1?Integer.MAX_VALUE:ind2;

				if(ind1-ind2 != 0)
					return ind1-ind2;

				return id1-id2;		
			}
		};


		Comparator<UE[]> comparatorTab = new Comparator<UE[]>() {
			@Override
			public int compare(UE[] o1, UE[] o2) {
				//compare les index
				for (int i = 0; i < o1.length; i++) {
					int ind1 = pref.indexOf(o1[i].getId());
					int ind2 = pref.indexOf(o2[i].getId());

					ind1=ind1==-1?Integer.MAX_VALUE:ind1;
					ind2=ind2==-1?Integer.MAX_VALUE:ind2;

					if(ind1-ind2 != 0)
						return ind1-ind2;
				}
				//les id
				for (int i = 0; i < o1.length; i++) {
					final int dif = o1[i].getId() - o2[i].getId();

					if(dif != 0)
						return dif;
				}
				//les groupes
				if(groupe){
					for (int i = 0; i < o1.length; i++) {
						final int difG = o1[i].getTdChoisi()-o2[i].getTdChoisi();
						if(difG!=0)
							return difG;
					}
				}
				return 0;
			}
		};
		return tri(comparatorId, comparatorTab);
	}

	/**
	 * commence par afficher les r�sultats dont la somme des points est la plus haute
	 * points[i] correspond au nombre de point associ� � l'UE id[i].
	 * par d�faut les UE comptent pour 0 points.
	 * Il est possible de mettre des valeurs n�gatives.
	 */
	public TreeSet<UE[]> triParPoints(int[] id, int[] points) {
		//tri les UE par ordre de points d�croissant
		Comparator<Integer> comparatorId = new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				final int dif = -pointsOf(o1, id, points) + pointsOf(o2, id, points); 
				if(dif!=0)
					return dif;
				return o1-o2;		
			}
		};

		Comparator<UE[]> comparatorTab = new Comparator<UE[]>() {
			@Override
			public int compare(UE[] o1, UE[] o2) {
				//compare la somme des points
				int s=0;
				for (int i = 0; i < o1.length; i++) {
					s = s - pointsOf(o1[i].getId(), id, points) + pointsOf(o2[i].getId(), id, points);
				}
				if(s!=0)
					return s;

				//puis les id un par un
				for (int i = 0; i < o1.length; i++) {
					final int dif = UE.listeId.indexOf(o1[i].getId()) - UE.listeId.indexOf(o2[i].getId());
					if(dif!=0)
						return dif;
				}

				//puis les groupes un par un
				if(groupe){
					for (int i = 0; i < o1.length; i++) {
						final int difG = o1[i].getTdChoisi()-o2[i].getTdChoisi();
						if(difG!=0)
							return difG;
					}
				}

				return 0;
			}
		};

		return tri(comparatorId, comparatorTab);
	}

	private static int pointsOf(int uId, int[] id, int[] points) {
		for (int i = 0; i < id.length; i++) {
			if(id[i]==uId)
				return points[i];
		}
		return 0;
	}



	/**
	 * helper function
	 */
	private void enumeration() {
		enumeration(new UE[NB_UE],0,0);
	}

	/**
	 * Enum�re toutes les combinaisons possibles de {@value #NB_UE} UE
	 * @param tree Les r�sulats sont stock�s dans un arbre de UE[NB_UE], l'ordonnement est d�fini par l'arbre lors de sa cr�ation
	 * @param tab tableau auxiliaire de NB_UE cases
	 * @param indMin mettre � 0 lors de l'appel
	 * @param profondeur mettre � 0 lors de l'appel
	 * La complexit� th�orique doit �tre O(nb_groupe ^ taille_tableau * log(nb_groupe ^ taille_tableau))
	 * Apr�s ex�cution, avec une taille d'arbre obtenue n, on sait qu'on a eu une complexit� de
	 * n*log(n)		pour les ajouts dans le tableau
	 * n*nb_groupe 	(au maximum) pour �num�rer les combinaisons
	 */
	private void enumeration(UE[] tab, int indMin, int profondeur){		
		//inutile de tester avec les (length - profondeur) derni�res UE
		//car il ne resterait pas assez d'UE pour remplir tab
		final int indMax = UE.listeId.size() + profondeur - tab.length + 1;

		//�num�ration avec toutes les UE disponibles
		for (int i=indMin; i<indMax; i++) {
			UE u = UE.listeUE.get(UE.listeId.get(i));

			if(u.isDisponible()) {
				tab[profondeur]=u;

				//appel terminal stocke tab dans arbre 
				if(profondeur == tab.length - 1) {
					u.ajoutChaqueGroupeDispo(arbre, tab);
				}
				else {
					for(int index : u.getIndicesLibres()) {
						//rend indisponibles les cr�neaux incompatibles
						LinkedList<HashSet<Creneau>> m = u.prendre(index);

						UE.checkOk(tab,profondeur);//debug

						//appel r�cursif
						enumeration(tab, i+1, profondeur+1);

						//re-rend disponibles les cr�neaux pr�c�dents
						u.undo(m);
					}
				}
			}
		}
	}

	/**
	 * affiche la liste des r�sultats.
	 * Il est possible d'effectuer un appel � {@link #differencieSelonGroupe(boolean)}
	 * apr�s avoir tri� afin de changer l'affichage
	 */
	public void afficheListe() {
		for (UE[] tabU : arbre) {
			UE.checkOk(tabU, tabU.length);//debug
			System.out.println(UE.toString(tabU, groupe));
		}
	}

	/**
	 * retourne le nombre d'entr�es dans l'arbre
	 */
	public void afficheTaille() {
		System.out.println("nb combinaisons possibles " + arbre.size());
	}

	/**
	 * retourne le nombre d'entr�es dans l'arbre.
	 */
	public int taille() {
		return arbre.size();
	}

	/**
	 * retourne le nombre de combinaisons th�oriques.
	 * Prend en compte les param�tres actuels (pas ceux du dernier tri effectu�)
	 */
	public int nbTheorique() {
		//ne consid�re pas les UE qu'on ne prend pas
		UE.listeId.removeAll(listeNonContenue);
		
		try {
			//nombre d'UE qu'on a d�j� pris
			final int nbDejaChoisies = listeContenue.size();
			
			if(groupe) {
				//tableau contenant le nombre de groupe pour chaque UE restante
				int[] nbGroupe = new int[UE.listeId.size()-nbDejaChoisies];
				
				//index dans le tableau
				int i=0;
				
				//nombre de possibilit� juste avec les UE choisies
				int p=1;
				
				for (int id : UE.listeId) {
					if(!listeContenue.contains(id))
						nbGroupe[i++]=UE.listeUE.get(id).getTd().size();
					else
						p *= UE.listeUE.get(id).getTd().size();
				}
				
				return p * sumProdKNbParmiN(0, 0, nbGroupe, new int[NB_UE-nbDejaChoisies]);
			}

			return kParmiN(NB_UE - nbDejaChoisies, UE.listeId.size() - nbDejaChoisies);
		}
		
		finally {
			//remet les UE
			UE.listeId.addAll(listeNonContenue);
		}
	}

	private static int kParmiN(int k, int n) {
		int r = 1;
		for(int i = 0; i < k ; i++){
			r *= n-i;
			r /= i+1;
		}
		return r;
	}

	/**
	 * @param indexMin		mettre � 0
	 * @param profondeur	mettre � 0
	 * @param nbGroupe		tableau de n cases contenant le nombre de groupe de chaque UE
	 * @param nbGroupeDesUEChoisies		tableau auxiliaire de k cases
	 */
	private static int sumProdKNbParmiN(int indexMin, int profondeur, int[] nbGroupe, int[] nbGroupeDesUEChoisies) {
		if(profondeur==nbGroupeDesUEChoisies.length) {
			int p=1;
			for (int nb:nbGroupeDesUEChoisies) {
				p*=nb;
			}
			return p;
		}

		int s=0;
		for (int i = indexMin; i < nbGroupe.length; i++) {
			nbGroupeDesUEChoisies[profondeur]=nbGroupe[i];
			s+=sumProdKNbParmiN(i+1, profondeur+1,nbGroupe,nbGroupeDesUEChoisies);
		}
		return s;
	}



}
