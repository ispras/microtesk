/*
 * Copyright 2014-2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.sim;

/**
 * {@link EvictPolicyId} enumerates basic data replacement (eviction) policies.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public enum EvictPolicyId {
  /**
   * The random data replacement policy.
   */
  RANDOM {
    @Override
    public EvictPolicy newPolicy(final int associativity) {
      return new EvictPolicyRandom(associativity);
    }
  },

  /**
   * The FIFO (First In - First Out) data replacement policy.
   */
  FIFO {
    @Override
    public EvictPolicy newPolicy(final int associativity) {
      return new EvictPolicyFifo(associativity);
    }
  },

  /**
   * The LRU (Least Recently Used) data replacement policy.
   */
  LRU {
    @Override
    public EvictPolicy newPolicy(final int associativity) {
      return new EvictPolicyLru(associativity);
    }
  },

  /**
   * The PLRU (Pseudo Least Recently Used) data replacement policy.
   */
  PLRU {
    @Override
    public EvictPolicy newPolicy(final int associativity) {
      return new EvictPolicyPlru(associativity);
    }
  },

  /**
   * The NONE policy (no data replacement).
   */
  NONE {
    @Override
    public EvictPolicy newPolicy(final int associativity) {
      return null;
    }
  };

  public abstract EvictPolicy newPolicy(int associativity);
}
