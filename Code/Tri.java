import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
	public void plageHoraire(int jour, int debut, int fin, boolean libre) {

	}

	public void forcerUE(int id, boolean prise) {

	}
	public void resetUE(int id) {

	}

	public TreeSet<UE[]> triParId() {
		Collections.sort(UE.listeId);

		arbre = new TreeSet<>(UE.getComparator(displayGroup));

		enumeration(arbre);

		return arbre;
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
				final int dif = pointsOf(o1, id, points) - pointsOf(o2, id, points); 
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
					s = s + pointsOf(o1[i].getId(), id, points) - pointsOf(o2[i].getId(), id, points);
				}
				if(s!=0)
					return s;

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
				return -points[i];
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
	 * Enum�re toutes les combinaisons possibles de {@value #NB_UE} UE
	 * @param tree Les r�sulats sont stock�s dans un arbre de UE[NB_UE], l'ordonnement est d�fini par l'arbre lors de sa cr�ation
	 * @param tab tableau auxiliaire de NB_UE cases
	 * @param indMin mettre � 0 lors de l'appel
	 * @param profondeur mettre � 0 lors de l'appel
	 * La complexit� th�orique doit �tre O(nb_groupe ^ taille_tableau * log(nb_groupe ^ taille_tableau))
	 * Apr�s ex�cution, avec une taille d'arbre donn�e n, on sait qu'on a eu une complexit� de
	 * n*log(n)		pour les ajouts dans le tableau
	 * n*nb_groupe 	(au maximum) pour �num�rer les combinaisons
	 */
	private static void enumeration(TreeSet<UE[]> tree, UE[] tab, int indMin, int profondeur){		
		//inutile de tester avec les (length - profondeur) derni�res UE
		//car il ne resterait pas assez d'UE pour remplir tab
		final int indMax = UE.listeUE.size() + profondeur - tab.length + 1;

		//�num�ration avec toutes les UE disponibles
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
						//rend indisponibles les cr�neaux incompatibles
						HashMap<Integer, HashSet<Creneau>> m = u.prendre(index);

						UE.checkOk(tab,profondeur);//debug

						//appel r�cursif
						enumeration(tree, tab, i+1, profondeur+1);

						//re-rend disponibles les cr�neaux pr�c�dents
						u.undo(m);
					}
				}
			}
		}
	}




}
