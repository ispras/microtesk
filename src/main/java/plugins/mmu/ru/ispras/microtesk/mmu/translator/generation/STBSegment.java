/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.generation;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.mmu.model.api.Data;
import ru.ispras.microtesk.mmu.translator.ir.AbstractStorage;
import ru.ispras.microtesk.mmu.translator.ir.Attribute;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBSegment implements STBuilder {
  private static final Class<?> BASE_CLASS =
      ru.ispras.microtesk.mmu.model.api.Segment.class;

  private final String packageName;
  private final Segment segment;
  private final String variablePrefix; 

  public STBSegment(final String packageName, final Segment segment) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(segment);

    this.packageName = packageName;
    this.segment = segment;
    this.variablePrefix = segment.getId() + ".";
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("segment");

    buildHeader(st);
    buildConstructor(st, group);
    buildGetData(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("pack", packageName);
    st.add("imps", BASE_CLASS.getName());
    st.add("imps", BitVector.class.getName());

    final String baseName = String.format("%s<%s, %s>",
        BASE_CLASS.getSimpleName(),
        segment.getDataArgAddress().getId(),
        segment.getAddress().getId());

    st.add("name", segment.getId()); 
    st.add("base", baseName);
  }

  private void buildConstructor(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("constructor");
    stConstructor.add("name", segment.getId());

    final int bitSize = segment.getAddress().getAddressType().getBitSize();

    stConstructor.add(
        "start", ExprPrinter.bitVectorToString(BitVector.valueOf(segment.getMin(), bitSize)));
    stConstructor.add(
        "end", ExprPrinter.bitVectorToString(BitVector.valueOf(segment.getMax(), bitSize)));

    st.add("members", stConstructor);
  }

  private void buildGetData(final ST st, final STGroup group) {
    final Attribute attr = segment.getAttribute(AbstractStorage.READ_ATTR_NAME);
    if (null == attr) {
      return;
    }

    st.add("imps", Data.class.getName());

    ExprPrinter.get().pushVariableScope();

    final String addressName = segment.getAddressArg().getName().replaceFirst(variablePrefix, "");
    ExprPrinter.get().addVariableMappings(segment.getAddressArg(), addressName);

    final String dataName = segment.getDataArg().getName().replaceFirst(variablePrefix, "");
    ExprPrinter.get().addVariableMappings(segment.getDataArg(), dataName);

    final ST stMethod = group.getInstanceOf("get_data");

    stMethod.add("addr_type", segment.getAddress().getId());
    stMethod.add("addr_name", addressName);
    stMethod.add("data_type", segment.getDataArgAddress().getId());

    stMethod.add("stmts", String.format("%s %s = null;",
        segment.getDataArgAddress().getId(), dataName));
    stMethod.add("stmts", "");

    buildStmts(stMethod, group);

    stMethod.add("stmts", "");
    stMethod.add("stmts", String.format("return %s;", dataName));

    st.add("members", "");
    st.add("members", stMethod);

    ExprPrinter.get().popVariableScope();
  }

  private void buildStmts(final ST st, final STGroup group) {
    for (final Variable variable : segment.getVariables()) {
      buildVariableDef(st, variable);
    }
  }

  private void buildVariableDef(final ST st, final Variable variable) {
    final String mappingName =
        variable.getName().replaceFirst(variablePrefix, "");

    final String typeName = variable.isStruct() ?
        Data.class.getSimpleName() : BitVector.class.getSimpleName();

    ExprPrinter.get().addVariableMappings(variable, mappingName);
    st.add("stmts", String.format("%s %s;", typeName,  mappingName));
  }
}
