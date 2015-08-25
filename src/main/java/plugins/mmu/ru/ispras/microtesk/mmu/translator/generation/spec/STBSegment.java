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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.AbstractStorage;
import ru.ispras.microtesk.mmu.translator.ir.Attribute;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBSegment implements STBuilder {
  public static final Class<?> SEGMENT_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuSegment.class;

  public static final Class<?> INTEGER_CLASS =
      ru.ispras.microtesk.basis.solver.integer.IntegerVariable.class;

  public static final Class<?> SPEC_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem.class;

  private final String packageName;
  private final Segment segment;

  protected STBSegment(final String packageName, final Segment segment) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(segment);

    this.packageName = packageName;
    this.segment = segment;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildArguments(st, group);
    buildConstructor(st, group);
    buildControlFlow(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", segment.getId()); 
    st.add("pack", packageName);
    st.add("ext", SEGMENT_CLASS.getSimpleName());
    st.add("instance", "INSTANCE");

    st.add("imps", INTEGER_CLASS.getName());
    st.add("imps", SEGMENT_CLASS.getName());
    st.add("imps", SPEC_CLASS.getName());
  }

  private void buildArguments(final ST st, final STGroup group) {
    final ST stAddress = group.getInstanceOf("field_alias");
    stAddress.add("name", getVariableName(segment.getAddressArg().getName()));
    stAddress.add("type", segment.getAddress().getId());
    st.add("members", stAddress);

    final ST stData = group.getInstanceOf("field_alias");
    stData.add("name", getVariableName(segment.getDataArg().getName()));
    stData.add("type", segment.getDataArgAddress().getId());
    st.add("members", stData);

    st.add("members", "");
  }

  private void buildConstructor(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("constructor");

    for (final Variable variable : segment.getVariables()) {
      final String name = getVariableName(variable.getName());
      final Type type = variable.getType();

      STBStruct.buildFieldDecl(
          name,
          type,
          st,
          stConstructor,
          group
          );
    }

    stConstructor.add("name", segment.getId());
    stConstructor.add("va", segment.getAddress().getId());
    stConstructor.add("pa", segment.getDataArgAddress().getId());
    stConstructor.add("start", String.format("0x%xL", segment.getMin()));
    stConstructor.add("end", String.format("0x%xL", segment.getMax()));
    stConstructor.add("mapped", "false");
    stConstructor.add("va_expr", "null");
    stConstructor.add("pa_expr", "null");

    st.add("members", "");
    st.add("members", stConstructor);
  }

  private void buildControlFlow(final ST st, final STGroup group) {
    final ST stReg = group.getInstanceOf("register");
    stReg.add("type", SPEC_CLASS.getSimpleName());

    final ControlFlowBuilder builder = new ControlFlowBuilder(
        segment.getId(),
        st,
        group,
        stReg
        );

    final Attribute read = segment.getAttribute(AbstractStorage.READ_ATTR_NAME);
    if (null != read) {
      builder.build("START", "STOP", read.getStmts());
    }

    st.add("members", "");
    st.add("members", stReg);
  }

  private String getVariableName(final String prefixedName) {
    return Utils.getVariableName(segment.getId(), prefixedName);
  }
}
