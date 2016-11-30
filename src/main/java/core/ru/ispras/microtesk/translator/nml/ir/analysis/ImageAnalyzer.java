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

package ru.ispras.microtesk.translator.nml.ir.analysis;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprTreeVisitorDefault;
import ru.ispras.fortress.expression.ExprTreeWalker;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalker;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.ImageInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFormat;
import ru.ispras.microtesk.utils.FormatMarker;

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

    @Override
    public void onPrimitiveBegin(final Primitive item) {
      if (item.getModifier() == Primitive.Modifier.PSEUDO ||
          visited.containsKey(item.getName())) {
        setStatus(Status.SKIP);
        return;
      }
    }

    @Override
    public void onPrimitiveEnd(final Primitive item) {
      if (getStatus() == Status.SKIP) {
        setStatus(Status.OK);
      } else {
        visited.put(item, item);

        /*
        final ImageInfo imageInfo = item.getInfo().getImageInfo();
        if (null != imageInfo) {
          System.out.printf("%s: %s%n", item.getName(), imageInfo);
        }
        */
      }
    }

    @Override
    public void onAlternativeBegin(final PrimitiveOR orRule, final Primitive item) {
      if (item.getModifier() == Primitive.Modifier.PSEUDO) {
        setStatus(Status.SKIP);
        return;
      }

      final ImageInfo sourceInfo = item.getInfo().getImageInfo();
      final ImageInfo targetInfo = orRule.getInfo().getImageInfo();

      if (null == targetInfo) {
        orRule.getInfo().setImageInfo(sourceInfo);
      } else {
        orRule.getInfo().setImageInfo(targetInfo.or(sourceInfo));
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
      setStatus(Status.OK);
    }

    @Override
    public void onStatement(
        final PrimitiveAND andRule,
        final Attribute attr,
        final Statement stmt) {
      InvariantChecks.checkTrue(stmt.getKind() == Statement.Kind.FORMAT ||
                                stmt.getKind() == Statement.Kind.CALL);
      if (stmt instanceof StatementFormat) {
        onStatementFormat(andRule, (StatementFormat) stmt);
      } else if (stmt instanceof StatementAttributeCall) {
        onStatementAttributeCall(andRule, (StatementAttributeCall) stmt);
      } else {
        InvariantChecks.checkTrue(
            false, "Unsupported statement: " + stmt.getKind());
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
        // Calls to 'init' are ignored so far.
        return;
      }

      if (stmt.getAttributeName().equals(Attribute.IMAGE_NAME)) {
        final Primitive arg = primitive.getArguments().get(stmt.getCalleeName());
        primitive.getInfo().setImageInfo(arg.getInfo().getImageInfo());
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
      BitVector opc = null;
      BitVector opcMask = null;
      boolean hasOpc = false;
      final List<Node> fields = new ArrayList<>();

      final List<Pair<String, Integer>> tokens = FormatMarker.tokenize(format, markers);
      for(final Pair<String, Integer> token : tokens) {
        final String text = token.first;
        final int markerIndex = token.second;

        final boolean isTokenOpc = markerIndex == -1;

        final ImageInfo tokenImageInfo;
        if (isTokenOpc) {
          tokenImageInfo = new ImageInfo(text.length(), true);
          fields.add(NodeValue.newString(text));
        } else {
          final Node argument = arguments.get(markerIndex);
          tokenImageInfo = getImageInfo(primitive, argument);
          fields.add(argument);
        }
        imageInfo = imageInfo.and(tokenImageInfo);

        if (imageInfo.isImageSizeFixed() && tokenImageInfo.getMaxImageSize() > 0) {
          final BitVector tokenOpc;
          final BitVector tokenOpcMask = BitVector.newEmpty(tokenImageInfo.getMaxImageSize());

          if (isTokenOpc) {
            tokenOpc = BitVector.valueOf(text, 2, tokenImageInfo.getMaxImageSize());
            tokenOpcMask.setAll();
            hasOpc = true;
          } else {
            tokenOpc = BitVector.valueOf(0, tokenImageInfo.getMaxImageSize());
            tokenOpcMask.reset();
          }

          opc = null == opc ? tokenOpc : BitVector.newMapping(tokenOpc, opc);
          opcMask = null == opcMask ? tokenOpcMask : BitVector.newMapping(tokenOpcMask, opcMask);
        } else {
          opc = null;
          opcMask = null;
        }
      }

      if (hasOpc && imageInfo.isImageSizeFixed()) {
        imageInfo.setOpc(opc);
        imageInfo.setOpcMask(opcMask);
      }

      imageInfo.setFields(fields);
      primitive.getInfo().setImageInfo(imageInfo);
    }

    private ImageInfo getImageInfo(final PrimitiveAND primitive, final Node argument) {
      InvariantChecks.checkTrue(argument.isType(DataTypeId.BIT_VECTOR) ||
                                argument.isType(DataTypeId.LOGIC_STRING), primitive.getName());

      if (argument.isType(DataTypeId.BIT_VECTOR)) {
        return new ImageInfo(argument.getDataType().getSize(), true);
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
          final StatementAttributeCall callInfo = (StatementAttributeCall) variable.getUserData();

          InvariantChecks.checkNotNull(callInfo);
          InvariantChecks.checkTrue(callInfo.getAttributeName().equals(Attribute.IMAGE_NAME));

          if (null != callInfo.getCalleeName()) {
            final Primitive arg = primitive.getArguments().get(callInfo.getCalleeName());
            imageInfos.push(arg.getInfo().getImageInfo());
            return;
          }

          if (null != callInfo.getCalleeInstance()) {
            imageInfos.push(callInfo.getCalleeInstance().getPrimitive().getInfo().getImageInfo());
            return;
          }

          throw new IllegalArgumentException("Illegal attribute call.");
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
            final NodeOperation operation,
            final Node operand,
            final int index) {
          InvariantChecks.checkTrue(operation.getOperationId() == StandardOperation.ITE);
          if (0 == index) {
            setStatus(Status.SKIP);
          }
        }

        @Override
        public void onOperandEnd(
            final NodeOperation operation,
            final Node operand,
            final int index) {
          InvariantChecks.checkTrue(operation.getOperationId() == StandardOperation.ITE);
          if (0 == index) {
            setStatus(Status.OK);
          }
        }
      }

      final ExprVisitor visitor = new ExprVisitor();
      final ExprTreeWalker walker = new ExprTreeWalker(visitor);
      walker.visit(argument);

      final ImageInfo imageInfo = visitor.getImageInfo();
      InvariantChecks.checkNotNull(imageInfo);

      return imageInfo;
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
  }
}
