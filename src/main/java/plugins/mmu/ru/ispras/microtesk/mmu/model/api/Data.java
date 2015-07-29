/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * This class represents cache data.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Data {

  /** The data fields. */
  private Map<String, BitVector> fields = new LinkedHashMap<>();

  /**
   * Returns the value of the field with the given name.
   * 
   * @param name the field name.
   * @return the field value.
   */

  public BitVector getField(final String name) {
    return fields.get(name);
  }

  /**
   * Sets the value of the field with the given name.
   * 
   * @param name the field name.
   * @return the field value.
   */

  public BitVector setField(final String name, final BitVector value) {
    return fields.put(name, value);
  }

  /**
   * Defines a data field of the specified size.
   * 
   * @param name field name.
   * @param bitSize field size in bits.
   */

  protected final void defineField(final String name, final int bitSize) {
    InvariantChecks.checkFalse(fields.containsKey(name));
    setField(name, BitVector.newEmpty(bitSize));
  }
}
