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

import ru.ispras.microtesk.translator.nml.ir.shared.Type;

import java.util.HashSet;
import java.util.Set;

abstract class StbBase {
  private final Set<String> imports = new HashSet<>();

  protected final void addImport(final ST st, final String name) {
    if (!imports.contains(name)) {
      st.add("imps", name);
      imports.add(name);
    }
  }

  protected final String makeTypeName(final ST st, final Type type) {
    final String typeName;

    if (type.getAlias() != null) {
      typeName = WhymlUtils.getTypeName(type.getAlias());
    } else {
      final int typeSize = type.getBitSize();
      typeName = WhymlUtils.getTypeName(typeSize);
      BitVectorTheoryGenerator.getInstance().generate(typeSize);
      addImport(st, WhymlUtils.getTypeFullName(typeSize));
    }

    return typeName;
  }
}
