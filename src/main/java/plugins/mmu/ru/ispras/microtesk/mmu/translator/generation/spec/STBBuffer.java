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
import ru.ispras.microtesk.mmu.model.api.PolicyId;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBBuffer implements STBuilder {
  public static final Class<?> BUFFER_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer.class;

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

    st.add("imps", INTEGER_CLASS.getName());
    st.add("imps", BUFFER_CLASS.getName());
    st.add("imps", STRUCT_CLASS.getName());
  }

  private void buildEntry(final ST st, final STGroup group) {
    final ST stEntry = group.getInstanceOf("entry");
    stEntry.add("name", "Entry");
    stEntry.add("ext", STRUCT_CLASS.getSimpleName());

    final ST stConstructor = group.getInstanceOf("entry_constructor");
    stConstructor.add("name", "Entry");

    STBStruct.buildFieldDecls(buffer.getEntry(), stEntry, stConstructor, group);
    stConstructor.add("stmts", "");
    STBStruct.buildAddField(buffer.getEntry(), stConstructor, group);

    stEntry.add("members", "");
    stEntry.add("members", stConstructor);

    st.add("members", "");
    st.add("members", stEntry);
  }

  private void buildConstructor(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("constructor");

    STBStruct.buildFieldDecls(buffer.getEntry(), st, stConstructor, group);
    stConstructor.add("stmts", "");
    STBStruct.buildAddField(buffer.getEntry(), stConstructor, group);
 
    stConstructor.add("name", buffer.getId());
    stConstructor.add("ways", String.format("%dL", buffer.getWays().longValue()));
    stConstructor.add("sets", String.format("%dL", buffer.getSets().longValue()));
    stConstructor.add("addr", buffer.getAddress().getId());
    stConstructor.add("tag", "null" /*toMmuExpressionText(analyzer.getTagFields())*/);
    stConstructor.add("index", "null" /*toMmuExpressionText(analyzer.getIndexFields())*/);
    stConstructor.add("offset", "null" /*toMmuExpressionText(analyzer.getOffsetFields())*/);
    stConstructor.add("match", "null" /*toMmuBindingsText(analyzer.getMatchBindings())*/);
    stConstructor.add("guard_cond", "null"); // TODO
    stConstructor.add("guard", "null"); // TODO
    stConstructor.add("replaceable", Boolean.toString(buffer.getPolicy() != PolicyId.NONE));
    if (buffer.getParent() != null) {
      stConstructor.add("parent", buffer.getParent().getId());
    }
 
    st.add("members", "");
    st.add("members", stConstructor);
  }
}
