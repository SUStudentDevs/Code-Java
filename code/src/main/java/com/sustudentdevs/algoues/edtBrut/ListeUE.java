package com.sustudentdevs.algoues.edtBrut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class ListeUE {
	private final HashMap<Integer, UE> hashMapUE = new HashMap<>(15);
	private final ArrayList<Integer> listeId = new ArrayList<>(15);
	
	/**
	 * Crée la liste des UE
	 * Si le ième créneau est un amphi, mettre groupe[i] à 0
	 */
	public ListeUE(int[] id, int[] jour, int[] heure, int[] groupe) {
    if(id.length != jour.length || id.length != heure.length || id.length != groupe.length) {
			throw new RuntimeException("les tableaux n'ont pas la même taille");
		}

		//crée les UE avec leurs amphis
		for (int i = 0; i < groupe.length; i++) {
			if(groupe[i]==0) {
				hashMapUE.put(id[i],new UE(id[i],jour[i],heure[i]));
				listeId.add(id[i]);				
			}
		}
		
		//ajoute les TD
		for (int i = 0; i < groupe.length; i++) {
			if(groupe[i]!=0) {
				final UE u = hashMapUE.get(id[i]);
				u.add(jour[i],heure[i],groupe[i]);
			}
		}
	}

	/**
	 * s'inscrit à une UE dans le groupe de TD donné en paramètre
	 * @param indexTD retourné par {@link #getIndicesLibres()}
	 * @return objet à fournir à {@link #undo()} afin de rétablir la situation telle qu'avant cet appel de fonction 
	 */
	public LinkedList<HashSet<Creneau>> prendre(UE ue, int indexTD) {
		if(!ue.isDisponible() || !ue.getTd().get(indexTD).isDisponible()) {
			throw new RuntimeException("inscription à un TD ou UE indisponible");
		}

		ue.setIndexTdChoisi(indexTD);

		LinkedList<HashSet<Creneau>> list= new LinkedList<>();

		//ne préviens que les UE suivantes
		for(int ueId : listeId.subList(listeId.indexOf(ue.getId())+1, listeId.size())) {
			UE u = hashMapUE.get(ueId);
			HashSet<Creneau> set = null;
			if(u.isDisponible()) {
				set = u.prevenirCreneauPris(ue.getAmphi());
				if(u.isDisponible()) {
					set.addAll(u.prevenirCreneauPris(ue.getTdChoisi()));
				}
			}
			list.add(set);
		}

		//ue.disponible=false;
		return list;
	}


	/**
	 * annule un appel à {@link #prendre(int)}
	 * @param l objet retourné par {@link #prendre(int)}
	 */
	public void undo(UE ue, LinkedList<HashSet<Creneau>> l) {
		for(int ueId : listeId.subList(listeId.indexOf(ue.getId()) + 1, listeId.size())) {
			hashMapUE.get(ueId).undo(l.removeFirst());		
		}
		//disponible=true;
	}

	/**
	 * Libère un créneau ne correspondant pas à une UE
	 * @return objet à fournir à {@link #undoLaisserLibre()} afin de rétablir la situation telle qu'avant cet appel de fonction 
	 */
	public LinkedList<HashSet<Creneau>> laisserLibre(Creneau c) {
		LinkedList<HashSet<Creneau>> l = new LinkedList<>();

		for(int id : listeId) {
			UE u = hashMapUE.get(id);
			HashSet<Creneau> set = null;
			if(u.isDisponible()) {
				set = u.prevenirCreneauPris(c);
			}
			l.add(set);
		}

		return l;
	}

	/**
	 * annule un appel à {@link #laisserLibre()}
	 * @param l objet retourné par {@link #laisserLibre()}
	 */
	public void undoLaisserLibre(LinkedList<HashSet<Creneau>> l) {
		for(int id : listeId) {
			hashMapUE.get(id).undo(l.removeFirst());
		}
	}
	

	/**
	 * ajoute une entrée dans l'abre pour chaque groupe de TD disponible de l'UE.
	 * @param tab 	doit être rempli par une bonne combinaison d'UE
	 * 				et contenir l'UE appelante en dernière position
	 */
	public void ajoutChaqueGroupeDispo(UE ue, Collection<UE[]> tree, UE[] tab) {
		if(!ue.isDisponible() || ue != tab[tab.length - 1]) {
			throw new RuntimeException("appel incorrect");
		}

		for (int i = 0; i < ue.getTd().size(); i++) {
			if(ue.getTd().get(i).isDisponible()) {
				ue.setIndexTdChoisi(i);
				tree.add(UE.deepCopy(tab));
				UE.checkOk(tab, tab.length);
			}
		}
	}

	public UE get(int id) {
		return hashMapUE.get(id);
	}

	public ArrayList<Integer> getListeId() {
		return listeId;
	}
	
	

}
