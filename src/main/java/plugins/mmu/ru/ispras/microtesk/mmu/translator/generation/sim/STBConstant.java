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

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Constant;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBConstant implements STBuilder {
  private final String packageName;
  private final Constant constant;

  public STBConstant(
      final String packageName,
      final Constant constant) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(constant);

    this.packageName = packageName;
    this.constant = constant;

    ExprPrinter.get().addVariableMapping(
        constant.getId(), String.format("%s.get()", constant.getId()));
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildBody(st, group);

    return st;
  }

  protected final void buildHeader(final ST st) {
    st.add("name", constant.getId()); 
    st.add("pack", packageName);
    st.add("imps", String.format("%s.*", STBCommon.BIT_VECTOR_CLASS.getPackage().getName()));
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stBody = group.getInstanceOf("constant_body");

    final String exprText = ExprPrinter.get().toString(constant.getExpression());
    stBody.add("expr", exprText);

    st.add("members", stBody);
  }
}
