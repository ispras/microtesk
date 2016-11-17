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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalker;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Format;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveInfo;
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
        System.out.println(item.getName());
        System.out.printf(
            "%s: (%d, %b)%n",
            item.getName(),
            item.getInfo().getMaxImageSize(),
            item.getInfo().isImageSizeFixed()
            );
      }
    }

    @Override
    public void onAlternativeBegin(final PrimitiveOR orRule, final Primitive item) {
      final PrimitiveInfo sourceInfo = item.getInfo();
      final PrimitiveInfo targetInfo = orRule.getInfo();

      if (targetInfo.isMaxImageSizeInitialized()) {
        targetInfo.setImageSizeFixed(
            targetInfo.isImageSizeFixed() && sourceInfo.isImageSizeFixed() &&
            targetInfo.getMaxImageSize() == sourceInfo.getMaxImageSize());

        targetInfo.setMaxImageSize(
            Math.max(targetInfo.getMaxImageSize(), sourceInfo.getMaxImageSize()));
      } else {
        targetInfo.setMaxImageSize(sourceInfo.getMaxImageSize());
        targetInfo.setImageSizeFixed(sourceInfo.isImageSizeFixed());
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
        final Primitive arg = 
            primitive.getArguments().get(stmt.getCalleeName());

        primitive.getInfo().setMaxImageSize(arg.getInfo().getMaxImageSize());
        primitive.getInfo().setImageSizeFixed(arg.getInfo().isImageSizeFixed());

        return;
      }

      InvariantChecks.checkTrue(false,
          "Illegal attribute call: " + stmt.getAttributeName());
    }

    @Override
    public void onAttributeCallBegin(final StatementAttributeCall stmt) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onFormat(final StatementFormat stmt) {
      throw new UnsupportedOperationException();
    }

    private void analyzeImage(
        final PrimitiveAND primitive,
        final String format,
        final List<FormatMarker> markers,
        final List<Format.Argument> arguments) {
      int maxImageSize = 0;
      boolean imageSizeFixed = true;

      final List<Pair<String, Integer>> tokens = tokenize(format, markers);
      for(final Pair<String, Integer> token : tokens) {
        final String text = token.first;
        final int markerIndex = token.second;

        if (markerIndex == -1) {
          maxImageSize += text.length();
        } else {
          final Pair<Integer, Boolean> sizeInfo =
              getSizeInfo(primitive, arguments.get(markerIndex));

          maxImageSize += sizeInfo.first;
          imageSizeFixed = sizeInfo.second ? imageSizeFixed : false;
        }
      }

      primitive.getInfo().setMaxImageSize(maxImageSize);
      primitive.getInfo().setImageSizeFixed(imageSizeFixed);
    }

    private Pair<Integer, Boolean> getSizeInfo(
        final PrimitiveAND primitive,
        final Format.Argument argument) {

      if (argument instanceof Format.ExprBasedArgument) {
        return new Pair<Integer, Boolean>(argument.getBinaryLength(), true);
      }

      if (argument instanceof Format.StringBasedArgument) {
        return new Pair<Integer, Boolean>(argument.getBinaryLength(), true);
      }

      if (argument instanceof Format.TernaryConditionalArgument) {
        final Format.TernaryConditionalArgument ternary =
            (Format.TernaryConditionalArgument) argument;

        final Pair<Integer, Boolean> leftInfo =
            getSizeInfo(primitive, ternary.getLeft());

        final Pair<Integer, Boolean> rightInfo =
            getSizeInfo(primitive, ternary.getRight());

        return new Pair<Integer, Boolean>(
            Math.max(leftInfo.first, rightInfo.first),
            leftInfo.second && rightInfo.second
            );
      }

      if (argument instanceof Format.AttributeCallBasedArgument) {
        final Format.AttributeCallBasedArgument attributeCall =
            (Format.AttributeCallBasedArgument) argument;

        InvariantChecks.checkTrue(attributeCall.getAttributeName().equals(Attribute.IMAGE_NAME));

        final Primitive arg = 
            primitive.getArguments().get(attributeCall.getCalleeName());

        return new Pair<Integer, Boolean>(
            arg.getInfo().getMaxImageSize(), arg.getInfo().isImageSizeFixed());
      }

      throw new IllegalArgumentException();
    }

    @Override
    public void onShortcutBegin(final PrimitiveAND andRule, final Shortcut shortcut) {
      setStatus(Status.SKIP);
    }

    @Override
    public void onShortcutEnd(final PrimitiveAND andRule, final Shortcut shortcut) {
      setStatus(Status.OK);
    }
  }

  private static List<Pair<String, Integer>> tokenize(
      final String text,
      final List<FormatMarker> markers) {
    InvariantChecks.checkNotNull(text);
    InvariantChecks.checkNotNull(markers);

    final List<Pair<String, Integer>> tokens = new ArrayList<>();

    int position = 0;
    int markerIndex = 0;

    for (final FormatMarker marker : markers) {
      InvariantChecks.checkTrue(marker.isKind(FormatMarker.Kind.BIN) ||
                                marker.isKind(FormatMarker.Kind.STR));

      if (position < marker.getStart()) {
        tokens.add(new Pair<>(text.substring(position, marker.getStart()), -1));
      }

      tokens.add(new Pair<>(text.substring(marker.getStart(), marker.getEnd()), markerIndex));

      position = marker.getEnd();
      markerIndex++;
    }

    if (position < text.length()) {
      tokens.add(new Pair<>(text.substring(position, text.length()), -1));
    }

    return tokens;
  }
}
