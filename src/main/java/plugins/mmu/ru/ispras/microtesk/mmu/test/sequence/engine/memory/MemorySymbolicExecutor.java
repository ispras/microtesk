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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerClause;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerFormula;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBinding;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuConditionAtom;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

/**
 * {@link MemorySymbolicExecutor} implements a simple symbolic executor of memory access structures.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemorySymbolicExecutor {
  public static final class Result {
    private final Collection<IntegerVariable> variables;
    private final IntegerFormula<IntegerField> formula;

    public Result(
        final Collection<IntegerVariable> variables,
        final IntegerFormula<IntegerField> formula) {
      InvariantChecks.checkNotNull(variables);
      InvariantChecks.checkNotNull(formula);

      this.variables = variables;
      this.formula = formula;
    }

    public Collection<IntegerVariable> getVariables() {
      return variables;
    }

    public IntegerFormula<IntegerField> getFormula() {
      return formula;
    }
  }

  public static String getVarName(final String varName, final int pathIndex) {
    InvariantChecks.checkNotNull(varName);
    return String.format("%s$%d", varName, pathIndex);
  }

  private final MemoryAccessPath path;
  private final MemoryAccessStructure structure;

  private final Collection<IntegerVariable> variables = new LinkedHashSet<>();
  private final IntegerFormula<IntegerField> formula = new IntegerFormula<>();

  private final Map<String, IntegerVariable> varCache = new HashMap<>();

  public MemorySymbolicExecutor(final MemoryAccessPath path) {
    InvariantChecks.checkNotNull(path);

    this.path = path;
    this.structure = null;
  }

  public MemorySymbolicExecutor(final MemoryAccessStructure structure) {
    InvariantChecks.checkNotNull(structure);

    this.path = null;
    this.structure = structure;
  }

  public Result execute() {
    InvariantChecks.checkTrue(variables.isEmpty());
    InvariantChecks.checkTrue(formula.size() == 0);

    if (path != null) {
      execute(path);
    } else {
      execute(structure);
    }

    return new Result(variables, formula);
  }

  private void execute(final MemoryAccessStructure structure) {
    InvariantChecks.checkNotNull(structure);
    InvariantChecks.checkGreaterThanZero(structure.size());

    for (int j = 0; j < structure.size(); j++) {
      final MemoryAccessPath path2 = structure.getAccess(j).getPath();

      for (int i = 0; i < j; i++) {
        final MemoryAccessPath path1 = structure.getAccess(i).getPath();
        final MemoryDependency dependency = structure.getDependency(i, j);

        // It does not execute the paths (only the dependency).
        execute(path1, i, path2, j, dependency);
      }

      execute(path2, j);
    }
  }

  private void execute(final MemoryAccessPath path) {
    InvariantChecks.checkNotNull(path);

    execute(path, -1);
  }

  private void execute(final MemoryAccessPath path, final int pathIndex) {
    InvariantChecks.checkNotNull(path);

    for (final MmuTransition transition : path.getTransitions()) {
      execute(transition, pathIndex);
    }
  }

  private void execute(
      final MemoryAccessPath path1,
      final int pathIndex1,
      final MemoryAccessPath path2,
      final int pathIndex2,
      final MemoryDependency dependency) {
    InvariantChecks.checkNotNull(path1);
    InvariantChecks.checkNotNull(path2);
    InvariantChecks.checkNotNull(dependency);

    for (final MemoryHazard hazard : dependency.getHazards()) {
      execute(hazard, pathIndex1, pathIndex2);
    }
  }

  private void execute(final MemoryHazard hazard, final int pathIndex1, final int pathIndex2) {
    InvariantChecks.checkNotNull(hazard);

    final MmuCondition condition = hazard.getCondition();
    if (condition != null) {
      execute(condition, pathIndex1, pathIndex2);
    }
  }

  private void execute(final MmuCondition condition, final int pathIndex1, final int pathIndex2) {
    InvariantChecks.checkNotNull(condition);

    for (final MmuConditionAtom atom : condition.getAtoms()) {
      if (atom.getType() != MmuConditionAtom.Type.EQUAL) {
        continue;
      }

      final MmuExpression expression = atom.getExpression();

      final IntegerClause<IntegerField> clause = new IntegerClause<>(
          !atom.isNegated() ? IntegerClause.Type.AND : IntegerClause.Type.OR);

      for (final IntegerField term : expression.getTerms()) {
        final IntegerField field1 = getPathField(term, pathIndex1);
        final IntegerField field2 = getPathField(term, pathIndex2);

        clause.addEquation(field1, field2, !atom.isNegated());

        variables.add(field1.getVariable());
        variables.add(field2.getVariable());
      }

      formula.addClause(clause);
    }
  }

  private void execute(final MmuTransition transition, final int pathIndex) {
    InvariantChecks.checkNotNull(transition);

    final MmuGuard guard = transition.getGuard();
    if (guard != null) {
      execute(guard, pathIndex);
    }

    final MmuAction action = transition.getTarget();
    if (action != null) {
      execute(action, pathIndex);
    }
  }

  private void execute(final MmuGuard guard, final int pathIndex) {
    InvariantChecks.checkNotNull(guard);

    final MmuBuffer buffer = guard.getBuffer();
    if (buffer != null) {
      execute(buffer, pathIndex);
    }

    final MmuCondition condition = guard.getCondition();
    if (condition != null) {
      execute(condition, pathIndex);
    }
  }

  private void execute(final MmuAction action, final int pathIndex) {
    InvariantChecks.checkNotNull(action);

    final Map<IntegerField, MmuBinding> assignments = action.getAction();
    if (assignments != null) {
      execute(assignments.values(), pathIndex);
    }
  }

  private void execute(final MmuBuffer buffer, final int pathIndex) {
    InvariantChecks.checkNotNull(buffer);
    execute(buffer.getMatchBindings(), pathIndex);
  }

  private void execute(final MmuCondition condition, final int pathIndex) {
    InvariantChecks.checkNotNull(condition);
    InvariantChecks.checkTrue(condition.getType() == MmuCondition.Type.AND);

    for (final MmuConditionAtom atom : condition.getAtoms()) {
      InvariantChecks.checkTrue(atom.getType() == MmuConditionAtom.Type.EQUAL_CONST);

      final IntegerClause<IntegerField> clause = new IntegerClause<>(
          !atom.isNegated() ? IntegerClause.Type.AND : IntegerClause.Type.OR);

      final MmuExpression expression = atom.getExpression();
      final BigInteger constant = atom.getConstant();

      int offset = 0;

      for (final IntegerField term : expression.getTerms()) {
        final int lo = offset;
        final int hi = offset + (term.getWidth() - 1);

        final IntegerField field = getPathField(term, pathIndex);
        final BigInteger value = BitUtils.getField(constant, lo, hi);

        clause.addEquation(field, value, !atom.isNegated());
        offset += term.getWidth();

        variables.add(field.getVariable());
      }

      formula.addClause(clause);
    }
  }

  private void execute(final Collection<MmuBinding> bindings, final int pathIndex) {
    InvariantChecks.checkNotNull(bindings);

    final IntegerClause<IntegerField> clause = new IntegerClause<>(IntegerClause.Type.AND);

    for (final MmuBinding binding : bindings) {
      final IntegerField lhs = binding.getLhs();
      final MmuExpression rhs = binding.getRhs();

      final IntegerVariable lhsVar = getPathVar(lhs.getVariable(), pathIndex);
      variables.add(lhsVar);

      if (rhs != null) {
        int offset = lhs.getLoIndex();

        for (final IntegerField term : rhs.getTerms()) {
          final IntegerField field = getPathField(term, pathIndex);

          final int lo = offset;
          final int hi = offset + (field.getWidth() - 1);

          clause.addEquation(new IntegerField(lhsVar, lo, hi), field, true);
          offset += field.getWidth();

          variables.add(field.getVariable());
        }

        if (offset <= lhs.getHiIndex()) {
          final int lo = offset;
          final int hi = lhs.getHiIndex();
  
          clause.addEquation(new IntegerField(lhsVar, lo, hi), BigInteger.ZERO, true);
        }
      }
    } // for binding.

    if (clause.size() != 0) {
      formula.addClause(clause);
    }
  }

  private IntegerVariable getPathVar(final IntegerVariable var, final int pathIndex) {
    InvariantChecks.checkNotNull(var);

    if (pathIndex == -1) {
      return var;
    }

    final String pathVarName = getVarName(var.getName(), pathIndex);

    IntegerVariable pathVar = varCache.get(pathVarName);
    if (pathVar == null) {
      varCache.put(pathVarName, pathVar = new IntegerVariable(pathVarName, var.getWidth()));
    }

    return pathVar;
  }

  private IntegerField getPathField(final IntegerField field, final int pathIndex) {
    InvariantChecks.checkNotNull(field);

    if (pathIndex == -1) {
      return field;
    }

    final IntegerVariable var = getPathVar(field.getVariable(), pathIndex);

    final int lo = field.getLoIndex();
    final int hi = field.getHiIndex();

    return new IntegerField(var, lo, hi);
  }
}
