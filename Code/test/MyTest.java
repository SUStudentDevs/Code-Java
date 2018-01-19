package test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import main.edtBrut.UE;
import main.edtTravaille.Tri;

class MyTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		int[] id = 		{5,	8,	10,	26,	2,	18,	24,	27,	17,	19,	25,	21,	12,	22,	20,		10,	24,	24,	2,	10,	20,		25,	17,	8,	26,	18,	22,		12,	17,	2,	17,	26,	10,	5,	21,	24,		12,	2,	10,	27,	19,	17,	2,		8,	2,	12,	27,	2,	5};
		int[] jour = 	{0,	0,	0,	0,	1,	1,	2,	2,	3,	3,	3,	3,	4,	4,	4,		0,	0,	0,	0,	0,	0,		1,	1,	1,	1,	1,	1,		2,	2,	2,	2,	2,	2,	2,	2,	2,		3,	3,	3,	3,	3,	3,	3,		4,	4,	4,	4,	4,	4};
		int[] heure = 	{0,	1,	2,	3,	0,	1,	1,	2,	0,	1,	2,	3,	1,	2,	3,		0,	0,	1,	2,	3,	3,		1,	1,	2,	3,	3,	3,		0,	0,	0,	2,	2,	2,	2,	3,	3,		0,	1,	1,	2,	3,	3,	3,		0,	0,	2,	2,	2,	2};
		int[] groupe = 	{0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,		1,	4,	1,	2,	2,	1,		1,	1,	1,	3,	1,	1,		1,	2,	1,	4,	1,	3,	1,	1,	2,		2,	3,	4,	1,	1,	3,	4,		2,	5,	3,	2,	6,	2};
		UE.genererListeUE(id, jour, heure, groupe);
	}

	private Tri tri;
	
	@Test
	void tri(){
		tri=new Tri();
		tri.differencieSelonGroupe(false);
		triParId(2);
		triParId(10);
		triParId(5);
		tri.differencieSelonGroupe(true);
		triParId(2);
		triParId(10);
		triParId(5);
	}
	
	private void triParId(int id) {
		tri.triParId();
		int tot = tri.taille();

		tri.nePasPrendre(id);
		tri.triParId();
		int sansU = tri.taille();

		tri.prendreUE(id);
		tri.triParId();
		int avecU = tri.taille();

		assertEquals(tot,sansU+avecU);
		
		tri.resetAllUE();
		assertEquals(tot, tri.triParId().size());
	}

}
