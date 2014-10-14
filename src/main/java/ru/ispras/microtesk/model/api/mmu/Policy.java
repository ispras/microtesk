/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.model.api.mmu;

import java.util.LinkedList;

/**
 * Base interface to be implemented by all data replacement policies.
 * 
 * @author Tatiana Sergeeva
 */

public interface Policy {
  /**
   * Handles a buffer hit.
   * 
   * @param index the line being hit.
   */

  void accessLine(int index);

  /**
   * Handles a buffer miss.
   * 
   * @return the line to be replaced.
   */

  int chooseVictim();
}


/**
 * The FIFO (First In - First Out) data replacement policy.
 */

final class PolicyFIFO implements Policy {
  /** Keeps line indexes in the order of their usage. */
  private LinkedList<Integer> fifo = new LinkedList<Integer>();

  PolicyFIFO(int associativity) {
    for (int i = 0; i < associativity; i++) {
      fifo.add(i);
    }
  }

  @Override
  public void accessLine(int index) {
    for (int i = 0; i < fifo.size(); i++) {
      if (fifo.get(i) == index) {
        fifo.remove(i);
        fifo.add(index);

        return;
      }
    }

    throw new IllegalStateException(String.format("Index %d cannot be found.", index));
  }

  @Override
  public int chooseVictim() {
    return fifo.peek();
  }
}


/**
 * The LRU (Least Recently Used) data replacement policy.
 */

final class PolicyLRU implements Policy {
  PolicyLRU(int associativity) {
    // TODO:
  }

  @Override
  public void accessLine(int index) {
    // TODO:
  }

  @Override
  public int chooseVictim() {
    // TODO:
    return 0;
  }
}


/**
 * The PLRU (Pseudo Least Recently Used) data replacement policy.
 */

final class PolicyPLRU implements Policy {
  /** The associativity. */
  private int associativity;

  /** The PLRU bits. */
  private int bits;

  /**
   * {@inheritDoc}
   * 
   * The associativity should not exceed 32.
   */

  PolicyPLRU(int associativity) {
    this.associativity = associativity;
  }

  @Override
  public void accessLine(int index) {
    setBit(index);
  }

  @Override
  public int chooseVictim() {
    for (int i = 0; i < associativity; i++) {
      if ((bits & (1 << i)) == 0) {
        setBit(i);
        return i;
      }
    }

    throw new IllegalStateException("All bits are set to 1");
  }

  private void setBit(int i) {
    final int mask = (1 << i);

    bits |= mask;
    if (bits == ((1 << associativity) - 1)) {
      bits = mask;
    }
  }
}
