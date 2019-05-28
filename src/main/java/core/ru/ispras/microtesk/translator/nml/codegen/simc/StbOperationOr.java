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

package ru.ispras.microtesk.translator.nml.codegen.simc;

import static ru.ispras.microtesk.translator.codegen.PackageInfo.OP_PACKAGE_FORMAT;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.castle.codegen.StringTemplateBuilder;
import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOr;

import java.util.ArrayList;
import java.util.List;

public final class StbOperationOr implements StringTemplateBuilder {
  private final String modelName;
  private final PrimitiveOr op;

  public StbOperationOr(String modelName, PrimitiveOr op) {
    assert op.getKind() == Primitive.Kind.OP;

    this.modelName = modelName;
    this.op = op;
  }

  @Override
  public ST build(STGroup group) {
    final ST t = group.getInstanceOf("op");

    t.add("name", op.getName());
    t.add("pack", String.format(OP_PACKAGE_FORMAT, modelName));
    t.add("base", IsaPrimitive.class.getSimpleName());
    t.add("imps", String.format("%s.*", IsaPrimitive.class.getPackage().getName()));

    final List<String> opNames = new ArrayList<String>(op.getOrs().size());
    for (Primitive p : op.getOrs()) {
      opNames.add(p.getName());
    }

    t.add("ops", opNames);
    return t;
  }
}
