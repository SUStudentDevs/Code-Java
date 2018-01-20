package com.sustudentdevs.algoues.edtBrut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class UE{
	public static final HashMap<Integer, UE> listeUE = new HashMap<>(15);
	public static final ArrayList<Integer> listeId = new ArrayList<>(15);

	private final int id;
	private final Creneau amphi;
	private final LinkedList<Creneau> td;

	private int tdChoisi;//variable utilisée pour énumérer les combinaisons

	private boolean disponible=true;//variable utilisée pour énumérer les combinaisons

	/**
	 * Les constructeurs de cette classe sont privés. Les UE doivent être créées via {@link #genererListeUE()}
	 */
	private UE(int i, int jour, int heure){
		id = i;
		amphi = new Creneau(jour, heure, heure+1, 0);
		td = new LinkedList<>();
	}

	/**
	 * Crée la liste des UE
	 * Si le ième créneau est un amphi, mettre groupe[i] à 0
	 */
	public static void genererListeUE(int[] id, int[] jour, int[] heure, int[] groupe) {
		if(id.length != jour.length || id.length != heure.length || id.length != groupe.length) {
			throw new RuntimeException("les tableaux n'ont pas la même taille");
		}

		//crée les UE avec leurs amphis
		for (int i = 0; i < groupe.length; i++) {
			if(groupe[i]==0) {
				listeUE.put(id[i],new UE(id[i],jour[i],heure[i]));
				listeId.add(id[i]);				
			}
		}
		
		//ajoute les TD
		for (int i = 0; i < groupe.length; i++) {
			if(groupe[i]!=0) {
				final UE u = listeUE.get(id[i]);
				u.add(jour[i],heure[i],groupe[i]);
			}
		}
	}

	/**
	 * Crée un créneau et l'ajoute à l'UE
	 */
	private void add(int jour, int heure, int groupe) {
		if(groupe==0) {
			throw new RuntimeException("amphi déjà défini");
		}
		for(Creneau c : getTd())
			if(c.getGroupe()==groupe) {
				throw new RuntimeException("groupe déjà défini");
			}

		//insertion en place
		int i=0;
		while(i<getTd().size() && getTd().get(i).getGroupe()<groupe) {
			i++;
		}
		
		//TODO: les TD sont forcément par bloc de 4 heures
		getTd().add(i, new Creneau(jour, heure, heure+2,groupe));
	}



	/**
	 * @return la liste des indices (dans TD) des créneaux libres.
	 * Les indices sont décalés de 1 par rapport au numéro de goupe de TD.
	 */
	public LinkedList<Integer> getIndicesLibres() {
		LinkedList<Integer> l = new LinkedList<>();
		int i = 0;
		for(Creneau c : getTd()) {
			if(c.disponible) {
				l.add(i);
			}
			i++;
		}
		return l;
	}

	/**
	 * Libère un créneau ne correspondant pas à une UE
	 * @return objet à fournir à {@link #undoLaisserLibre()} afin de rétablir la situation telle qu'avant cet appel de fonction 
	 */
	public static LinkedList<HashSet<Creneau>> laisserLibre(Creneau c) {
		LinkedList<HashSet<Creneau>> l = new LinkedList<>();

		for(int id : listeId) {
			UE u = listeUE.get(id);
			HashSet<Creneau> set = null;
			if(u.disponible) {
				set = u.prevenirCreneauPris(c);
			}
			l.add(set);
		}

		return l;
	}

	/**
	 * s'inscrit à une UE dans le groupe de TD donné en paramètre
	 * @param indexTD retourné par {@link #getIndicesLibres()}
	 * @return objet à fournir à {@link #undo()} afin de rétablir la situation telle qu'avant cet appel de fonction 
	 */
	public LinkedList<HashSet<Creneau>> prendre(int indexTD) {
		if(!disponible || !getTd().get(indexTD).disponible) {
			throw new RuntimeException("inscription à un TD ou UE indisponible");
		}

		tdChoisi=indexTD;

		LinkedList<HashSet<Creneau>> list= new LinkedList<>();

		//ne préviens que les UE suivantes
		for(int ueId : listeId.subList(listeId.indexOf(id)+1, listeId.size())) {
			UE u = listeUE.get(ueId);
			HashSet<Creneau> set = null;
			if(u.disponible) {
				set = u.prevenirCreneauPris(amphi);
				if(u.disponible) {
					set.addAll(u.prevenirCreneauPris(tdChoisi()));
				}
			}
			list.add(set);
		}

		disponible=false;
		return list;
	}

	/**
	 * Met à jour (rend non disponible) les créneaux chevauchant le créneau pris.
	 * L'UE devient non disponible si son amphi ou tous ses TD ne sont plus disponibles.
	 * Si l'amphi est désactivé, la fonction retourne immédiatement et ne regarde pas les TD.
	 * @param	creneau n'appartenant pas à cette UE
	 * @return	l'ensemble des créneaux rendus non disponible : l'amphi ou des TD.
	 */
	private HashSet<Creneau> prevenirCreneauPris(Creneau creneau){
		if(!disponible) {
			throw new RuntimeException("désactive créneaux d'une UE non dispo");
		}

		if(getTd().contains(creneau)) {
			throw new RuntimeException("désactive créneaux de l'UE choisie");
		}

		HashSet<Creneau> set = new HashSet<>(2);

		if(Creneau.gene(creneau, amphi)) {
			disponible=false;
			amphi.disponible=false;
			set.add(amphi);
			return set;
		}

		//désactive l'UE et la réactive si un TD est dispo
		disponible=false;
		for (Creneau c : getTd()) {
			if (c.disponible) {
				if (Creneau.gene(creneau, c)) {
					set.add(c);
					c.disponible = false;
				} else {
					disponible = true;
				}
			}
		}

		return set;
	}


	/**
	 * annule un appel à {@link #laisserLibre()}
	 * @param l objet retourné par {@link #laisserLibre()}
	 */
	public static void undoLaisserLibre(LinkedList<HashSet<Creneau>> l) {
		for(int id : listeId) {
			listeUE.get(id).undo(l.removeFirst());
		}
	}


	/**
	 * annule un appel à {@link #prendre(int)}
	 * @param l objet retourné par {@link #prendre(int)}
	 */
	public void undo(LinkedList<HashSet<Creneau>> l) {
		for(int ueId : listeId.subList(listeId.indexOf(id) + 1, listeId.size())) {
			listeUE.get(ueId).undo(l.removeFirst());		
		}
		disponible=true;
	}
	
	/**
	 * remet les créneaux enlevés et la disponibilité
	 * @param creneauPris
	 */
	private void undo(HashSet<Creneau> creneauPris) {
		if(creneauPris == null) {
			return;
		}

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
	public void ajoutChaqueGroupeDispo(Collection<UE[]> tree, UE[] tab) {
		if(!disponible || this != tab[tab.length - 1]) {
			throw new RuntimeException("appel incorrect");
		}

		for (int i = 0; i < getTd().size(); i++) {
			if(getTd().get(i).disponible) {
				tdChoisi = i;
				tree.add(deepCopy(tab));
				checkOk(tab, 5);
			}
		}
	}

	/**
	 * @return un nouveau tableau contenant une copie des objets
	 */
	private static UE[] deepCopy(UE[] liste) {
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
		td = other.getTd();
		tdChoisi = other.tdChoisi;
	}

	/**
	 * vérifie que les UE choisies ainsi que leur TD choisi ne se chevauchent pas
	 */
	public static void checkOk(UE[] liste, int taille) {
		//pour chaque couple d'UE choisie
		for (int i = 0; i < taille-1; i++) {
			for (int j = i+1; j < taille; j++) {

				//amphi et TDChoisi des deux UE
				Creneau[] tab = {liste[i].amphi, liste[i].tdChoisi(), liste[j].amphi, liste[j].tdChoisi()};

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



	private Creneau tdChoisi() {
		return getTd().get(tdChoisi);
	}
	
	@Override
	public String toString() {
		return id+" "+tdChoisi();
	}
	
	public boolean isDisponible() {
		return disponible;
	}
	public int getId() {
		return id;
	}
	public int getTdChoisi() {
		return tdChoisi;
	}
	public LinkedList<Creneau> getTd() {
		return td;
	}

}
