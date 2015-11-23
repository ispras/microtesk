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

import java.math.BigInteger;
import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.ir.Operation;
import ru.ispras.microtesk.mmu.translator.ir.Stmt;
import ru.ispras.microtesk.mmu.translator.ir.StmtAssign;
import ru.ispras.microtesk.translator.generation.STBuilder;

public final class STBOperation implements STBuilder {
  public static final Class<?> OPERATION_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuOperation.class;

  private final String packageName;
  private final Operation operation;
  private final String context;

  protected STBOperation(
      final String packageName,
      final Operation operation) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(operation);

    this.packageName = packageName;
    this.operation = operation;
    this.context = operation.getId();
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildBody(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", operation.getId()); 
    st.add("pack", packageName);
    st.add("imps", OPERATION_CLASS.getName());
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stBody = group.getInstanceOf("operation_body");

    stBody.add("name", operation.getId());
    stBody.add("addr", operation.getAddress().getId());
    stBody.add("addr_name", Utils.getVariableName(context, operation.getAddressArg().getName()));

    for (final Stmt stmt : operation.getStmts()) {
      buildStmt(stBody, stmt);
    }

    st.add("members", stBody);
  }

  private void buildStmt(final ST st, final Stmt stmt) {
    if (stmt.getKind() == Stmt.Kind.TRACE) {
      // Trace statements are ignored (they work only in the simulator)
      return;
    }

    if (stmt.getKind() != Stmt.Kind.ASSIGN) {
      throw new IllegalArgumentException(String.format(
          "The %s operation contains illegal statement %s.%n" +
          "Only assignment statements are allowed.",
          operation.getId(),
          stmt
          ));
    }

    final StmtAssign assignment = (StmtAssign) stmt;
    final Atom lhs = AtomExtractor.extract(assignment.getLeft());
    final Atom rhs = AtomExtractor.extract(assignment.getRight());

    if (Atom.Kind.VARIABLE != lhs.getKind() &&
        Atom.Kind.FIELD != lhs.getKind()) {
      throw new IllegalArgumentException(
          assignment.getLeft() + " cannot be used as left side of assignment.");
    }

    final String left = toString(lhs);
    final String right = "null";

    final String binding = String.format("new MmuBinding(%s, %s)", left, right);
    st.add("bindings", binding);
  }

  @SuppressWarnings("unchecked")
  private String toString(final Atom atom) {
    InvariantChecks.checkNotNull(atom);

    final Object object = atom.getObject();
    switch (atom.getKind()) {
      case VALUE: {
        return Utils.toString((BigInteger) object);
      }

      case VARIABLE: {
        final IntegerVariable variable = (IntegerVariable) object;
        return getVariableName(variable);
      }

      case FIELD: {
        final IntegerField field = (IntegerField) object;
        final IntegerVariable variable = field.getVariable();
        return String.format("%s.field(%d, %d)",
            getVariableName(variable), field.getLoIndex(), field.getHiIndex());
      }

      case CONCAT: {
        return Utils.toMmuExpressionText(
            context, (List<IntegerField>) object);
      }

      default:
        throw new IllegalStateException(
            "Unsupported atom kind: " + atom.getKind());
    }
  }

  private String getVariableName(final IntegerVariable variable) {
    final String name = variable.getName();
    return Utils.getVariableName(context, name);
  }
}
