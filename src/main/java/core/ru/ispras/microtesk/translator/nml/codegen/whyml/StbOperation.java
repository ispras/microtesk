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
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;

import java.util.Date;

final class StbOperation extends StbBase implements StringTemplateBuilder {
  private final String modelName;
  private final PrimitiveAND operation;

  public StbOperation(final String modelName, final PrimitiveAND operation) {
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

    final ST stFunction = group.getInstanceOf("function");
    final String functionName = operation.getName().toLowerCase();

    stFunction.add("name", functionName);
    stFunction.add("ret_type", "state");

    stFunction.add("arg_names", "s__");
    stFunction.add("arg_types", "state");
    stFunction.add("expr", "s__");

    st.add("funcs", stFunction);

    return st;
  }
}
