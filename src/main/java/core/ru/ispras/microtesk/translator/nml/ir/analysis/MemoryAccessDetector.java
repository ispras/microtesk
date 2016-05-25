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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationAtom;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationConcat;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourceMemory;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourcePrimitive;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAssignment;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementCondition;

public final class MemoryAccessDetector {

  public static boolean isMemoryReference(final Expr expr) {
    if (null == expr) {
      return false;
    }

    final NodeInfo nodeInfo = expr.getNodeInfo();
    if (!nodeInfo.isLocation()) {
      return false;
    }

    return isMemoryReference((Location) nodeInfo.getSource()); 
  }

  private static boolean isMemoryReference(final Location location) {
    if (location instanceof LocationAtom) {
      return isMemoryReference((LocationAtom) location);
    } else {
      return isMemoryReference((LocationConcat) location);
    }
  }

  private static boolean isMemoryReference(final LocationAtom locationAtom) {
    if ((locationAtom.getSource() instanceof LocationSourceMemory)) {
      final LocationSourceMemory source = (LocationSourceMemory) locationAtom.getSource();
      final BigInteger memorySize = source.getMemory().getSize();

      // MEMs of length 1 are often used as global variables.
      // For this reason, there such MEMs are excluded.
      return source.getMemory().getKind() == Memory.Kind.MEM &&
          memorySize.compareTo(BigInteger.ONE) > 0;
    }

    if ((locationAtom.getSource() instanceof LocationSourcePrimitive)) {
      final LocationSourcePrimitive source =
          (LocationSourcePrimitive) locationAtom.getSource();

      if (source.getPrimitive() instanceof PrimitiveAND) {
        return ((PrimitiveAND) source.getPrimitive()).isMemoryReference();
      }
    }

    return false;
  }

  private static boolean isMemoryReference(final LocationConcat locationConcat) { 
    for (final LocationAtom locationAtom : locationConcat.getLocations()) {
      if (isMemoryReference(locationAtom)) {
        return true;
      }
    }
    return false;
  }

  private final Map<String, Primitive> args;
  private final Map<String, Attribute> attrs;
  private final List<Location> loadTargets;

  public MemoryAccessDetector(
      final Map<String, Primitive> args,
      final Map<String, Attribute> attrs) {
    this.args = args;
    this.attrs = attrs;
    this.loadTargets = new ArrayList<>();
  }

  private void addLoadTarget(final Location location) {
    loadTargets.add(location);
  }

  private boolean isLoadTarget(final Expr expr) {
    final NodeInfo nodeInfo = expr.getNodeInfo();
    if (!nodeInfo.isLocation()) {
      return false;
    }

    final Location location = (Location) nodeInfo.getSource();
    for (final Location loadLocation : loadTargets) {
      if (location.equals(loadLocation)) {
        return true;
      }
    }

    return false;
  }

  public MemoryAccessStatus getMemoryAccessStatus(final String attributeName) {
    final Attribute attribute = attrs.get(attributeName);
    if (null == attribute) {
      return MemoryAccessStatus.NO;
    }

    return getMemoryAccessStatus(attribute.getStatements());
  }

  private MemoryAccessStatus getMemoryAccessStatus(final List<Statement> stmts) {
    MemoryAccessStatus result = MemoryAccessStatus.NO;

    for (final Statement stmt : stmts) {
      switch(stmt.getKind()) {
        case ASSIGN:
          final StatementAssignment stmtAssign = (StatementAssignment) stmt;
          final MemoryAccessStatus assignResult = getMemoryAccessStatus(stmtAssign);

          // If the same a variable was used by a load and a store action,
          // we assume that this is store action and the load was performed just
          // make it possible to to write a small portion of data (smaller than the storage unit).

          if (assignResult.isStore() && isLoadTarget(stmtAssign.getRight())) {
            result = assignResult;
          } else {
            result = result.merge(getMemoryAccessStatus((StatementAssignment) stmt));
          }

          break;

        case CALL:
          result = result.merge(getMemoryAccessStatus((StatementAttributeCall) stmt));
          break;

        case COND:
          result = result.merge(getMemoryAccessStatus((StatementCondition) stmt));
          break;

        case FORMAT:  // Ignored
        case FUNCALL: // Ignored
          break;

        default:
          throw new IllegalArgumentException("Unknown statement kind: " + stmt.getKind());
      }
    }

    return result;
  }

  private MemoryAccessStatus getMemoryAccessStatus(final StatementAssignment stmt) {
    MemoryAccessStatus result = MemoryAccessStatus.NO;

    final Expr right = stmt.getRight();
    final Location left = stmt.getLeft();

    if (isMemoryReference(right)) {
      // Load action is detected
      final int bitSize = right.getNodeInfo().getType().getBitSize();
      result = new MemoryAccessStatus(true, false, bitSize);
      addLoadTarget(left);
    }

    if (isMemoryReference(left)) {
      // Store action is detected
      final int bitSize = left.getType().getBitSize();
      result = result.merge(new MemoryAccessStatus(false, true, bitSize));
    }

    return result;
  }

  private MemoryAccessStatus getMemoryAccessStatus(final StatementAttributeCall stmt) {
    // Instance Attribute Call
    if (stmt.getCalleeInstance() != null) {
      final PrimitiveAND primitive = stmt.getCalleeInstance().getPrimitive();
      return new MemoryAccessStatus(
          primitive.isLoad(), primitive.isStore(), primitive.getBlockSize());
    }

    // Argument Attribute Call
    if (stmt.getCalleeName() != null) {
      final Primitive callee = args.get(stmt.getCalleeName());

      if (callee.isOrRule()) {
        return MemoryAccessStatus.NO;
      }

      final PrimitiveAND primitive = (PrimitiveAND) callee;
      return new MemoryAccessStatus(
          primitive.isLoad(), primitive.isStore(), primitive.getBlockSize());
    }

    // This Object Attribute Call
    final Attribute attribute = attrs.get(stmt.getAttributeName());
    return getMemoryAccessStatus(attribute.getStatements());
  }

  private MemoryAccessStatus getMemoryAccessStatus(final StatementCondition stmt) {
    MemoryAccessStatus result = MemoryAccessStatus.NO;

    for (int index = 0; index < stmt.getBlockCount(); ++index) {
      final StatementCondition.Block block = stmt.getBlock(index);
      result = result.merge(getMemoryAccessStatus(block.getStatements()));
    }

    return result;
  }
}

