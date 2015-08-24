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
import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBAddress implements STBuilder {
  public static final Class<?> ADDRESS_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType.class;

  public static final Class<?> INTEGER_CLASS =
      ru.ispras.microtesk.basis.solver.integer.IntegerVariable.class;

  private final String packageName;
  private final Address address;

  protected STBAddress(final String packageName, final Address address) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(address);

    this.packageName = packageName;
    this.address = address;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildFields(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", address.getId()); 
    st.add("pack", packageName);
    st.add("ext", ADDRESS_CLASS.getSimpleName());
    st.add("instance", "INSTANCE");

    st.add("imps", INTEGER_CLASS.getName());
    st.add("imps", ADDRESS_CLASS.getName());
  }

  private void buildFields(final ST st, final STGroup group) {
    final Type type = address.getContentType();

    final ST stConstructorDef = group.getInstanceOf("constructor_default");
    final ST stConstructor = group.getInstanceOf("constructor");

    stConstructorDef.add("name", address.getId());
    stConstructor.add("name", type.getId());

    STBStruct.buildFieldDecls(type, st, stConstructor, group);
    stConstructor.add("stmts", "");
    STBStruct.buildAddField(type, stConstructor, group);

    stConstructor.add("stmts", "");
    stConstructor.add("stmts", String.format("setVariable(%s);",
                      Utils.toString(address.getAccessChain(), ".")));

    st.add("members", "");
    st.add("members", stConstructorDef);

    st.add("members", "");
    st.add("members", stConstructor);
  }
}
