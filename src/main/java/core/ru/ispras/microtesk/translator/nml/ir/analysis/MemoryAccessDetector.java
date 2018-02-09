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

import ru.ispras.fortress.expression.ExprTreeVisitorDefault;
import ru.ispras.fortress.expression.ExprTreeWalker;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.memory.Memory;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalkerFlow;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourceMemory;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourcePrimitive;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAssignment;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class MemoryAccessDetector implements TranslatorHandler<Ir> {
  @Override
  public void processIr(final Ir ir) {
    final IrWalkerFlow walker = new IrWalkerFlow(ir, new Visitor());
    walker.visit();
  }

  private static final class Visitor extends IrVisitorDefault {

    private static final class Context {
      private final PrimitiveAND primitive;
      private MemoryAccessStatus status;
      private final List<Location> loadTargets;

      private Context(final PrimitiveAND primitive) {
        this.primitive = primitive;
        this.status = MemoryAccessStatus.NO;
        this.loadTargets = new ArrayList<>();
      }
    }

    private final Deque<Context> contexts = new ArrayDeque<>();

    @Override
    public void onPrimitiveBegin(final Primitive item) {
      if (!item.isOrRule()) {
        final PrimitiveAND primitive = (PrimitiveAND) item;

        final boolean isMemoryReference = primitive.getReturnExpr() != null
            ? isMemoryReference(primitive.getReturnExpr()) : false;

        primitive.getInfo().setMemoryReference(isMemoryReference);
        contexts.push(new Context(primitive));
      }
    }

    @Override
    public void onPrimitiveEnd(final Primitive item) {
      if (!item.isOrRule()) {
        final Context context = contexts.pop();
        InvariantChecks.checkTrue(item == context.primitive);

        final PrimitiveAND primitive = context.primitive;
        final MemoryAccessStatus status = context.status;

        primitive.getInfo().setLoad(status.isLoad());
        primitive.getInfo().setStore(status.isStore());
        primitive.getInfo().setBlockSize(status.getBlockSize());
      }

      if (isStatus(Status.SKIP)) {
        setStatus(Status.OK);
      }
    }

    @Override
    public void onAssignment(final StatementAssignment stmt) {
      MemoryAccessStatus status = MemoryAccessStatus.NO;

      final Expr right = stmt.getRight();
      final Expr left = stmt.getLeft();

      if (isMemoryReference(right)) {
        final int bitSize = right.getNodeInfo().getType().getBitSize();
        status = new MemoryAccessStatus(true, false, bitSize);
        addLoadTarget(left);
      }

      if (isMemoryReference(left)) {
        final int bitSize = left.getNodeInfo().getType().getBitSize();
        status = status.merge(new MemoryAccessStatus(false, true, bitSize));
      }

      final Context context = contexts.peek();
      if (status.isStore() && isLoadTarget(stmt.getRight())) {
        context.status = status;
      } else {
        context.status = context.status.merge(status);
      }
    }

    @Override
    public void onAttributeBegin(final PrimitiveAND andRule, final Attribute attr) {
      final Context context = contexts.peek();
      if (andRule != context.primitive) {
        setStatus(Status.SKIP);

        final PrimitiveInfo info = andRule.getInfo();
        final MemoryAccessStatus status =
            new MemoryAccessStatus(info.isLoad(), info.isStore(), info.getBlockSize());

        context.status = context.status.merge(status);
      }
    }

    @Override
    public void onAttributeEnd(final PrimitiveAND andRule, final Attribute attr) {
      if (andRule != contexts.peek().primitive) {
        setStatus(Status.OK);
      }
    }

    private void addLoadTarget(final Expr expr) {
      final Context context = contexts.peek();
      context.loadTargets.addAll(getLocations(expr));
    }

    private List<Location> getLocations(final Expr expr) {
      final MemoryReferenceCollector visitor = new MemoryReferenceCollector();
      final ExprTreeWalker walker = new ExprTreeWalker(visitor);

      walker.visit(expr.getNode());
      return visitor.getLocations();
    }

    private boolean isLoadTarget(final Expr expr) {
      final NodeInfo nodeInfo = expr.getNodeInfo();
      if (!nodeInfo.isLocation()) {
        return false;
      }

      final Location location = (Location) nodeInfo.getSource();
      final Context context = contexts.peek();
      for (final Location loadLocation : context.loadTargets) {
        if (location.equals(loadLocation)) {
          return true;
        }
      }

      return false;
    }
  }

  private static boolean isMemoryReference(final Expr expr) {
    InvariantChecks.checkNotNull(expr);

    final MemoryReferenceDetector visitor = new MemoryReferenceDetector();
    final ExprTreeWalker walker = new ExprTreeWalker(visitor);

    walker.visit(expr.getNode());
    return visitor.isDetected();
  }

  private static boolean isMemoryReference(final Location locationAtom) {
    if ((locationAtom.getSource() instanceof LocationSourceMemory)) {
      final LocationSourceMemory source = (LocationSourceMemory) locationAtom.getSource();
      final BigInteger memorySize = source.getMemory().getSize();

      // MEMs of length 1 are often used as global variables.
      // For this reason, there such MEMs are excluded.
      return source.getMemory().getKind() == Memory.Kind.MEM
          && memorySize.compareTo(BigInteger.ONE) > 0;
    }

    if ((locationAtom.getSource() instanceof LocationSourcePrimitive)) {
      final LocationSourcePrimitive source =
          (LocationSourcePrimitive) locationAtom.getSource();

      if (source.getPrimitive() instanceof PrimitiveAND) {
        return ((PrimitiveAND) source.getPrimitive()).getInfo().isMemoryReference();
      }
    }

    return false;
  }

  private static final class MemoryReferenceDetector extends ExprTreeVisitorDefault {
    private boolean detected = false;

    public boolean isDetected() {
      return detected;
    }

    @Override
    public void onVariable(final NodeVariable variable) {
      final NodeInfo nodeInfo = (NodeInfo) variable.getUserData();
      final Location location = (Location) nodeInfo.getSource();

      if (isMemoryReference(location)) {
        detected = true;
        setStatus(Status.ABORT);
      }
    }

    @Override
    public void onOperationBegin(final NodeOperation node) {
      if (node.getOperationId() != StandardOperation.BVCONCAT) {
        setStatus(Status.SKIP);
      }
    }

    @Override
    public void onOperationEnd(final NodeOperation node) {
      if (node.getOperationId() != StandardOperation.BVCONCAT) {
        setStatus(Status.OK);
      }
    }
  }

  private static final class MemoryReferenceCollector extends ExprTreeVisitorDefault {
    private final List<Location> locations = new ArrayList<>();

    public List<Location> getLocations() {
      return locations;
    }

    @Override
    public void onVariable(final NodeVariable variable) {
      final NodeInfo nodeInfo = (NodeInfo) variable.getUserData();
      final Location location = (Location) nodeInfo.getSource();

      if (isMemoryReference(location)) {
        locations.add(location);
      }
    }

    @Override
    public void onOperationBegin(final NodeOperation node) {
      if (node.getOperationId() != StandardOperation.BVCONCAT) {
        setStatus(Status.SKIP);
      }
    }

    @Override
    public void onOperationEnd(final NodeOperation node) {
      if (node.getOperationId() != StandardOperation.BVCONCAT) {
        setStatus(Status.OK);
      }
    }
  }
}
