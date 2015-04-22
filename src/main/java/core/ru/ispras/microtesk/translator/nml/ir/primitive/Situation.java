/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.primitive;

public final class Situation {
  /**
   * Unique ID, corresponds to Java class name. Format: "<INSTRUCTION NAME>_<Situation name>" (for
   * shared situations: _<Situation name>).
   */
  private final String fullName;

  /**
   * ID that will be used to refer to the situation from test templates. Used to identify situations
   * linked to a particular instruction (serves as a key).
   * 
   * Format: <situation name> (the second part of the full name, but all is in lower case).
   */
  private final String id;

  /**
   * If it is linked to all instructions or to one specific instruction.
   */
  private final boolean isShared;

  public Situation(String fullName, String id, boolean isShared) {
    this.fullName = fullName;
    this.id = id;
    this.isShared = isShared;
  }

  public String getFullName() {
    return fullName;
  }

  public String getId() {
    return id;
  }

  public boolean isShared() {
    return isShared;
  }
}
