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

/**
 * The {@link Operation} interface describes objects responsible for initializing
 * fields of an address passed to the MMU simulator when simulation of a memory
 * access is started. Each {@code Operation} object is associated with a specific
 * operation defined in the ISA model and is called when a memory access has been
 * initiated by that ISA operation.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 * @param <A> the address type.
 */

public interface Operation <A extends Address> {
  void init(final A address);
}
