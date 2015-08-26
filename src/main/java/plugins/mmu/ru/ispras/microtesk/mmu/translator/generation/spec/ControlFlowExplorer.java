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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.AbstractStorage;
import ru.ispras.microtesk.mmu.translator.ir.Attribute;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.mmu.translator.ir.Stmt;
import ru.ispras.microtesk.mmu.translator.ir.StmtAssign;
import ru.ispras.microtesk.mmu.translator.ir.StmtIf;

final class ControlFlowExplorer {
  public ControlFlowExplorer(final Memory memory) {
    InvariantChecks.checkNotNull(memory);

    final Attribute read = memory.getAttribute(AbstractStorage.READ_ATTR_NAME);
    final Buffer readTarget = visitStmts(read.getStmts());

    final Attribute write = memory.getAttribute(AbstractStorage.WRITE_ATTR_NAME);
    final Buffer writeTarget = visitStmts(write.getStmts());
  }

  public Buffer getTargetBuffer() {
    return null;
  }

  private Buffer visitStmts(final List<Stmt> stmts) {
    InvariantChecks.checkNotNull(stmts);

    Buffer lastAccess = null;
    for (final Stmt stmt : stmts) {
      Buffer currentAccess = null;
      switch(stmt.getKind()) {
        case ASSIGN:
          currentAccess = visitStmtAssign((StmtAssign) stmt);
          break;

        case IF:
          currentAccess = visitStmtIf((StmtIf) stmt);
          break;

        case EXCEPT: // Other statements cannot be visited after exception
          return lastAccess;

        case MARK: // Ignored
          break;

        case TRACE: // Ignored
          break;

        default:
          throw new IllegalStateException("Unknown statement: " + stmt.getKind());
      }

      if (null != currentAccess) {
        lastAccess = currentAccess;
      }
    }

    return lastAccess;
  }

  private Buffer visitStmtAssign(final StmtAssign stmt) {
    InvariantChecks.checkNotNull(stmt);
    // TODO Auto-generated method stub
    return null;
  }

  private Buffer visitStmtIf(final StmtIf stmt) {
    InvariantChecks.checkNotNull(stmt);
    // TODO Auto-generated method stub
    return null;
  }
}
