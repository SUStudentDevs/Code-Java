package main;
import main.edtBrut.UE;
import main.edtTravaille.Tri;

public class Main {
	/*
	 * group=false n'affiche pas les groupes de TD 
	 */
	public static final boolean GROUP = true;
	public static final int NB_UE = 5;

	public static void main(String[] args) {
		//crée les UE et assigne les TD
		ini();

		System.out.println("nb possibilités théoriques " + UE.nbTheo(NB_UE, GROUP));

		Tri tri = new Tri();
		tri.differencieSelonGroupe(true);	
		tri.triParId();
		tri.afficheTaille();

		System.out.println("sans 2 avec 25");
		tri.prendreUE(2, false);
		tri.prendreUE(25, true);
		tri.triParId();
		//tri.afficheTaille();
		
		
		tri.resetAllUE();
		System.out.println("sans groupes, sans 2,17,18,19,20. tri avec 26,5,8,20,22,24,10,2,25 en priorité");
		tri.differencieSelonGroupe(false);
		tri.prendreUE(2, false);
		tri.prendreUE(17, false);
		tri.prendreUE(18, false);
		tri.prendreUE(19, false);
		tri.prendreUE(20, false);
		tri.triParUEPrioritaire(new int[] {26,5,8,20,22,24,10,2,25}); //2,5,8,10,12,17,18,19,20,21,22,24,25,26,27};
		tri.afficheTaille();
		tri.afficheListe();

		System.out.println("\n\n\n\npareil, tri par somme des points");
		tri.triParPoints(new int[] {5,8,25,26,12}, new int[] {5,3,8,7,-2});
		tri.afficheTaille();
		tri.afficheListe();

		tri.resetAllUE();
		System.out.println("\n\n\n\npareil, tri par somme des points");
		tri.plageHoraire(4, 2, 4);//vendredi aprem
		tri.plageHoraire(0, 0, 2);//lundi matin
		tri.differencieSelonGroupe(true);
		tri.prendreUE(10, false);
		tri.triParId();
		tri.afficheTaille();
		tri.afficheListe();
	}

	
	private static void ini() {
		int[] id = 		{5,	8,	10,	26,	2,	18,	24,	27,	17,	19,	25,	21,	12,	22,	20,		10,	24,	24,	2,	10,	20,		25,	17,	8,	26,	18,	22,		12,	17,	2,	17,	26,	10,	5,	21,	24,		12,	2,	10,	27,	19,	17,	2,		8,	2,	12,	27,	2,	5};
		int[] jour = 	{0,	0,	0,	0,	1,	1,	2,	2,	3,	3,	3,	3,	4,	4,	4,		0,	0,	0,	0,	0,	0,		1,	1,	1,	1,	1,	1,		2,	2,	2,	2,	2,	2,	2,	2,	2,		3,	3,	3,	3,	3,	3,	3,		4,	4,	4,	4,	4,	4};
		int[] heure = 	{0,	1,	2,	3,	0,	1,	1,	2,	0,	1,	2,	3,	1,	2,	3,		0,	0,	1,	2,	3,	3,		1,	1,	2,	3,	3,	3,		0,	0,	0,	2,	2,	2,	2,	3,	3,		0,	1,	1,	2,	3,	3,	3,		0,	0,	2,	2,	2,	2};
		int[] groupe = 	{0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,		1,	4,	1,	2,	2,	1,		1,	1,	1,	3,	1,	1,		1,	2,	1,	4,	1,	3,	1,	1,	2,		2,	3,	4,	1,	1,	3,	4,		2,	5,	3,	2,	6,	2};
		UE.genererListeUE(id, jour, heure, groupe);
	}
}
