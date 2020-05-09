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

package ru.ispras.microtesk.mmu.translator.codegen.sim;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.castle.codegen.StringTemplateBuilder;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.mmu.translator.ir.AbstractStorage;
import ru.ispras.microtesk.mmu.translator.ir.Attribute;
import ru.ispras.microtesk.mmu.translator.ir.Memory;

final class StbMemory extends StbCommon implements StringTemplateBuilder {
  private final Memory memory;

  public StbMemory(final String packageName, final Memory memory) {
    super(packageName);

    InvariantChecks.checkNotNull(memory);
    this.memory = memory;
  }

  @Override
  protected String getId() {
    return memory.getId();
  }

  @Override
  public ST build(final STGroup group) {
    ExprPrinter.get().pushVariableScope();

    final String addressName = removePrefix(memory.getAddressArg().getName());
    ExprPrinter.get().addVariableMappings(memory.getAddressArg(), addressName);

    final String dataName = removePrefix(memory.getDataArg().getName());
    ExprPrinter.get().addVariableMappings(memory.getDataArg(), dataName);

    final ST st = group.getInstanceOf("source_file");
    st.add("instance", "instance");
    st.add("imps", ru.ispras.microtesk.test.TestEngine.class.getName());

    buildHeader(st);
    buildConstructor(st, group);
    buildGetSize(st, group);
    buildGetData(st, group, addressName, dataName);
    buildSetData(st, group, addressName, dataName);

    ExprPrinter.get().popVariableScope();
    return st;
  }

  private void buildHeader(final ST st) {
    final String baseName = String.format("%s<%s>",
        MMU_CLASS.getSimpleName(),
        memory.getAddress().getId());

    buildHeader(st, baseName);
  }

  private void buildConstructor(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("mmu_constructor");

    stConstructor.add("name", memory.getId());
    stConstructor.add("addr_type", memory.getAddress().getId());

    st.add("members", stConstructor);
  }

  private void buildGetSize(final ST st, final STGroup group) {
    final ST stMethod = group.getInstanceOf("get_size");

    stMethod.add("addr_size", memory.getAddress().getAddressType().getBitSize());
    stMethod.add("data_size", memory.getDataArg().getBitSize());

    st.add("members", "");
    st.add("members", stMethod);
  }

  private void buildGetData(
      final ST st,
      final STGroup group,
      final String addressName,
      final String dataName) {
    final Attribute attr = memory.getAttribute(AbstractStorage.READ_ATTR_NAME);
    InvariantChecks.checkNotNull(attr, "Attribute is undefined: " + AbstractStorage.READ_ATTR_NAME);

    final ST stMethod = group.getInstanceOf("get_data");

    stMethod.add("addr_type", memory.getAddress().getId());
    stMethod.add("addr_name", addressName);
    stMethod.add("data_type", BIT_VECTOR_CLASS.getSimpleName());

    final String dataTypeName = BIT_VECTOR_CLASS.getSimpleName();

    final String dataValue =
        String.format("%s.newEmpty(%d)", dataTypeName, memory.getDataArg().getBitSize());

    final String dataStmtText =
        String.format("final %s %s = %s;", dataTypeName, dataName, dataValue);

    stMethod.add("stmts", dataStmtText);
    stMethod.add("stmts", "");

    buildVariableDecls(stMethod, memory.getVariables());
    buildStmts(stMethod, group, attr.getStmts());

    stMethod.add("stmts", "");
    stMethod.add("stmts", String.format("return %s;", dataName));

    st.add("members", "");
    st.add("members", stMethod);
  }

  private void buildSetData(
      final ST st,
      final STGroup group,
      final String addressName,
      final String dataName) {
    final Attribute attr = memory.getAttribute(AbstractStorage.WRITE_ATTR_NAME);
    InvariantChecks.checkNotNull(attr, "Attribute is undefined: "
        + AbstractStorage.WRITE_ATTR_NAME);

    final ST stMethod = group.getInstanceOf("set_data");

    stMethod.add("addr_type", memory.getAddress().getId());
    stMethod.add("addr_name", addressName);
    stMethod.add("data_type", BIT_VECTOR_CLASS.getSimpleName());
    stMethod.add("data_name", dataName);

    buildVariableDecls(stMethod, memory.getVariables());
    buildStmts(stMethod, group, attr.getStmts());

    st.add("members", "");
    st.add("members", stMethod);
  }
}
