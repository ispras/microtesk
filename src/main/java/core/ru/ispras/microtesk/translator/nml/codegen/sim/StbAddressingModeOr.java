/*
 * Copyright 2012-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.sim;

import static ru.ispras.microtesk.translator.codegen.PackageInfo.MODE_PACKAGE_FORMAT;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.codegen.StringTemplateBuilder;
import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;

import java.util.ArrayList;
import java.util.List;

final class StbAddressingModeOr implements StringTemplateBuilder {
  private final String modelName;
  private final PrimitiveOR mode;

  public StbAddressingModeOr(String modelName, PrimitiveOR mode) {
    assert mode.getKind() == Primitive.Kind.MODE;

    this.modelName = modelName;
    this.mode = mode;
  }

  @Override
  public ST build(STGroup group) {
    final ST t = group.getInstanceOf("modeor");

    t.add("name", mode.getName());
    t.add("pack", String.format(MODE_PACKAGE_FORMAT, modelName));
    t.add("base", IsaPrimitive.class.getSimpleName());
    t.add("imps", String.format("%s.*", IsaPrimitive.class.getPackage().getName()));

    final List<String> modeNames = new ArrayList<String>(mode.getOrs().size());
    for (Primitive p : mode.getOrs()) {
      modeNames.add(p.getName());
    }

    t.add("modes", modeNames);

    return t;
  }
}
