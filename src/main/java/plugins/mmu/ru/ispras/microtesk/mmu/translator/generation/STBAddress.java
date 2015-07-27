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

import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.translator.generation.STBuilder;

import java.util.Map;

final class STBAddress implements STBuilder {
  private static final Class<?> BASE_CLASS =
      ru.ispras.microtesk.mmu.model.api.Address.class; 

  private final String packageName;
  private final Address address;

  public STBAddress(final String packageName, final Address address) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(address);

    this.packageName = packageName;
    this.address = address;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("address");

    buildHeader(st);
    buildFields(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", address.getId()); 
    st.add("base", BASE_CLASS.getSimpleName());
    st.add("pack", packageName);
    st.add("imps", BASE_CLASS.getName());
  }

  private void buildFields(final ST st, final STGroup group) {
    for (final Map.Entry<String, Type> field : address.getContentType().getFields().entrySet()) {
      final ST stField = group.getInstanceOf("field");
      stField.add("name", field.getKey());
      stField.add("size", field.getValue().getBitSize());
      st.add("fields", stField);
    }
  }
}
