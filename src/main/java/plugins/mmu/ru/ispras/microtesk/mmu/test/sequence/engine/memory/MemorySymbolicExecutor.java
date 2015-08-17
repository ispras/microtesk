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
import java.util.LinkedHashSet;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerClause;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerFormula;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAssignment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuConditionAtom;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;
import ru.ispras.microtesk.utils.BitUtils;

/**
 * {@link MemorySymbolicExecutor} implements a simple symbolic executor of memory accesses.
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

  public static MemorySymbolicExecutor instance = null;

  public MemorySymbolicExecutor get() {
    if (instance == null) {
      instance = new MemorySymbolicExecutor();
    }

    return instance;
  }

  private MemorySymbolicExecutor() {}

  public Result execute(final MemoryAccessPath path) {
    InvariantChecks.checkNotNull(path);

    final Collection<IntegerVariable> variables = new LinkedHashSet<>();
    final IntegerFormula<IntegerField> formula = new IntegerFormula<>();

    for (final MmuTransition transition : path.getTransitions()) {
      final MmuGuard guard = transition.getGuard();

      if (guard != null) {
        final MmuCondition condition = guard.getCondition();

        if (condition != null) {
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
              final int hi = (offset + term.getWidth()) - 1;

              final BigInteger value = BitUtils.getField(constant, lo, hi);

              clause.addEquation(term, value, !atom.isNegated());
              offset += term.getWidth();

              variables.add(term.getVariable());
            }

            formula.addEquationClause(clause);
          }
        }
      }

      final MmuAction action = transition.getTarget();

      if (action != null) {
        final Map<IntegerField, MmuAssignment> assignments = action.getAction();

        if (assignments != null) {
          final IntegerClause<IntegerField> clause = new IntegerClause<>(IntegerClause.Type.AND);

          for (final MmuAssignment assignment : assignments.values()) {
            final IntegerField lhs = assignment.getLhs();
            final MmuExpression rhs = assignment.getRhs();

            final IntegerVariable lhsVar = lhs.getVariable();
            variables.add(lhsVar);

            int offset = lhs.getLoIndex();

            for (final IntegerField term : rhs.getTerms()) {
              final int lo = offset;
              final int hi = (offset + term.getWidth()) - 1;

              clause.addEquation(new IntegerField(lhsVar, lo, hi), term, true);
              offset += term.getWidth();

              variables.add(term.getVariable());
            }

            if (offset <= lhs.getHiIndex()) {
              final int lo = offset;
              final int hi = lhs.getHiIndex();

              clause.addEquation(new IntegerField(lhsVar, lo, hi), BigInteger.ZERO, true);
            }
          }
        }
      }
    }

    return new Result(variables, formula);
  }
}
