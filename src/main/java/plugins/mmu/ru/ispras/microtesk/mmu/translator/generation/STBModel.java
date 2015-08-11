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
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBModel implements STBuilder {
  public static final String CLASS_NAME = "Model";

  private static final Class<?> MODEL_CLASS =
      ru.ispras.microtesk.mmu.model.api.MmuModel.class;

  private static final Class<?> MMU_CLASS =
      ru.ispras.microtesk.mmu.model.api.Mmu.class;

  private static final Class<?> SPEC_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem.class;

  private final String packageName;

  public STBModel(final String packageName) {
    InvariantChecks.checkNotNull(packageName);
    this.packageName = packageName;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("model");
    buildHeader(st);
    buildBody(st, group);
    return st;
  }

  protected final void buildHeader(final ST st) {
    st.add("name", CLASS_NAME); 
    st.add("base", MODEL_CLASS.getSimpleName());
    st.add("pack", packageName);

    st.add("imps", java.util.Map.class.getName());
    st.add("imps", MODEL_CLASS.getName());
    st.add("imps", MMU_CLASS.getName());
    st.add("imps", SPEC_CLASS.getName());
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stBody = group.getInstanceOf("body");

    stBody.add("name", CLASS_NAME);
    stBody.add("spec", SPEC_CLASS.getSimpleName());
    stBody.add("mmu",  MMU_CLASS.getSimpleName());

    st.add("members", stBody);
  }
}
