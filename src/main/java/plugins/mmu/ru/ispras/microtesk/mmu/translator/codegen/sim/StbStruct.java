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

package ru.ispras.microtesk.mmu.translator.codegen.sim;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.castle.codegen.StringTemplateBuilder;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;

import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.Type;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

final class StbStruct implements StringTemplateBuilder {
  public static final boolean LOW_TO_HIGH = false;

  public static final Class<?> BIT_VECTOR_CLASS =
      ru.ispras.fortress.data.types.bitvector.BitVector.class;

  public static final Class<?> STRUCT_CLASS =
      ru.ispras.microtesk.mmu.model.sim.Struct.class;

  public static final Class<?> ADDRESS_CLASS =
      ru.ispras.microtesk.mmu.model.sim.Address.class;

  private final String packageName;
  private final boolean isAddress;
  private final Type type;
  private final String valueFieldName;

  public StbStruct(final String packageName, final Address address) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(address);

    this.packageName = packageName;
    this.isAddress = true;
    this.type = address.getContentType();

    final StringBuilder sb = new StringBuilder();
    for (final String name : address.getAccessChain()) {
      if (sb.length() > 0) {
        sb.append('.');
      }
      sb.append(name);
    }

    this.valueFieldName = sb.toString();
  }

  public StbStruct(final String packageName, final Type type) {
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
    buildSetValue(st, group, type.getId());

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", type.getId());
    st.add("pack", packageName);

    if (isAddress) {
      st.add("impls", String.format("%s<%s>", ADDRESS_CLASS.getSimpleName(), type.getId()));
      st.add("imps", ADDRESS_CLASS.getName());
    } else {
      st.add("impls", String.format("%s<%s>", STRUCT_CLASS.getSimpleName(), type.getId()));
      st.add("imps", STRUCT_CLASS.getName());
    }

    st.add("imps", BIT_VECTOR_CLASS.getName());
  }

  public static void buildFields(
      final ST st,
      final STGroup group,
      final String typeName,
      final Type type) {
    final ST stStruct = group.getInstanceOf("struct_body");
    stStruct.add("type", typeName);

    // {Name, isStruct} - required to generate asBitVector.
    final Deque<Pair<String, Boolean>> fields =
        new ArrayDeque<>(type.getFields().size());

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
        fieldValue = fieldType.getDefaultValue() != null
            ? ExprPrinter.bitVectorToString(fieldType.getDefaultValue())
            : String.format("%s.newEmpty(%d)", fieldTypeName, fieldType.getBitSize());
      }

      stStruct.add("fnames",  fieldName);
      stStruct.add("ftypes",  fieldTypeName);
      stStruct.add("fvalues", fieldValue);
      stStruct.add("fis_struct", fieldType.isStruct());

      if (LOW_TO_HIGH) {
        fields.addFirst(new Pair<>(fieldName, fieldType.isStruct()));
      } else {
        fields.addLast(new Pair<>(fieldName, fieldType.isStruct()));
      }
    }

    for (final Pair<String, Boolean> fieldInfo : fields) {
      stStruct.add("fnames_rev",  fieldInfo.first);
      stStruct.add("fis_struct_rev", fieldInfo.second);
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

  private void buildSetValue(final ST st, final STGroup group, final String typeName) {
    if (!isAddress) {
      return;
    }

    final ST stAddress = group.getInstanceOf("struct_set_value");
    stAddress.add("type", typeName);
    stAddress.add("temp_name", "address");
    stAddress.add("field_name", valueFieldName);

    st.add("members", "");
    st.add("members", stAddress);
  }
}
