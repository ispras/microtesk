/*
 * Copyright 2012-2018 ISP RAS (http://www.iStateas.ru)
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

import org.stringtemplate.v4.ST;

import ru.ispras.castle.codegen.StringTemplateBuilder;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;

import java.util.EnumMap;
import java.util.Map;

public abstract class StbPrimitiveBase implements StringTemplateBuilder {
  private static final Map<Attribute.Kind, String> RET_TYPE_MAP =
      new EnumMap<>(Attribute.Kind.class);

  static {
    RET_TYPE_MAP.put(Attribute.Kind.ACTION, "void");
    RET_TYPE_MAP.put(Attribute.Kind.EXPRESSION, "char*");
  }

  protected final String getRetTypeName(final Attribute.Kind kind) {
    return RET_TYPE_MAP.get(kind);
  }

  protected static void addStatement(
      final ST attrST, final Statement stmt, final boolean isReturn) {
    new StatementBuilder(attrST, isReturn).build(stmt);
  }
}
