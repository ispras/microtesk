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

package ru.ispras.microtesk.mmu.translator.generation;

import java.util.Collection;
import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Stmt;
import ru.ispras.microtesk.mmu.translator.ir.StmtException;
import ru.ispras.microtesk.mmu.translator.ir.StmtTrace;
import ru.ispras.microtesk.mmu.translator.ir.Variable;

public abstract class STBBuilderBase {
  public static final Class<?> BIT_VECTOR_CLASS = 
      ru.ispras.fortress.data.types.bitvector.BitVector.class;

  public static final Class<?> DATA_CLASS =
      ru.ispras.microtesk.mmu.model.api.Data.class;

  public static final Class<?> BUFFER_CLASS =
      ru.ispras.microtesk.mmu.model.api.Buffer.class;

  public static final Class<?> CACHE_CLASS =
      ru.ispras.microtesk.mmu.model.api.Cache.class;

  public static final Class<?> MEMORY_CLASS =
      ru.ispras.microtesk.mmu.model.api.Memory.class;

  public static final Class<?> INDEXER_CLASS =
      ru.ispras.microtesk.mmu.model.api.Indexer.class;

  public static final Class<?> MATCHER_CLASS =
      ru.ispras.microtesk.mmu.model.api.Matcher.class;

  public static final Class<?> POLICY_ID_CLASS =
      ru.ispras.microtesk.mmu.model.api.PolicyId.class;

  protected abstract String getId();

  protected final String removePrefix(final String name) {
    final String prefix = getId() + ".";
    InvariantChecks.checkTrue(name.startsWith(prefix), name + " prefix is expected: " + prefix);

    return name.substring(prefix.length());
  }

  protected final void buildVariableDecl(final ST st, final Variable variable) {
    final String mappingName = removePrefix(variable.getName());

    final String typeName =
        (variable.isStruct() ? DATA_CLASS : BIT_VECTOR_CLASS).getSimpleName();

    ExprPrinter.get().addVariableMappings(variable, mappingName);
    st.add("stmts", String.format("%s %s;", typeName,  mappingName));
  }

  protected final void buildVariableDecls(final ST st, final Collection<Variable> variables) {
    for (final Variable variable : variables) {
      buildVariableDecl(st, variable);
    }
  }

  protected final void buildStmt(final ST st, final STGroup group, final Stmt stmt) {
    switch(stmt.getKind()) {
      case ASSIGN:
        break;

      case IF:
        break;

      case EXCEPT:
        buildStmtException(st, (StmtException) stmt);
        break;

      case TRACE:
        buildStmtTrace(st, (StmtTrace) stmt);
        break;

      default:
        throw new IllegalArgumentException(
            "Unsupported statement kind: " + stmt.getKind());
    }
  }

  protected final void buildStmts(final ST st, final STGroup group, final List<Stmt> stmts) {
    for (final Stmt stmt : stmts) {
      buildStmt(st, group, stmt);
    }
  }

  private void buildStmtException(final ST st, final StmtException stmt) {
    st.add("stmts", String.format("exception(\"%s\");", stmt.getMessage()));
  }

  private void buildStmtTrace(final ST st, final StmtTrace stmt) {
    // TODO
  }
}
