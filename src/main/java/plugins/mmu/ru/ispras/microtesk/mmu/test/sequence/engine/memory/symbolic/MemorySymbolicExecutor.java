/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.integer.IntegerClause;
import ru.ispras.microtesk.basis.solver.integer.IntegerConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerEquation;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerFormula;
import ru.ispras.microtesk.basis.solver.integer.IntegerFormulaBuilder;
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
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuProgram;
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

  public MemorySymbolicResult getResult() {
    return result;
  }

  public void execute(final IntegerConstraint<IntegerField> constraint) {
    InvariantChecks.checkNotNull(constraint);
    executeFormula(result, null, constraint.getFormula(), -1);
  }

  public void execute(final MemoryAccessPath.Entry entry) {
    InvariantChecks.checkNotNull(entry);
    executeEntry(result, null, entry, -1);
  }

  public void execute(final MemoryAccessPath path, final boolean finalize) {
    InvariantChecks.checkNotNull(path);

    executePath(result, null, path, -1);

    if (finalize) {
      result.includeOriginalVariables();
    }
  }

  public void execute(final MemoryAccessStructure structure, final boolean finalize) {
    InvariantChecks.checkNotNull(structure);
    InvariantChecks.checkGreaterThanZero(structure.size());

    executeStructure(result, null, structure);

    if (finalize) {
      result.includeOriginalVariables();
    }
  }

  private void executeStructure(
      final MemorySymbolicResult result,
      final Set<IntegerVariable> defines,
      final MemoryAccessStructure structure) {

    for (int j = 0; j < structure.size(); j++) {
      final MemoryAccessPath path2 = structure.getAccess(j).getPath();

      for (int i = 0; i < j; i++) {
        final MemoryAccessPath path1 = structure.getAccess(i).getPath();
        final MemoryDependency dependency = structure.getDependency(i, j);

        if (dependency != null) {
          // It does not execute the paths (only the dependency).
          executeDependency(result, defines, path1, i, path2, j, dependency);
        }
      }

      executePath(result, defines, path2, j);
    }
  }

  private void executePath(
      final MemorySymbolicResult result,
      final Set<IntegerVariable> defines,
      final MemoryAccessPath path,
      final int pathIndex) {

    for (final MemoryAccessPath.Entry entry : path.getEntries()) {
      if (result.hasConflict()) {
        return;
      }

      executeEntry(result, defines, entry, pathIndex);
    }
  }

  private void executeDependency(
      final MemorySymbolicResult result,
      final Set<IntegerVariable> defines,
      final MemoryAccessPath path1,
      final int pathIndex1,
      final MemoryAccessPath path2,
      final int pathIndex2,
      final MemoryDependency dependency) {

    for (final MemoryHazard hazard : dependency.getHazards()) {
      if (result.hasConflict()) {
        return;
      }

      executeHazard(result, defines, hazard, pathIndex1, pathIndex2);
    }
  }

  private void executeHazard(
      final MemorySymbolicResult result, 
      final Set<IntegerVariable> defines,
      final MemoryHazard hazard,
      final int pathIndex1,
      final int pathIndex2) {

    if (result.hasConflict()) {
      return;
    }

    final MmuCondition condition = hazard.getCondition();
    if (condition != null) {
      executeCondition(result, defines, condition, pathIndex1, pathIndex2);
    }
  }

  private void executeCondition(
      final MemorySymbolicResult result,
      final Set<IntegerVariable> defines,
      final MmuCondition condition,
      final int pathIndex1,
      final int pathIndex2) {

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

        final IntegerField field1 = result.getVersion(term1, pathIndex1);
        final IntegerField field2 = result.getVersion(term2, pathIndex2);

        clauseBuilder.addEquation(field1, field2, !atom.isNegated());

        result.addOriginalVariable(result.getOriginal(term1.getVariable(), pathIndex1));
        result.addOriginalVariable(result.getOriginal(term2.getVariable(), pathIndex2));
      }
    }

    result.addClause(clauseBuilder.build());
  }

  private void executeFormula(
      final MemorySymbolicResult result,
      final Set<IntegerVariable> defines,
      final IntegerFormula<IntegerField> formula,
      final int pathIndex) {

    for (final IntegerClause<IntegerField> clause : formula.getClauses()) {
      final IntegerClause.Builder<IntegerField> clauseBuilder =
          new IntegerClause.Builder<>(clause.getType());

      for (final IntegerEquation<IntegerField> equation : clause.getEquations()) {
        final IntegerVariable variable = equation.lhs.getVariable();

        // Do not add the assertion if there are no corresponding variables.
        if (!result.containsOriginalVariable(result.getOriginal(variable, pathIndex))) {
          if (clause.getType() == IntegerClause.Type.OR) {
            clauseBuilder.clear();
            break;
          } else {
            continue;
          }
        }

        if (equation.val == null) {
          clauseBuilder.addEquation(
              result.getVersion(equation.lhs, pathIndex),
              result.getVersion(equation.rhs, pathIndex), equation.equal);
        } else {
          clauseBuilder.addEquation(
              result.getVersion(equation.lhs, pathIndex),
              equation.val, equation.equal);
        }
      }

      if (!clauseBuilder.isEmpty()) {
        result.addClause(clauseBuilder.build());
      }
    }
  }

  private void executeEntry(
      final MemorySymbolicResult result,
      final Set<IntegerVariable> defines,
      final MemoryAccessPath.Entry entry,
      final int pathIndex) {

    if (result.hasConflict()) {
      return;
    }

    final MmuProgram program = entry.getProgram();

    if (entry.isCall()) {
      InvariantChecks.checkTrue(program.isAtomic());

      final MmuTransition transition = program.getTransition();
      final MmuAction action = transition.getTarget();

      final MmuBufferAccess oldBufferAccess = action.getBufferAccess(result.getStack(pathIndex));
      final MmuAddressInstance actualArg = oldBufferAccess.getArgument();

      result.updateStack(entry, pathIndex);

      final MmuBufferAccess newBufferAccess = action.getBufferAccess(result.getStack(pathIndex));
      final MmuAddressInstance formalArg = newBufferAccess.getAddress();

      final Collection<MmuBinding> bindings = formalArg.bindings(actualArg);
      Logger.debug("Bindings: %s", bindings);

      executeBindings(result, defines, bindings, pathIndex);
    } else {
      result.updateStack(entry, pathIndex);

      if (entry.isNormal()) {
        executeProgram(result, defines, program, pathIndex);
      }
    }
  }

  private void executeProgram(
      final MemorySymbolicResult result,
      final Set<IntegerVariable> defines,
      final MmuProgram program,
      final int pathIndex) {

    if (result.hasConflict()) {
      return;
    }

    if (program.isAtomic()) {
      executeTransition(result, defines, program.getTransition(), pathIndex);
    } else {
      for (final Collection<MmuProgram> statement : program.getStatements()) {
        executeStatement(result, defines, statement, pathIndex);
      }
    }
  }

  private void executeStatement(
      final MemorySymbolicResult result,
      final Set<IntegerVariable> defines,
      final Collection<MmuProgram> statement,
      final int pathIndex) {

    if (result.hasConflict()) {
      return;
    }

    if (statement.isEmpty()) {
      result.setConflict(true);
      return;
    }

    if (statement.size() == 1) {
      executeProgram(result, defines, statement.iterator().next(), pathIndex);
      return;
    }

    final List<MemorySymbolicResult> switchResults = new ArrayList<>(statement.size());
    final List<Set<IntegerVariable>> switchDefines = new ArrayList<>(statement.size());

    for (final MmuProgram program : statement) {
      final IntegerFormulaBuilder<IntegerField> caseBuilder = new IntegerFormula.Builder<>();
      final MemorySymbolicResult caseResult = new MemorySymbolicResult(caseBuilder, result);
      final Set<IntegerVariable> caseDefines = new LinkedHashSet<>();

      executeProgram(caseResult, caseDefines, program, pathIndex);

      if (!caseResult.hasConflict()) {
        switchResults.add(caseResult);
        switchDefines.add(caseDefines);
      }
    }

    if (switchResults.isEmpty()) {
      result.setConflict(true);
      return;
    }

    final Set<IntegerVariable> allDefines = new LinkedHashSet<>();

    for (final Set<IntegerVariable> caseDefines : switchDefines) {
      allDefines.addAll(caseDefines);
    }

    if (defines != null) {
      defines.addAll(allDefines);
    }

    // Construct PHI functions.
    for (final IntegerVariable originalVariable : allDefines) {
      final List<Integer> indices = new ArrayList<>(switchDefines.size());

      int maxVersionNumber = 0;

      for (int i = 0; i < switchDefines.size(); i++) {
        final Set<IntegerVariable> caseDefines = switchDefines.get(i);

        if (caseDefines.contains(originalVariable)) {
          final MemorySymbolicResult caseResult = switchResults.get(i);
          final int versionNumber = caseResult.getVersionNumber(originalVariable);

          if (versionNumber > maxVersionNumber) {
            maxVersionNumber = versionNumber;
          }

          indices.add(i);
        }
      }

      for (final int i : indices) {
        final MemorySymbolicResult caseResult = switchResults.get(i);
        final int versionNumber = caseResult.getVersionNumber(originalVariable);

        if (versionNumber < maxVersionNumber) {
          final IntegerVariable oldVersion = caseResult.getVersion(originalVariable);
          caseResult.setVersionNumber(originalVariable, maxVersionNumber);
          final IntegerVariable newVersion = caseResult.getVersion(originalVariable);

          caseResult.addEquation(new IntegerField(newVersion), new IntegerField(oldVersion));
        }
      }

      result.setVersionNumber(originalVariable, maxVersionNumber);
    }

    // Join the control flows.
    final int width = getWidth(statement.size());
    final IntegerField phi = getPhiField(width);

    final IntegerClause.Builder<IntegerField> switchBuilder =
        new IntegerClause.Builder<>(IntegerClause.Type.OR);

    // Switch: (PHI == 0) | ... | (PHI == N-1)
    for (int i = 0; i < switchResults.size(); i++) {
      switchBuilder.addEquation(phi, BigInteger.valueOf(i), true);
    }

    result.addClause(switchBuilder.build());

    for (int i = 0; i < switchResults.size(); i++) {
      final MemorySymbolicResult caseResult = switchResults.get(i);
      final IntegerFormula.Builder<IntegerField> caseBuilder =
          (IntegerFormula.Builder<IntegerField>) caseResult.getBuilder();
      final IntegerFormula<IntegerField> caseFormula = caseBuilder.build();

      // Case: (PHI == i) -> CASE(i).
      result.addFormula(getIfThenFormula(phi, i, caseFormula));
    }
  }

  private void executeTransition(
      final MemorySymbolicResult result,
      final Set<IntegerVariable> defines,
      final MmuTransition transition,
      final int pathIndex) {

    if (result.hasConflict()) {
      return;
    }

    final MmuGuard guard = transition.getGuard();

    if (guard != null) {
      executeGuard(result, defines, guard, pathIndex);
    }

    final MmuAction action = transition.getTarget();

    if (action != null) {
      executeAction(result, defines, action, pathIndex);
    }
  }

  private void executeGuard(
      final MemorySymbolicResult result,
      final Set<IntegerVariable> defines,
      final MmuGuard guard,
      final int pathIndex) {

    if (result.hasConflict()) {
      return;
    }

    final MemoryAccessStack stack = result.getStack(pathIndex);

    final MmuBufferAccess bufferAccess = guard.getBufferAccess(stack);
    if (bufferAccess != null) {
      executeBufferAccess(result, defines, bufferAccess, pathIndex);
    }

    final MmuCondition condition = guard.getCondition(stack);
    if (condition != null) {
      executeCondition(result, defines, condition, pathIndex);
    }
  }

  private void executeAction(
      final MemorySymbolicResult result,
      final Set<IntegerVariable> defines,
      final MmuAction action,
      final int pathIndex) {

    if (result.hasConflict()) {
      return;
    }

    final MemoryAccessStack stack = result.getStack(pathIndex);

    final Map<IntegerField, MmuBinding> assignments = action.getAction(stack);
    if (assignments != null) {
      executeBindings(result, defines, assignments.values(), pathIndex);
    }
  }

  private void executeBufferAccess(
      final MemorySymbolicResult result,
      final Set<IntegerVariable> defines,
      final MmuBufferAccess bufferAccess,
      final int pathIndex) {

    if (result.hasConflict()) {
      return;
    }

    final MemoryAccessStack stack = result.getStack(pathIndex);
    executeBindings(result, defines, bufferAccess.getBuffer().getMatchBindings(stack), pathIndex);
  }

  private void executeCondition(
      final MemorySymbolicResult result,
      final Set<IntegerVariable> defines,
      final MmuCondition condition,
      final int pathIndex) {

    if (result.hasConflict()) {
      return;
    }

    final IntegerClause.Type definedType = condition.getType() == MmuCondition.Type.AND ?
        IntegerClause.Type.AND : IntegerClause.Type.OR;

    final IntegerClause.Builder<IntegerField> clauseBuilder =
        new IntegerClause.Builder<>(definedType);

    for (final MmuConditionAtom atom : condition.getAtoms()) {
      if (result.hasConflict()) {
        return;
      }

      executeConditionAtom(result, defines, clauseBuilder, atom, pathIndex);
    }

    if (clauseBuilder.size() != 0) {
      result.addClause(clauseBuilder.build());
    }
  }

  private void executeConditionAtom(
      final MemorySymbolicResult result,
      final Set<IntegerVariable> defines,
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

          final IntegerField field = result.getVersion(term, pathIndex);
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

          result.addOriginalVariable(result.getOriginal(term.getVariable(), pathIndex));
        }
        break;

      case EQ_EXPR_EXPR:
        final MmuExpression rhsExpr = atom.getRhsExpr();
        InvariantChecks.checkTrue(lhsExpr.size() == rhsExpr.size());

        for (int i = 0; i < lhsExpr.size(); i++) {
          final IntegerField lhsTerm = lhsExpr.getTerms().get(i);
          final IntegerField rhsTerm = rhsExpr.getTerms().get(i);
          InvariantChecks.checkTrue(lhsTerm.getWidth() == rhsTerm.getWidth());

          final IntegerField lhsField = result.getVersion(lhsTerm, pathIndex);
          final IntegerField rhsField = result.getVersion(rhsTerm, pathIndex);

          clauseBuilder.addEquation(lhsField, rhsField, !atom.isNegated());

          result.addOriginalVariable(result.getOriginal(lhsTerm.getVariable(), pathIndex));
          result.addOriginalVariable(result.getOriginal(rhsTerm.getVariable(), pathIndex));
        }
        break;

      default:
        InvariantChecks.checkTrue(false);
        break;
    }
  }

  private void executeBindings(
      final MemorySymbolicResult result,
      final Set<IntegerVariable> defines,
      final Collection<MmuBinding> bindings,
      final int pathIndex) {

    if (result.hasConflict()) {
      return;
    }

    final IntegerClause.Builder<IntegerField> clauseBuilder =
        new IntegerClause.Builder<>(IntegerClause.Type.AND);

    for (final MmuBinding binding : bindings) {
      final IntegerField lhs = binding.getLhs();
      final MmuExpression rhs = binding.getRhs();

      final IntegerVariable oldLhsVar = result.getVersion(lhs.getVariable(), pathIndex);
      final IntegerVariable lhsOriginal = result.getOriginal(lhs.getVariable(), pathIndex);

      result.addOriginalVariable(lhsOriginal);

      if (defines != null) {
        defines.add(lhsOriginal);
      }

      if (rhs != null) {
        final IntegerVariable newLhsVar = result.getNextVersion(lhs.getVariable(), pathIndex);

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
          final IntegerField field = result.getVersion(term, pathIndex);

          final int lo = offset;
          final int hi = offset + (field.getWidth() - 1);

          clauseBuilder.addEquation(new IntegerField(newLhsVar, lo, hi), field, true);
          rhsTerms.add(field);

          offset += field.getWidth();

          result.addOriginalVariable(result.getOriginal(term.getVariable(), pathIndex));
        }

        if (offset <= lhs.getHiIndex()) {
          final int lo = offset;
          final int hi = lhs.getHiIndex();
  
          clauseBuilder.addEquation(new IntegerField(newLhsVar, lo, hi), BigInteger.ZERO, true);
        }

        // Equation of the suffix part.
        if (lhs.getHiIndex() < lhs.getWidth() - 1) {
          final IntegerField oldLhsPost =
              new IntegerField(oldLhsVar, lhs.getHiIndex() + 1, lhs.getWidth() - 1);
          final IntegerField newLhsPost =
              new IntegerField(newLhsVar, lhs.getHiIndex() + 1, lhs.getWidth() - 1);

          clauseBuilder.addEquation(newLhsPost, oldLhsPost, true);
          rhsTerms.add(oldLhsPost);
        }

        result.defineVersion(lhs.getVariable(), pathIndex);

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

  private static int uniqueId = 0;

  private static int getWidth(final int size) {
    int width = 0;
    int value = size;

    while ((value >>= 1) != 0) {
      width++;
    }

    if (((size - 1) & size) != 0) {
      width++;
    }

    return width;
  }

  private static IntegerField getPhiField(final int width) {
    final IntegerVariable var = new IntegerVariable(String.format("phi_%d", uniqueId++), width);
    return new IntegerField(var);
  }

  private static IntegerField getIfThenField(final IntegerVariable phi, final int i) {
    final IntegerVariable var = new IntegerVariable(String.format("%s_%d", phi.getName(), i), 1);
    return new IntegerField(var);
  }

  private static IntegerFormula<IntegerField> getIfThenFormula(
      final IntegerField phi, final int i, final IntegerFormula<IntegerField> formula) {
    final IntegerFormula.Builder<IntegerField> ifThenBuilder = new IntegerFormula.Builder<>();

    final IntegerClause.Builder<IntegerField> clauseBuilder1 =
        new IntegerClause.Builder<>(IntegerClause.Type.OR);
    final IntegerClause.Builder<IntegerField> clauseBuilder2 =
        new IntegerClause.Builder<>(IntegerClause.Type.OR);

    // Introduce a Boolean variable: C == (PHI == i).
    final IntegerField condition = getIfThenField(phi.getVariable(), i);

    clauseBuilder1.addEquation(condition, BigInteger.valueOf(1), true);
    clauseBuilder1.addEquation(phi, BigInteger.valueOf(i), false);

    clauseBuilder2.addEquation(condition, BigInteger.valueOf(1), false);
    clauseBuilder2.addEquation(phi, BigInteger.valueOf(i), true);

    ifThenBuilder.addClause(clauseBuilder1.build());
    ifThenBuilder.addClause(clauseBuilder2.build());

    // Transform the formula according to the rule:
    // C -> (x[1] & ... & x[n]) == (~C | x[1]) & ... & (~C | x[n]).
    for (final IntegerClause<IntegerField> clause : formula.getClauses()) {
      if (clause.getType() == IntegerClause.Type.OR) {
        final IntegerClause.Builder<IntegerField> clauseBuilder =
            new IntegerClause.Builder<>(IntegerClause.Type.OR);

        clauseBuilder.addEquation(condition, BigInteger.valueOf(1), false);
        clauseBuilder.addClause(clause);

        ifThenBuilder.addClause(clauseBuilder.build());
      } else {
        for (final IntegerEquation<IntegerField> equation : clause.getEquations()) {
          final IntegerClause.Builder<IntegerField> clauseBuilder =
              new IntegerClause.Builder<>(IntegerClause.Type.OR);

          clauseBuilder.addEquation(condition, BigInteger.valueOf(1), false);
          clauseBuilder.addEquation(equation);

          ifThenBuilder.addClause(clauseBuilder.build());
        }
      }
    }

    return ifThenBuilder.build();
  }
}
