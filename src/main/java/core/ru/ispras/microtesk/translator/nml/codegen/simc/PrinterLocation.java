/*
 * Copyright 2013-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.simc;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.memory.Memory;
import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourceMemory;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourcePrimitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;

public final class PrinterLocation {
  private static final String ACCESS_FORMAT = ".access(%s)";
  private static final String BITFIELD_FORMAT = ".bitField(%s, %s)";

  private PrinterLocation() {}

  public static boolean addPE = true;

  public static String toString(final Location location) {
    InvariantChecks.checkNotNull(location);
    final StringBuilder sb = new StringBuilder();

    if (addPE && location.getSource().getSymbolKind() == NmlSymbolKind.MEMORY) {
      final boolean isVar =
          ((LocationSourceMemory) location.getSource()).getKind() == Memory.Kind.VAR;
      sb.append(isVar ? "vars__." : "pe__.");
    }

    sb.append(location.getName());

    final String indexText;
    if (location.getSource().getSymbolKind() == NmlSymbolKind.ARGUMENT) {
      final boolean isImm =
          ((LocationSourcePrimitive) location.getSource()).getKind() == Primitive.Kind.IMM;
      indexText = isImm ? "" : "pe__, vars__" ;
    } else {
      indexText = ExprPrinter.toString(location.getIndex());
    }

    sb.append(String.format(ACCESS_FORMAT, indexText));

    if (null != location.getBitfield()) {
      final Location.Bitfield bitfield = location.getBitfield();
      sb.append(String.format(BITFIELD_FORMAT,
          ExprPrinter.toString(bitfield.getFrom()),
          ExprPrinter.toString(bitfield.getTo()))
      );
    }

    addPE = true;
    return sb.toString();
  }
}
