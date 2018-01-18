import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;

public class UE{
	static final HashMap<Integer, UE> listeUE = new HashMap<>();
	static final ArrayList<Integer> listeId = new ArrayList<>();

	private final int id;
	private final Creneau amphi;
	private final LinkedList<Creneau> TD;

	private int TDChoisi;//variable utilisée pour énumérer les combinaisons

	private boolean disponible=true;//variable utilisée pour énumérer les combinaisons

	/**
	 * Tous les constructeurs de cette classe sont privés. Les UE doivent être créées via {@link #generateListUE}
	 */
	private UE(int i, int jour, int heure){
		id = i;
		amphi = new Creneau(jour, heure, heure+1, 0);
		TD = new LinkedList<>();
	}

	/**
	 * Crée la liste des UE
	 * Tous les tableaux doivent avoir la même taille
	 * Si le ième créneau est un amphi, mettre groupe[i] à 0
	 */
	public static void genererListeUE(int[] id, int[] jour, int[] heure, int[] groupe) {
		if(id.length != jour.length || id.length != heure.length || id.length != groupe.length)
			throw new RuntimeException("les tableaux n'ont pas la même taille");

		for (int i = 0; i < groupe.length; i++) {
			if(listeUE.containsKey(id[i]))
				listeUE.get(id[i]).add(jour[i],heure[i],groupe[i]);
			else {
				listeUE.put(id[i],new UE(id[i],jour[i],heure[i]));
				listeId.add(id[i]);
			}
		}
	}

	/**
	 * crée un créneau et l'ajoute à l'UE
	 */
	private void add(int jour, int heure, int groupe) {
		if(groupe==0)
			throw new RuntimeException("amphi déjà défini");
		for(Creneau c : TD)
			if(c.getGroupe()==groupe)
				throw new RuntimeException("groupe déjà défini");

		int i=0;
		while(i<TD.size() && TD.get(i).getGroupe()<groupe) {
			i++;
		}
		TD.add(i, new Creneau(jour, heure, heure+2,groupe));
	}



	/**
	 * @return la liste des index (dans TD) des créneaux libres.
	 * Les indexes sont décalés de 1 par rapport au numéro de TD.
	 */
	public LinkedList<Integer> getIndicesLibres() {
		LinkedList<Integer> l = new LinkedList<>();
		int i = 0;
		for(Creneau c : TD) {
			if(c.disponible)
				l.add(i);
			i++;
		}
		return l;
	}

	/**
	 * s'inscrit à un TD disponible d'une UE disponible
	 * @param indexTD retourné par {@link #getIndicesLibres()}
	 * @return objet à fournir à {@link #undo()} afin de rétablir la situation telle qu'avant cet appel de fonction 
	 */
	public HashMap<Integer, HashSet<Creneau>> prendre(int indexTD) {
		if(!disponible || !TD.get(indexTD).disponible)
			throw new RuntimeException("inscription à un TD ou UE indisponible");

		TDChoisi=indexTD;

		HashMap<Integer, HashSet<Creneau>> map= new HashMap<>();

		int indIni = listeId.indexOf(id);
		for (int i=indIni; i<listeId.size(); i++) {
				UE u = listeUE.get(listeId.get(i));
				HashSet<Creneau> set = null;
				if(u.disponible) {
					set = new HashSet<>();
					set.addAll(u.prevenirCreneauPris(amphi));
					if(u.disponible)
						set.addAll(u.prevenirCreneauPris(TDChoisi()));
				}
				map.put(u.id, set);
		}

		disponible=false;
		return map;
	}

	/**
	 * Met à jour (rend non disponible) les créneaux chevauchant le créneau pris.
	 * L'UE devient non disponible si son amphi ou tous ses TD ne sont plus disponibles.
	 * Si l'amphi est désactivé, la fonction retourne immédiatement et ne regarde pas les TD.
	 * @param	creneau n'appartenant pas à cette UE
	 * @return	l'ensemble des créneaux rendus non disponible : l'amphi ou des TD).
	 */
	public HashSet<Creneau> prevenirCreneauPris(Creneau creneau){
		if(!disponible)
			throw new RuntimeException("désactive créneaux d'une UE non dispo");

		if(TD.contains(creneau))
			throw new RuntimeException("désactive créneaux de l'UE choisie");

		HashSet<Creneau> set = new HashSet<>();

		if(Creneau.gene(creneau, amphi)) {
			disponible=false;
			amphi.disponible=false;
			set.add(amphi);
			return set;
		}

		//désactive l'UE et la réactive si un TD est dispo
		disponible=false;
		for (int i=0; i<TD.size(); i++) {
			if(TD.get(i).disponible) {
				if(Creneau.gene(creneau, TD.get(i))) {
					set.add(TD.get(i));
					TD.get(i).disponible=false;		
				}
				else {
					disponible=true;
				}
			}
		}

		return set;
	}


	/**
	 * annule un appel à {@link #prendre(int)}
	 * @param map objet retourné par {@link #prendre(int)}
	 */
	public void undo(HashMap<Integer, HashSet<Creneau>> map) {
		for(int keyId : map.keySet()) {
			listeUE.get(keyId).undo(map.get(keyId));
		}
		disponible=true;
	}


	/**
	 * remet les créneaux enlevés et la disponibilité
	 * @param creneauPris
	 */
	private void undo(HashSet<Creneau> creneauPris) {
		if(creneauPris == null)
			return;

		//remet les créneaux enlevés
		for (Creneau c : creneauPris)
			c.disponible=true;

		//creneauPris n'est pas vide donc l'UE était disponible
		disponible=true;
	}


	/**
	 * ajoute une entrée dans l'abre pour chaque groupe de TD disponible de l'UE.
	 * @param tab doit être rempli et contenir l'UE appelante en dernière position
	 */
	public void ajoutChaqueGroupeDispo(TreeSet<UE[]> tree, UE[] tab) {
		if(!disponible || this != tab[tab.length - 1])
			throw new RuntimeException("appel incorrect");

		for (int i = 0; i < TD.size(); i++) {
			if(TD.get(i).disponible) {
				TDChoisi = i;
				tree.add(deepCopy(tab));
				checkOk(tab, 5);
			}
		}
	}

	/**
	 * @return un nouveau tableau contenant une copie des objets
	 */
	public static UE[] deepCopy(UE[] liste) {
		UE[] n = new UE[liste.length];
		for (int i = 0; i < n.length; i++) {
			n[i]=new UE(liste[i]);
		}
		return n;
	}

	/**
	 * constructeur privé de copie, sert pour la fonction {@link #deepCopy()}}
	 */
	private UE(UE other){
		id = other.id;
		amphi = other.amphi;
		TD = other.TD;
		TDChoisi = other.TDChoisi;
	}


	/**
	 * @param group indique si il faut prendre en compte les groupes de TD
	 * @return un objet permettant de comparer 2 sélections d'UE
	 */
	public static Comparator<UE[]> getComparator(final boolean group) {

		//le boolean group n'est pas défini dans le nouvel objet créé mais il peut quand même être utilisé cf:
		//https://en.wikipedia.org/wiki/Closure_(computer_programming)#Local_classes_and_lambda_functions_(Java)
		return new Comparator<UE[]>() {
			@Override
			public int compare(UE[] o1, UE[] o2) {
				for (int i = 0; i < o1.length; i++) {
					final int dif = o1[i].id - o2[i].id;
					if(dif!=0)
						return dif;
				}

				if(group/*acces a group possible*/) {
					for (int i = 0; i < o1.length; i++) {
						final int difG = o1[i].TDChoisi-o2[i].TDChoisi;
						if(difG!=0)
							return difG;
					}
				}

				return 0;
			}
		};
	}

	@Override
	public boolean equals(Object obj) {
		if(obj.getClass()!=UE.class)
			return false;
		return ((UE)obj).id==id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * vérifie que les UE choisies ainsi que leur TD choisi ne se chevauchent pas
	 */
	public static boolean checkOk(UE[] liste, int taille) {
		//pour chaque couple d'UE choisie
		for (int i = 0; i < taille-1; i++) {
			for (int j = i+1; j < taille; j++) {

				//amphi et TDChoisi des deux UE
				Creneau[] tab = {liste[i].amphi, liste[i].TDChoisi(), liste[j].amphi, liste[j].TDChoisi()};

				//pour chaque couple de créneau
				for (int k = 0; k < tab.length-1; k++) {
					for (int l = k+1; l < tab.length; l++) {

						//regarde si il y a une collision
						if(Creneau.gene(tab[k], tab[l]))
							throw new RuntimeException(tab[k]+"\t"+tab[l]+" se chevauchent "+toString(liste, true));
					}
				}
			}
		}
		return true;
	}

	/**
	 * retourne le nombre de combinaisons théoriques
	 */
	public static int nbTheo(int nbAChoisir, boolean group) {
		if(group) {			
			int[] nbGroupe = new int[listeUE.size()];
			int i=0;
			for (UE u : listeUE.values()) {
				nbGroupe[i++]=u.TD.size();
			}
			int[] nbGroupeDesUEChoisies = new int[nbAChoisir];
			return sum_prod_k_nb_parmi_n(0, 0, nbGroupe, nbGroupeDesUEChoisies);
		}

		return k_parmi_n(nbAChoisir, listeUE.size());
	}

	private static int k_parmi_n(int k, int n) {
		int r = 1;
		for(int i = 0; i < k ; i++){
			r *= n-i;
			r /= i+1;
		}
		return r;
	}

	private static int sum_prod_k_nb_parmi_n(int indexMin, int profondeur, int[] nbGroupe, int[] nbGroupeDesUEChoisies) {
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
			s+=sum_prod_k_nb_parmi_n(i+1, profondeur+1,nbGroupe,nbGroupeDesUEChoisies);
		}
		return s;
	}


	public static String toString(UE[] tabU, boolean group) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < tabU.length; i++) {
			stringBuilder.append(group ? tabU[i] : tabU[i].id);
			stringBuilder.append('\t');
		}
		stringBuilder.setLength(stringBuilder.length()-1);
		return stringBuilder.toString();		
	}



	public boolean isDisponible() {
		return disponible;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return id+" "+TDChoisi();
	}

	private Creneau TDChoisi() {
		return TD.get(TDChoisi);
	}
	public int getTDChoisi() {
		return TDChoisi;
	}



}
