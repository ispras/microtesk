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
import ru.ispras.microtesk.mmu.translator.ir.Callable;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBFunction extends STBCommon implements STBuilder {
  private static final Class<?> STD_FUNC_CLASS =
      ru.ispras.microtesk.model.api.StandardFunctions.class;

  private final Callable function;

  public STBFunction(
      final String packageName,
      final Callable function) {
    super(packageName);

    InvariantChecks.checkNotNull(function);
    this.function = function;
  }

  @Override
  protected String getId() {
    return function.getName();
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildBody(st, group);

    return st;
  }

  protected final void buildHeader(final ST st) {
    st.add("name", getId()); 
    st.add("pack", packageName);
    st.add("ext",  STD_FUNC_CLASS.getName());
    st.add("imps", BigInteger.class.getName());
    st.add("imps", String.format("%s.*", BIT_VECTOR_CLASS.getPackage().getName()));
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stBody = group.getInstanceOf("function_body");

    final Variable output = function.getOutput();
    if (output != null) {
      final Type type = output.getType();
      stBody.add("type", type.getId() != null ? type.getId() : BIT_VECTOR_CLASS.getSimpleName());
    }

    for (final Variable variable : function.getParameters()) {
      final String name = removePrefix(variable.getName());
      final Type type = variable.getType();

      final String typeName = type.getId() != null ?
          type.getId() : BIT_VECTOR_CLASS.getSimpleName();

      stBody.add("anames", name);
      stBody.add("atypes", typeName);

      ExprPrinter.get().addVariableMappings(variable, name);
    }

    buildVariableDecls(stBody, function.getLocals().values());
    buildStmts(stBody, group, function.getBody());

    st.add("members", stBody);
  }
}
