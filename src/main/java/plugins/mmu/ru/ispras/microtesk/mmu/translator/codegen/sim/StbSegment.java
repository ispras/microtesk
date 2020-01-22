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

package ru.ispras.microtesk.mmu.translator.codegen.sim;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.castle.codegen.StringTemplateBuilder;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.mmu.translator.ir.AbstractStorage;
import ru.ispras.microtesk.mmu.translator.ir.Attribute;
import ru.ispras.microtesk.mmu.translator.ir.Segment;

final class StbSegment extends StbCommon implements StringTemplateBuilder {
  private final Segment segment;

  public StbSegment(final String packageName, final Segment segment) {
    super(packageName);

    InvariantChecks.checkNotNull(segment);
    this.segment = segment;
  }

  @Override
  protected String getId() {
    return segment.getId();
  }

  @Override
  public ST build(final STGroup group) {
    ExprPrinter.get().pushVariableScope();

    final ST st = group.getInstanceOf("source_file");
    st.add("instance", "instance");

    buildHeader(st);
    buildConstructor(st, group);
    buildGetData(st, group);

    ExprPrinter.get().popVariableScope();
    return st;
  }

  private void buildHeader(final ST st) {
    final String baseName = String.format("%s<%s, %s>",
        SEGMENT_CLASS.getSimpleName(),
        segment.getDataArgAddress().getId(),
        segment.getAddress().getId());

    buildHeader(st, baseName);
  }

  private void buildConstructor(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("segment_constructor");
    stConstructor.add("name", segment.getId());

    final int bitSize = segment.getAddress().getAddressType().getBitSize();

    stConstructor.add("pa_type", segment.getDataArgAddress().getId());
    stConstructor.add("va_type", segment.getAddress().getId());
    stConstructor.add("start",
        ExprPrinter.bitVectorToString(BitVector.valueOf(segment.getMin(), bitSize)));
    stConstructor.add("end",
        ExprPrinter.bitVectorToString(BitVector.valueOf(segment.getMax(), bitSize)));

    st.add("members", stConstructor);
  }

  private void buildGetData(final ST st, final STGroup group) {
    final Attribute attr = segment.getAttribute(AbstractStorage.READ_ATTR_NAME);
    if (null == attr) {
      return;
    }

    final String addressName = removePrefix(segment.getAddressArg().getName());
    ExprPrinter.get().addVariableMappings(segment.getAddressArg(), addressName);

    final String dataName = removePrefix(segment.getDataArg().getName());
    final String dataTypeName = segment.getDataArgAddress().getId();

    ExprPrinter.get().addVariableMappings(segment.getDataArg(), dataName);

    final ST stMethod = group.getInstanceOf("get_data");

    stMethod.add("addr_type", segment.getAddress().getId());
    stMethod.add("addr_name", addressName);
    stMethod.add("data_type", dataTypeName);

    stMethod.add("stmts", String.format("final %s %s = new %s();",
        dataTypeName, dataName, dataTypeName));

    stMethod.add("stmts", "");

    buildVariableDecls(stMethod, segment.getVariables());
    buildStmts(stMethod, group, attr.getStmts());

    stMethod.add("stmts", "");
    stMethod.add("stmts", String.format("return %s;", dataName));

    st.add("members", "");
    st.add("members", stMethod);
  }
}
