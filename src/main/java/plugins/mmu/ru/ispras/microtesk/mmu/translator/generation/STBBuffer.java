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

import java.math.BigInteger;
import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBBuffer extends STBBuilderBase implements STBuilder {
  private static final String DATA_NAME = "data";

  private final Buffer buffer;
  private final Buffer parentBuffer;
  private final boolean isView;

  public STBBuffer(final String packageName, final Buffer buffer) {
    super(packageName);

    InvariantChecks.checkNotNull(buffer);
    this.buffer = buffer;
    this.parentBuffer = getParentBuffer(buffer);
    this.isView = buffer != parentBuffer;
  }

  private static Buffer getParentBuffer(final Buffer buffer) {
    Buffer parent = buffer;

    while (null != parent.getParent()) {
      parent = parent.getParent();
    }

    return parent;
  }

  @Override
  protected String getId() {
    return buffer.getId();
  }

  @Override
  public ST build(final STGroup group) {
    ExprPrinter.get().pushVariableScope();

    ExprPrinter.get().addVariableMappings(
        buffer.getAddressArg(), removePrefix(buffer.getAddressArg().getName()));
    ExprPrinter.get().addVariableMappings(
        buffer.getDataArg(), DATA_NAME);

    final ST st = group.getInstanceOf("buffer");

    buildHeader(st);
    buildEntry(st, group);
    buildIndexer(st, group);
    buildMatcher(st, group);
    buildConstructor(st, group);

    ExprPrinter.get().popVariableScope();
    return st;
  }

  private void buildHeader(final ST st) {
    st.add("imps", java.math.BigInteger.class.getName());

    final String baseName = String.format("%s<%s, %s>",
        CACHE_CLASS.getSimpleName(),
        String.format("%s.Entry", parentBuffer.getId()),
        buffer.getAddress().getId()
        );

    buildHeader(st, baseName);
  }

  private void buildNewLine(final ST st) {
    st.add("members", "");
  }

  private void buildEntry(final ST st, final STGroup group) {
    if (isView) {
      return;
    }

    final ST stEntry = group.getInstanceOf("entry");

    final Type type = buffer.getEntry();
    buildFields(type, stEntry, group);

    st.add("members", stEntry);
  }
  
  
  private void buildFields(final Type type, final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("struct_constructor");
    stConstructor.add("name", "Entry");

    for (final Map.Entry<String, Type>  field : type.getFields().entrySet()) {
      final String fieldName = field.getKey();
      final Type fieldType = field.getValue();

      final String fieldTypeName;
      final String fieldValue;

      if (fieldType.getId() != null) {
        fieldTypeName = fieldType.getId();
        fieldValue = String.format("new %s()", fieldTypeName);
      } else {
        fieldTypeName = BIT_VECTOR_CLASS.getSimpleName();
        fieldValue = String.format("%s.newEmpty(%d)", fieldTypeName, fieldType.getBitSize());
      }

      final ST stField = group.getInstanceOf("struct_field");
      stField.add("name", fieldName);
      stField.add("type", fieldTypeName);
      st.add("members", stField);

      final ST stFieldInit = group.getInstanceOf("struct_field_init");
      stFieldInit.add("name", fieldName);
      stFieldInit.add("value", fieldValue);
      stConstructor.add("fields", stFieldInit);
    }

    st.add("members", "");
    st.add("members", stConstructor);
  }

  /*private static void buildField(
      final String name,
      final Type type,
      final ST st,
      final STGroup group) {
    if (type.isStruct()) {
      for (final Map.Entry<String, Type> field : type.getFields().entrySet()) {
        final String fieldName = String.format("%s.%s", name, field.getKey());
        buildField(fieldName, field.getValue(), st, group);
      }
    } else {
      final ST stField = group.getInstanceOf("field");
      stField.add("name", name);

      if (type.getDefaultValue() != null) {
        stField.add("arg", ExprPrinter.bitVectorToString(type.getDefaultValue()));
      } else {
        stField.add("arg", type.getBitSize());
      }

      st.add("fields", stField);
    }
  }*/

  private void buildIndexer(final ST st, final STGroup group) {
    final ST stIndexer = group.getInstanceOf("indexer");

    stIndexer.add("addr_type", buffer.getAddress().getId());
    stIndexer.add("addr_name", removePrefix(buffer.getAddressArg().getName()));
    stIndexer.add("expr", indexToString(buffer.getIndex()));

    st.add("members", stIndexer);
  }

  private void buildMatcher(final ST st, final STGroup group) {
    buildNewLine(st);
    final ST stMatcher = group.getInstanceOf("matcher");

    stMatcher.add("entry_type", String.format("%s.Entry", parentBuffer.getId()));
    stMatcher.add("addr_type", buffer.getAddress().getId());
    stMatcher.add("addr_name", removePrefix(buffer.getAddressArg().getName()));
    stMatcher.add("data_name", DATA_NAME);
    stMatcher.add("expr", matchToString(buffer.getMatch()));

    st.add("members", stMatcher);
  }

  private void buildConstructor(final ST st, final STGroup group) {
    buildNewLine(st);
    final ST stConstructor = group.getInstanceOf("constructor");

    stConstructor.add("name", buffer.getId());
    stConstructor.add("ways", buffer.getWays());
    stConstructor.add("sets", buffer.getSets());

    stConstructor.add("policy", String.format("%s.%s",
        POLICY_ID_CLASS.getSimpleName(), buffer.getPolicy().name()));

    st.add("members", stConstructor);
  }

  private String indexToString(final Node expr) {
    InvariantChecks.checkNotNull(expr);

    if (ExprUtils.isConstant(expr)) {
      final NodeValue value = (NodeValue) expr;
      if (value.isType(DataTypeId.LOGIC_INTEGER)) {
        return ExprPrinter.bitVectorToString(BitVector.valueOf(
            value.getInteger(), buffer.getAddressArg().getBitSize()));
      }
      throw new IllegalArgumentException(
          String.format("Illegal index expression: %s", value));
    }

    return ExprPrinter.get().toString(expr);
  }

  private String matchToString(final Node expr) {
    InvariantChecks.checkNotNull(expr);

    if (ExprUtils.isConstant(expr)) {
      final NodeValue value = (NodeValue) expr;
      if (value.isType(DataTypeId.LOGIC_BOOLEAN)) {
        return Boolean.toString(value.getBoolean());
      } else if (value.isType(DataTypeId.LOGIC_INTEGER) && 
                 value.getInteger().equals(BigInteger.ZERO)) {
        return Boolean.toString(true);
      } else {
        throw new IllegalArgumentException(
            String.format("Illegal match expression: %s", value));
      }
    }

    return ExprPrinter.get().toString(expr);
  }
}
