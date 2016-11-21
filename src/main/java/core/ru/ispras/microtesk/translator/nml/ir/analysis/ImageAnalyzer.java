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
          System.out.printf(
              "%s: (%d, %b)%n",
              item.getName(),
              imageInfo.getMaxImageSize(),
              imageInfo.isImageSizeFixed()
              );
        }*/
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
        final List<Format.Argument> arguments) {
      ImageInfo imageInfo = new ImageInfo(0, true); 

      final List<Pair<String, Integer>> tokens = FormatMarker.tokenize(format, markers);
      for(final Pair<String, Integer> token : tokens) {
        final String text = token.first;
        final int markerIndex = token.second;

        if (markerIndex == -1) {
          imageInfo = imageInfo.and(new ImageInfo(text.length(), true));
        } else {
          final ImageInfo tokenImageInfo = getImageInfo(primitive, arguments.get(markerIndex));
          imageInfo = imageInfo.and(tokenImageInfo);
        }
      }

      primitive.getInfo().setImageInfo(imageInfo);
    }

    private ImageInfo getImageInfo(final PrimitiveAND primitive, final Format.Argument argument) {
      if (argument instanceof Format.ExprBasedArgument) {
        return new ImageInfo(argument.getBinaryLength(), true);
      }

      if (argument instanceof Format.StringBasedArgument) {
        return new ImageInfo(argument.getBinaryLength(), true);
      }

      if (argument instanceof Format.TernaryConditionalArgument) {
        final Format.TernaryConditionalArgument ternary =
            (Format.TernaryConditionalArgument) argument;

        final ImageInfo leftImageInfo = getImageInfo(primitive, ternary.getLeft());
        final ImageInfo rightImageInfo = getImageInfo(primitive, ternary.getRight());

        return leftImageInfo.or(rightImageInfo);
      }

      if (argument instanceof Format.AttributeCallBasedArgument) {
        final Format.AttributeCallBasedArgument attributeCall =
            (Format.AttributeCallBasedArgument) argument;

        InvariantChecks.checkTrue(attributeCall.getAttributeName().equals(Attribute.IMAGE_NAME));

        if (null != attributeCall.getCalleeName()) {
          final Primitive arg = primitive.getArguments().get(attributeCall.getCalleeName());
          return arg.getInfo().getImageInfo();
        }

        if (null != attributeCall.getCalleeInstance()) {
          return attributeCall.getCalleeInstance().getPrimitive().getInfo().getImageInfo();
        }

        throw new IllegalArgumentException("Illegal attribute call.");
      }

      throw new IllegalArgumentException("Illegal format argument.");
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
