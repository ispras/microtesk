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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;

final class ExprPrinter {
  private ExprPrinter() {}

  public static String toString(final Expr expr) {
    final NodeInfo nodeInfo = expr.getNodeInfo();

    if (nodeInfo.getSource() instanceof Location) {
      return toString((Location) nodeInfo.getSource());
    }

    return "";
  }

  public static String toString(final Location location) {
    InvariantChecks.checkNotNull(location);
    final String name = location.getName().toLowerCase();

    String text = location.getSource().getSymbolKind() == NmlSymbolKind.MEMORY
        ? "s__." + name : name;

    if (location.getIndex() != null) {
      final Expr index = location.getIndex();
      final String indexText = toString(index);
      text = String.format("(get %s (to_uint %s))", text, indexText);
    }

    if (location.getBitfield() != null) {
      final Location.Bitfield bitfield = location.getBitfield();
      final String fromText = toString(bitfield.getFrom());
      final String toText = toString(bitfield.getTo());
      text = String.format("(extract %s (to_uint %s) (to_uint %s))", text, fromText, toText);
    }

    return text;
  }
}
