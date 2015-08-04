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

import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.mmu.translator.ir.AbstractStorage;
import ru.ispras.microtesk.mmu.translator.ir.Attribute;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBMemory extends STBBuilderBase implements STBuilder {
  private final String packageName;
  private final Ir ir;
  private final Memory memory;

  public STBMemory(
      final String packageName,
      final Ir ir,
      final Memory memory) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(memory);

    this.packageName = packageName;
    this.ir = ir;
    this.memory = memory;
  }

  @Override
  protected String getId() {
    return memory.getId();
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("memory");

    buildHeader(st);
    buildConstructor(st, group);
    buildGetData(st, group);
    buildSetData(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("pack", packageName);
    st.add("imps", MEMORY_CLASS.getName());
    st.add("imps", DATA_CLASS.getName());
    st.add("imps", BIT_VECTOR_CLASS.getName());

    final String baseName = String.format("%s<%s, %s>",
        MEMORY_CLASS.getSimpleName(),
        DATA_CLASS.getSimpleName(),
        memory.getAddress().getId());

    st.add("name", memory.getId()); 
    st.add("base", baseName);
  }

  private void buildConstructor(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("constructor");
    stConstructor.add("name", memory.getId());

    st.add("members", "");
    st.add("members", stConstructor);
  }

  private void buildGetData(final ST st, final STGroup group) {
    final Attribute attr = memory.getAttribute(AbstractStorage.READ_ATTR_NAME);
    InvariantChecks.checkNotNull(attr, "Attribute is undefined: " + AbstractStorage.READ_ATTR_NAME);

    final ST stMethod = group.getInstanceOf("get_data");

    stMethod.add("addr_type", memory.getAddress().getId());
    stMethod.add("addr_name", removePrefix(memory.getAddressArg().getName()));
    stMethod.add("data_type", DATA_CLASS.getSimpleName());

    buildVariableDecls(stMethod, memory.getVariables());
    buildStmts(stMethod, group, attr.getStmts());

    stMethod.add("stmts", "");
    stMethod.add("stmts", "return null;");

    st.add("members", "");
    st.add("members", stMethod);
  }

  private void buildSetData(final ST st, final STGroup group) {
    final Attribute attr = memory.getAttribute(AbstractStorage.WRITE_ATTR_NAME);
    InvariantChecks.checkNotNull(attr, "Attribute is undefined: " + AbstractStorage.WRITE_ATTR_NAME);

    final ST stMethod = group.getInstanceOf("set_data");

    stMethod.add("addr_type", memory.getAddress().getId());
    stMethod.add("addr_name", removePrefix(memory.getAddressArg().getName()));
    stMethod.add("data_type", DATA_CLASS.getSimpleName());
    stMethod.add("data_name", removePrefix(memory.getDataArg().getName()));

    buildVariableDecls(stMethod, memory.getVariables());
    buildStmts(stMethod, group, attr.getStmts());

    stMethod.add("stmts", "");
    stMethod.add("stmts", "return null;");

    st.add("members", "");
    st.add("members", stMethod);
  }
}
