/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.generation;

import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationAtom;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationConcat;

public final class PrinterLocation {
  private static final String ACCESS_FORMAT = ".access(%s)";
  private static final String BITFIELD_FORMAT = ".bitField(%s, %s)";
  private static final String CONCAT_FORMAT = "Location.concat(%s)";

  private PrinterLocation() {}
  public static boolean addPE = true; 

  public static String toString(Location location) {
    final String result = location instanceof LocationConcat ?
        toString((LocationConcat) location) :
        toString((LocationAtom) location);

    addPE = true;
    return result;
  }

  private static String toString(LocationAtom location) {
    final StringBuilder sb = new StringBuilder();

    if (addPE && location.getSource().getSymbolKind() == NmlSymbolKind.MEMORY) {
      sb.append("pe__.");
    }

    sb.append(location.getName());

    final String indexText;
    if (location.getSource().getSymbolKind() == NmlSymbolKind.ARGUMENT) {
      indexText = "pe__";
    } else {
      indexText = ExprPrinter.toString(location.getIndex());
    }

    sb.append(String.format(ACCESS_FORMAT, indexText));

    if (null != location.getBitfield()) {
      final LocationAtom.Bitfield bitfield = location.getBitfield();
      sb.append(String.format(BITFIELD_FORMAT,
          ExprPrinter.toString(bitfield.getFrom()),
          ExprPrinter.toString(bitfield.getTo()))
      );
    }

    return sb.toString();
  }

  private static String toString(LocationConcat location) {
    final StringBuilder sb = new StringBuilder();

    for (LocationAtom la : location.getLocations()) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(toString(la));
    }

    return String.format(CONCAT_FORMAT, sb.toString());
  }
}
