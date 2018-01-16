import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

public class UE{
	static final ArrayList<UE> listeUE = new ArrayList<>();

	private final int id;
	private final Creneau amphi;

	private final LinkedList<Creneau> TD;

	private int TDChoisi;
	private boolean disponible=true;

	/**
	 * constructeur priv�: les UE doivent �tre cr��es via {@link #generateListUE}
	 */
	private UE(int i, int jour, int heure){
		id = i;
		amphi = new Creneau(jour, heure, heure+1, 0);
		TD = new LinkedList<>();
	}

	/**
	 * constructeur priv� de copie, sert pour la fonction {@link #clone()}
	 */
	private UE(UE other){
		id = other.id;
		amphi = other.amphi;
		TD = other.TD;
		TDChoisi = other.TDChoisi;
	}

	/**
	 * Cr�e les UE
	 * Tous les tableaux doivent avoir la m�me taille
	 * Si le i�me cr�neau est un amphi, mettre groupe[i] � 0
	 */
	public static void generateListUE(int[] id, int[] jour, int[] heure, int[] groupe) {
		if(id.length != jour.length || id.length != heure.length || id.length != groupe.length)
			throw new RuntimeException("les tableaux n'ont pas la m�me taille");

		int i = 0;

		while(groupe[i]==0) {
			int j;
			for(j=0; j<listeUE.size(); j++) {
				if(listeUE.get(j).id>=id[i]) {
					break;
				}
			}
			listeUE.add(j,new UE(id[i],jour[i],heure[i]));

			i++;
		}

		while(i < id.length) {
			boolean trouve=false;
			for (UE u : listeUE){
				if (u.id==id[i]){
					u.add(jour[i],heure[i],groupe[i]);
					trouve=true;
					break;
				}
			}

			if(!trouve)
				throw new RuntimeException("UE non trouv�e");

			i++;
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
	 * cr�e un cr�neau et l'ajoute � l'UE
	 */
	private void add(int jour, int heure, int groupe) {
		if(groupe==0)
			throw new RuntimeException("amphi d�j� d�fini");
		for(Creneau c : TD)
			if(c.getGroupe()==groupe)
				throw new RuntimeException("groupe d�j� d�fini");

		TD.add(new Creneau(jour, heure, heure+2,groupe));
	}

	/**
	 * @return la liste des index (dans TD) des cr�neaux libres
	 */
	public LinkedList<Integer> getFreeIndexList() {
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
	 * s'inscrit � un TD disponible d'une UE disponible
	 * @param indexTD retourn� par {@link #getFreeIndexList()}, ce n'est pas le num�ro de groupe de TD
	 * @return liste � fournir � {@link #undo(LinkedList)} afin de r�tablir la situation telle qu'avant cet appel de fonction 
	 */
	public LinkedList<HashSet<Creneau>> prendre(int indexTD) {
		if(!disponible || !TD.get(indexTD).disponible)
			throw new RuntimeException("inscription � un TD ou UE indisponible");

		TDChoisi=indexTD;

		LinkedList<HashSet<Creneau>> l= new LinkedList<>();
		for(UE u:listeUE) {
			HashSet<Creneau> l2 = null;
			if(u!=this && u.disponible) {
				l2 = new HashSet<>();
				l2.addAll(u.prevenirCreneauPris(amphi));
				if(u.disponible)
					l2.addAll(u.prevenirCreneauPris(TD.get(indexTD)));
			}
			l.add(l2);
		}

		disponible=false;
		return l;
	}

	/**
	 * Met � jour (rend non disponible) les cr�neaux chevauchant le cr�neau pris.
	 * L'UE devient non disponible si son amphi ou tous ses TD ne sont plus disponibles.
	 * Si l'amphi est d�sactiv�, la fonction retourne imm�diatement et ne regarde pas les TD.
	 * @param	creneau n'appartenant pas � cette UE
	 * @return	l'ensemble des cr�neaux rendus non disponible : l'amphi ou des TD).
	 */
	public HashSet<Creneau> prevenirCreneauPris(Creneau creneau){
		HashSet<Creneau> list = new HashSet<>();

		if(!disponible)
			throw new RuntimeException("d�sactive cr�neaux d'une UE non dispo");

		if(TD.contains(creneau))
			throw new RuntimeException("d�sactive cr�neaux de l'UE choisie");

		if(Creneau.gene(creneau, amphi)) {
			disponible=false;
			amphi.disponible=false;
			list.add(amphi);
			return list;
		}

		//d�sactive l'UE et la r�active si un TD est dispo
		disponible=false;

		for (int i=0; i<TD.size(); i++) {
			if(TD.get(i).disponible) {
				if(Creneau.gene(creneau, TD.get(i))) {
					list.add(TD.get(i));
					TD.get(i).disponible=false;		
				}
				else {
					disponible=true;
				}
			}
		}
		return list;
	}


	/**
	 * annule un appel � {@link #prendre(int)}
	 * @param l la liste retourn�e par {@link #prendre(int)}
	 */
	public void undo(LinkedList<HashSet<Creneau>> l) {
		for(UE u : UE.listeUE) {
			u.undo(l.removeFirst());						
		}
		disponible=true;
	}


	private void undo(HashSet<Creneau> creneauPris) {
		if(creneauPris == null)
			return;

		//remet les cr�neaux enlev�s
		for (Creneau c : creneauPris)
			c.disponible=true;

		//v�rifie si un TD est dispo
		disponible=false;
		for (Creneau c : TD)
			if(c.disponible)
				disponible=true;

		//v�rifie si l'amphi est dispo
		disponible = disponible && amphi.disponible;
	}


	/**
	 * retourne le nombre de combinaisons th�oriques
	 */
	public static int nbTheo(int nbAChoisir, boolean group) {
		if(group) {			
			int[] nbGroupe = new int[listeUE.size()];
			for (int i = 0; i < listeUE.size(); i++) {
				nbGroupe[i]=listeUE.get(i).TD.size();	
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

	/**
	 * v�rifie que les UE choisies ainsi que leur TD choisi ne se chevauchent pas
	 */
	public static boolean checkOk(UE[] liste, int taille) {
		for (int i = 0; i < taille-1; i++) {
			for (int j = i+1; j < taille; j++) {
				Creneau[] tab = {liste[i].amphi, liste[i].TDChoisi(), liste[j].amphi, liste[j].TDChoisi()};
				for (int k = 0; k < tab.length-1; k++) {
					for (int l = k+1; l < tab.length; l++) {
						if(Creneau.gene(tab[k], tab[l]))
							throw new RuntimeException(tab[k]+"\t"+tab[l]+" se chevauchent "+liste[0] +"\t"+ liste[1] + "\t"+liste[2] + "\t"+liste[3] + "\t"+liste[4]);
					}
				}
			}
		}
		return true;
	}

	/**
	 * @param group indique si il faut prendre en compte les groupes de TD
	 * @return un objet permettant de comparer 2 s�lections d'UE
	 */
	public static Comparator<UE[]> getComparator(final boolean group) {

		//le boolean group n'est pas d�fini dans le nouvel objet cr�� mais il peut quand m�me �tre utilis� cf:
		//https://en.wikipedia.org/wiki/Closure_(computer_programming)#Local_classes_and_lambda_functions_(Java)
		return new Comparator<UE[]>() {
			@Override
			public int compare(UE[] o1, UE[] o2) {
				if(o1.length!=o2.length)
					throw new RuntimeException("compare not full UE[]");

				for (int i = 0; i < o1.length; i++) {
					final int dif = listeUE.indexOf(o1[i])-listeUE.indexOf(o2[i]);
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


	public static void affiche(UE[] tabU, boolean group) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < tabU.length; i++) {
			stringBuilder.append(group ? tabU[i] : tabU[i].id);
			stringBuilder.append('\t');
		}
		stringBuilder.setLength(stringBuilder.length()-1);
		System.out.println(stringBuilder);
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

	public boolean isDisponible() {
		return disponible;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return id+" "+TD.get(TDChoisi);
	}

	public Creneau TDChoisi() {
		return TD.get(TDChoisi);
	}

}
