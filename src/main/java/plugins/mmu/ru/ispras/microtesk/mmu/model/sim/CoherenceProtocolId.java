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
 * {@link CoherenceProtocolId} enumerates basic cache coherence protocols.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public enum CoherenceProtocolId {
  /**
   * The MSI cache coherence protocol.
   */
  MSI {
    @Override
    public CoherenceProtocol newProtocol() {
      return new CoherenceProtocolMsi();
    }
  },

  /**
   * The MOSI cache coherence protocol.
   */
  MOSI {
    @Override
    public CoherenceProtocol newProtocol() {
      return new CoherenceProtocolMosi();
    }
  },

  /**
   * The MESI cache coherence protocol.
   */
  MESI {
    @Override
    public CoherenceProtocol newProtocol() {
      return new CoherenceProtocolMesi();
    }
  },

  /**
   * The MOESI cache coherence protocol.
   */
  MOESI {
    @Override
    public CoherenceProtocol newProtocol() {
      return new CoherenceProtocolMoesi();
    }
  },

  /**
   * The NONE cache coherence protocol.
   */
  NONE {
    @Override
    public CoherenceProtocol newProtocol() {
      return new CoherenceProtocolNone();
    }
  };

  public abstract CoherenceProtocol newProtocol();
}
