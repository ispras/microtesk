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

package ru.ispras.microtesk.translator.simnml.coverage;

import ru.ispras.fortress.expression.Node;

import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;

public final class ArgumentSpec {
  public final InstanceSpec instance;
  public final Node immediate;

  private ArgumentSpec(InstanceSpec instance, Node immediate) {
    this.instance = instance;
    this.immediate = null;
  }

  public static ArgumentSpec createInstanceArgument(InstanceSpec instance) {
    return new ArgumentSpec(instance, null);
  }

  public static ArgumentSpec createImmediateArgument(Node immediate) {
    return new ArgumentSpec(null, immediate);
  }

  public boolean isMode() {
    return !isImmediate()
        && instance.getOrigin().getKind() == Primitive.Kind.MODE;
  }

  public boolean isOp() {
    return !isImmediate()
        && instance.getOrigin().getKind() == Primitive.Kind.OP;
  }

  public boolean isImmediate() {
    return immediate != null;
  }
}
