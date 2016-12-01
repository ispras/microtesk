/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.generation.decoder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.decoder.DecoderItem;
import ru.ispras.microtesk.decoder.DecoderResult;
import ru.ispras.microtesk.model.api.Immediate;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.generation.STBuilder;
import ru.ispras.microtesk.translator.nml.ir.primitive.ImageInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;

final class STBDecoder implements STBuilder {
  private final String name;
  private final String modelName;
  private final ImageInfo imageInfo;
  private final PrimitiveAND item;

  public STBDecoder(final String modelName, final PrimitiveAND item) {
    InvariantChecks.checkNotNull(modelName);
    InvariantChecks.checkNotNull(item);
    InvariantChecks.checkNotNull(item.getInfo().getImageInfo());

    this.name = DecoderGenerator.getDecoderName(item.getName());
    this.modelName = modelName;
    this.imageInfo = item.getInfo().getImageInfo();
    this.item = item;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildBody(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", name);
    st.add("pack", String.format(PackageInfo.MODEL_PACKAGE_FORMAT + ".decoder", modelName));
    st.add("ext", DecoderItem.class.getSimpleName());
    st.add("instance", "instance");
    st.add("imps", DecoderItem.class.getName());
    st.add("imps", DecoderResult.class.getName());
    st.add("imps", BitVector.class.getName());
    st.add("imps", ru.ispras.microtesk.model.api.data.Type.class.getName());
    st.add("simps", String.format(PackageInfo.MODEL_PACKAGE_FORMAT + ".TypeDefs", modelName));

    final Set<String> imported = new HashSet<>();
    importPrimitive(st, item, imported);

    for (final Primitive primitive : item.getArguments().values()) {
      importPrimitive(st, primitive, imported);
    }
  }

  private void importPrimitive(
      final ST st,
      final Primitive primitive,
      final Set<String> imported) {
    final String primitiveClass = getPrimitiveClassName(primitive);
    if (!imported.contains(primitiveClass)) {
      imported.add(primitiveClass);
      st.add("imps", primitiveClass);
    }
  }

  private String getPrimitiveClassName(final Primitive primitive) {
    InvariantChecks.checkNotNull(primitive);

    switch (primitive.getKind()) {
      case IMM:
        return Immediate.class.getName();

      case MODE:
        return String.format(PackageInfo.MODE_CLASS_FORMAT, modelName, primitive.getName());

      case OP:
        return String.format(PackageInfo.OP_CLASS_FORMAT, modelName, primitive.getName());
    }

    throw new IllegalArgumentException(
        "Unknown primitive kind: " + primitive.getKind());
  }

  private static String getPrimitiveName(final Primitive primitive) {
    final boolean isImm = primitive.getKind() == Primitive.Kind.IMM;
    return isImm ? Immediate.class.getSimpleName() : primitive.getName();
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("decoder_constructor");

    stConstructor.add("name", name);
    stConstructor.add("size", imageInfo.getMaxImageSize());
    stConstructor.add("is_fixed", Boolean.toString(imageInfo.isImageSizeFixed()));

    final BitVector opc = imageInfo.getOpc();
    final BitVector opcMask = imageInfo.getOpcMask();

    stConstructor.add("opc", null != opc ? "\"" + opc.toBinString() + "\"": "null");
    stConstructor.add("opc_mask", null != opcMask ? "\"" + opcMask.toBinString() + "\"" : "null");

    final List<String> argumentNames = new ArrayList<>();
    for (final Map.Entry<String, Primitive> entry : item.getArguments().entrySet()) {
      final String argumentName = entry.getKey();
      argumentNames.add(argumentName);

      stConstructor.add("stmts", String.format(
          "%s %s = null;", getPrimitiveName(entry.getValue()), argumentName));
    }

    for (final Node field : imageInfo.getFields()) {
      if (ExprUtils.isValue(field)) {
        stConstructor.add("stmts", "");
        buildOpcCheck(stConstructor, group, field);
      } else if (isImmediateArgument(field)) {
        buildImmediate(stConstructor, group, field);
      }
    }

    final ST stResult = group.getInstanceOf("decoder_result");
    stResult.add("name", item.getName());
    stResult.add("args", argumentNames);
    stConstructor.add("stmts", stResult);

    st.add("members", stConstructor);
  }

  private void buildOpcCheck(final ST st, final STGroup group, final Node field) {
    InvariantChecks.checkTrue(ExprUtils.isValue(field));
    InvariantChecks.checkTrue(field.isType(DataTypeId.BIT_VECTOR) ||
                              field.isType(DataTypeId.LOGIC_STRING));

    final ST stOpcCheck = group.getInstanceOf("decoder_opc_check");
    final int size = field.isType(DataTypeId.BIT_VECTOR) ?
        field.getDataType().getSize() : field.toString().length();

    stOpcCheck.add("value", field.toString());
    stOpcCheck.add("size", size);

    st.add("stmts", stOpcCheck);
  }

  private boolean isImmediateArgument(final Node field) {
    if (!ExprUtils.isVariable(field)) {
      return false;
    }

    final NodeVariable variable = (NodeVariable) field;
    return item.getArguments().containsKey(variable.getName());
  }

  private void buildImmediate(final ST st, final STGroup group, final Node field) {
    InvariantChecks.checkTrue(ExprUtils.isVariable(field));

    final String name = field.toString();
    final Primitive primitive = item.getArguments().get(name);

    InvariantChecks.checkNotNull(primitive);
    InvariantChecks.checkTrue(primitive.getKind() == Primitive.Kind.IMM);

    final ST stImmediate = group.getInstanceOf("decoder_immediate");

    stImmediate.add("name", name);
    stImmediate.add("type", primitive.getReturnType().getJavaText());

    st.add("stmts", stImmediate);
  }
}
