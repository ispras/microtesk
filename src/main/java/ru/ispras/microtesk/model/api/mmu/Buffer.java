/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.mmu;

/**
 * This is a generic interface of a buffer (i.e., a component that stores addressable data).
 * 
 * @param <D> the data type.
 * @param <A> the address type.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */

public interface Buffer<D, A> {

  /**
   * Checks whether the given address causes a hit.
   * 
   * @param address the data address.
   * @return <code>true</code> if the address causes a hit; <code>false</code> otherwise.
   */

  public boolean isHit(final A address);

  /**
   * Returns the data associated with the given address.
   * 
   * @param address the data address.
   * @return the data object if the address causes a hit; <code>null</code> otherwise.
   */

  public D getData(final A address);

  /**
   * Updates the data associated with the given address.
   * 
   * @param address the data address.
   * @param data the new data.
   * 
   * @return the old data if they exist; <code>null</code> otherwise.
   */

  public D setData(final A address, final D data);
}
