/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.codegen.spec;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.castle.codegen.StringTemplateBuilder;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;

import ru.ispras.microtesk.mmu.model.sim.EvictPolicyId;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class StbBuffer implements StringTemplateBuilder {
  public static final Class<?> BINDING_CLASS =
      ru.ispras.microtesk.mmu.model.spec.MmuBinding.class;

  public static final Class<?> BUFFER_CLASS =
      ru.ispras.microtesk.mmu.model.spec.MmuBuffer.class;

  public static final Class<?> STRUCT_CLASS =
      ru.ispras.microtesk.mmu.model.spec.MmuStruct.class;

  public static final Class<?> BIT_VECTOR_CLASS =
      ru.ispras.fortress.data.types.bitvector.BitVector.class;

  public static final Class<?> DATA_TYPE_CLASS =
      ru.ispras.fortress.data.DataType.class;

  public static final Class<?> VARIABLE_CLASS =
      ru.ispras.fortress.expression.NodeVariable.class;

  private final String packageName;
  private final Buffer buffer;

  public StbBuffer(final String packageName, final Buffer buffer) {
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
    st.add("imps", DATA_TYPE_CLASS.getName());
    st.add("imps", VARIABLE_CLASS.getName());
    st.add("imps", BIT_VECTOR_CLASS.getName());

    st.add("imps", ru.ispras.fortress.expression.Nodes.class.getName());
    st.add("imps", ru.ispras.fortress.expression.NodeValue.class.getName());

    st.add("imps", BINDING_CLASS.getName());
    st.add("imps", BUFFER_CLASS.getName());
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

    StbStruct.buildFieldDecls(buffer.getEntry(), stEntry, stConstructor, group);
    stConstructor.add("stmts", "");
    StbStruct.buildAddField(buffer.getEntry(), stConstructor, group);

    stEntry.add("members", "");
    stEntry.add("members", stConstructor);

    st.add("members", "");
    st.add("members", stEntry);
  }

  private void buildConstructor(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("buffer_constructor");

    st.add("members", "");
    StbStruct.buildFieldDecls(buffer.getEntry(), st, stConstructor, group);
    stConstructor.add("stmts", "");
    StbStruct.buildAddField(buffer.getEntry(), stConstructor, group);

    final BufferExprAnalyzer analyzer = new BufferExprAnalyzer(
        buffer.getAddress(), buffer.getAddressArg(), buffer.getIndex(), buffer.getMatch());

    stConstructor.add("name", buffer.getId());
    stConstructor.add("kind", buffer.getKind().name());
    stConstructor.add("ways", String.format("%dL", buffer.getWays().longValue()));
    stConstructor.add("sets", String.format("%dL", buffer.getSets().longValue()));
    stConstructor.add("addr", buffer.getAddress().getId());

    stConstructor.add("tag", Utils.toMmuExpressionText(buffer.getId(), analyzer.getTagFields()));
    stConstructor.add("index",
        Utils.toMmuExpressionText(buffer.getId(), analyzer.getIndexFields()));
    stConstructor.add("offset",
        Utils.toMmuExpressionText(buffer.getId(), analyzer.getOffsetFields()));
    stConstructor.add("match",
        String.format("Collections.<%s>emptyList()", BINDING_CLASS.getSimpleName()));

    stConstructor.add("replaceable",
            Boolean.toString(buffer.getPolicy().evict != EvictPolicyId.NONE));
    if (buffer.getNext() != null) {
      stConstructor.add("parent", buffer.getNext().getId());
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

  private String toMmuBindingsText(final List<Pair<Node, Node>> bindings) {
    final StringBuilder sb = new StringBuilder();
    sb.append(String.format("Arrays.<MmuBinding>asList(", Arrays.class.getSimpleName()));

    boolean isFirst = true;
    for (final Pair<Node, Node> binding : bindings) {
      if (isFirst) {
        isFirst = false;
      } else {
        sb.append(',');
      }

      sb.append(System.lineSeparator());
      sb.append("    ");

      final String leftText = Utils.toMmuExpressionText(buffer.getId(), binding.first);
      final String rightText = Utils.toMmuExpressionText(buffer.getId(), binding.second);

      sb.append(String.format("new MmuBinding(%s, %s)", leftText, rightText));
    }

    sb.append(')');
    return sb.toString();
  }
}
