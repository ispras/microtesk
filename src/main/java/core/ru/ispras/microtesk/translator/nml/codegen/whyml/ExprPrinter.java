/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.whyml;

import ru.ispras.fortress.expression.printer.MapBasedPrinter;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;

final class ExprPrinter extends MapBasedPrinter {

  public static String toString(final Expr expr) {
    return new ExprPrinter().print(expr);
  }

  public static String toString(final Location location) {
    InvariantChecks.checkNotNull(location);
    String text = getLocationName(location);

    if (location.getIndex() != null) {
      final Expr index = location.getIndex();
      final String indexText = toStringAsUint(index);
      text = String.format("(get %s %s)", text, indexText);
    }

    if (location.getBitfield() != null) {
      final Location.Bitfield bitfield = location.getBitfield();
      final String fromText = toStringAsUint(bitfield.getFrom());
      final String toText = toStringAsUint(bitfield.getTo());
      text = String.format("(extract %s %s %s)", text, fromText, toText);
    }

    return text;
  }

  private static String getLocationName(final Location location) {
    final String name = location.getName().toLowerCase();
    return location.getSource().getSymbolKind() == NmlSymbolKind.MEMORY
        ? getStateFieldName(name) : name;
  }

  private static String getStateFieldName(final String name) {
    return String.format("s__.%s", name);
  }

  private static String toStringAsUint(final Expr expr) {
    final String text = toString(expr);
    final boolean isBitVector = expr.getNodeInfo().getType() != null;
    return isBitVector ? String.format("(to_uint %s)", text) : text;
  }

  private String print(final Expr expr) {
    final NodeInfo nodeInfo = expr.getNodeInfo();

    if (nodeInfo.getSource() instanceof Location) {
      return toString((Location) nodeInfo.getSource());
    }

    //return toString(expr.getNode());
    return "";
  }

  private ExprPrinter() {
    setVisitor(new Visitor());
  }

  private final class Visitor extends ExprTreeVisitor { }
}
