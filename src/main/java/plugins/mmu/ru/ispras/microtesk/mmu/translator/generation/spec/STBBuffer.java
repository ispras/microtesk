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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.mmu.model.api.PolicyId;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBBuffer implements STBuilder {
  public static final Class<?> BINDING_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuBinding.class;

  public static final Class<?> BUFFER_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer.class;

  public static final Class<?> COND_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition.class;

  public static final Class<?> COND_ATOM_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuConditionAtom.class;

  public static final Class<?> EXPRESSION_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression.class;

  public static final Class<?> STRUCT_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuStruct.class;

  public static final Class<?> INTEGER_CLASS =
      ru.ispras.microtesk.basis.solver.integer.IntegerVariable.class;

  private final String packageName;
  private final Buffer buffer;

  public STBBuffer(final String packageName, final Buffer buffer) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(buffer);

    this.packageName = packageName;
    this.buffer = buffer;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildEntry(st, group);
    buildConstructor(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", buffer.getId()); 
    st.add("pack", packageName);
    st.add("ext",  BUFFER_CLASS.getSimpleName());
    st.add("instance", "INSTANCE");

    st.add("imps", Collections.class.getName());
    st.add("imps", Arrays.class.getName());
    st.add("imps", BigInteger.class.getName());
    st.add("imps", INTEGER_CLASS.getName());
    st.add("imps", BINDING_CLASS.getName());
    st.add("imps", BUFFER_CLASS.getName());
    st.add("imps", EXPRESSION_CLASS.getName());
    st.add("imps", STRUCT_CLASS.getName());
  }

  private void buildEntry(final ST st, final STGroup group) {
    final ST stEntry = group.getInstanceOf("entry");
    stEntry.add("name", "Entry");
    stEntry.add("ext", STRUCT_CLASS.getSimpleName());

    final ST stConstructor = group.getInstanceOf("entry_constructor");
    stConstructor.add("name", "Entry");

    final ST stAddress = group.getInstanceOf("field_alias");
    stAddress.add("name", getVariableName(buffer.getAddressArg().getName()));
    stAddress.add("type", buffer.getAddress().getId());
    st.add("members", stAddress);

    STBStruct.buildFieldDecls(buffer.getEntry(), stEntry, stConstructor, group);
    stConstructor.add("stmts", "");
    STBStruct.buildAddField(buffer.getEntry(), stConstructor, group);

    stEntry.add("members", "");
    stEntry.add("members", stConstructor);

    st.add("members", "");
    st.add("members", stEntry);
  }

  private void buildConstructor(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("buffer_constructor");

    STBStruct.buildFieldDecls(buffer.getEntry(), st, stConstructor, group);
    stConstructor.add("stmts", "");
    STBStruct.buildAddField(buffer.getEntry(), stConstructor, group);

    final BufferExprAnalyzer analyzer = new BufferExprAnalyzer(
        buffer.getAddress(), buffer.getAddressArg(), buffer.getIndex(), buffer.getMatch());
 
    stConstructor.add("name", buffer.getId());
    stConstructor.add("kind", buffer.getKind().name());
    stConstructor.add("ways", String.format("%dL", buffer.getWays().longValue()));
    stConstructor.add("sets", String.format("%dL", buffer.getSets().longValue()));
    stConstructor.add("addr", buffer.getAddress().getId());

    stConstructor.add("tag", Utils.toMmuExpressionText(buffer.getId(), analyzer.getTagFields()));
    stConstructor.add("index", Utils.toMmuExpressionText(buffer.getId(), analyzer.getIndexFields()));
    stConstructor.add("offset", Utils.toMmuExpressionText(buffer.getId(), analyzer.getOffsetFields()));
    stConstructor.add("match", String.format("Collections.<%s>emptyList()", BINDING_CLASS.getSimpleName()));

    stConstructor.add("replaceable", Boolean.toString(buffer.getPolicy() != PolicyId.NONE));
    if (buffer.getParent() != null) {
      stConstructor.add("parent", buffer.getParent().getId());
    }

    if (!analyzer.getMatchBindings().isEmpty()) {
      stConstructor.add("stmts", "");
      stConstructor.add("stmts",
          String.format("setMatchBindings(%s);", toMmuBindingsText(analyzer.getMatchBindings())));
    }

    st.add("members", stConstructor);
  }

  private String getVariableName(final String prefixedName) {
    return Utils.getVariableName(buffer.getId(), prefixedName);
  }

  private String toMmuBindingsText(final List<Pair<IntegerField, IntegerField>> bindings) {
    final StringBuilder sb = new StringBuilder();
    sb.append(String.format("Arrays.<MmuBinding>asList(", Arrays.class.getSimpleName()));

    boolean isFirst = true;
    for (final Pair<IntegerField, IntegerField> binding : bindings) {
      if (isFirst) {
        isFirst = false;
      } else {
        sb.append(',');
      }

      sb.append(System.lineSeparator());
      sb.append("    ");

      final String leftText = Utils.toString(buffer.getId(), binding.first);
      final String rightText = Utils.toString(buffer.getId(), binding.second);

      sb.append(String.format("new MmuBinding(%s, %s)", leftText, rightText));
    }

    sb.append(')');
    return sb.toString();
  }
}
