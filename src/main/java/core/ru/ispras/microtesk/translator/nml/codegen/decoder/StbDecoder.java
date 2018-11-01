/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.decoder;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.castle.codegen.StringTemplateBuilder;
import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.model.Immediate;
import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.model.decoder.DecoderItem;
import ru.ispras.microtesk.model.decoder.DecoderResult;
import ru.ispras.microtesk.translator.codegen.PackageInfo;
import ru.ispras.microtesk.translator.nml.codegen.sim.ExprPrinter;
import ru.ispras.microtesk.translator.nml.codegen.sim.StatementBuilder;
import ru.ispras.microtesk.translator.nml.codegen.sim.StbTemporaryVariables;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourceMemory;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Instance;
import ru.ispras.microtesk.translator.nml.ir.primitive.InstanceArgument;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAnd;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class StbDecoder implements StringTemplateBuilder {
  private final String name;
  private final String modelName;
  private final ImageInfo imageInfo;
  private final PrimitiveAnd item;
  private final Set<String> imported;
  private final Set<String> undecoded;

  public StbDecoder(final String modelName, final PrimitiveAnd item) {
    InvariantChecks.checkNotNull(modelName);
    InvariantChecks.checkNotNull(item);
    InvariantChecks.checkNotNull(ImageAnalyzer.getImageInfo(item));

    this.name = DecoderGenerator.getDecoderName(item.getName());
    this.modelName = modelName;
    this.imageInfo = ImageAnalyzer.getImageInfo(item);
    this.item = item;
    this.imported = new HashSet<>();
    this.undecoded = new LinkedHashSet<>();
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
    st.add("imps", ru.ispras.microtesk.model.data.Type.class.getName());
    st.add("imps", String.format(PackageInfo.MODEL_PACKAGE_FORMAT + ".TempVars", modelName));
    st.add("simps", String.format(PackageInfo.MODEL_PACKAGE_FORMAT + ".TypeDefs", modelName));

    importPrimitive(st, item);
    for (final Primitive primitive : item.getArguments().values()) {
      importPrimitive(st, primitive);
    }
  }

  private void importPrimitive(final ST st, final Primitive primitive) {
    final String primitiveClass = getPrimitiveClassName(primitive);
    if (!imported.contains(primitiveClass)) {
      imported.add(primitiveClass);
      st.add("imps", primitiveClass);
    }
  }

  private String getPrimitiveClassName(final Primitive primitive) {
    InvariantChecks.checkNotNull(primitive);

    if (primitive.isOrRule()) {
      return IsaPrimitive.class.getName();
    }

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
    if (primitive.getKind() == Primitive.Kind.IMM) {
      return Immediate.class.getSimpleName();
    }

    if (primitive.isOrRule()) {
      return IsaPrimitive.class.getSimpleName();
    }

    return primitive.getName();
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("decoder_constructor");

    stConstructor.add("name", name);
    stConstructor.add("size", imageInfo.getMaxImageSize());
    stConstructor.add("is_fixed", Boolean.toString(imageInfo.isImageSizeFixed()));

    final BitVector opc = imageInfo.getOpc();
    final BitVector opcMask = imageInfo.getOpcMask();

    stConstructor.add("opc", null != opc ? "\"" + opc.toBinString() + "\"" : "null");
    stConstructor.add("opc_mask", null != opcMask ? "\"" + opcMask.toBinString() + "\"" : "null");

    final List<String> argumentNames = new ArrayList<>();
    final Set<String> variableNames = new LinkedHashSet<>();

    // Temporal variables used in the image.
    final String tempVars = StbTemporaryVariables.CLASS_NAME;
    stConstructor.add("stmts", String.format("final %s vars__ = (%s) %s.newFactory().create();",
        tempVars, tempVars, tempVars));
    stConstructor.add("stmts", "");

    // Primitive's arguments.
    for (final Map.Entry<String, Primitive> entry : item.getArguments().entrySet()) {
      final String argumentName = entry.getKey();
      argumentNames.add(argumentName);
      variableNames.add(argumentName);

      stConstructor.add("stmts", String.format(
          "%s %s = null;", getPrimitiveName(entry.getValue()), argumentName));
    }

    if (!item.getArguments().isEmpty()) {
      stConstructor.add("stmts", "");
    }

    undecoded.addAll(variableNames);

    for (final Pair<Node, ImageInfo> fieldx : imageInfo.getFields()) {
      final Node field = fieldx.first;

      if (ExprUtils.isValue(field)) {
        // Constant (treated as an OPC).
        buildOpcCheck(stConstructor, group, field);
      } else if (isArgument(field)) {
        // Image of an immediate value.
        buildArgument(stConstructor, group, field);
      } else if (isArgumentExtract(field)) {
        // Image of an immediate value's part.
        buildArgumentExtract(stConstructor, group, field);
      } else if (isArgumentImage(field)) {
        // Image of an mode/operation (argument).
        buildArgumentImage(stConstructor, group, field);
      } else if (isInstanceImage(field)) {
        // Image of an mode/operation (instance).
        buildInstanceImage(st, stConstructor, group, field);
      } else if (isTemporalVariable(field)) {
        // Temporal variable.
        buildTemporalVariable(stConstructor, group, field);
      } else if (isTemporalVariableField(field)) {
        // Temporal variable's field.
        buildTemporalVariableExtract(stConstructor, group, field);
      } else {
        // Unknown.
        reportError("Unrecognized field: %s", field);
      }
    }

    // Try to use the decode hints.
    final Attribute decode = item.getAttributes().get(Attribute.DECODE_NAME);

    if (null != decode) {
      stConstructor.add("stmts", "");

      for (final Map.Entry<String, Primitive> entry : item.getArguments().entrySet()) {
        final String argumentName = entry.getKey();
        final Primitive argumentPrimitive = entry.getValue();

        if (!undecoded.contains(argumentName)) {
          continue;
        }

        // Construct the argument.
        stConstructor.add("stmts", String.format("%s = new %s(%s);",
            argumentName,
            // All undecoded attributes are assumed to be immediate values.
            Immediate.class.getSimpleName(),
            argumentPrimitive.getReturnType().getJavaText()));

        undecoded.remove(argumentName);
      }

      // Insert the decode logic.
      final StatementBuilder statementBuilder = new StatementBuilder(stConstructor, false);

      for (final Statement statement : decode.getStatements()) {
        statementBuilder.build(statement);
      }
    }

    if (!undecoded.isEmpty()) {
      reportError("Undecoded arguments: %s", undecoded);
    }

    final ST stResult = group.getInstanceOf("decoder_result");
    stResult.add("name", item.getName());
    stResult.add("args", argumentNames);
    stConstructor.add("stmts", stResult);

    st.add("members", stConstructor);
  }

  private void buildOpcCheck(final ST st, final STGroup group, final Node field) {
    InvariantChecks.checkTrue(ExprUtils.isValue(field));
    InvariantChecks.checkTrue(field.isType(DataTypeId.BIT_VECTOR)
        || field.isType(DataTypeId.LOGIC_STRING));

    final ST stOpcCheck = group.getInstanceOf("decoder_opc_check");
    final int size = field.isType(DataTypeId.BIT_VECTOR)
        ? field.getDataType().getSize() : field.toString().length();

    stOpcCheck.add("value", field.toString());
    stOpcCheck.add("size", size);

    st.add("stmts", stOpcCheck);
  }

  private boolean isArgument(final Node field) {
    return isNodeVariable(field) && !isNodeExtract(field) && isNodeArgument(field);
  }

  private void buildArgument(final ST st, final STGroup group, final Node field) {
    InvariantChecks.checkTrue(ExprUtils.isVariable(field));

    final String name = field.toString();

    // NOTE: The primitive is allowed to be non-immediate.
    final Primitive primitive = item.getArguments().get(name);
    InvariantChecks.checkNotNull(primitive);

    final ST stImmediate = group.getInstanceOf("decoder_immediate");

    stImmediate.add("name", name);
    stImmediate.add("type", primitive.getReturnType().getJavaText());

    st.add("stmts", stImmediate);
    undecoded.remove(name);
  }

  private boolean isArgumentExtract(final Node field) {
    return isNodeVariable(field) && isNodeExtract(field) && isNodeArgument(field);
  }

  private void buildArgumentExtract(final ST st, final STGroup group, final Node field) {
    final NodeInfo nodeInfo = (NodeInfo) field.getUserData();
    InvariantChecks.checkNotNull(nodeInfo);

    final Location location = (Location) nodeInfo.getSource();
    InvariantChecks.checkNotNull(location);

    final String name = location.getName();

    // NOTE: The primitive is allowed to be non-immediate.
    final Primitive primitive = item.getArguments().get(name);
    InvariantChecks.checkNotNull(primitive);

    if (undecoded.contains(name)) {
      st.add("stmts", String.format("%s = new %s(%s);",
          name, Immediate.class.getSimpleName(), location.getSource().getType().getJavaText()));
    }

    final ST stImmediate = group.getInstanceOf("decoder_immediate_field");

    stImmediate.add("name", name);
    stImmediate.add("type", location.getType().getJavaText());
    stImmediate.add("from", ExprPrinter.toString(location.getBitfield().getFrom()));
    stImmediate.add("to", ExprPrinter.toString(location.getBitfield().getTo()));

    st.add("stmts", stImmediate);
    undecoded.remove(name);
  }

  private boolean isArgumentImage(final Node field) {
    if (!ExprUtils.isVariable(field) || !field.isType(DataTypeId.LOGIC_STRING)) {
      return false;
    }

    final StatementAttributeCall call = (StatementAttributeCall) field.getUserData();
    final String attributeName = call.getAttributeName();

    if (!attributeName.equals(Attribute.IMAGE_NAME)) {
      return false;
    }

    final String name = call.getCalleeName();
    return item.getArguments().containsKey(name);
  }

  private void buildArgumentImage(final ST st, final STGroup group, final Node field) {
    final ST stPrimitive = group.getInstanceOf("decoder_primitive");

    final StatementAttributeCall call = (StatementAttributeCall) field.getUserData();
    final String name = call.getCalleeName();
    final Primitive primitive = item.getArguments().get(name);

    stPrimitive.add("name", name);
    stPrimitive.add("type", getPrimitiveName(primitive));
    stPrimitive.add("decoder", DecoderGenerator.getDecoderName(primitive.getName()));

    st.add("stmts", stPrimitive);
    undecoded.remove(name);
  }

  private boolean isInstanceImage(final Node field) {
    if (!ExprUtils.isVariable(field) || !field.isType(DataTypeId.LOGIC_STRING)) {
      return false;
    }

    final StatementAttributeCall call = (StatementAttributeCall) field.getUserData();
    final String attributeName = call.getAttributeName();

    if (!attributeName.equals(Attribute.IMAGE_NAME)) {
      return false;
    }

    return call.getCalleeInstance() != null;
  }

  private void buildInstanceImage(
      final ST st, final ST stConstructor, final STGroup group, final Node field) {
    final StatementAttributeCall call = (StatementAttributeCall) field.getUserData();
    final Instance instance = call.getCalleeInstance();
    final PrimitiveAnd primitive = instance.getPrimitive();
    final String name = primitive.getName() + "_instance";

    importPrimitive(st, primitive);

    stConstructor.add("stmts", String.format("final %s %s;", primitive.getName(), name));
    final ST stPrimitive = group.getInstanceOf("decoder_primitive");

    stPrimitive.add("name", name);
    stPrimitive.add("type", getPrimitiveName(primitive));
    stPrimitive.add("decoder", DecoderGenerator.getDecoderName(primitive.getName()));

    stConstructor.add("stmts", stPrimitive);

    final Map<String, Primitive> arguments = primitive.getArguments();
    final int argumentCount = arguments.size();
    final String[] argumentNames = arguments.keySet().toArray(new String[argumentCount]);

    int index = 0;
    for (final InstanceArgument argument : instance.getArguments()) {
      final String argumentName = argumentNames[index];
      buildInstanceArgument(stConstructor, name, argumentName, argument);
      index++;
    }
  }

  private void buildInstanceArgument(
      final ST stConstructor,
      final String primitiveName,
      final String argumentName,
      final InstanceArgument argument) {
    final String sourceName = String.format("%s.%s", primitiveName, argumentName);
    switch (argument.getKind()) {

      case PRIMITIVE: {
        final String targetName = argument.getName();
        if (item.getArguments().containsKey(targetName)) {
          stConstructor.add("stmts", String.format("%s = %s;", targetName, sourceName));
          undecoded.remove(targetName);
          return;
        }

        reportError("Instance argument %s (%s) is not an %s argument.",
            targetName, sourceName, item.getName());
        break;
      }

      case EXPR: {
        if (argument.getExpr().isConstant()) {
          break;
        }

        if (argument.getExpr().getNodeInfo().getKind() == NodeInfo.Kind.OPERATOR) {
          reportError(sourceName + " is not recognized. Expressions are not supported.");
          break;
        }

        if (argument.getExpr().getNodeInfo().getKind() == NodeInfo.Kind.LOCATION) {
          final Location location = (Location) argument.getExpr().getNodeInfo().getSource();
          if (location.getSource() instanceof LocationSourceMemory) {
            // Ignore it.
            break;
          }
        }

        reportError("%s (%s) is not recognized. ", argument.getExpr().toString(), sourceName);
        break;
      }

      case INSTANCE: {
        reportError("%s (%s) is not recognized. Nested instances are not supported.",
            argument.getInstance().getPrimitive().getName(), sourceName);
        break;
      }

      default:
        throw new IllegalArgumentException("Unsupported kind: " + argument.getKind());
    }
  }

  private boolean isTemporalVariable(final Node field) {
    return isNodeVariable(field) && !isNodeExtract(field) && !isNodeArgument(field);
  }

  private void buildTemporalVariable(final ST st, final STGroup group, final Node field) {
    InvariantChecks.checkTrue(ExprUtils.isVariable(field));
    InvariantChecks.checkTrue(field.getUserData() instanceof NodeInfo);

    final NodeInfo nodeInfo = (NodeInfo) field.getUserData();
    InvariantChecks.checkTrue(nodeInfo.getKind() == NodeInfo.Kind.LOCATION);

    final Location location = (Location) nodeInfo.getSource();
    InvariantChecks.checkNotNull(location);

    final ST stImmediate = group.getInstanceOf("decoder_immediate");

    stImmediate.add("name", String.format("vars__.%s", location.getName()));
    stImmediate.add("type", location.getType().getJavaText());

    st.add("stmts", stImmediate);
    undecoded.remove(location.getName());
  }

  private boolean isTemporalVariableField(final Node field) {
    return isNodeVariable(field) && isNodeExtract(field) && !isNodeArgument(field);
  }

  private void buildTemporalVariableExtract(final ST st, final STGroup group, final Node field) {
    final NodeInfo nodeInfo = (NodeInfo) field.getUserData();
    InvariantChecks.checkNotNull(nodeInfo);

    final Location location = (Location) nodeInfo.getSource();
    InvariantChecks.checkNotNull(location);

    final ST stImmediate = group.getInstanceOf("decoder_immediate_field");

    stImmediate.add("name", String.format("vars__.%s", location.getName()));
    stImmediate.add("type", location.getType().getJavaText());
    stImmediate.add("from", ExprPrinter.toString(location.getBitfield().getFrom()));
    stImmediate.add("to", ExprPrinter.toString(location.getBitfield().getTo()));

    st.add("stmts", stImmediate);
    undecoded.remove(location.getName());
  }

  private boolean isNodeVariable(final Node field) {
    if (!ExprUtils.isVariable(field)) {
      return false;
    }

    if (!(field.getUserData() instanceof NodeInfo)) {
      return false;
    }

    final NodeInfo nodeInfo = (NodeInfo) field.getUserData();
    return nodeInfo.getKind() == NodeInfo.Kind.LOCATION;
  }

  private boolean isNodeExtract(final Node field) {
    final NodeInfo nodeInfo = (NodeInfo) field.getUserData();
    final Location location = (Location) nodeInfo.getSource();

    return null != location.getBitfield();
  }

  private boolean isNodeArgument(final Node field) {
    final NodeInfo nodeInfo = (NodeInfo) field.getUserData();
    final Location location = (Location) nodeInfo.getSource();
    final Primitive primitive = item.getArguments().get(location.getName());

    return null != primitive;
  }

  private void reportError(final String format, final Object... args) {
    Logger.warning("Failed to construct decoder for " + item.getName() + ". " + format, args);
  }
}
