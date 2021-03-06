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
 * {@link CoherenceProtocolMsi} describes the MSI cache coherence protocol.
 *
 * <p>
 * -----------------------------
 * ( INVALID,  READ,  SHARED   )
 * ( INVALID,  WRITE, MODIFIED )
 * ( SHARED,   READ,  SHARED   )
 * ( SHARED,   WRITE, MODIFIED )
 * ( SHARED,   EVICT, INVALID  )
 * ( MODIFIED, READ,  MODIFIED )
 * ( MODIFIED, WRITE, MODIFIED )
 * ( MODIFIED, EVICT, INVALID  )
 * -----------------------------
 * ( INVALID,  SN_RD, INVALID  )
 * ( SHARED,   SN_RD, SHARED   )
 * ( MODIFIED, SN_RD, SHARED   )
 * ( INVALID,  SN_WR, INVALID  )
 * ( SHARED,   SN_WR, INVALID  )
 * ( MODIFIED, SN_WR, INVALID  )
 * -----------------------------
 * </p>
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class CoherenceProtocolMsi extends CoherenceProtocolBase {
  private static final CoherenceProtocol instance = new CoherenceProtocolMsi();

  public static CoherenceProtocol get() {
    return instance;
  }

  private CoherenceProtocolMsi() {}

  @Override
  public Enum<?> onRead(final Enum<?> state, final boolean exclusive) {
    switch ((State) state) {
      case INVALID:
      case MODIFIED:
        return State.MODIFIED;
      default:
        return state;
    }
  }

  @Override
  public Enum<?> onSnoopRead(final Enum<?> state) {
    switch ((State) state) {
      case MODIFIED:
        return State.SHARED;
      default:
        return state;
    }
  }
}
