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

import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.translator.generation.STBuilder;

import java.util.Map;

final class STBStruct implements STBuilder {
  public static final Class<?> BIT_VECTOR_CLASS =
      ru.ispras.fortress.data.types.bitvector.BitVector.class;

  public static final Class<?> ADDRESS_CLASS =
      ru.ispras.microtesk.mmu.model.api.Address.class;

  public static final Class<?> DATA_CLASS =
      ru.ispras.microtesk.mmu.model.api.Data.class;

  private final String packageName; 
  private final boolean isAddress;
  private final Type type;
  private final String valueFieldName;

  public STBStruct(final String packageName, final Address address) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(address);

    this.packageName = packageName;
    this.isAddress = true;
    this.type = address.getContentType();

    final StringBuilder sb = new StringBuilder();
    for(final String name : address.getAccessChain()) {
      if (sb.length() > 0) sb.append('.');
      sb.append(name);
    }

    this.valueFieldName = sb.toString();
  }

  public STBStruct(final String packageName, final Type type) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(type);

    this.packageName = packageName;
    this.isAddress = false;
    this.type = type;
    this.valueFieldName = null;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildFields(st, group, type.getId(), type);
    buildGetValue(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", type.getId());
    st.add("pack", packageName);
    st.add("impls", DATA_CLASS.getSimpleName());

    st.add("imps", BIT_VECTOR_CLASS.getName());
    st.add("imps", DATA_CLASS.getName());

    if (isAddress) {
      st.add("impls", ADDRESS_CLASS.getSimpleName());
      st.add("imps", ADDRESS_CLASS.getName());
    }
  }

  public static void buildFields(
      final ST st,
      final STGroup group,
      final String typeName,
      final Type type) {
    final ST stStruct = group.getInstanceOf("struct_body");
    stStruct.add("type", typeName);

    for (final Map.Entry<String, Type> field : type.getFields().entrySet()) {
      final String fieldName = field.getKey();
      final Type fieldType = field.getValue();

      final String fieldTypeName;
      final String fieldValue;

      if (fieldType.getId() != null) {
        fieldTypeName = fieldType.getId();
        fieldValue = String.format("new %s()", fieldTypeName);
      } else {
        fieldTypeName = BIT_VECTOR_CLASS.getSimpleName();
        fieldValue = fieldType.getDefaultValue() != null ?
            ExprPrinter.bitVectorToString(fieldType.getDefaultValue()) :
            String.format("%s.newEmpty(%d)", fieldTypeName, fieldType.getBitSize());
      }

      stStruct.add("fnames",  fieldName);
      stStruct.add("ftypes",  fieldTypeName);
      stStruct.add("fvalues", fieldValue);
      stStruct.add("fis_struct", fieldType.isStruct());
    }

    st.add("members", "");
    st.add("members", stStruct);
  }

  private void buildGetValue(final ST st, final STGroup group) {
    if (!isAddress) {
      return;
    }

    final ST stAddress = group.getInstanceOf("struct_get_value");
    stAddress.add("field_name", valueFieldName);

    st.add("members", "");
    st.add("members", stAddress);
  }
}
