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
 * {@link CoherenceProtocolBase} is a base class for the MOESI family cache coherence protocols.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
abstract class CoherenceProtocolBase implements CoherenceProtocol {

  public enum State {
    /** Modified (M). */
    MODIFIED,
    /** Owned (O). */
    OWNED,
    /** Exclusive (E). */
    EXCLUSIVE,
    /** Shared (S). */
    SHARED,
    /** Invalid (I). */
    INVALID
  }

  @Override
  public Enum<?> onReset() {
    return State.INVALID;
  }

  @Override
  public abstract Enum<?> onRead(final Enum<?> state);

  @Override
  public Enum<?> onReadX(final Enum<?> state) {
    return onRead(state);
  }

  @Override
  public Enum<?> onWrite(final Enum<?> state) {
    return State.MODIFIED;
  }

  @Override
  public abstract Enum<?> onSnoopRead(final Enum<?> state);

  @Override
  public Enum<?> onSnoopWrite(final Enum<?> state) {
    return State.INVALID;
  }

  @Override
  public Enum<?> onSnoopEvict(final Enum<?> state) {
    return state;
  }

  @Override
  public boolean isCoherent(final Enum<?>[] states) {
    boolean isModified = false;
    boolean isOwned = false;
    boolean isExclusive = false;
    boolean isShared = false;

    for (final Enum<?> state : states) {
      switch ((State) state) {
        case MODIFIED:
        case EXCLUSIVE:
          if (isModified || isOwned || isExclusive || isShared) {
            return false;
          }
          break;
        case OWNED:
          if (isModified || isOwned || isExclusive) {
            return false;
          }
          break;
        case SHARED:
          if (isModified || isExclusive) {
            return false;
          }
          break;
        default:
          break;
      }
    }

    return true;
  }
}
