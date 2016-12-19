/*
 * Copyright 2015-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.symbolic;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.integer.IntegerClause;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.basis.MemoryAccessStack;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessStructure;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryDependency;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryHazard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBinding;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCalculator;
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
  private final MemorySymbolicResult result;

  public MemorySymbolicExecutor(final MemorySymbolicResult result) {
    InvariantChecks.checkNotNull(result);
    this.result = result;
  }

  public MemorySymbolicExecutor() {
    this(new MemorySymbolicResult());
  }

  public MemorySymbolicResult execute(final MemoryAccessPath.Entry entry, final boolean finalize) {
    InvariantChecks.checkNotNull(entry);

    execute(entry);

    if (finalize) {
      includeOriginalVariables();
    }

    return result;
  }

  public MemorySymbolicResult execute(final MemoryAccessPath path, final boolean finalize) {
    InvariantChecks.checkNotNull(path);

    execute(path);

    if (finalize) {
      includeOriginalVariables();
    }

    return result;
  }

  public MemorySymbolicResult execute(final MemoryAccessStructure structure, final boolean finalize) {
    InvariantChecks.checkNotNull(structure);
    InvariantChecks.checkGreaterThanZero(structure.size());

    execute(structure);

    if (finalize) {
      includeOriginalVariables();
    }

    return result;
  }

  private void includeOriginalVariables() {
    // Add the constraints of the kind V = V(n), where n is the last instance number of V.
    result.addVariables(result.getOriginalVariables());

    for (final IntegerVariable original : result.getOriginalVariables()) {
      final IntegerVariable instance = result.getPathVarInstance(original.getName());

      final IntegerField lhs = new IntegerField(original);
      final IntegerField rhs = new IntegerField(instance);

      result.addEquation(lhs, rhs);

      // Propagate the constant if applicable.
      final BigInteger constant = result.getConstant(instance);

      if (constant != null) {
        result.addConstant(original, constant);
      }
    }
  }

  private void execute(final MemoryAccessStructure structure) {
    for (int j = 0; j < structure.size(); j++) {
      final MemoryAccessPath path2 = structure.getAccess(j).getPath();

      for (int i = 0; i < j; i++) {
        final MemoryAccessPath path1 = structure.getAccess(i).getPath();
        final MemoryDependency dependency = structure.getDependency(i, j);

        if (dependency != null) {
          // It does not execute the paths (only the dependency).
          execute(path1, i, path2, j, dependency);
        }
      }

      execute(path2, j);
    }
  }

  private void execute(final MemoryAccessPath path) {
    execute(path, -1);
  }

  private void execute(final MemoryAccessPath.Entry entry) {
    execute(entry, -1);
  }

  private void execute(final MemoryAccessPath path, final int pathIndex) {
    for (final MemoryAccessPath.Entry entry : path.getEntries()) {
      if (result.hasConflict()) {
        return;
      }

      execute(entry, pathIndex);
    }
  }

  private void execute(
      final MemoryAccessPath path1,
      final int pathIndex1,
      final MemoryAccessPath path2,
      final int pathIndex2,
      final MemoryDependency dependency) {
    for (final MemoryHazard hazard : dependency.getHazards()) {
      if (result.hasConflict()) {
        return;
      }

      execute(hazard, pathIndex1, pathIndex2);
    }
  }

  private void execute(final MemoryHazard hazard, final int pathIndex1, final int pathIndex2) {
    if (result.hasConflict()) {
      return;
    }

    final MmuCondition condition = hazard.getCondition();
    if (condition != null) {
      execute(condition, pathIndex1, pathIndex2);
    }
  }

  private void execute(final MmuCondition condition, final int pathIndex1, final int pathIndex2) {
    if (result.hasConflict()) {
      return;
    }

    final MemoryAccessStack stack1 = result.getStack(pathIndex1);
    final MemoryAccessStack stack2 = result.getStack(pathIndex2);

    final IntegerClause.Type clauseType = (condition.getType() == MmuCondition.Type.AND)
        ? IntegerClause.Type.AND
        : IntegerClause.Type.OR;

    final IntegerClause.Builder<IntegerField> clauseBuilder =
        new IntegerClause.Builder<>(clauseType);

    for (final MmuConditionAtom atom : condition.getAtoms()) {
      if (atom.getType() != MmuConditionAtom.Type.EQ_SAME_EXPR) {
        continue;
      }

      final MmuExpression expression = atom.getLhsExpr();

      for (final IntegerField term : expression.getTerms()) {
        final IntegerField term1 = stack1.getInstance(term);
        final IntegerField term2 = stack2.getInstance(term);

        final IntegerField field1 = result.getPathFieldInstance(term1, pathIndex1);
        final IntegerField field2 = result.getPathFieldInstance(term2, pathIndex2);

        clauseBuilder.addEquation(field1, field2, !atom.isNegated());

        result.addVariable(field1.getVariable());
        result.addOriginalVariable(result.getPathVar(term1.getVariable(), pathIndex1));

        result.addVariable(field2.getVariable());
        result.addOriginalVariable(result.getPathVar(term2.getVariable(), pathIndex2));
      }
    }

    result.addClause(clauseBuilder.build());
  }

  private void execute(final MemoryAccessPath.Entry entry, final int pathIndex) {
    if (result.hasConflict()) {
      return;
    }

    result.updateStack(entry, pathIndex);

    if (entry.isCall()) {
      final MmuAddressInstance formalArg = entry.getFormalArg();
      final MmuAddressInstance actualArg = entry.getActualArg();

      execute(formalArg.bindings(actualArg), pathIndex);
    }

    final MmuTransition transition = entry.getTransition();

    if (transition != null) {
      final MmuGuard guard = transition.getGuard();

      if (guard != null) {
        execute(guard, pathIndex);
      }

      final MmuAction action = transition.getTarget();

      if (action != null) {
        execute(action, pathIndex);
      }
    }
  }

  private void execute(final MmuGuard guard, final int pathIndex) {
    if (result.hasConflict()) {
      return;
    }

    final MemoryAccessStack stack = result.getStack(pathIndex);

    final MmuBufferAccess bufferAccess = guard.getBufferAccess(stack);
    if (bufferAccess != null) {
      execute(bufferAccess, pathIndex);
    }

    final MmuCondition condition = guard.getCondition(stack);
    if (condition != null) {
      execute(condition, pathIndex);
    }
  }

  private void execute(final MmuAction action, final int pathIndex) {
    final MemoryAccessStack stack = result.getStack(pathIndex);

    final Map<IntegerField, MmuBinding> assignments = action.getAction(stack);
    if (assignments != null) {
      execute(assignments.values(), pathIndex);
    }
  }

  private void execute(final MmuBufferAccess bufferAccess, final int pathIndex) {
    if (result.hasConflict()) {
      return;
    }

    final MemoryAccessStack stack = result.getStack(pathIndex);
    execute(bufferAccess.getBuffer().getMatchBindings(stack), pathIndex);
  }

  private void execute(final MmuCondition condition, final int pathIndex) {
    final IntegerClause.Type definedType = condition.getType() == MmuCondition.Type.AND ?
        IntegerClause.Type.AND : IntegerClause.Type.OR;

    final IntegerClause.Builder<IntegerField> clauseBuilder =
        new IntegerClause.Builder<>(definedType);

    for (final MmuConditionAtom atom : condition.getAtoms()) {
      if (result.hasConflict()) {
        return;
      }

      execute(clauseBuilder, atom, pathIndex);
    }

    if (clauseBuilder.size() != 0) {
      result.addClause(clauseBuilder.build());
    }
  }

  private void execute(
      final IntegerClause.Builder<IntegerField> clauseBuilder,
      final MmuConditionAtom atom,
      final int pathIndex) {

    if (result.hasConflict()) {
      return;
    }

    final MmuExpression lhsExpr = atom.getLhsExpr();

    switch(atom.getType()) {
      case EQ_EXPR_CONST:
        boolean isTrue = false;

        final BigInteger rhsConst = atom.getRhsConst();

        int offset = 0;
        for (final IntegerField term : lhsExpr.getTerms()) {
          final int lo = offset;
          final int hi = offset + (term.getWidth() - 1);

          final IntegerField field = result.getPathFieldInstance(term, pathIndex);
          final BigInteger value = BitUtils.getField(rhsConst, lo, hi);

          // Check whether the field's value is known (via constant propagation).
          final BigInteger constant = result.getConstants().get(field.getVariable());
  
          if (constant != null) {
            final int fieldLo = field.getLoIndex();
            final int fieldHi = field.getHiIndex();

            final BigInteger fieldConst = BitUtils.getField(constant, fieldLo, fieldHi);

            final boolean truthValue = (value.equals(fieldConst) != atom.isNegated());

            if (!truthValue && clauseBuilder.getType() == IntegerClause.Type.AND) {
              Logger.debug("Condition is always FALSE: %s %s %s",
                  field, (atom.isNegated() ? "!=" : "=="), fieldConst);

              result.setConflict(true);
              return;
            }

            if (truthValue && clauseBuilder.getType() == IntegerClause.Type.OR) {
              Logger.debug("Condition is always TRUE: %s %s %s",
                  field, (atom.isNegated() ? "!=" : "=="), fieldConst);

              // Formally, the empty OR clause is false, but it is simply ignored.
              clauseBuilder.clear();
              isTrue = true;
            }
          }

          if (!isTrue && constant == null) {
            clauseBuilder.addEquation(field, value, !atom.isNegated());
          }

          offset += term.getWidth();

          result.addVariable(field.getVariable());
          result.addOriginalVariable(result.getPathVar(term.getVariable(), pathIndex));
        }
        break;

      case EQ_EXPR_EXPR:
        final MmuExpression rhsExpr = atom.getRhsExpr();
        InvariantChecks.checkTrue(lhsExpr.size() == rhsExpr.size());

        for (int i = 0; i < lhsExpr.size(); i++) {
          final IntegerField lhsTerm = lhsExpr.getTerms().get(i);
          final IntegerField rhsTerm = rhsExpr.getTerms().get(i);
          InvariantChecks.checkTrue(lhsTerm.getWidth() == rhsTerm.getWidth());

          final IntegerField lhsField = result.getPathFieldInstance(lhsTerm, pathIndex);
          final IntegerField rhsField = result.getPathFieldInstance(rhsTerm, pathIndex);

          clauseBuilder.addEquation(lhsField, rhsField, !atom.isNegated());

          result.addVariable(lhsField.getVariable());
          result.addVariable(rhsField.getVariable());

          result.addOriginalVariable(result.getPathVar(lhsTerm.getVariable(), pathIndex));
          result.addOriginalVariable(result.getPathVar(rhsTerm.getVariable(), pathIndex));
        }
        break;

      default:
        InvariantChecks.checkTrue(false);
        break;
    }
  }

  private void execute(final Collection<MmuBinding> bindings, final int pathIndex) {
    if (result.hasConflict()) {
      return;
    }

    final IntegerClause.Builder<IntegerField> clauseBuilder =
        new IntegerClause.Builder<>(IntegerClause.Type.AND);

    for (final MmuBinding binding : bindings) {
      final IntegerField lhs = binding.getLhs();
      final MmuExpression rhs = binding.getRhs();

      final IntegerVariable oldLhsVar = result.getPathVarInstance(lhs.getVariable(), pathIndex);

      result.addVariable(oldLhsVar);
      result.addOriginalVariable(result.getPathVar(lhs.getVariable(), pathIndex));

      if (rhs != null) {
        final IntegerVariable newLhsVar = result.getNextPathVarInstance(lhs.getVariable(), pathIndex);
        result.addVariable(newLhsVar);

        final List<IntegerField> rhsTerms = new ArrayList<>();

        // Equation of the prefix part.
        if (lhs.getLoIndex() > 0) {
          final IntegerField oldLhsPre = new IntegerField(oldLhsVar, 0, lhs.getLoIndex() - 1);
          final IntegerField newLhsPre = new IntegerField(newLhsVar, 0, lhs.getLoIndex() - 1);

          clauseBuilder.addEquation(newLhsPre, oldLhsPre, true);
          rhsTerms.add(oldLhsPre);
        }

        int offset = lhs.getLoIndex();

        // Equation of the middle part.
        for (final IntegerField term : rhs.getTerms()) {
          final IntegerField field = result.getPathFieldInstance(term, pathIndex);

          final int lo = offset;
          final int hi = offset + (field.getWidth() - 1);

          clauseBuilder.addEquation(new IntegerField(newLhsVar, lo, hi), field, true);
          rhsTerms.add(field);

          offset += field.getWidth();

          result.addVariable(field.getVariable());
          result.addOriginalVariable(result.getPathVar(term.getVariable(), pathIndex));
        }

        if (offset <= lhs.getHiIndex()) {
          final int lo = offset;
          final int hi = lhs.getHiIndex();
  
          clauseBuilder.addEquation(new IntegerField(newLhsVar, lo, hi), BigInteger.ZERO, true);
        }

        // Equation of the suffix part.
        if (lhs.getHiIndex() < lhs.getWidth() - 1) {
          final IntegerField oldLhsPost = new IntegerField(oldLhsVar, lhs.getHiIndex() + 1, lhs.getWidth() - 1);
          final IntegerField newLhsPost = new IntegerField(newLhsVar, lhs.getHiIndex() + 1, lhs.getWidth() - 1);

          clauseBuilder.addEquation(newLhsPost, oldLhsPost, true);
          rhsTerms.add(oldLhsPost);
        }

        result.definePathVarInstance(lhs.getVariable(), pathIndex);

        // Try to propagate constants.
        final MmuExpression rhsExpr = MmuExpression.cat(rhsTerms);
        final BigInteger constant = MmuCalculator.eval(rhsExpr, result.getConstants(), false);

        if (constant != null) {
          Logger.debug("Constant propagation: %s == 0x%s", newLhsVar, constant.toString(16));
          result.getConstants().put(newLhsVar, constant);
        }
      } // if right-hand side exists.
    } // for each binding.

    if (clauseBuilder.size() != 0) {
      result.addClause(clauseBuilder.build());
    }
  }
}
