package ru.ispras.microtesk.model.api.memory2;

import ru.ispras.fortress.data.types.bitvector.BitVector;

public class MemoryStorageTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    System.out.println("Test");

    final MemoryStorage ms = new MemoryStorage((int) Math.pow(2, 30), 8);

    final BitVector regionData = BitVector.valueOf("11001010");
    System.out.println(regionData);

    System.out.println(ms.read(11));
    ms.write(11, regionData);
    System.out.println(ms.read(11));
  }
}
