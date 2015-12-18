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

package ru.ispras.microtesk.mmu.model.api;

import java.util.LinkedList;

import ru.ispras.fortress.randomizer.Randomizer;

/**
 * Base interface to be implemented by all data replacement policies.
 * 
 * @author <a href="mailto:leonsia@ispras.ru">Tatiana Sergeeva</a>
 */

public abstract class Policy {
  /** The associativity. */
  protected final int associativity;

  /**
   * Constructs a data replacement controller.
   * 
   * @param associativity the buffer associativity.
   */

  protected Policy(final int associativity) {
    if (associativity <= 0) {
      throw new IllegalArgumentException(String.format("Illegal associativity %d", associativity));
    }

    this.associativity = associativity;
  }

  /**
   * Handles a buffer hit.
   * 
   * @param index the line being hit.
   */

  public abstract void accessLine(int index);

  /**
   * Handles a buffer miss.
   * 
   * @return the line to be replaced.
   */

  public abstract int chooseVictim();
}

//--------------------------------------------------------------------------------------------------
// Random
//--------------------------------------------------------------------------------------------------

/**
 * The random data replacement policy.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */

final class PolicyRandom extends Policy {

  /**
   * Constructs a random data replacement controller.
   * 
   * @param associativity the buffer associativity.
   */

  PolicyRandom(final int associativity) {
    super(associativity);
  }

  @Override
  public void accessLine(final int index) {
    // Do nothing.
  }

  @Override
  public int chooseVictim() {
    return Randomizer.get().nextIntRange(0, associativity - 1);
  }
}

//--------------------------------------------------------------------------------------------------
// FIFO
//--------------------------------------------------------------------------------------------------

/**
 * The FIFO (First In - First Out) data replacement policy.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */

final class PolicyFIFO extends Policy {
  /** Keeps line indices in the order of their usage. */
  private LinkedList<Integer> fifo = new LinkedList<Integer>();

  /**
   * Constructs a FIFO data replacement controller.
   * 
   * @param associativity the buffer associativity.
   */

  PolicyFIFO(final int associativity) {
    super(associativity);

    for (int i = 0; i < associativity; i++) {
      fifo.add(i);
    }
  }

  @Override
  public void accessLine(final int index) {
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


//--------------------------------------------------------------------------------------------------
// LRU
//--------------------------------------------------------------------------------------------------

/**
 * The LRU (Least Recently Used) data replacement policy.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */

final class PolicyLRU extends Policy {
  /** Maps index to time. */
  private int times[];
  /** Current time. */
  private int time = 0;

  /**
   * Constructs an LRU data replacement controller.
   * 
   * @param associativity the buffer associativity.
   */

  PolicyLRU(final int associativity) {
    super(associativity);

    times = new int[associativity];
    for (int i = 0; i < associativity; i++) {
      times[i] = time++;
    }
  }

  @Override
  public void accessLine(final int index) {
    times[index] = time++;
  }

  @Override
  public int chooseVictim() {
    int victim = 0;
    int minTime = times[0];

    for (int i = 1; i < times.length; i++) {
      if (times[i] < minTime) {
        victim = i;
        minTime = times[i];
      }
    }

    return victim;
  }
}


//--------------------------------------------------------------------------------------------------
// PLRU
//--------------------------------------------------------------------------------------------------

/**
 * The PLRU (Pseudo Least Recently Used) data replacement policy.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */

final class PolicyPLRU extends Policy {
  /** The PLRU bits. */
  private int bits;
  /** The last access. */
  private int last;

  /**
   * Constructs a PLRU data replacement controller.
   * 
   * @param associativity the buffer associativity.
   */

  PolicyPLRU(final int associativity) {
    super(associativity);

    if (associativity > 32) {
      throw new IllegalArgumentException(String.format("Illegal associativity %d", associativity));
    }
  }

  @Override
  public void accessLine(final int index) {
    setBit(index);
  }

  @Override
  public int chooseVictim() {
    for (int i = 0; i < associativity; i++) {
      final int j = (last + i) % associativity;

      if ((bits & (1 << j)) == 0) {
        return j;
      }
    }

    throw new IllegalStateException("All bits are set to 1");
  }

  private void setBit(final int i) {
    final int mask = (1 << (last = i));

    bits |= mask;
    if (bits == ((1 << associativity) - 1)) {
      bits = mask;
    }
  }
}
