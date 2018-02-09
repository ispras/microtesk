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

package ru.ispras.microtesk.translator.nml.ir.analysis;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprTreeVisitorDefault;
import ru.ispras.fortress.expression.ExprTreeWalker;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalker;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.ImageInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Instance;
import ru.ispras.microtesk.translator.nml.ir.primitive.InstanceArgument;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAssignment;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFormat;
import ru.ispras.microtesk.utils.FormatMarker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link ImageAnalyzer} class analyzes the image format of addressing modes
 * and operations to find out how to decode instructions.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class ImageAnalyzer implements TranslatorHandler<Ir> {
  @Override
  public void processIr(final Ir ir) {
    final IrWalker walker = new IrWalker(ir);
    walker.visit(new Visitor(), IrWalker.Direction.LINEAR);
  }

  private static final class Visitor extends IrVisitorDefault {
    private final Map<Primitive, Primitive> visited = new HashMap<>();
    private final Map<String, Map<BitVector, String>> opcGroups = new HashMap<>();
    private final Map<Node, Node> localConstants = new HashMap<>();

    @Override
    public void onPrimitiveBegin(final Primitive item) {
      if (item.getModifier() == Primitive.Modifier.PSEUDO
          || item.getModifier() == Primitive.Modifier.LABEL
          || visited.containsKey(item)) {
        setStatus(Status.SKIP);
        return;
      }

      if (item.isOrRule()) {
        opcGroups.put(item.getName(), new HashMap<BitVector, String>());
      }
    }

    @Override
    public void onPrimitiveEnd(final Primitive item) {
      if (getStatus() == Status.SKIP) {
        setStatus(Status.OK);
        return;
      }

      visited.put(item, item);
      localConstants.clear();
    }

    @Override
    public void onAlternativeBegin(final PrimitiveOR orRule, final Primitive item) {
      if (item.getModifier() == Primitive.Modifier.PSEUDO
          || item.getModifier() == Primitive.Modifier.LABEL) {
        setStatus(Status.SKIP);
        return;
      }

      final ImageInfo sourceInfo = item.getInfo().getImageInfo();
      final ImageInfo targetInfo = orRule.getInfo().getImageInfo();

      final String itemName = item.getName();
      final BitVector itemOpc = sourceInfo.getOpc();
      final String groupName = orRule.getName();

      final Map<BitVector, String> group = opcGroups.get(groupName);
      InvariantChecks.checkNotNull(group, groupName);

      if (null != itemOpc) {
        checkOpcInGroup(groupName, group, itemName, itemOpc);
      } else if (item.isOrRule()) {
        final Map<BitVector, String> itemGroup = opcGroups.get(itemName);
        InvariantChecks.checkNotNull(itemGroup, itemName);

        for (final Map.Entry<BitVector, String> entry : itemGroup.entrySet()) {
          checkOpcInGroup(groupName, group, entry.getValue(), entry.getKey());
        }
      }

      if (null == targetInfo) {
        orRule.getInfo().setImageInfo(sourceInfo);
      } else {
        orRule.getInfo().setImageInfo(targetInfo.or(sourceInfo));

        if (targetInfo.getOpcMask() != null
            && targetInfo.getOpcMask().equals(sourceInfo.getOpcMask())) {
          orRule.getInfo().getImageInfo().setOpcMask(targetInfo.getOpcMask());
        }
      }
    }

    @Override
    public void onAlternativeEnd(final PrimitiveOR orRule, final Primitive item) {
      if (getStatus() == Status.SKIP) {
        setStatus(Status.OK);
      }
    }

    @Override
    public void onAttributeBegin(final PrimitiveAND andRule, final Attribute attr) {
      if (!attr.getName().equals(Attribute.IMAGE_NAME)) {
        setStatus(Status.SKIP);
        return;
      }

      InvariantChecks.checkTrue(
          2 >= attr.getStatements().size(), "image cannot have more than 2 statements");
    }

    @Override
    public void onAttributeEnd(final PrimitiveAND andRule, final Attribute attr) {
      if (getStatus() == Status.SKIP) {
        setStatus(Status.OK);
      }
    }

    @Override
    public void onStatementBegin(
        final PrimitiveAND andRule, final Attribute attr, final Statement stmt) {
      InvariantChecks.checkTrue(stmt.getKind() == Statement.Kind.FORMAT
          || stmt.getKind() == Statement.Kind.CALL);
      if (stmt instanceof StatementFormat) {
        onStatementFormat(andRule, (StatementFormat) stmt);
      } else if (stmt instanceof StatementAttributeCall) {
        onStatementAttributeCall(andRule, (StatementAttributeCall) stmt);
      } else {
        InvariantChecks.checkTrue(false, "Unsupported statement: " + stmt.getKind());
      }
    }

    private void onStatementFormat(
        final PrimitiveAND primitive, final StatementFormat stmt) {
      // null means call to the 'format' function (not 'trace' or anything else).
      InvariantChecks.checkTrue(null == stmt.getFunction());
      analyzeImage(primitive, stmt.getFormat(), stmt.getMarkers(), stmt.getArguments());
    }

    private void onStatementAttributeCall(
        final PrimitiveAND primitive, final StatementAttributeCall stmt) {
      if (stmt.getAttributeName().equals(Attribute.INIT_NAME)) {
        final Attribute attribute = primitive.getAttributes().get(Attribute.INIT_NAME);
        for (final Statement initStmt : attribute.getStatements()) {
          if (initStmt.getKind() == Statement.Kind.ASSIGN) {
            final StatementAssignment assignment = (StatementAssignment) initStmt;
            if (ExprUtils.isValue(assignment.getRight().getNode())) {
              localConstants.put(assignment.getLeft().getNode(), assignment.getRight().getNode());
            }
          }
        }
        return;
      } 

      if (stmt.getAttributeName().equals(Attribute.IMAGE_NAME)) {
        final Primitive arg = primitive.getArguments().get(stmt.getCalleeName());

        final ImageInfo imageInfo = new ImageInfo(arg.getInfo().getImageInfo());
        imageInfo.setFields(Collections.singletonList(new Pair<>(
            StatementAttributeCall.createCallNode(stmt), arg.getInfo().getImageInfo())));

        primitive.getInfo().setImageInfo(imageInfo);
        return;
      }

      InvariantChecks.checkTrue(false,
          "Illegal attribute call: " + stmt.getAttributeName());
    }

    private void analyzeImage(
        final PrimitiveAND primitive,
        final String format,
        final List<FormatMarker> markers,
        final List<Node> arguments) {
      ImageInfo imageInfo = new ImageInfo(0, true);

      final List<Pair<Node, ImageInfo>> fields = new ArrayList<>();

      final List<Pair<String, Integer>> tokens = FormatMarker.tokenize(format, markers);
      for (final Pair<String, Integer> token : tokens) {
        final String text = token.first;
        final int markerIndex = token.second;

        final Node argument =
            markerIndex == -1 ? NodeValue.newString(text) : arguments.get(markerIndex);

        final Node field =
            localConstants.containsKey(argument) ? localConstants.get(argument) : argument;

        final ImageInfo tokenImageInfo = getImageInfo(primitive, field);
        imageInfo = imageInfo.and(tokenImageInfo);

        fields.add(new Pair<>(field, tokenImageInfo));
      }

      imageInfo.setFields(fields);
      primitive.getInfo().setImageInfo(imageInfo);

      calculateOpc(primitive, imageInfo, localConstants);
    }

    private static void calculateOpc(
        final PrimitiveAND primitive,
        final ImageInfo imageInfo,
        final Map<Node, Node> mappings) {
      InvariantChecks.checkNotNull(primitive);
      InvariantChecks.checkNotNull(imageInfo);

      if (!imageInfo.isImageSizeFixed()) {
        return;
      }

      final List<Pair<Node, ImageInfo>> fields = imageInfo.getFields();

      BitVector opc = null;
      BitVector opcMask = null;
      boolean hasOpc = false;

      for (int index = 0; index < fields.size(); index++) {
        Node field = fields.get(index).first;
        field = mappings.containsKey(field) ? mappings.get(field) : field;
        final ImageInfo tokenImageInfo = fields.get(index).second;

        final int tokenBitSize = tokenImageInfo.getMaxImageSize();
        InvariantChecks.checkTrue(tokenImageInfo.isImageSizeFixed());

        final BitVector tokenOpc = BitVector.newEmpty(tokenBitSize);
        tokenOpc.reset();

        final BitVector tokenOpcMask = BitVector.newEmpty(tokenBitSize);
        tokenOpcMask.reset();

        if (ExprUtils.isValue(field)) {
          tokenOpc.assign(BitVector.valueOf(field.toString(), 2, tokenBitSize));
          tokenOpcMask.setAll();
          hasOpc = true;
        } else if (isArgumentImage(primitive, field)) {
          final ImageInfo calleeInfo = getArgumentImageInfo(primitive, field);
          if (calleeInfo.isImageSizeFixed() && calleeInfo.getOpc() != null) {
            tokenOpc.assign(calleeInfo.getOpc());
            tokenOpcMask.assign(calleeInfo.getOpcMask());
            hasOpc = true;
          }
        } else if (isInstanceImage(field)) {
          final StatementAttributeCall call = (StatementAttributeCall) field.getUserData();
          final Instance instance = call.getCalleeInstance();
          final PrimitiveAND callee = instance.getPrimitive();

          final String[] argumentNames =
              callee.getArguments().keySet().toArray(new String[callee.getArguments().size()]);

          final Map<Node, Node> fieldMappings = new HashMap<>();
          final List<InstanceArgument> arguments = instance.getArguments();

          for (int i = 0; i < arguments.size(); i++) {
            final InstanceArgument argument = arguments.get(i);
            if (argument.getKind() != InstanceArgument.Kind.EXPR) {
              continue;
            }

            Node argumentNode = argument.getExpr().getNode();
            argumentNode = mappings.containsKey(argumentNode) ? mappings.get(argumentNode) : argumentNode;

            if (ExprUtils.isValue(argumentNode)) {
              final String name = argumentNames[i];
              fieldMappings.put(new NodeVariable(name, argumentNode.getDataType()), argumentNode);
            }
          }

          final ImageInfo calleeInfo = new ImageInfo(callee.getInfo().getImageInfo());
          calculateOpc(callee, calleeInfo, fieldMappings);

          if (calleeInfo.isImageSizeFixed() && calleeInfo.getOpc() != null) {
            tokenOpc.assign(calleeInfo.getOpc());
            tokenOpcMask.assign(calleeInfo.getOpcMask());
            hasOpc = true;
          }
        }

        // Operand order for BitVector.newMapping: [HI, LOW].
        opc = null == opc ? tokenOpc : BitVector.newMapping(opc, tokenOpc);
        opcMask = null == opcMask ? tokenOpcMask : BitVector.newMapping(opcMask, tokenOpcMask);
      }

      if (hasOpc) {
        imageInfo.setOpc(opc);
        imageInfo.setOpcMask(opcMask);
      }
    }

    @Override
    public void onShortcutBegin(final PrimitiveAND andRule, final Shortcut shortcut) {
      setStatus(Status.SKIP);
    }

    @Override
    public void onShortcutEnd(final PrimitiveAND andRule, final Shortcut shortcut) {
      setStatus(Status.OK);
    }

    @Override
    public void onAttributeCallBegin(final StatementAttributeCall stmt) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onFormat(final StatementFormat stmt) {
      throw new UnsupportedOperationException();
    }

    private static ImageInfo getArgumentImageInfo(final PrimitiveAND primitive, final Node field) {
      final StatementAttributeCall call = (StatementAttributeCall) field.getUserData();
      InvariantChecks.checkNotNull(call);

      final Primitive callee = primitive.getArguments().get(call.getCalleeName());
      InvariantChecks.checkNotNull(callee);

      final ImageInfo result = callee.getInfo().getImageInfo();
      InvariantChecks.checkNotNull(result);

      return result;
    }
  }

  private static void checkOpcInGroup(
      final String groupName,
      final Map<BitVector, String> group,
      final String name,
      final BitVector opc) {
    final String existingName = group.get(opc);
    if (null == existingName) {
      group.put(opc, name);
      return;
    }

    Logger.warning(
        "Group %s contains two items %s and %s with the same opcode %s",
        groupName,
        existingName,
        name,
        opc
        );
  }

  private static ImageInfo getImageInfo(final PrimitiveAND primitive, final Node field) {
    InvariantChecks.checkTrue(field.isType(DataTypeId.BIT_VECTOR)
        || field.isType(DataTypeId.LOGIC_STRING), primitive.getName());

    if (field.isType(DataTypeId.BIT_VECTOR)) {
      return new ImageInfo(field.getDataType().getSize(), true);
    }

    class ExprVisitor extends ExprTreeVisitorDefault {
      private final Deque<ImageInfo> imageInfos = new ArrayDeque<>();

      ImageInfo getImageInfo() {
        return imageInfos.peek();
      }

      @Override
      public void onValue(final NodeValue value) {
        final String text = (String)((NodeValue) value).getValue();
        imageInfos.push(new ImageInfo(text.length(), true));
      }

      @Override
      public void onVariable(final NodeVariable variable) {
        final Primitive callee = getPrimitive(primitive, variable);
        InvariantChecks.checkNotNull(callee);

        final ImageInfo calleeInfo = callee.getInfo().getImageInfo();
        InvariantChecks.checkNotNull(calleeInfo);

        imageInfos.push(calleeInfo);
      }

      @Override
      public void onOperationBegin(final NodeOperation node) {
        InvariantChecks.checkTrue(node.getOperationId() == StandardOperation.ITE);
      }

      @Override
      public void onOperationEnd(final NodeOperation node) {
        imageInfos.push(imageInfos.pop().or(imageInfos.pop()));
      }

      @Override
      public void onOperandBegin(
          final NodeOperation operation, final Node operand, final int index) {
        InvariantChecks.checkTrue(operation.getOperationId() == StandardOperation.ITE);
        if (0 == index) {
          setStatus(Status.SKIP);
        }
      }

      @Override
      public void onOperandEnd(
          final NodeOperation operation, final Node operand, final int index) {
        InvariantChecks.checkTrue(operation.getOperationId() == StandardOperation.ITE);
        if (0 == index) {
          setStatus(Status.OK);
        }
      }
    }

    final ExprVisitor visitor = new ExprVisitor();
    final ExprTreeWalker walker = new ExprTreeWalker(visitor);
    walker.visit(field);

    final ImageInfo imageInfo = visitor.getImageInfo();
    InvariantChecks.checkNotNull(imageInfo);

    return imageInfo;
  }

  private static boolean isArgumentImage(final PrimitiveAND primitive, final Node field) {
    if (!ExprUtils.isVariable(field) || !field.isType(DataTypeId.LOGIC_STRING)) {
      return false;
    }

    final StatementAttributeCall call = (StatementAttributeCall) field.getUserData();
    final String attributeName = call.getAttributeName();

    if (!attributeName.equals(Attribute.IMAGE_NAME)) {
      return false;
    }

    final String name = call.getCalleeName();
    return primitive.getArguments().containsKey(name);
  }

  private static boolean isInstanceImage(final Node field) {
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

  private static Primitive getPrimitive(final PrimitiveAND andRule, final Node field) {
    final StatementAttributeCall call = (StatementAttributeCall) field.getUserData();

    if (call.getCalleeInstance() != null) {
      return call.getCalleeInstance().getPrimitive();
    }

    if (call.getCalleeName() != null) {
      return andRule.getArguments().get(call.getCalleeName());
    }

    return null;
  }
}
