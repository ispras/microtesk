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

import java.util.Date;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.codegen.StringTemplateBuilder;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;

final class StbPrimitive implements StringTemplateBuilder {
  private final Primitive primitive;

  public StbPrimitive(final Primitive primitive) {
    InvariantChecks.checkNotNull(primitive);
    this.primitive = primitive;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("primitive_file");

    st.add("time", new Date().toString());
    st.add("name", primitive.getName());

    return st;
  }
}
