/*
 * Copyright 2014-2016 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.instruction.Operation;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;
import ru.ispras.microtesk.model.api.instruction.PrimitiveBuilder;

public interface CallFactory {
  PrimitiveBuilder<AddressingMode> newMode(String name) throws ConfigurationException;
  PrimitiveBuilder<Operation> newOp(String name, String contextName) throws ConfigurationException;
  InstructionCall newCall(Operation op);
}
