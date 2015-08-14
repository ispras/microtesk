/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.types.bitvector.BitVector;

/**
 * The role of {@code BufferObserver} interface is to provide information
 * on buffer state to engines that interact with MMU model.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public interface BufferObserver {
  /**
   * Checks whether the given address causes a hit.
   * 
   * @param address Address to be checked.
   * @return {@code true} if the address causes a hit; {@code false} otherwise.
   */

  boolean isHit(BitVector address);
}
