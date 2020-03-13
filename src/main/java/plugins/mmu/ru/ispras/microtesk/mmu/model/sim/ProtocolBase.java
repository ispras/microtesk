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
 * {@link ProtocolBase} is a base class for the MOESI family cache coherence protocols.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
abstract class ProtocolBase implements Protocol<ProtocolBase.State> {

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
  public State getResetState() {
    return State.INVALID;
  }

  @Override
  public abstract State onRead(final State state);

  @Override
  public State onReadX(final State state) {
    return onRead(state);
  }

  @Override
  public State onWrite(final State state) {
    return State.MODIFIED;
  }

  @Override
  public State onEvict(final State state) {
    return State.INVALID;
  }

  @Override
  public State onSnI(final State state) {
    return State.INVALID;
  }

  @Override
  public abstract State onSnR(final State state);

  @Override
  public boolean isCoherent(final State[] states) {
    boolean isModified = false;
    boolean isOwned = false;
    boolean isExclusive = false;
    boolean isShared = false;

    for (final State state : states) {
      switch (state) {
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
