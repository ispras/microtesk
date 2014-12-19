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

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import static ru.ispras.microtesk.utils.InvariantChecks.checkNotNull;

import java.util.List;


/**
 * The Instance class describes a statically created instance of a MODE or OP.
 * 
 * @author Andrei Tatarnikov
 */

public final class Instance {
  private final PrimitiveAND primitive;
  private final List<InstanceArgument> arguments;

  Instance(PrimitiveAND primitive, List<InstanceArgument> arguments) {
    checkNotNull(primitive);
    checkNotNull(arguments);

    this.primitive = primitive;
    this.arguments = arguments;
  }

  public PrimitiveAND getPrimitive() {
    return primitive;
  }

  public List<InstanceArgument> getArguments() {
    return arguments;
  }
}
