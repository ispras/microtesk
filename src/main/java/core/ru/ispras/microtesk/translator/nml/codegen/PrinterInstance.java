/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen;

import ru.ispras.microtesk.model.Immediate;
import ru.ispras.microtesk.translator.nml.ir.primitive.Instance;
import ru.ispras.microtesk.translator.nml.ir.primitive.InstanceArgument;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;

import java.util.ArrayList;
import java.util.List;

public final class PrinterInstance {
  private PrinterInstance() {}

  public static String toString(final Instance instance) {
    final PrimitiveAND primitive = instance.getPrimitive();

    final StringBuilder sb = new StringBuilder();

    sb.append("new ");
    sb.append(primitive.getName());
    sb.append('(');

    final List<String> argumentNames = new ArrayList<>(primitive.getArguments().keySet());
    for (int index = 0; index < instance.getArguments().size(); ++index) {
      if (0 != index) {
        sb.append(", ");
      }

      final InstanceArgument instanceArgument = instance.getArguments().get(index);
      switch (instanceArgument.getKind()) {
        case EXPR: {
          final boolean isLocation = instanceArgument.getExpr().getNodeInfo().isLocation();
          final String text = ExprPrinter.toString(instanceArgument.getExpr(), isLocation);
          sb.append(String.format("new %s(%s)", Immediate.class.getSimpleName(), text));
          break;
        }

        case INSTANCE: {
          sb.append(toString(instanceArgument.getInstance()));
          break;
        }

        case PRIMITIVE: {
          final String argumentName = argumentNames.get(index);
          final Primitive argument = primitive.getArguments(). get(argumentName);

          if (Primitive.Kind.IMM == argument.getKind() &&
              Primitive.Kind.MODE == instanceArgument.getPrimitive().getKind()) {
            sb.append(String.format("new %s(%s.access(pe__, vars__))",
                Immediate.class.getSimpleName(), instanceArgument.getName()));
          } else {
            sb.append(instanceArgument.getName());
          }

          break;
        }

        default: {
          throw new IllegalArgumentException("Unknown kind: " + instanceArgument.getKind());
        }
      }
    }

    sb.append(')');
    return sb.toString();
  }
}
