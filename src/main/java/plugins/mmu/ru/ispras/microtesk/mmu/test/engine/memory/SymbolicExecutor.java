/*
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

package ru.ispras.microtesk.mmu.test.engine.memory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerClause;
import ru.ispras.microtesk.basis.solver.integer.IntegerConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerDomainConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerEquation;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerFormula;
import ru.ispras.microtesk.basis.solver.integer.IntegerFormulaBuilder;
import ru.ispras.microtesk.basis.solver.integer.IntegerRange;
import ru.ispras.microtesk.basis.solver.integer.IntegerRangeConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.DataType;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;
import ru.ispras.microtesk.mmu.basis.MemoryAccessStack;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBinding;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCalculator;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuConditionAtom;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuProgram;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSegment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuStruct;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;
import ru.ispras.microtesk.utils.function.Function;

/**
 * {@link SymbolicExecutor} implements a simple symbolic executor of memory access structures.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class SymbolicExecutor {
  private final SymbolicRestrictor restrictor;
  private final SymbolicResult result;

  public SymbolicExecutor(
      final SymbolicRestrictor restrictor,
      final SymbolicResult result) {
    InvariantChecks.checkNotNull(restrictor);
    InvariantChecks.checkNotNull(result);

    this.restrictor = restrictor;
    this.result = result;
  }

  public SymbolicExecutor(final SymbolicResult result) {
    InvariantChecks.checkNotNull(result);

    this.restrictor = null;
    this.result = result;
  }

  public SymbolicResult getResult() {
    return result;
  }

  public Boolean execute(final DataType dataType) {
    InvariantChecks.checkNotNull(dataType);
    return executeAlignment(result, null, dataType, -1);
  }

  public Boolean execute(final IntegerConstraint<IntegerField> constraint) {
    InvariantChecks.checkNotNull(constraint);
    return executeFormula(result, null, constraint.getFormula(), -1);
  }

  public Boolean execute(final MmuCondition condition) {
    InvariantChecks.checkNotNull(condition);
    return executeCondition(result, null, condition, -1);
  }

  public Boolean execute(final AccessPath.Entry entry) {
    InvariantChecks.checkNotNull(entry);
    return executeEntry(result, null, entry, -1);
  }

  public Boolean execute(final Access access, final boolean finalize) {
    InvariantChecks.checkNotNull(access);

    final Boolean status = executeAccess(result, null, access, -1);

    if (finalize) {
      result.includeOriginalVariables();
    }

    return status;
  }

  public Boolean execute(final List<Access> structure, final boolean finalize) {
    InvariantChecks.checkNotNull(structure);
    InvariantChecks.checkGreaterThanZero(structure.size());

    final Boolean status = executeStructure(result, null, structure);

    if (finalize) {
      result.includeOriginalVariables();
    }

    return status;
  }

  private Boolean executeStructure(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final List<Access> structure) {

    for (int j = 0; j < structure.size(); j++) {
      final Access access2 = structure.get(j);

      for (int i = 0; i < j; i++) {
        final Access access1 = structure.get(i);
        final BufferDependency dependency = access2.getDependency(i);

        if (dependency != null) {
          // It does not execute the paths (only the dependency).
          executeDependency(result, defines, access1, i, access2, j, dependency);
        }
      }

      executeAccess(result, defines, access2, j);
    }

    return result.hasConflict() ? Boolean.FALSE : null;
  }

  private Boolean executeAccess(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final Access access,
      final int pathIndex) {

    executeAlignment(result, defines, access.getType().getDataType(), pathIndex);
    return executePath(result, defines, access.getPath(), pathIndex);
  }

  private Boolean executePath(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final AccessPath path,
      final int pathIndex) {

    for (final AccessPath.Entry entry : path.getEntries()) {
      if (result.hasConflict()) {
        return Boolean.FALSE;
      }

      executeEntry(result, defines, entry, pathIndex);
    }

    return result.hasConflict() ? Boolean.FALSE : null;
  }

  private Boolean executeAlignment(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final DataType dataType,
      final int pathIndex) {

    final int lowerZeroBit = 0;
    final int upperZeroBit = dataType.getLowerAddressBit() - 1;

    if (upperZeroBit < 0) {
      return null;
    }

    final MmuAddressInstance addrType = MmuPlugin.getSpecification().getVirtualAddress();
    final IntegerField field = addrType.getVariable().field(lowerZeroBit, upperZeroBit);

    final IntegerConstraint<IntegerField> constraint =
        new IntegerDomainConstraint<>(field, BigInteger.ZERO);

    return executeFormula(result, defines, constraint.getFormula(), pathIndex);
  }

  private Boolean executeDependency(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final Access access1,
      final int pathIndex1,
      final Access access2,
      final int pathIndex2,
      final BufferDependency dependency) {

    for (final BufferHazard.Instance hazard : dependency.getHazards()) {
      if (result.hasConflict()) {
        return Boolean.FALSE;
      }

      executeHazard(result, defines, hazard, pathIndex1, pathIndex2);
    }

    return result.hasConflict() ? Boolean.FALSE : null;
  }

  private Boolean executeHazard(
      final SymbolicResult result, 
      final Set<IntegerVariable> defines,
      final BufferHazard.Instance hazard,
      final int pathIndex1,
      final int pathIndex2) {

    if (result.hasConflict()) {
      return Boolean.FALSE;
    }

    final MmuBuffer buffer = hazard.getPrimaryAccess().getBuffer();
    final MmuCondition condition = hazard.getCondition();

    if (condition != null) {
      final MemoryAccessContext context1 = result.getContext(pathIndex1);
      final MemoryAccessContext context2 = result.getContext(pathIndex2);

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
          final String instanceId1 = MmuBufferAccess.getId(buffer, context1);
          final String instanceId2 = MmuBufferAccess.getId(buffer, context2);

          final IntegerField term1 = context1.getInstance(instanceId1, term);
          final IntegerField term2 = context2.getInstance(instanceId2, term);

          final IntegerField field1 = result.getVersion(term1, pathIndex1);
          final IntegerField field2 = result.getVersion(term2, pathIndex2);

          clauseBuilder.addEquation(field1, field2, !atom.isNegated());

          result.addOriginalVariable(result.getOriginal(term1.getVariable(), pathIndex1));
          result.addOriginalVariable(result.getOriginal(term2.getVariable(), pathIndex2));
        }
      }

      result.addClause(clauseBuilder.build());
    }

    return result.hasConflict() ? Boolean.FALSE : null;
  }

  private Boolean executeFormula(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final IntegerFormula<IntegerField> formula,
      final int pathIndex) {

    for (final IntegerClause<IntegerField> clause : formula.getClauses()) {
      final IntegerClause.Builder<IntegerField> clauseBuilder =
          new IntegerClause.Builder<>(clause.getType());

      for (final IntegerEquation<IntegerField> equation : clause.getEquations()) {
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

    return result.hasConflict() ? Boolean.FALSE : null;
  }

  private void restrictTransition(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final boolean isStart,
      final MmuTransition transition,
      final int pathIndex) {
    final MemoryAccessContext context = result.getContext(pathIndex);

    if (restrictor != null) {
      final Collection<IntegerConstraint<IntegerField>> constraints =
          restrictor.getConstraints(isStart, transition, context);

      for (final IntegerConstraint<IntegerField> constraint : constraints) {
        executeFormula(result, defines, constraint.getFormula(), pathIndex);
      }
    }
  }

  private void restrictProgram(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final boolean isStart,
      final MmuProgram program,
      final int pathIndex) {
    final MemoryAccessContext context = result.getContext(pathIndex);

    if (restrictor != null) {
      final Collection<IntegerConstraint<IntegerField>> constraints =
          restrictor.getConstraints(isStart, program, context);

      for (final IntegerConstraint<IntegerField> constraint : constraints) {
        executeFormula(result, defines, constraint.getFormula(), pathIndex);
      }
    }
  }

  private Boolean executeEntry(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final AccessPath.Entry entry,
      final int pathIndex) {

    final MmuSubsystem memory = MmuPlugin.getSpecification();

    if (result.hasConflict()) {
      return Boolean.FALSE;
    }

    result.accessBuffer(entry, pathIndex);

    final boolean isStart = entry.isStart();
    final MmuProgram program = entry.getProgram();
    final MemoryAccessContext context = result.getContext(pathIndex);

    Boolean status = Boolean.TRUE;

    switch(entry.getKind()) {
    case NORMAL:
      status = executeProgram(result, defines, program, pathIndex);
      restrictProgram(result, defines, isStart, program, pathIndex);

      return status;

    case CALL:
      InvariantChecks.checkTrue(program.isAtomic());
      final MmuTransition transition = program.getTransition();

      // Execute the action as usual.
      status = executeTransition(result, defines, transition, pathIndex, true, false);
      restrictTransition(result, defines, isStart, transition, pathIndex);

      // Compose bindings.
      final MmuAction action = transition.getTarget();

      final MmuBufferAccess oldBufferAccess = action.getBufferAccess(context);
      final MmuAddressInstance oldFormalArg = oldBufferAccess.getAddress();

      // Call.
      result.updateStack(entry, pathIndex);

      final MmuBufferAccess newBufferAccess = action.getBufferAccess(context);
      final MmuAddressInstance newFormalArg = newBufferAccess.getAddress();

      final Collection<MmuBinding> bindings1 = newFormalArg.bindings(oldFormalArg);
      executeBindings(result, defines, bindings1, pathIndex);

      // The new virtual address is equal to the buffer access address.
      final MmuAddressInstance virtualAddress = memory.getVirtualAddress();
      final MmuAddressInstance newVirtualAddress = virtualAddress.getInstance(null, context);

      final Collection<MmuBinding> bindings2 = newVirtualAddress.bindings(newFormalArg);
      executeBindings(result, defines, bindings2, pathIndex);

      return status;

    case RETURN:
      // The entry read from the buffer is the data read from the memory.
      final IntegerVariable preData = context.getInstance(null, memory.getDataVariable());

      // Return.
      final MemoryAccessStack.Frame frame = result.updateStack(entry, pathIndex);

      final MmuTransition callTransition = frame.getTransition();
      final MmuAction callAction = callTransition.getTarget();

      final MmuBufferAccess postBufferAccess = callAction.getBufferAccess(context);

      final MmuStruct postEntry = postBufferAccess.getEntry();
      final List<IntegerVariable> postFields = postEntry.getFields();

      int bit = 0;
      final Collection<MmuBinding> bindings = new ArrayList<>();

      // Reverse order.
      for (int i = postFields.size() - 1; i >= 0; i--) {
        final IntegerVariable postField = postFields.get(i);
        final IntegerField preField = preData.field(bit, bit + postField.getWidth() - 1);
        // Buffer.Entry = Memory.DATA.
        final MmuBinding binding = new MmuBinding(postField, preField);

        bindings.add(binding);
        bit += postField.getWidth();
      }

      executeBindings(result, defines, bindings, pathIndex);

      // Variables=Buffer.Entry.
      status = executeTransition(result, defines, callTransition, pathIndex, false, true);

      return status;

    default:
      return status;
    }
  }

  private Boolean executeProgram(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final MmuProgram program,
      final int pathIndex) {

    if (result.hasConflict()) {
      return Boolean.FALSE;
    }

    if (program.isAtomic()) {
      return executeTransition(result, defines, program.getTransition(), pathIndex, true, true);
    }

    for (final Collection<MmuProgram> statement : program.getStatements()) {
      if (result.hasConflict()) {
        return Boolean.FALSE;
      }

      executeStatement(result, defines, statement, pathIndex);
    }

    return result.hasConflict() ? Boolean.FALSE : null;
  }

  private Boolean executeStatement(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final Collection<MmuProgram> statement,
      final int pathIndex) {

    if (result.hasConflict()) {
      return Boolean.FALSE;
    }

    if (statement.isEmpty()) {
      result.setConflict(true);
      return Boolean.FALSE;
    }

    if (statement.size() == 1) {
      return executeProgram(result, defines, statement.iterator().next(), pathIndex);
    }

    final List<SymbolicResult> switchResults = new ArrayList<>(statement.size());
    final List<Set<IntegerVariable>> switchDefines = new ArrayList<>(statement.size());

    for (final MmuProgram program : statement) {
      final IntegerFormulaBuilder<IntegerField> caseBuilder = new IntegerFormula.Builder<>();
      final SymbolicResult caseResult = new SymbolicResult(caseBuilder, result);
      final Set<IntegerVariable> caseDefines = new LinkedHashSet<>();

      executeProgram(caseResult, caseDefines, program, pathIndex);

      if (!caseResult.hasConflict()) {
        switchResults.add(caseResult);
        switchDefines.add(caseDefines);
      }
    }

    if (switchResults.isEmpty()) {
      result.setConflict(true);
      return Boolean.FALSE;
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
          final SymbolicResult caseResult = switchResults.get(i);
          final int versionNumber = caseResult.getVersionNumber(originalVariable);

          if (versionNumber > maxVersionNumber) {
            maxVersionNumber = versionNumber;
          }

          indices.add(i);
        }
      }

      for (final int i : indices) {
        final SymbolicResult caseResult = switchResults.get(i);
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

    if (switchResults.size() == 1) {
      // There is only one control flow.
      final SymbolicResult caseResult = switchResults.get(0);
      final IntegerFormula.Builder<IntegerField> caseBuilder =
          (IntegerFormula.Builder<IntegerField>) caseResult.getBuilder();
      final IntegerFormula<IntegerField> caseFormula = caseBuilder.build();

      result.addFormula(caseFormula);
      // TODO: Constant propagation can be optimized.
      result.getConstants().putAll(caseResult.getConstants());
    } else {
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
        final SymbolicResult caseResult = switchResults.get(i);
        final IntegerFormula.Builder<IntegerField> caseBuilder =
            (IntegerFormula.Builder<IntegerField>) caseResult.getBuilder();
        final IntegerFormula<IntegerField> caseFormula = caseBuilder.build();

        // Case: (PHI == i) -> CASE(i).
        final IntegerFormula<IntegerField> ifThenFormula = getIfThenFormula(phi, i, caseFormula);
        result.addFormula(ifThenFormula);
      }
    }

    return null;
  }

  private Boolean executeTransition(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final MmuTransition transition,
      final int pathIndex,
      final boolean executeCall,
      final boolean executeReturn) {

    if (result.hasConflict()) {
      return Boolean.FALSE;
    }

    Boolean status = null;

    if (executeCall) {
      final MmuGuard guard = transition.getGuard();
      if (guard != null) {
        status = executeGuard(result, defines, guard, pathIndex);
      } else {
        status = Boolean.TRUE;
      }
    }

    if (status == Boolean.FALSE) {
      return status;
    }

    final MmuAction action = transition.getTarget();
    if (action != null) {
      executeAction(result, defines, action, pathIndex, executeCall, executeReturn);
    }

    return status;
  }

  
  
  private Boolean executeGuard(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final MmuGuard guard,
      final int pathIndex) {

    if (result.hasConflict()) {
      return Boolean.FALSE;
    }

    Boolean status = Boolean.TRUE;

    final MemoryAccessContext context = result.getContext(pathIndex);
    final MmuBufferAccess bufferAccess = guard.getBufferAccess(context);
 
    // Buffer(Argument) => (Address == Argument).
    if (bufferAccess != null) {
      final MmuAddressInstance formalArg = bufferAccess.getAddress();
      final MmuAddressInstance actualArg = bufferAccess.getArgument();

      if (formalArg != null && actualArg != null) {
        final Collection<MmuBinding> bindings = formalArg.bindings(actualArg);
        executeBindings(result, defines, bindings, pathIndex);
      }

      if (bufferAccess.getEvent() == BufferAccessEvent.HIT) {
        final Collection<MmuBinding> bindings = bufferAccess.getMatchBindings();
        status = executeBindings(result, defines, bindings, pathIndex);
      }
    }

    if (status == Boolean.FALSE) {
      return status;
    }

    final MmuSegment segment = guard.getSegment();
    if (segment != null) {
      if (guard.isHit()) {
        final MmuAddressInstance address = segment.getVaType().getInstance(null, context);

        final IntegerRangeConstraint constraint =
            new IntegerRangeConstraint(address.getVariable(),
            new IntegerRange(segment.getMin(), segment.getMax()));

        status = executeFormula(result, defines, constraint.getFormula(), pathIndex);
      } else {
        // FIXME: Handle segment miss.
        InvariantChecks.checkTrue(false);
      }
    }

    if (status == Boolean.FALSE) {
      return status;
    }

    final MmuCondition condition = guard.getCondition(null, context);
    if (condition != null) {
      status = executeCondition(result, defines, condition, pathIndex);
    }

    return status;
  }

  private Boolean executeAction(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final MmuAction action,
      final int pathIndex,
      final boolean executeCall,
      final boolean executeReturn) {

    if (result.hasConflict()) {
      return Boolean.FALSE;
    }

    final MemoryAccessContext context = result.getContext(pathIndex);
    final MmuBufferAccess bufferAccess = action.getBufferAccess(context);

    if (executeCall) {
      // Buffer(Argument) => (Address == Argument).
      if (bufferAccess != null) {
        final MmuAddressInstance formalArg = bufferAccess.getAddress();
        final MmuAddressInstance actualArg = bufferAccess.getArgument();

        if (formalArg != null && actualArg != null) {
          final Collection<MmuBinding> bindings = formalArg.bindings(actualArg);
          executeBindings(result, defines, bindings, pathIndex);
        }
      }
    }

    if (executeReturn) {
      // Variable = Buffer(...).Entry => (Variable == Entry).
      final String lhsInstanceId =
          bufferAccess != null && bufferAccess.getEvent() == BufferAccessEvent.WRITE
            ? bufferAccess.getId() : null;

      final String rhsInstanceId =
          bufferAccess != null && bufferAccess.getEvent() == BufferAccessEvent.READ
            ? bufferAccess.getId() : null;

      final Map<IntegerField, MmuBinding> assignments =
          action.getAssignments(lhsInstanceId, rhsInstanceId, context);

      if (assignments != null) {
        executeBindings(result, defines, assignments.values(), pathIndex);

        if (bufferAccess != null && bufferAccess.getEvent() == BufferAccessEvent.READ) {
          final MmuBuffer lhs = bufferAccess.getBuffer();
          final MmuStruct rhs = bufferAccess.getEntry();

          // Add special bindings: Buffer.Field = BufferAccess.Field.
          // This introduces variables {Buffer.Field} to be used from test templates.
          executeBindings(result, defines, lhs.bindings(rhs), pathIndex);
        }
      }
    }

    return null;
  }

  private Boolean executeCondition(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final MmuCondition condition,
      final int pathIndex) {

    if (result.hasConflict()) {
      return Boolean.FALSE;
    }

    // Try to calculate the condition based on the derived constants.
    final Boolean value = MmuCalculator.eval(condition,
        new Function<IntegerVariable, BigInteger>() {
          @Override
          public BigInteger apply(final IntegerVariable original) {
            final IntegerVariable version = result.getVersion(original);
            return result.getConstant(version);
          }
        });

    if (value != null) {
      // If the result is false, there is a conflict.
      // If the result is true, the condition is redundant.
      result.setConflict(!value.booleanValue());
      return value;
    }

    final IntegerClause.Type definedType = condition.getType() == MmuCondition.Type.AND ?
        IntegerClause.Type.AND : IntegerClause.Type.OR;

    final IntegerClause.Builder<IntegerField> clauseBuilder =
        new IntegerClause.Builder<>(definedType);

    for (final MmuConditionAtom atom : condition.getAtoms()) {
      if (result.hasConflict()) {
        return Boolean.FALSE;
      }

      executeConditionAtom(result, defines, clauseBuilder, atom, pathIndex);
    }

    if (clauseBuilder.size() != 0) {
      result.addClause(clauseBuilder.build());
    }

    return null;
  }

  private Boolean executeConditionAtom(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final IntegerClause.Builder<IntegerField> clauseBuilder,
      final MmuConditionAtom atom,
      final int pathIndex) {

    if (result.hasConflict()) {
      return Boolean.FALSE;
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
              // Condition is always false.
              result.setConflict(true);
              return Boolean.FALSE;
            }

            if (truthValue && clauseBuilder.getType() == IntegerClause.Type.OR) {
              // Condition is always true.
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

    return null;
  }

  private Boolean executeBindings(
      final SymbolicResult result,
      final Set<IntegerVariable> defines,
      final Collection<MmuBinding> bindings,
      final int pathIndex) {

    if (result.hasConflict()) {
      return Boolean.FALSE;
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

      if (rhs == null) {
        continue;
      }

      final IntegerVariable newLhsVar = result.getNextVersion(lhs.getVariable(), pathIndex);
      final List<IntegerField> rhsTerms = new ArrayList<>();

      // Equation for the prefix part.
      if (lhs.getLoIndex() > 0) {
        final IntegerField oldLhsPre = new IntegerField(oldLhsVar, 0, lhs.getLoIndex() - 1);
        final IntegerField newLhsPre = new IntegerField(newLhsVar, 0, lhs.getLoIndex() - 1);

        clauseBuilder.addEquation(newLhsPre, oldLhsPre, true);
        rhsTerms.add(oldLhsPre);
      }

      int offset = lhs.getLoIndex();

      // Equations for the middle part.
      for (final IntegerField term : rhs.getTerms()) {
        final IntegerField field = result.getVersion(term, pathIndex);

        result.addOriginalVariable(result.getOriginal(term.getVariable(), pathIndex));

        final int upper = offset + (field.getWidth() - 1);
        final int trunc = upper > lhs.getHiIndex() ? lhs.getHiIndex() : upper;

        final IntegerField lhsField = new IntegerField(newLhsVar, offset, trunc);
        final IntegerField rhsField = trunc == upper
            ? field
            : new IntegerField(
                field.getVariable(),
                field.getLoIndex(),
                field.getHiIndex() - (upper - trunc)
              );

        clauseBuilder.addEquation(lhsField, rhsField, true);
        rhsTerms.add(rhsField);

        offset += field.getWidth();

        // Truncate the upper bits of the expression.
        if (offset > lhs.getHiIndex()) {
          break;
        }
      } // For each right-hand-side term.

      if (offset <= lhs.getHiIndex()) {
        final IntegerField lhsZeroField = new IntegerField(newLhsVar, offset, lhs.getHiIndex());
        clauseBuilder.addEquation(lhsZeroField, BigInteger.ZERO, true);
      }

      // Equation for the suffix part.
      if (lhs.getHiIndex() < oldLhsVar.getWidth() - 1) {
        final IntegerField oldLhsPost =
            new IntegerField(oldLhsVar, lhs.getHiIndex() + 1, oldLhsVar.getWidth() - 1);
        final IntegerField newLhsPost =
            new IntegerField(newLhsVar, lhs.getHiIndex() + 1, newLhsVar.getWidth() - 1);

        clauseBuilder.addEquation(newLhsPost, oldLhsPost, true);
        rhsTerms.add(oldLhsPost);
      }

      // Update the version of the left-hand-side variable.
      result.defineVersion(lhs.getVariable(), pathIndex);

      // Try to propagate constants.
      final Map<IntegerVariable, BigInteger> constants = result.getConstants();
      final MmuExpression rhsExpr = MmuExpression.cat(rhsTerms);
      final BigInteger constant = MmuCalculator.eval(rhsExpr,
          new Function<IntegerVariable, BigInteger>() {
            @Override
            public BigInteger apply(final IntegerVariable variable) {
              return constants.get(variable);
            }
          }, false);

      if (constant != null) {
        // Constant propagation.
        result.addConstant(newLhsVar, constant);
      }
    } // For each binding.

    if (!clauseBuilder.isEmpty()) {
      final IntegerClause<IntegerField> clause = clauseBuilder.build();
      result.addClause(clause);
    }

    return Boolean.TRUE;
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
