import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

public class Tri {
	private TreeSet<UE[]> arbre;

	private boolean displayGroup;
	public void setDisplayGroup(boolean b) {
		displayGroup=b;
	}

	private final LinkedList<Creneau> plages = new LinkedList<>();
	public void plageHoraire(int jour, int debut, int fin) {
		Creneau n = new Creneau(jour, debut, fin);
		for(Creneau c:plages) {
			if (Creneau.gene(c, n)) {
				throw new RuntimeException();
			}
		}
		plages.add(n);
	}

	private final LinkedList<Integer> listeContenue = new LinkedList<>(); 
	private final LinkedList<Integer> listeNonContenue = new LinkedList<>(); 

	public void forcerUE(int id, boolean prise) {
		if(prise) {
			if(listeContenue.contains(id))
				throw new RuntimeException("déjà donnée");
			listeNonContenue.remove(id);
			listeContenue.add(id);
		}
		else {
			if(listeNonContenue.contains(id))
				throw new RuntimeException("déjà donnée");
			listeContenue.remove(id);
			listeNonContenue.add(id);
		}
	}

	public void resetUE(int id) {
		listeNonContenue.remove(id);
		listeContenue.remove(id);
	}

	private TreeSet<UE[]> tri(Comparator<Integer> compUE, Comparator<UE[]> compTab) {
		for (Integer id : listeNonContenue) {
			UE.listeId.remove(id);
		}

		if(compUE==null)
			Collections.sort(UE.listeId);
		else
			Collections.sort(UE.listeId, compUE);

		arbre = new TreeSet<>(compTab);

		enumeration(arbre);


		Iterator<UE[]> it = arbre.iterator();
		while (it.hasNext()) {
			UE[] tabU = it.next();
			for(Integer id : listeContenue) {
				boolean trouve=false;
				for (UE ue : tabU) {
					if(ue.getId()==id) {
						trouve=true;
						break;
					}
				}
				if(!trouve) {
					it.remove();
					break;
				}
			}
		}

		for (Integer id : listeNonContenue) {
			UE.listeId.add(id);
		}

		return arbre;
	}

	public TreeSet<UE[]> triParId() {
		Collections.sort(UE.listeId);

		arbre = new TreeSet<>(UE.getComparator(displayGroup));

		enumeration(arbre);

		return arbre;
	}


	public TreeSet<UE[]> triParUEPrioritaire(int[] a) {
		List<Integer> l = new LinkedList<>();
		for (int i : a) {
			l.add(i);
		}
		return triParUEPrioritaire(l);
	}

	public TreeSet<UE[]> triParUEPrioritaire(Integer... a) {
		return triParUEPrioritaire(Arrays.asList(a));
	}

	public TreeSet<UE[]> triParUEPrioritaire(List<Integer> pref) {
		Collections.sort(UE.listeId, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				int i1 = pref.indexOf(o1);
				int i2 = pref.indexOf(o2);

				i1=i1==-1?Integer.MAX_VALUE:i1;
				i2=i2==-1?Integer.MAX_VALUE:i2;

				if(i1-i2 != 0)
					return i1-i2;

				return o1-o2;		
			}
		});

		Comparator<UE[]> comparator = new Comparator<UE[]>() {
			@Override
			public int compare(UE[] o1, UE[] o2) {
				for (int i = 0; i < o1.length; i++) {
					final int id1 = o1[i].getId();
					final int id2 = o2[i].getId();

					int i1 = pref.indexOf(id1);
					int i2 = pref.indexOf(id2);

					i1=i1==-1?Integer.MAX_VALUE:i1;
					i2=i2==-1?Integer.MAX_VALUE:i2;

					if(i1-i2 != 0)
						return i1-i2;
					if(id1-id2 != 0)
						return id1-id2;
				}
				if(displayGroup){
					for (int i = 0; i < o1.length; i++) {
						final int difG = o1[i].getTDChoisi()-o2[i].getTDChoisi();
						if(difG!=0)
							return difG;
					}
				}
				return 0;
			}
		};

		arbre = new TreeSet<>(comparator);
		enumeration(arbre);
		return arbre;
	}


	public TreeSet<UE[]> triParPoints(int[] id, int[] points) {
		Collections.sort(UE.listeId, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				final int dif = -pointsOf(o1, id, points) + pointsOf(o2, id, points); 
				if(dif!=0)
					return dif;
				return o1-o2;		
			}
		});

		Comparator<UE[]> comparator = new Comparator<UE[]>() {
			@Override
			public int compare(UE[] o1, UE[] o2) {
				int s=0;
				for (int i = 0; i < o1.length; i++) {
					s = s - pointsOf(o1[i].getId(), id, points) + pointsOf(o2[i].getId(), id, points);
				}
				if(s!=0)
					return s;

				for (int i = 0; i < o1.length; i++) {
					final int dif = UE.listeId.indexOf(o1[i].getId()) - UE.listeId.indexOf(o2[i].getId());
					if(dif!=0)
						return dif;
				}

				if(displayGroup){
					for (int i = 0; i < o1.length; i++) {
						final int difG = o1[i].getTDChoisi()-o2[i].getTDChoisi();
						if(difG!=0)
							return difG;
					}
				}

				return 0;
			}
		};

		arbre = new TreeSet<>(comparator);
		enumeration(arbre);
		return arbre;
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
	 * @param tree
	 */
	private static void enumeration(TreeSet<UE[]> tree) {
		enumeration(tree,new UE[Main.NB_UE],0,0);
	}

	/**
	 * Enumère toutes les combinaisons possibles de {@value #NB_UE} UE
	 * @param tree Les résulats sont stockés dans un arbre de UE[NB_UE], l'ordonnement est défini par l'arbre lors de sa création
	 * @param tab tableau auxiliaire de NB_UE cases
	 * @param indMin mettre à 0 lors de l'appel
	 * @param profondeur mettre à 0 lors de l'appel
	 * La complexité théorique doit être O(nb_groupe ^ taille_tableau * log(nb_groupe ^ taille_tableau))
	 * Après exécution, avec une taille d'arbre donnée n, on sait qu'on a eu une complexité de
	 * n*log(n)		pour les ajouts dans le tableau
	 * n*nb_groupe 	(au maximum) pour énumérer les combinaisons
	 */
	private static void enumeration(TreeSet<UE[]> tree, UE[] tab, int indMin, int profondeur){		
		//inutile de tester avec les (length - profondeur) dernières UE
		//car il ne resterait pas assez d'UE pour remplir tab
		final int indMax = UE.listeUE.size() + profondeur - tab.length + 1;

		//énumération avec toutes les UE disponibles
		for (int i=indMin; i<indMax; i++) {
			UE u = UE.listeUE.get(UE.listeId.get(i));

			if(u.isDisponible()) {
				tab[profondeur]=u;

				//appel terminal stocke tab dans arbre 
				if(profondeur == tab.length - 1) {
					u.ajoutChaqueGroupeDispo(tree, tab);
				}
				else {
					for(int index : u.getIndicesLibres()) {
						//rend indisponibles les créneaux incompatibles
						HashMap<Integer, HashSet<Creneau>> m = u.prendre(index);

						UE.checkOk(tab,profondeur);//debug

						//appel récursif
						enumeration(tree, tab, i+1, profondeur+1);

						//re-rend disponibles les créneaux précédents
						u.undo(m);
					}
				}
			}
		}
	}




}
