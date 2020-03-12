/*
 * Copyright 2020 ISP RAS (http://www.ispras.ru)
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
 * {@link ProtocolId} enumerates basic cache coherence protocols.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public enum ProtocolId {
  /**
   * The MSI cache coherence protocol.
   */
  MSI {
    @Override
    public ProtocolBase newProtocol() {
      return new ProtocolMsi();
    }
  },

  /**
   * The MOSI cache coherence protocol.
   */
  MOSI {
    @Override
    public ProtocolBase newProtocol() {
      return new ProtocolMosi();
    }
  },

  /**
   * The MESI cache coherence protocol.
   */
  MESI {
    @Override
    public ProtocolBase newProtocol() {
      return new ProtocolMesi();
    }
  },

  /**
   * The MOESI cache coherence protocol.
   */
  MOESI {
    @Override
    public ProtocolBase newProtocol() {
      return new ProtocolMoesi();
    }
  },

  /**
   * The NONE cache coherence protocol.
   */
  NONE {
    @Override
    public ProtocolBase newProtocol() {
      return null;
    }
  };

  public abstract Protocol<? extends Enum<?>> newProtocol();
}
