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

package ru.ispras.microtesk.mmu.translator.generation.sim;

import java.math.BigInteger;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Operation;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBOperation extends STBCommon implements STBuilder {
  public static final Class<?> OPERATION_CLASS =
      ru.ispras.microtesk.mmu.model.api.Operation.class;

  private final Operation operation;

  public STBOperation(
      final String packageName,
      final Operation operation) {
    super(packageName);

    InvariantChecks.checkNotNull(operation);
    this.operation = operation;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");
    st.add("instance", "instance");

    buildHeader(st);
    buildBody(st, group);

    return st;
  }

  protected final void buildHeader(final ST st) {
    final String implText = String.format(
        "%s<%s>",
        OPERATION_CLASS.getName(),
        operation.getAddress().getId()
        );

    st.add("name", operation.getId()); 
    st.add("pack", packageName);
    st.add("impls", implText);

    st.add("imps", BigInteger.class.getName());
    st.add("imps", String.format("%s.*", STBCommon.BIT_VECTOR_CLASS.getPackage().getName()));
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stBody = group.getInstanceOf("operation_body");
    stBody.add("addr", operation.getAddress().getId());

    final String addrName = removePrefix(operation.getAddressArg().getName());
    stBody.add("addr_name", addrName);

    ExprPrinter.get().pushVariableScope();
    ExprPrinter.get().addVariableMappings(operation.getAddressArg(), addrName);

    buildStmts(stBody, group, operation.getStmts());
    ExprPrinter.get().popVariableScope();

    st.add("members", stBody);
  }

  @Override
  protected String getId() {
    return operation.getId();
  }
}
