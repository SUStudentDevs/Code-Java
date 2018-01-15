import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

public class UE{
	static final ArrayList<UE> listeUE = new ArrayList<>();

	private final int id;
	private final Creneau amphi;

	/*
	 * On pourrait en faire un tableau via un UEBuilder
	 */
	final LinkedList<Creneau> TD;

	private int TDChoisi;
	boolean disponible=true;

	private UE(int i, int jour, int heure){
		id = i;
		amphi = new Creneau(jour, heure, heure+1, 0);
		TD = new LinkedList<>();
	}

	private UE(UE other){
		id = other.id;
		amphi = other.amphi;
		TD = other.TD;
		TDChoisi = other.TDChoisi;
	}

	public void add(int jour, int heure, int groupe) {
		if(groupe==0)
			throw new RuntimeException("amphi déjà défini");
		for(Creneau c : TD)
			if(c.getGroupe()==groupe)
				throw new RuntimeException("groupe déjà défini");

		TD.add(new Creneau(jour, heure, heure+2,groupe));
	}

	public HashSet<Creneau> prevenirCreneauPris(Creneau creneau){
		HashSet<Creneau> list = new HashSet<>();

		try {
			if(!disponible)
				throw new RuntimeException("previens ue non dispo");

			if(Creneau.gene(creneau, amphi)) {
				disponible=false;
				amphi.disponible=false;
				list.add(amphi);
				return list;
			}

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
		finally {
			//System.out.println("do "+list.size());
		}
	}

	LinkedList<HashSet<Creneau>> prendre(int indexTD) {
		LinkedList<UE> dispo=new LinkedList<>();
		for (UE ue : listeUE) {
			if (ue.disponible) {
				dispo.add(ue);
			}
		}
		//System.out.println("nb dispo "+dispo.size());
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
		TDChoisi=indexTD;

		for (UE ue : listeUE) {
			if (ue.disponible) {
				dispo.remove(ue);
			}
		}
		//System.out.println("perdues "+dispo);

		disponible=false;
		return l;

	}

	LinkedList<Integer> getFreeIndexList() {
		LinkedList<Integer> l = new LinkedList<>();
		int i = 0;
		for(Creneau c : TD) {
			if(c.disponible)
				l.add(i);
			i++;
		}
		return l;
	}

	public void undo(HashSet<Creneau> creneauPris) {
		LinkedList<UE> dispo=new LinkedList<>();
		for (UE ue : listeUE) {
			if (!ue.disponible) {
				dispo.add(ue);
			}
		}
		try {
			if(creneauPris == null)
				return;
			//System.out.println("!dispo "+dispo.size());

			for (Creneau c : creneauPris) {
				c.disponible=true;
			}

			disponible=false;
			for (Creneau c : TD) {
				if(c.disponible)
					disponible=true;
			}
			disponible = disponible && amphi.disponible;
		}
		finally {
			for (UE ue : listeUE) {
				if (!ue.disponible) {
					dispo.remove(ue);
				}
			}
			//System.out.println(id+" gagnées "+dispo);
			disponible = disponible && amphi.disponible;
		}
	}

	/*
	public void reset() {
		disponible=true;
		for (Creneau creneau : TD) {
			creneau.disponible=true;
		}
	}
	 */



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

	public static void generateListUE(int[] id, int[] jour, int[] heure, int[] groupe) {
		int i = 0;
		while(groupe[i]==0) {
			int j=0;
			for(UE u:listeUE) {
				if(u.id>=id[i])
					break;
				j++;
			}
			listeUE.add(j,new UE(id[i],jour[i],heure[i]));
			i++;
		}


		while(i < id.length) {
			boolean trouve=false;
			for (UE u : listeUE){
				if (u.getId()==id[i]){
					u.add(jour[i],heure[i],groupe[i]);
					trouve=true;
					break;
				}
			}
			if(!trouve)
				throw new RuntimeException("UE non trouvée");
			i++;
		}
	}

	public static int nbTheo(int nbAChoisir, boolean group) {
		if(group) {			
			int[] nbGroupe = new int[listeUE.size()];
			int i=0;
			for (UE ue : listeUE) 
				nbGroupe[i++]=ue.TD.size();
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

	public void afficher() {
		StringBuilder s = new StringBuilder();
		s.append(toString());

		if(!disponible)
			s.append("nondispo");
		else {
			s.append(amphi.toString());
			for(Creneau c : TD) {
				if(c.disponible)
					s.append(c.toString());
			}
		}

		//System.out.println(s);	
	}

	public static UE[] deepCopy(UE[] liste) {
		UE[] n = new UE[liste.length];
		for (int i = 0; i < n.length; i++) {
			n[i]=new UE(liste[i]);
		}
		return n;
	}

	public Creneau TDChoisi() {
		return TD.get(TDChoisi);
	}

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

	public static Comparator<UE[]> getComparator(final boolean group) {
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
				if(group) {
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
	/*
		static final Comparator<UE[]> comparatorGroup = new Comparator<UE[]>() {
			@Override
			public int compare(UE[] o1, UE[] o2) {
				if(o1.length!=o2.length)
					throw new RuntimeException("compare not full UE[]");

				for (int i = 0; i < o1.length; i++) {
					final int dif = listeUE.indexOf(o1[i])-listeUE.indexOf(o2[i]);
					if(dif!=0)
						return dif;
				}

				for (int i = 0; i < o1.length; i++) {
					final int difG = o1[i].TDChoisi-o2[i].TDChoisi;
					if(difG!=0)
						return difG;
				}
				return 0;
			}
		};*/

	public static void affiche(UE[] tabU, boolean group) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < tabU.length; i++) {
			stringBuilder.append(group ? tabU[i] : tabU[i].id);
			stringBuilder.append('\t');
		}
		stringBuilder.setLength(stringBuilder.length()-1);
		System.out.println(stringBuilder);
	}

	public void undo(LinkedList<HashSet<Creneau>> l) {
		for(UE u : UE.listeUE) {
			u.undo(l.removeFirst());						
		}
		disponible=true;
	}

}
