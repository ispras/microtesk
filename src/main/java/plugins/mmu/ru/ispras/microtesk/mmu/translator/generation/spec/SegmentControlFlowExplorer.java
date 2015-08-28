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

import java.util.List;

import ru.ispras.fortress.expression.ExprTreeVisitorDefault;
import ru.ispras.fortress.expression.ExprTreeWalker;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.mmu.translator.ir.AbstractStorage;
import ru.ispras.microtesk.mmu.translator.ir.Attribute;
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Stmt;
import ru.ispras.microtesk.mmu.translator.ir.StmtAssign;
import ru.ispras.microtesk.mmu.translator.ir.StmtIf;

public final class SegmentControlFlowExplorer {
  private final boolean isMapped;
  private final Node paExpr;
  private final Node restExpr;

  public SegmentControlFlowExplorer(final Segment segment) {
    InvariantChecks.checkNotNull(segment);

    final Attribute attribute = segment.getAttribute(AbstractStorage.READ_ATTR_NAME);
    if (null == attribute) {
      isMapped = false;
      paExpr = segment.getAddressArg().getNode();
      restExpr = NodeValue.newInteger(0);
    } else if (isExternalAccess(attribute.getStmts())) {
      isMapped = true;
      paExpr = null;
      restExpr = null;
    } else {
      isMapped = false;
      paExpr = NodeValue.newInteger(0); // TODO: extract from Stmts
      restExpr = NodeValue.newInteger(0); // TODO: extract from Stmts
    }
  }

  public boolean isMapped() {
    return isMapped;
  }

  public Node getPaExpr() {
    return paExpr;
  }

  public Node getRestExpr() {
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
          if (!isExternalAccess((StmtAssign) stmt)) {
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
}
