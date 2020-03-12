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
 * {@link ProtocolMsi} describes the MSI cache coherence protocol.
 *
 * <p>
 * ( INVALID,   READ,   SHARED   )
 * ( INVALID,   WRITE,  MODIFIED )
 * ( SHARED,    READ,   SHARED   )
 * ( SHARED,    WRITE,  MODIFIED )
 * ( SHARED,    EVICT,  INVALID  )
 * ( MODIFIED,  READ,   MODIFIED )
 * ( MODIFIED,  WRITE,  MODIFIED )
 * ( MODIFIED,  EVICT,  INVALID  )
 * ( INVALID,   SN_I,   INVALID  )
 * ( SHARED,    SN_I,   INVALID  )
 * ( MODIFIED,  SN_I,   INVALID  )
 * ( INVALID,   SN_R,   INVALID  )
 * ( SHARED,    SN_R,   SHARED   )
 * ( MODIFIED,  SN_R,   SHARED   )
 * </p>
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class ProtocolMsi extends ProtocolBase {

  @Override
  public ProtocolBase.State onRead(final ProtocolBase.State state) {
    switch (state) {
      case INVALID:
      case MODIFIED:
        return ProtocolBase.State.MODIFIED;
      default:
        return state;
    }
  }

  @Override
  public ProtocolBase.State onSnR(final ProtocolBase.State state) {
    switch (state) {
      case MODIFIED:
        return ProtocolBase.State.SHARED;
      default:
        return state;
    }
  }
}
