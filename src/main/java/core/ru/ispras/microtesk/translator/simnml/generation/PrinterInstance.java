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

package ru.ispras.microtesk.translator.simnml.generation;

import ru.ispras.microtesk.translator.simnml.ir.primitive.Instance;
import ru.ispras.microtesk.translator.simnml.ir.primitive.InstanceArgument;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;

public final class PrinterInstance {
  private PrinterInstance() {}

  public static String toString(Instance instance) {
    final PrimitiveAND primitive = instance.getPrimitive();

    final StringBuilder sb = new StringBuilder();

    sb.append("new ");
    sb.append(primitive.getName());
    sb.append('(');

    boolean isFirst = true;
    for (InstanceArgument arg : instance.getArguments()) {
      if (!isFirst) {
        sb.append(", ");
      }
      isFirst = false;

      switch(arg.getKind()) {
        case EXPR:
          sb.append(new PrinterExpr(arg.getExpr(), true).toString());
          break;

        case INSTANCE:
          sb.append(toString(arg.getInstance()));
          break;

        case PRIMITIVE:
          sb.append(arg.getName());
          break;

        default:
          throw new IllegalArgumentException("Unknown kind: " + arg.getKind());
      }
    }

    sb.append(')');
    return sb.toString();
  }
}
