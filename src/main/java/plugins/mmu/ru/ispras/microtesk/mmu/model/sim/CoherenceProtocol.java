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
 * {@link CoherenceProtocol} is an interface of a cache coherence protocol.
 *
 * @param <S> the state enumeration type.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface CoherenceProtocol<S extends Enum<?>> {
  S getResetState();
  S onRead(S state);
  S onReadX(S state);
  S onWrite(S state);
  S onEvict(S state);
  S onSnI(S state);
  S onSnR(S state);
  boolean isCoherent(S[] states);
}
