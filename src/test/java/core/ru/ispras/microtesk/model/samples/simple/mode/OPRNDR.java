/*
 * Copyright 2012-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.samples.simple.mode;

import ru.ispras.microtesk.model.api.instruction.AddressingMode;

/*
 * mode OPRNDR = OPRNDL | IMM
 */

public abstract class OPRNDR extends AddressingMode {
  public static final String NAME = "OPRNDR";

  public static final IInfo INFO = new InfoOrRule(NAME,
      OPRNDL.INFO,
      IMM.INFO
      );
}
