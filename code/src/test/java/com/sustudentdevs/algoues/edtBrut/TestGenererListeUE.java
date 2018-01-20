package com.sustudentdevs.algoues.edtBrut;

import java.lang.reflect.Executable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;


class TestGenererListeUE {
  private int id[];
  private int[] jour;
  private int[] heure;
  private int[] groupe;

  @BeforeAll
  static void resetEverything() {
    
  }

  @Test
  void testException() {
    int[] id1 = new int[2]; int[] jour1 = new int[1]; int[] heure1 = new int[1]; int[] groupe1 = new int[1];
    assertThrows(RuntimeException.class, () -> {UE.genererListeUE(id1, jour1, heure1, groupe1);});
    int[] id2 = new int[1]; int[] jour2 = new int[2]; int[] heure2 = new int[1]; int[] groupe2 = new int[1];
    assertThrows(RuntimeException.class, () -> {UE.genererListeUE(id2, jour2, heure2, groupe2);});
    int[] id3 = new int[1]; int[] jour3 = new int[1]; int[] heure3 = new int[2]; int[] groupe3 = new int[1];
    assertThrows(RuntimeException.class, () -> {UE.genererListeUE(id3, jour3, heure3, groupe3);});
    int[] id4 = new int[1]; int[] jour4 = new int[1]; int[] heure4 = new int[1]; int[] groupe4 = new int[2];
    assertThrows(RuntimeException.class, () -> {UE.genererListeUE(id4, jour4, heure4, groupe4);});
  }

  @Test
  void testGenerationLength() {
    int[] ids = {1, 2, 3, 4};
    int[] jours = {1, 1, 1, 1};
    int[] heures = {1, 1, 1, 1};
    int[] groupes = {0, 0, 0, 0};
    UE.genererListeUE(ids, jours, heures, groupes);
    assertEquals(4, UE.listeId.size());
  }
}
