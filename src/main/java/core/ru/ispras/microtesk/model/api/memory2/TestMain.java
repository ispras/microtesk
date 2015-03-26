package ru.ispras.microtesk.model.api.memory2;

import ru.ispras.fortress.data.types.bitvector.BitVector;


public class TestMain {

  /**
   * @param args
   */
  public static void main(String[] args) {
    System.out.println(new MemoryStorage(32, 1 << 4).setId("Test"));
    System.out.println(new MemoryStorage(32, 1 << 5).setId("Test"));
    
    final MemoryStorage ms = new MemoryStorage(32, 1L << 32).setId("Test");

    ms.write(0xDEADBEEF, BitVector.valueOf(0xDEADBEEF, 32));
    System.out.println(ms.read(0xDEADBEEF));
  }

}
