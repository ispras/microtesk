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
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBSegment implements STBuilder {
  public static final Class<?> EXPRESSION_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression.class;

  public static final Class<?> SEGMENT_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuSegment.class;

  public static final Class<?> INTEGER_CLASS =
      ru.ispras.microtesk.basis.solver.integer.IntegerVariable.class;

  public static final Class<?> SPEC_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem.class;

  private final String packageName;
  private final Ir ir;
  private final Segment segment;

  protected STBSegment(final String packageName, final Ir ir, final Segment segment) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(segment);

    this.packageName = packageName;
    this.ir = ir;
    this.segment = segment;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildArguments(st, group);
    buildConstructor(st, group);
    buildFunction(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", segment.getId()); 
    st.add("pack", packageName);
    st.add("ext", SEGMENT_CLASS.getSimpleName());
    st.add("instance", "INSTANCE");

    st.add("imps", INTEGER_CLASS.getName());
    st.add("imps", EXPRESSION_CLASS.getName());
    st.add("imps", SEGMENT_CLASS.getName());
    st.add("imps", SPEC_CLASS.getName());
  }

  private void buildArguments(final ST st, final STGroup group) {
    STBStruct.buildFieldAlias(
        segment.getId(),
        segment.getAddressArg(),
        segment.getAddress(),
        true,
        st,
        group
        );

    STBStruct.buildFieldAlias(
        segment.getId(),
        segment.getDataArg(),
        segment.getDataArgAddress(),
        true,
        st,
        group
        );
  }

  private void buildConstructor(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("constructor");

    stConstructor.add("name", segment.getId());
    stConstructor.add("va", segment.getAddress().getId());
    stConstructor.add("pa", segment.getDataArgAddress().getId());
    stConstructor.add("start", String.format("0x%xL", segment.getMin()));
    stConstructor.add("end", String.format("0x%xL", segment.getMax()));

    final SegmentControlFlowExplorer explorer =
        new SegmentControlFlowExplorer(segment);

    stConstructor.add("mapped", Boolean.toString(explorer.isMapped()));
    stConstructor.add("va_expr", Utils.toMmuExpressionText(segment.getId(), explorer.getPaExpr()));
    stConstructor.add("pa_expr", Utils.toMmuExpressionText(segment.getId(), explorer.getRestExpr()));

    final ST stReg = group.getInstanceOf("register");
    stReg.add("type", SPEC_CLASS.getSimpleName());

    st.add("members", "");
    st.add("members", stConstructor);
 
    st.add("members", "");
    st.add("members", stReg);
  }

  private String getVariableName(final String prefixedName) {
    return Utils.getVariableName(segment.getId(), prefixedName);
  }

  private void buildFunction(final ST st, final STGroup group) {
    final Attribute read = segment.getAttribute(AbstractStorage.READ_ATTR_NAME);
    if (read == null) {
      return;
    }

    ControlFlowBuilder.buildImports(st, group);
    st.add("imps", java.util.ArrayList.class.getName());
    st.add("imps", java.util.List.class.getName());

    final ST stFunction = group.getInstanceOf("function");
    stFunction.add("name", "Function");

    buildArguments(stFunction, group);

    final ST stConstructor = group.getInstanceOf("function_constructor");
    stConstructor.add("name", "Function");

    final ST stReg = group.getInstanceOf("register");
    stReg.add("type", SPEC_CLASS.getSimpleName());

    if (!segment.getVariables().isEmpty()) {
      stFunction.add("members", "");
    }

    for (final Variable variable : segment.getVariables()) {
      final String name = getVariableName(variable.getName());
      final Type type = variable.getType();

      STBStruct.buildFieldDecl(
          name,
          type,
          false,
          stFunction,
          stConstructor,
          group
          );

      stReg.add("vars", name);
    }

    final ControlFlowBuilder controlFlowBuilder = new ControlFlowBuilder(
        ir,
        segment.getId(),
        stFunction,
        group,
        stConstructor,
        stReg
        );

    controlFlowBuilder.build("START", "STOP", read.getStmts());

    stFunction.add("members", "");
    stFunction.add("members", stConstructor);

    stFunction.add("members", "");
    stFunction.add("members", stReg);

    st.add("members", "");
    st.add("members", stFunction);
  }
}
