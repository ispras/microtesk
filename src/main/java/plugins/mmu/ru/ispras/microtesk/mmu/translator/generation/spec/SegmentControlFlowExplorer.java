/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.expression.ExprTreeVisitorDefault;
import ru.ispras.fortress.expression.ExprTreeWalker;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.ir.AbstractStorage;
import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.Attribute;
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Stmt;
import ru.ispras.microtesk.mmu.translator.ir.StmtAssign;
import ru.ispras.microtesk.mmu.translator.ir.StmtIf;
import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.mmu.translator.ir.spec.builder.IntegerFieldTracker;
import ru.ispras.microtesk.utils.FortressUtils;

final class SegmentControlFlowExplorer {
  private final boolean isMapped;
  private final List<IntegerField> paExpr;
  private final List<IntegerField> restExpr;

  public SegmentControlFlowExplorer(final Segment segment) {
    InvariantChecks.checkNotNull(segment);

    final Attribute attribute =
        segment.getAttribute(AbstractStorage.READ_ATTR_NAME);

    if (null == attribute || attribute.getStmts().isEmpty()) {
      isMapped = false;
      paExpr = Collections.singletonList(
          new IntegerField(newVariableForAddress(segment.getAddress())));
      restExpr = Collections.emptyList();
    } else if (isExternalAccess(attribute.getStmts())) {
      isMapped = true;
      paExpr = null;
      restExpr = null;
    } else {
      final AddressFieldExtractor fieldExtractor = new AddressFieldExtractor(segment);
      isMapped = false;
      paExpr = fieldExtractor.getPaExpr();
      restExpr = fieldExtractor.getRestExpr();
    }
  }

  private static IntegerVariable newVariableForAddress(final Address address) {
    InvariantChecks.checkNotNull(address);

    final int variableSize =
        address.getAddressType().getBitSize();

    final String variableId =
        address.getId() + "." + Utils.toString(address.getAccessChain());

    return new IntegerVariable(variableId, variableSize);
  }

  public boolean isMapped() {
    return isMapped;
  }

  public List<IntegerField> getPaExpr() {
    return paExpr;
  }

  public List<IntegerField> getRestExpr() {
    return restExpr;
  }

  private static boolean isExternalAccess(final List<Stmt> stmts) {
    InvariantChecks.checkNotNull(stmts);

    for (final Stmt stmt : stmts) {
      switch(stmt.getKind()) {
        case IF:
          if (isExternalAccess((StmtIf) stmt)) {
            return true;
          }
          break;

        case ASSIGN:
          if (isExternalAccess((StmtAssign) stmt)) {
            return true;
          }
          break;

        case EXCEPT: // Other statements cannot be visited after exception
          return false;

        case MARK: // Ignored
          break;

        case TRACE: // Ignored
          break;

        default:
          throw new IllegalStateException("Unknown statement: " + stmt.getKind());
      }
    }

    return false;
  }

  private static boolean isExternalAccess(final StmtIf stmt) {
    InvariantChecks.checkNotNull(stmt);

    for (final Pair<Node, List<Stmt>> block : stmt.getIfBlocks()) {
      final Node cond = block.first;
      final List<Stmt> stmts = block.second;

      if (isExternalAccess(cond)) {
        return true;
      }

      if (isExternalAccess(stmts)) {
        return true;
      }
    }

    return isExternalAccess(stmt.getElseBlock());
  }

  private static boolean isExternalAccess(final StmtAssign stmt) {
    InvariantChecks.checkNotNull(stmt);
    return isExternalAccess(stmt.getLeft()) || isExternalAccess(stmt.getRight());
  }

  private static boolean isExternalAccess(final Node node) {
    InvariantChecks.checkNotNull(node);

    final ExternalAccessDetector visitor = new ExternalAccessDetector();
    final ExprTreeWalker walker = new ExprTreeWalker(visitor);

    walker.visit(node);
    return visitor.isExternalAccess();
  }

  private static final class ExternalAccessDetector extends ExprTreeVisitorDefault {
    private boolean externalAccess = false;

    public boolean isExternalAccess() {
      return externalAccess;
    }

    @Override
    public void onVariable(final NodeVariable variable) {
      if (variable.getUserData() instanceof AttributeRef) {
        externalAccess = true;
        setStatus(Status.ABORT);
      }
    }
  }

  private static final class AddressFieldExtractor {
    private final Variable paVariable;
    private final Variable vaVariable;
    private final IntegerVariable variableForVa;
    private final IntegerFieldTracker vaFieldTracker;

    private final List<IntegerField> paExpr;
    private final List<IntegerField> restExpr;

    private AddressFieldExtractor(final Segment segment) {
      InvariantChecks.checkNotNull(segment);

      this.paVariable =
          segment.getDataArg().accessNested(segment.getDataArgAddress().getAccessChain());

      this.vaVariable =
          segment.getAddressArg().accessNested(segment.getAddress().getAccessChain());

      this.variableForVa = newVariableForAddress(segment.getAddress());
      this.vaFieldTracker = new IntegerFieldTracker(this.variableForVa);

      this.paExpr = new ArrayList<>();

      final Attribute attribute = segment.getAttribute(AbstractStorage.READ_ATTR_NAME);
      visitStmts(attribute.getStmts());

      this.restExpr = vaFieldTracker.getFields();
    }

    public List<IntegerField> getPaExpr() {
      return paExpr;
    }

    public List<IntegerField> getRestExpr() {
      return restExpr;
    }

    private void visitStmts(final List<Stmt> stmts) {
      InvariantChecks.checkNotNull(stmts);

      for (final Stmt stmt : stmts) {
        switch(stmt.getKind()) {
          case IF: // Conditions are ignored
            break;

          case ASSIGN:
            visitStmtAssign((StmtAssign) stmt);
            break;

          case EXCEPT: // Other statements cannot be visited after exception
            return;

          case MARK: // Ignored
            break;

          case TRACE: // Ignored
            break;

          default:
            throw new IllegalStateException(
                "Unknown statement: " + stmt.getKind());
        }
      }
    }

    private void visitStmtAssign(final StmtAssign stmt) {
      InvariantChecks.checkNotNull(stmt);

      final Node left = stmt.getLeft();
      if (!isPa(left)) {
        return;
      }

      final Node right = stmt.getRight();

      final Visitor visitor = new Visitor();
      final ExprTreeWalker walker = new ExprTreeWalker(visitor);
      walker.visit(right);

      paExpr.addAll(visitor.fields);
    }

    private boolean isPa(final Node expr) {
      InvariantChecks.checkNotNull(expr);

      if (expr.getKind() == Node.Kind.VARIABLE &&
          expr.getUserData() instanceof Variable) {
        return paVariable.equals(expr.getUserData());
      }

      if (expr.getKind() != Node.Kind.OPERATION) {
        return false;
      }

      final NodeOperation op = (NodeOperation) expr;
      if (op.getOperationId() != StandardOperation.BVEXTRACT) {
        return false;
      }

      InvariantChecks.checkTrue(op.getOperandCount() == 3);
      final Node var = op.getOperand(2);

      return paVariable.equals(var.getUserData());
    }

    private IntegerField newAddressField(final NodeOperation node) {
      InvariantChecks.checkTrue(node.getOperationId() == StandardOperation.BVEXTRACT);

      if (node.getOperandCount() != 3) {
        throw new IllegalStateException("Wrong operand count (3 is expected): " + node);
      }

      final Node variable = node.getOperand(2);
      if (variable.getKind() != Node.Kind.VARIABLE) {
        throw new IllegalStateException(variable + " is not a variable.");
      }

      if (!variable.equals(vaVariable.getNode())) {
        return null;
      }

      final int lo = FortressUtils.extractInt(node.getOperand(1));
      final int hi = FortressUtils.extractInt(node.getOperand(0));

      vaFieldTracker.exclude(lo, hi);
      return new IntegerField(variableForVa, lo, hi);
    }

    private IntegerField newAddressField(final NodeVariable node) {
      if (!node.equals(vaVariable.getNode())) {
        return null;
      }

      vaFieldTracker.excludeAll();
      return new IntegerField(variableForVa);
    }

    private class Visitor extends ExprTreeVisitorDefault {
      private final Set<StandardOperation> SUPPORTED_OPS = EnumSet.of(
          StandardOperation.BVEXTRACT, StandardOperation.BVCONCAT);

      private final List<IntegerField> fields = new ArrayList<>();

      @Override
      public void onOperationBegin(final NodeOperation node) {
        final Enum<?> op = node.getOperationId();
        if (!SUPPORTED_OPS.contains(op)) {
          throw new IllegalStateException(String.format(
              "Operation %s is not supported in address expressions.", op));
        }

        if (op == StandardOperation.BVEXTRACT) {
          final IntegerField addressField = newAddressField(node);
          if (null != addressField) {
            fields.add(addressField);
          }
          setStatus(Status.SKIP);
        }
      }

      @Override
      public void onVariable(final NodeVariable node) {
        final IntegerField addressField = newAddressField(node);
        if (null != addressField) {
          fields.add(addressField);
        }
      }
    }
  }
}
