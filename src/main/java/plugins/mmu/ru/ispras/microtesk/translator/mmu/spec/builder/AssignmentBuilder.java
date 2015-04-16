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

package ru.ispras.microtesk.translator.mmu.spec.builder;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ru.ispras.microtesk.translator.mmu.spec.MmuAction;
import ru.ispras.microtesk.translator.mmu.spec.MmuAssignment;
import ru.ispras.microtesk.translator.mmu.spec.MmuDevice;
import ru.ispras.microtesk.translator.mmu.spec.MmuExpression;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;

public class AssignmentBuilder {
  private final String name;
  private final Atom lhs;
  private final Atom rhs;

  AssignmentBuilder(String name, Atom lhs, Atom rhs) {
    checkNotNull(name);
    checkNotNull(lhs);
    checkNotNull(rhs);

    this.name = name;
    this.lhs = lhs;
    this.rhs = rhs;
  }

  public MmuAction build() {
    final MmuDevice device = getDevice(lhs, rhs);
    final Iterator<IntegerVariable> leftIt = newVariableIterator(lhs);
    final Iterator<MmuExpression> rightIt = newExpressionIterator(rhs);

    final List<MmuAssignment> assignments = new ArrayList<>();
    while (leftIt.hasNext() && rightIt.hasNext()) {
      final IntegerVariable leftVar = leftIt.next();
      final MmuExpression rightExpr = rightIt.next();

      final MmuAssignment assignment = new MmuAssignment(leftVar, rightExpr);
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

/*
if (lhs.getUserData() instanceof AttributeRef) {
    final AttributeRef attrRef = (AttributeRef) lhs.getUserData();
    final MmuDevice device = spec.getDevice(attrRef.getTarget().getId());
    assigmentBuilder.setDevice(device);
    assigmentBuilder.setLeftSide(device.getFields());
  } else if (lhs.getUserData() instanceof FieldRef) {
    final FieldRef fieldRef = (FieldRef) lhs.getUserData();
    final IntegerVariable intVar = 
        variables.getGroup(fieldRef.getVariable().getId()).getVariable(fieldRef.getField().getId());
    assigmentBuilder.setLeftSide(intVar);
  } else {
    final IntegerVariableTracker.Status status = variables.checkDefined(lhs.getName());
    switch (status) {
      case VARIABLE:
        assigmentBuilder.setLeftSide(variables.getVariable(lhs.getName()));
        break;
      case GROUP:
        assigmentBuilder.setLeftSide(variables.getGroup(lhs.getName()).getVariables());
        break;
      default:
        throw new IllegalStateException("Undeclared variable: " + lhs.getName());
    }
  }

  if (rhs.getUserData() instanceof AttributeRef) {
    final AttributeRef attrRef = (AttributeRef) rhs.getUserData();
    final MmuDevice device = spec.getDevice(attrRef.getTarget().getId());
    assigmentBuilder.setDevice(device);
    assigmentBuilder.setRightSide(device.getFields());
  } else if (rhs.getUserData() instanceof FieldRef) {
    final FieldRef fieldRef = (FieldRef) rhs.getUserData();
    final IntegerVariable intVar = 
        variables.getGroup(fieldRef.getVariable().getId()).getVariable(fieldRef.getField().getId());
    assigmentBuilder.setRightSide(intVar);
  } else {
    final IntegerVariableTracker.Status status = variables.checkDefined(rhs.getName());
    switch (status) {
      case VARIABLE:
        assigmentBuilder.setRightSide(variables.getVariable(rhs.getName()));
        break;
      case GROUP:
        assigmentBuilder.setRightSide(variables.getGroup(rhs.getName()).getVariables());
        break;
      default:
        throw new IllegalStateException("Undeclared variable: " + rhs.getName());
    }
  }
   */
  
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

  private static Iterator<IntegerVariable> newVariableIterator(Atom atom) {
    final Collection<IntegerVariable> variables;

    if (Atom.Kind.VARIABLE == atom.getKind()) {
      variables = Collections.singletonList((IntegerVariable) atom.getObject());
    } else if (Atom.Kind.GROUP == atom.getKind()) {
      variables = ((IntegerVariableGroup) atom.getObject()).getVariables();
    } else {
      throw new IllegalStateException(
          atom.getKind() + " cannot be used as a variable in assigment.");
    }

    return variables.iterator();
  }

  private static Iterator<MmuExpression> newExpressionIterator(Atom atom) {
    if (Atom.Kind.VARIABLE == atom.getKind() || Atom.Kind.GROUP == atom.getKind()) {
      return new VarToExprIteratorAdapter(newVariableIterator(atom));
    } else if (Atom.Kind.CONCAT == atom.getKind()) {
      return Collections.singletonList((MmuExpression) atom.getObject()).iterator();
    } else {
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
