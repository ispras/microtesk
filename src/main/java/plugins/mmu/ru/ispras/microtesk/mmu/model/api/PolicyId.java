/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

/**
 * This enumeration contains basic data replacement policies.
 * 
 * @author Tatiana Sergeeva
 */

public enum PolicyId {

  /**
   * The random data replacement policy.
   */

  RANDOM {
    @Override
    public Policy newPolicy(final int associativity) {
      return new PolicyRandom(associativity);
    }
  },

  /**
   * The FIFO (First In - First Out) data replacement policy.
   */

  FIFO {
    @Override
    public Policy newPolicy(final int associativity) {
      return new PolicyFIFO(associativity);
    }
  },

  /**
   * The LRU (Least Recently Used) data replacement policy.
   */

  LRU {
    @Override
    public Policy newPolicy(final int associativity) {
      return new PolicyLRU(associativity);
    }
  },

  /**
   * The PLRU (Pseudo Least Recently Used) data replacement policy.
   */

  PLRU {
    @Override
    public Policy newPolicy(final int associativity) {
      return new PolicyPLRU(associativity);
    }
  },

  /**
   * The NONE policy. Means no data replacement is allowed.
   */

  NONE {
    @Override
    public Policy newPolicy(final int associativity) {
      return null;
    }
  };

  public abstract Policy newPolicy(int associativity);
}
