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

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link CoherenceProtocolMesi} describes the MESI cache coherence protocol.
 *
 * <p>
 * ( INVALID,   READ,   SHARED    )
 * ( INVALID,   READX,  EXCLUSIVE )
 * ( INVALID,   WRITE,  MODIFIED  )
 * ( SHARED,    READ,   SHARED    )
 * ( SHARED,    WRITE,  MODIFIED  )
 * ( SHARED,    EVICT,  INVALID   )
 * ( EXCLUSIVE, READ,   EXCLUSIVE )
 * ( EXCLUSIVE, WRITE,  MODIFIED  )
 * ( EXCLUSIVE, EVICT,  INVALID   )
 * ( MODIFIED,  READ,   MODIFIED  )
 * ( MODIFIED,  WRITE,  MODIFIED  )
 * ( MODIFIED,  EVICT,  INVALID   )
 * ( INVALID,   SN_I,   INVALID   )
 * ( SHARED,    SN_I,   INVALID   )
 * ( EXCLUSIVE, SN_I,   INVALID   )
 * ( MODIFIED,  SN_I,   INVALID   )
 * ( INVALID,   SN_R,   INVALID  )
 * ( SHARED,    SN_R,   SHARED   )
 * ( EXCLUSIVE, SN_R,   SHARED    )
 * ( MODIFIED,  SN_R,   SHARED    )
 * </p>
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class CoherenceProtocolMesi extends CoherenceProtocolBase {
  @Override
  public CoherenceProtocolBase.State onRead(final CoherenceProtocolBase.State state) {
    switch (state) {
      case INVALID:
        return CoherenceProtocolBase.State.SHARED;
      default:
        return state;
    }
  }

  @Override
  public CoherenceProtocolBase.State onReadX(final CoherenceProtocolBase.State state) {
    switch (state) {
      case INVALID:
        return CoherenceProtocolBase.State.EXCLUSIVE;
      default:
        InvariantChecks.checkTrue(false);
        return null;
    }
  }

  @Override
  public CoherenceProtocolBase.State onSnR(final CoherenceProtocolBase.State state) {
    switch (state) {
      case EXCLUSIVE:
      case MODIFIED:
        return CoherenceProtocolBase.State.SHARED;
      default:
        return state;
    }
  }
}
