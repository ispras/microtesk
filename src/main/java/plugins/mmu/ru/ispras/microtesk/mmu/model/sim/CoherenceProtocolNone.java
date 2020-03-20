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
 * {@link CoherenceProtocolNone} implements the trivial cache coherence protocols.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class CoherenceProtocolNone implements CoherenceProtocol {

  public enum State {
    VALID,
    INVALID
  }

  @Override
  public Enum<?> onRead(final Enum<?> state, final boolean exclusive) {
    return State.VALID;
  }

  @Override
  public Enum<?> onWrite(final Enum<?> state) {
    return State.VALID;
  }

  @Override
  public Enum<?> onSnoopRead(final Enum<?> state) {
    return state;
  }

  @Override
  public Enum<?> onSnoopWrite(final Enum<?> state) {
    return state;
  }

  @Override
  public Enum<?> onSnoopEvict(final Enum<?> state) {
    return state;
  }

  @Override
  public Enum<?> onReset() {
    return State.INVALID;
  }

  @Override
  public boolean isCoherent(final Enum<?>[] states) {
    return false;
  }
}
