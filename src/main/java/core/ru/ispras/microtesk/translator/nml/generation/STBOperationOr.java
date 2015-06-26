/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.generation;

import static ru.ispras.microtesk.translator.generation.PackageInfo.OP_PACKAGE_FORMAT;

import java.util.ArrayList;
import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.instruction.Operation;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;

final class STBOperationOr implements ITemplateBuilder {
  private final String specFileName;
  private final String modelName;
  private final PrimitiveOR op;

  public STBOperationOr(String specFileName, String modelName, PrimitiveOR op) {
    assert op.getKind() == Primitive.Kind.OP;

    this.specFileName = specFileName;
    this.modelName = modelName;
    this.op = op;
  }

  @Override
  public ST build(STGroup group) {
    final ST t = group.getInstanceOf("op");

    t.add("name", op.getName());
    t.add("file", specFileName);
    t.add("pack", String.format(OP_PACKAGE_FORMAT, modelName));

    t.add("imps", Operation.class.getName());
    t.add("base", Operation.class.getSimpleName());

    final List<String> opNames = new ArrayList<String>(op.getORs().size());
    for (Primitive p : op.getORs())
      opNames.add(p.getName());

    t.add("ops", opNames);
    return t;
  }
}
