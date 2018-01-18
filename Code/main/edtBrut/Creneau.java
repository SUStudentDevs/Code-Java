package main.edtBrut;
public class Creneau {

	private final int jour, debut, fin, groupe;

	boolean disponible = true;// variable utilisée pour énumérer les combinaisons

	public Creneau(int jour, int debut, int fin, int groupe) {
		this.jour = jour;
		this.debut = debut;
		this.fin = fin;
		this.groupe = groupe;
	}

	private static final int PAS_AMPHI_NI_TD = -1;

	public Creneau(int jour, int debut, int fin) {
		this.jour = jour;
		this.debut = debut;
		this.fin = fin;
		groupe = PAS_AMPHI_NI_TD;
	}

	public static boolean gene(Creneau a, Creneau b) {
		if (a.jour != b.jour)
			return false;
		if (a.debut >= b.fin || a.fin <= b.debut)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return groupe + "," + jour + "," + debut;
	}

	public int getGroupe() {
		return groupe;
	}

	public int getDebut() {
		return debut;
	}

	public int getFin() {
		return fin;
	}

}