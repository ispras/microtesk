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

package ru.ispras.microtesk.model.api.memory;

import java.math.BigInteger;

/**
 * The LocationAccessor interfaces is used by the simulator to access data stored 
 * in the specified location. This should not cause any memory-related event in the model.
 * 
 * @author Andrei Tatarnikov
 */

public interface LocationAccessor {
  
  /**
   * Returns the size of the location in bits.
   * 
   * @return Size in bits.
   */
  
  public int getBitSize();
  
  /**
   * Returns textual representation of stored data (a string of 0 and 1 characters).
   * 
   * @return Binary string.
   */

  public String toBinString();
  
  /**
   * Returns the value stored in the location packed in a BigInteger object.
   * 
   * @return Binary data packed in a BigInteger object.
   */

  public BigInteger getValue();

  /**
   * Sets the value of the specified location.
   * 
   * @param value Binary data packed in a BigInteger object.
   */

  // public void setValue(BigInteger value); // TODO: NOT IMPLEMENTED YET.
}





