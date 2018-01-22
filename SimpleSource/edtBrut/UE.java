package edtBrut;

import java.util.HashSet;
import java.util.LinkedList;

public class UE{
	private final int id;
	private final Creneau amphi;
	private final LinkedList<Creneau> td;

	private int indTdChoisi;//variable utilisée pour énumérer les combinaisons

	private boolean disponible=true;//variable utilisée pour énumérer les combinaisons

	/**
	 * Les UE doivent être créées via {@link #genererListeUE()}
	 */
	UE(int identifiant, int jour, int heure){
		id = identifiant;
		amphi = new Creneau(jour, heure, heure+1, 0);
		td = new LinkedList<>();
	}

	/**
	 * Crée un créneau et l'ajoute à l'UE
	 */
	void add(int jour, int heure, int groupe) {
		if(groupe==0) {
			throw new RuntimeException("amphi déjà défini");
		}
		for(Creneau c : td)
			if(c.getGroupe()==groupe) {
				throw new RuntimeException("groupe déjà défini");
			}

		//insertion en place
		int i=0;
		while(i<td.size() && td.get(i).getGroupe()<groupe) {
			i++;
		}
		
		//TODO: les TD sont forcément par bloc de 4 heures
		td.add(i, new Creneau(jour, heure, heure+2,groupe));
	}



	/**
	 * @return la liste des indices (dans TD) des créneaux libres.
	 * Les indices sont décalés de 1 par rapport au numéro de goupe de TD.
	 */
	public LinkedList<Integer> getIndicesLibres() {
		LinkedList<Integer> l = new LinkedList<>();
		int i = 0;
		for(Creneau c : td) {
			if(c.isDisponible()) {
				l.add(i);
			}
			i++;
		}
		return l;
	}


	/**
	 * Met à jour (rend non disponible) les créneaux chevauchant le créneau pris.
	 * L'UE devient non disponible si son amphi ou tous ses TD ne sont plus disponibles.
	 * Si l'amphi est désactivé, la fonction retourne immédiatement et ne regarde pas les TD.
	 * @param	creneau n'appartenant pas à cette UE
	 * @return	l'ensemble des créneaux rendus non disponible : l'amphi ou des TD.
	 */
	HashSet<Creneau> prevenirCreneauPris(Creneau creneau){
		if(!disponible) {
			throw new RuntimeException("désactive créneaux d'une UE non dispo");
		}

		if(td.contains(creneau)) {
			throw new RuntimeException("désactive créneaux de l'UE choisie");
		}

		HashSet<Creneau> set = new HashSet<>(2);

		if(Creneau.gene(creneau, amphi)) {
			disponible=false;
			amphi.setDisponible(false);
			set.add(amphi);
			return set;
		}

		//désactive l'UE et la réactive si un TD est dispo
		disponible=false;
		for (Creneau c : td) {
			if (c.isDisponible()) {
				if (Creneau.gene(creneau, c)) {
					set.add(c);
					c.setDisponible(false);
				} else {
					disponible = true;
				}
			}
		}

		return set;
	}

	
	/**
	 * remet les créneaux enlevés et la disponibilité
	 * @param creneauPris
	 */
	void undo(HashSet<Creneau> creneauPris) {
		if(creneauPris == null) {
			return;
		}

		//remet les créneaux enlevés
		for (Creneau c : creneauPris)
			c.setDisponible(true);

		//creneauPris n'est pas vide donc l'UE était disponible
		disponible=true;
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
		td = other.td;
		indTdChoisi = other.indTdChoisi;
	}

	/**
	 * vérifie que les UE choisies ainsi que leur TD choisi ne se chevauchent pas
	 */
	public static void checkOk(UE[] liste, int taille) {
		//pour chaque couple d'UE choisie
		for (int i = 0; i < taille-1; i++) {
			for (int j = i+1; j < taille; j++) {

				//amphi et TDChoisi des deux UE
				Creneau[] tab = {liste[i].amphi, liste[i].getTdChoisi(), liste[j].amphi, liste[j].getTdChoisi()};

				//pour chaque couple de créneau
				for (int k = 0; k < tab.length-1; k++) {
					for (int l = k+1; l < tab.length; l++) {

						//regarde si il y a une collision
						if(Creneau.gene(tab[k], tab[l])){
							throw new RuntimeException(tab[k]+"\t"+tab[l]+" se chevauchent "+toString(liste, true));
						}
					}
				}
			}
		}
	}


	public static String toString(UE[] tabU, boolean group) {
		StringBuilder stringBuilder = new StringBuilder(40);
		for (UE ue : tabU) {
			stringBuilder.append(group ? ue : ue.id);
			stringBuilder.append('\t');
		}
		stringBuilder.setLength(stringBuilder.length()-1);
		return stringBuilder.toString();		
	}



	LinkedList<Creneau> getTd() {
		return td;
	}
	Creneau getTdChoisi() {
		return td.get(indTdChoisi);
	}
	public int getTdSize() {
		return td.size();
	}
	
	void setIndexTdChoisi(int tdChoisi) {
		this.indTdChoisi = tdChoisi;
	}
	public int getIndexTdChoisi() {
		return indTdChoisi;
	}
	
	@Override
	public String toString() {
		return id+" "+getTdChoisi();
	}
	
	public boolean isDisponible() {
		return disponible;
	}
	public int getId() {
		return id;
	}
	public Creneau getAmphi() {
		return amphi;
	}




}
