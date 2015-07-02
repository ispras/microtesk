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

package ru.ispras.microtesk.mmu.translator.ir.spec.builder;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ru.ispras.microtesk.basis.solver.IntegerField;
import ru.ispras.microtesk.basis.solver.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAssignment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;

public class AssignmentBuilder {
  private final String name;
  private final Atom lhs;
  private final Atom rhs;

  AssignmentBuilder(final String name, final Atom lhs, final Atom rhs) {
    checkNotNull(name);
    checkNotNull(lhs);
    checkNotNull(rhs);

    this.name = name;
    this.lhs = lhs;
    this.rhs = rhs;
  }

  public MmuAction build() {
    final MmuDevice device = getDevice(lhs, rhs);

    final Iterator<?> leftIt = newVariableIterator(lhs);
    final Iterator<MmuExpression> rightIt = newExpressionIterator(rhs);

    final List<MmuAssignment> assignments = new ArrayList<>();
    while (leftIt.hasNext() && rightIt.hasNext()) {
      final Object leftVar = leftIt.next();
      final MmuExpression rightExpr = rightIt.next();

      final MmuAssignment assignment;
      if (leftVar instanceof IntegerVariable) {
        assignment = new MmuAssignment((IntegerVariable) leftVar, rightExpr);
      } else {
        assignment= new MmuAssignment((IntegerField) leftVar, rightExpr);
      }

      assignments.add(assignment);
    }

    while (leftIt.hasNext() != rightIt.hasNext()) {
      throw new IllegalStateException("Assignment mismatch.");
    }

    if (assignments.isEmpty()) {
      throw new IllegalStateException("Nothing to assign.");
    }

    final MmuAssignment[] array = assignments.toArray(new MmuAssignment[assignments.size()]);
    return new MmuAction(name, device, array);
  }

  private static MmuDevice getDevice(Atom lhs, Atom rhs) {
    final MmuDevice left = extractDevice(lhs);
    final MmuDevice right = extractDevice(rhs);

    if (left == right) {
      return left;
    }

    if (null == left) {
      return right;
    }

    if (null == right) {
      return left;
    }

    throw new IllegalStateException(String.format(
        "Different devices cannot be used in left and right sides of assigment: %s and %s",
        left.getName(), right.getName()));
  }

  private static MmuDevice extractDevice(Atom atom) {
    if (atom.getKind() == Atom.Kind.GROUP) {
      return ((IntegerVariableGroup) atom.getObject()).getDevice();
    }
    return null;
  }

  private static Iterator<?> newVariableIterator(final Atom atom) {
    switch (atom.getKind()) {
      case VARIABLE: 
        return Collections.singletonList((IntegerVariable) atom.getObject()).iterator();

      case FIELD: 
        return Collections.singletonList((IntegerField) atom.getObject()).iterator();

      case GROUP:
        return ((IntegerVariableGroup) atom.getObject()).getVariables().iterator();

      default:
        throw new IllegalStateException(
            atom.getKind() + " cannot be used as a variable in assigment.");
    }
  }

  private static Iterator<MmuExpression> newExpressionIterator(final Atom atom) {
    switch (atom.getKind()) {
      case VARIABLE:
        return Collections.singletonList(MmuExpression.VAR(
            (IntegerVariable) atom.getObject())).iterator();

      case GROUP:
        return new VarToExprIteratorAdapter(
            ((IntegerVariableGroup) atom.getObject()).getVariables().iterator());

      case FIELD: {
        final IntegerField intField = (IntegerField) atom.getObject();
        final MmuExpression mmuExpr = MmuExpression.VAR(
            intField.getVariable(), intField.getLoIndex(), intField.getHiIndex());
        return Collections.singletonList(mmuExpr).iterator();
      }

      case CONCAT:
        return Collections.singletonList((MmuExpression) atom.getObject()).iterator();

      default:
        throw new IllegalStateException(
            atom.getKind() + " cannot be used as an expresion in assigment.");
    }
  }

  private static final class VarToExprIteratorAdapter implements Iterator<MmuExpression> {
    final Iterator<IntegerVariable> iterator;

    private VarToExprIteratorAdapter(Iterator<IntegerVariable> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public MmuExpression next() {
      return MmuExpression.VAR(iterator.next());
    }

    @Override
    public void remove() {
      iterator.remove();
    }
  }
}
