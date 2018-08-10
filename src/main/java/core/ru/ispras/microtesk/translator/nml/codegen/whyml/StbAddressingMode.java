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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.codegen.StringTemplateBuilder;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

import java.util.Date;
import java.util.Map;

final class StbAddressingMode extends StbBase implements StringTemplateBuilder {
  private final String modelName;
  private final PrimitiveAND addressingMode;

  public StbAddressingMode(final String modelName, final PrimitiveAND addressingMode) {
    InvariantChecks.checkNotNull(modelName);
    InvariantChecks.checkNotNull(addressingMode);

    this.modelName = modelName;
    this.addressingMode = addressingMode;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("primitive_file");

    st.add("time", new Date().toString());
    st.add("name", WhymlUtils.getModuleName(addressingMode.getName()));

    addImport(st, "mach.int.Int32");
    addImport(st, "mach.array.Array32");
    addImport(st, String.format("%s.state.State", modelName));

    if (null != addressingMode.getReturnExpr()) {
      buildReturnExpression(group, st);
    }

    return st;
  }

  private void buildReturnExpression(final STGroup group, final ST st) {
    final Expr expr = addressingMode.getReturnExpr();
    final Type type = addressingMode.getReturnType();

    final ST stFunction = group.getInstanceOf("function");

    final String functionName = addressingMode.getName().toLowerCase();
    stFunction.add("name", functionName);

    final String typeName = makeTypeName(st, type);
    stFunction.add("ret_type", typeName);

    stFunction.add("arg_names", "s__");
    stFunction.add("arg_types", "state");

    for (final Map.Entry<String, Primitive> entry : addressingMode.getArguments().entrySet()) {
      final String argName = entry.getKey();
      final Type argType = entry.getValue().getReturnType();
      final String argTypeName = makeTypeName(st, argType);

      stFunction.add("arg_names", argName);
      stFunction.add("arg_types", argTypeName);
    }

    // TODO
    stFunction.add("expr", "get s__.gpr 0");

    st.add("funcs", stFunction);
  }
}
