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

package ru.ispras.microtesk.model.api;

/**
 * The {@code ArgumentMode} enumeration specifies how an argument of 
 * an instructions or its primitives (addressing modes, operations, shortcuts)
 * is used (in, out, in/out).
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public enum ArgumentMode {
  /** {@code IN} argument. Used for immediate values and addressing modes. */
  IN("in", true, false),

  /** {@code OUT} argument. Used for addressing modes.*/
  OUT("out", false, true),

  /** {@code IN/OUT} argument. Used for addressing modes. */
  INOUT("in/out", true, true),

  /** Not applicable. Used for operations. */
  NA("na", false, false);

  private final String text;
  private final boolean isIn;
  private final boolean isOut;

  private ArgumentMode(
      final String text,
      final boolean isIn,
      final boolean isOut) { 
    this.text = text;
    this.isIn = isIn;
    this.isOut = isOut;
  }

  public String getText() {
    return text;
  }

  public boolean isIn() {
    return isIn;
  }

  public boolean isOut() {
    return isOut;
  }
}
