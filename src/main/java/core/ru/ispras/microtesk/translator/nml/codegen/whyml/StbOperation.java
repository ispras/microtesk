/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.whyml;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.castle.codegen.StringTemplateBuilder;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAnd;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOr;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

import java.util.Map;
import java.util.Date;

final class StbOperation extends StbBase implements StringTemplateBuilder {
  private final String modelName;
  private final PrimitiveAnd operation;

  public StbOperation(final String modelName, final PrimitiveAnd operation) {
    InvariantChecks.checkNotNull(modelName);
    InvariantChecks.checkNotNull(operation);

    this.modelName = modelName;
    this.operation = operation;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("primitive_file");

    st.add("time", new Date().toString());
    st.add("name", WhymlUtils.getModuleName(operation.getName()));

    addImport(st, "mach.int.Int32");
    addImport(st, "mach.array.Array32");
    addImport(st, String.format("%s.state.State", modelName));

    final ST stFunction = newFunction(group, st, operation.getName());
    addFunctionArguments(st, stFunction);

    stFunction.add("expr", "s__");
    st.add("funcs", stFunction);

    return st;
  }

  private ST newFunction(final STGroup group, final ST st, final String name) {
    final ST stFunction = group.getInstanceOf("function");

    stFunction.add("name", name.toLowerCase());
    stFunction.add("ret_type", "state");

    return stFunction;
  }

  private void addFunctionArguments(final ST st, final ST stFunction) {
    stFunction.add("arg_names", "s__");
    stFunction.add("arg_types", "state");

    for (final Map.Entry<String, Primitive> entry : operation.getArguments().entrySet()) {
      final String argName = entry.getKey();
      final Primitive argPrimitive = entry.getValue();

      if (null == argPrimitive.getReturnType()) {
        continue;
      }

      final Type argType = getArgumentType(argPrimitive);
      final String argTypeName = makeTypeName(st, argType);

      stFunction.add("arg_names", argName);
      stFunction.add("arg_types", argTypeName);
    }
  }

  private Type getArgumentType(final Primitive primitive) {
    if (Primitive.Kind.OP == primitive.getKind()) {
      throw new IllegalArgumentException("Argument cannot be an operation!");
    }

    if (Primitive.Kind.IMM == primitive.getKind()) {
      return primitive.getReturnType();
    }

    InvariantChecks.checkTrue(Primitive.Kind.MODE == primitive.getKind());
    if (primitive.isOrRule()) {
      return getArgumentType((PrimitiveOr) primitive);
    } else {
      return getArgumentType((PrimitiveAnd) primitive);
    }
  }

  private Type getArgumentType(final PrimitiveAnd addressingMode) {
    if (addressingMode.getArguments().size() == 1) {
      return addressingMode.getArguments().values().iterator().next().getReturnType();
    } else {
      // TODO:
      return addressingMode.getReturnType();
    }
  }

  private Type getArgumentType(final PrimitiveOr addressingMode) {
    // TODO:
    return addressingMode.getReturnType();
  }
}
