package ru.ispras.microtesk.mmu.model.sim.model;

import java.util.HashMap;
import java.util.Map;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.mmu.model.sim.CachePolicy;
import ru.ispras.microtesk.model.memory.MemoryDevice;

public class Model {
  public static final int N1 = 4;
  public static final int N2 = 2;

  public final PA pa = new PA();
  public final L1[] l1 = new L1[N1];
  public final L2[] l2 = new L2[N2];
  public final M m = new M();

  private final Map<BitVector, BitVector> storage = new HashMap<>();

  private final MemoryDevice device = new MemoryDevice() {
    @Override
    public int getAddressBitSize() {
      return 32;
    }

    @Override
    public int getDataBitSize() {
      return 256;
    }

    @Override
    public BitVector load(final BitVector address) {
      System.out.format("load: %s %s\n", address.toHexString(), storage.get(address).toHexString());
      return storage.get(address);
    }

    @Override
    public void store(final BitVector address, final BitVector data) {
      System.out.format("store: %s %s\n", address.toHexString(), data.toHexString());
      storage.put(address, data);
    }

    @Override
    public void store(final BitVector address, final int offset, final BitVector data) {
      final BitVector entry = storage.get(address);
      entry.field(offset, offset + data.getBitSize() - 1).assign(data);
    }

    @Override
    public boolean isInitialized(final BitVector address) {
      return storage.containsKey(address);
    }
  };

  public Model(final CachePolicy policy) {
    m.setStorage(device);

    for (int i = 0 ; i < N2; i++) {
      l2[i] = new L2(policy, m);
    }

    for (int i = 0; i < N1; i++) {
      l1[i] = new L1(policy, l2[i % N2]);
    }

    for (int i = 0; i < N2; i++) {
      for (int j = 0; j < N2; j++) {
        if (i != j) {
          l2[i].addNeighbor(l2[j]);
        }
      }
    }

    for (int i = 0; i < N1; i++) {
      for (int j = 0; j < N1; j++) {
        if (i != j) {
          l1[i].addNeighbor(l1[j]);
        }
      }
    }
  }

  private PA getAddress(final int address) {
    return pa.newStruct(BitVector.valueOf(address, pa.getBitSize()));
  }

  private int getLowerBit(final int address) {
    return (address >> 2) & 7;
  }

  private int getUpperBit(final int address) {
    return getLowerBit(address) + 31;
  }

  public void memset(final int start, final int end, final int fill) {
    for (int address = (start & ~0x1f); address <= end; address += 32) {
      final BitVector line = BitVector.valueOf(fill, 32).repeat(8);
      storage.put(BitVector.valueOf(address, 32), line);
    }
  }

  public int lw(final int core, final int address) {
    final L1.Entry entry = l1[core].readEntry(getAddress(address));
    return entry.asBitVector().field(getLowerBit(address), getUpperBit(address)).intValue();
  }

  public void sw(final int core, final int address, final int word) {
    final BitVector data = BitVector.valueOf(word, 32);
    l1[core].writeEntry(getAddress(address), getLowerBit(address), getUpperBit(address), data);
  }
}
