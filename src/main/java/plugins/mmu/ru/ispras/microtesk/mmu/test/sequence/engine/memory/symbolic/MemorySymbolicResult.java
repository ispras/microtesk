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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.integer.IntegerClause;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerFormula;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.basis.MemoryAccessStack;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;

/**
 * {@link MemorySymbolicExecutor} represents a result of symbolic execution.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemorySymbolicResult {
  /**
   * Indicates whether a contradiction is detected during symbolic execution.
   * 
   * <p>If it is {@code true}, further symbolic execution does not make sense.</p>
   */
  private boolean hasConflict = false;

  /** Stores all variables included into the formula. */
  private final Collection<IntegerVariable> variables;

  /** Allows updating the formula, i.e. performing symbolic execution. */
  private final IntegerFormula.Builder<IntegerField> formulaBuilder;

  /** Enables recursive memory calls. */
  private final Map<Integer, MemoryAccessStack> stacks;

  /** Contains original variables. */
  private final Collection<IntegerVariable> originals;

  /**
   * Maps a variable name to the variable instance number (SSA version).
   * 
   * <p>The instance number is increased upon each assignment.</p>
   */
  private final Map<String, Integer> instances;

  /** Maps a variable instance name to the corresponding variable. */
  private final Map<String, IntegerVariable> cache;

  /** Maps a variable to the derived values (constant propagation). */
  private final Map<IntegerVariable, BigInteger> constants;

  private MemorySymbolicResult(
      final Collection<IntegerVariable> variables,
      final IntegerFormula.Builder<IntegerField> formula,
      final Map<Integer, MemoryAccessStack> stacks,
      final Collection<IntegerVariable> originals,
      final Map<String, Integer> instances,
      final Map<String, IntegerVariable> cache,
      final Map<IntegerVariable, BigInteger> constants) {
    InvariantChecks.checkNotNull(variables);
    InvariantChecks.checkNotNull(formula);
    InvariantChecks.checkNotNull(stacks);
    InvariantChecks.checkNotNull(originals);
    InvariantChecks.checkNotNull(instances);
    InvariantChecks.checkNotNull(cache);
    InvariantChecks.checkNotNull(constants);

    this.variables = variables;
    this.formulaBuilder = formula;
    this.stacks = stacks;
    this.originals = originals;
    this.instances = instances;
    this.cache = cache;
    this.constants = constants;
  }

  public MemorySymbolicResult() {
    this(
        new LinkedHashSet<IntegerVariable>(),
        new IntegerFormula.Builder<IntegerField>(),
        new HashMap<Integer, MemoryAccessStack>(),
        new LinkedHashSet<IntegerVariable>(),
        new HashMap<String, Integer>(),
        new HashMap<String, IntegerVariable>(),
        new HashMap<IntegerVariable, BigInteger>());
  }

  public MemorySymbolicResult(final MemorySymbolicResult r) {
    this(
        new LinkedHashSet<>(r.variables),
        new IntegerFormula.Builder<>(r.formulaBuilder),
        new HashMap<>(r.stacks),
        new LinkedHashSet<>(r.originals),
        new HashMap<>(r.instances),
        new HashMap<>(r.cache),
        new HashMap<>(r.constants));
  }

  public boolean hasConflict() {
    return hasConflict;
  }

  public void setConflict(final boolean hasConflict) {
    this.hasConflict = hasConflict;
  }

  public Collection<IntegerVariable> getVariables() {
    return variables;
  }

  public void addVariable(final IntegerVariable var) {
    variables.add(var);
  }

  public void addVariables(final Collection<IntegerVariable> vars) {
    variables.addAll(vars);
  }

  public IntegerFormula<IntegerField> getFormula() {
    return formulaBuilder.build();
  }

  public Map<Integer, MemoryAccessStack> getStacks() {
    return stacks;
  }

  public Collection<IntegerVariable> getOriginalVariables() {
    return originals;
  }

  public void addOriginalVariable(final IntegerVariable var) {
    originals.add(var);
  }

  public void addOriginalVariables(final Collection<IntegerVariable> vars) {
    originals.addAll(vars);
  }

  public BigInteger getConstant(final IntegerVariable var) {
    return constants.get(var);
  }

  public Map<IntegerVariable, BigInteger> getConstants() {
    return constants;
  }

  public Map<IntegerVariable, BigInteger> getOriginalConstants() {
    final Map<IntegerVariable, BigInteger> result = new HashMap<>();

    for (final IntegerVariable original : originals) {
      final IntegerVariable instance = getPathVarInstance(original.getName());
      final BigInteger constant = constants.get(instance);

      if (constant != null) {
        result.put(original, constant);
      }
    }

    return result;
  }

  public void addConstant(final IntegerVariable var, final BigInteger constant) {
    constants.put(var, constant);
  }

  public void addEquation(final IntegerField lhs, final IntegerField rhs) {
    InvariantChecks.checkNotNull(lhs);
    InvariantChecks.checkNotNull(rhs);

    formulaBuilder.addEquation(lhs, rhs, true);
  }

  public void addClause(final IntegerClause<IntegerField> clause) {
    InvariantChecks.checkNotNull(clause);
    formulaBuilder.addClause(clause);
  }

  public MemoryAccessStack getStack(final int pathIndex) {
    MemoryAccessStack stack = stacks.get(pathIndex);

    if (stack == null) {
      final String id = pathIndex != -1 ? String.format("%d", pathIndex) : "";
      stacks.put(pathIndex, stack = new MemoryAccessStack(id));
    }

    return stack;
  }

  public void updateStack(final MemoryAccessPath.Entry entry, final int pathIndex) {
    InvariantChecks.checkNotNull(entry);

    final MemoryAccessStack stack = getStack(pathIndex);

    if (entry.isCall()) {
      stack.call(entry.getFrame());
      Logger.debug("CALL: %s", stack);
    } else if (entry.isReturn()) {
      stack.ret();
      Logger.debug("RETURN: %s", stack);
    }
  }

  public static String getPathVarName(final String varName, final int pathIndex) {
    InvariantChecks.checkNotNull(varName);
    return pathIndex == -1 ? varName : String.format("%s$%d", varName, pathIndex);
  }

  public static String getPathVarInstanceName(final String pathVarName, final int n) {
    return String.format("%s(%d)", pathVarName, n);
  }

  public int getPathVarNumber(final String pathVarName) {
    InvariantChecks.checkNotNull(pathVarName);
    return instances.containsKey(pathVarName) ? instances.get(pathVarName) : 0;
  }

  public IntegerVariable getPathVarInstance(final String pathVarName) {
    InvariantChecks.checkNotNull(pathVarName);

    final int n = getPathVarNumber(pathVarName);
    final String pathVarInstanceName = getPathVarInstanceName(pathVarName, n);

    return cache.get(pathVarInstanceName);
  }

  public void definePathVarInstance(final String pathVarName) {
    InvariantChecks.checkNotNull(pathVarName);

    final int n = getPathVarNumber(pathVarName);
    instances.put(pathVarName, n + 1);
  }

  public IntegerVariable getPathVar(final IntegerVariable var, final int pathIndex) {
    InvariantChecks.checkNotNull(var);

    final String pathVarName = getPathVarName(var.getName(), pathIndex);
    return getVariable(pathVarName, var.getWidth(), var.getValue());
  }

  public IntegerVariable getPathVarInstance(final IntegerVariable var, final int pathIndex) {
    InvariantChecks.checkNotNull(var);

    final String pathVarName = getPathVarName(var.getName(), pathIndex);
    final int n = getPathVarNumber(pathVarName);
    final String pathVarInstanceName = getPathVarInstanceName(pathVarName, n);

    return getVariable(pathVarInstanceName, var.getWidth(), var.getValue());
  }

  public IntegerVariable getNextPathVarInstance(final IntegerVariable var, final int pathIndex) {
    InvariantChecks.checkNotNull(var);

    final String pathVarName = getPathVarName(var.getName(), pathIndex);
    final int n = getPathVarNumber(pathVarName);
    final String pathVarInstanceName = getPathVarInstanceName(pathVarName, n + 1);

    return getVariable(pathVarInstanceName, var.getWidth(), var.getValue());
  }

  public IntegerField getPathFieldInstance(final IntegerField field, final int pathIndex) {
    InvariantChecks.checkNotNull(field);

    final IntegerVariable var = getPathVarInstance(field.getVariable(), pathIndex);

    final int lo = field.getLoIndex();
    final int hi = field.getHiIndex();

    return new IntegerField(var, lo, hi);
  }

  public void definePathVarInstance(final IntegerVariable var, final int pathIndex) {
    InvariantChecks.checkNotNull(var);

    final String pathVarName = getPathVarName(var.getName(), pathIndex);
    definePathVarInstance(pathVarName);
  }

  private IntegerVariable getVariable(
      final String varInstanceName, final int width, final BigInteger value) {
    InvariantChecks.checkNotNull(varInstanceName);

    IntegerVariable varInstance = cache.get(varInstanceName);
    if (varInstance == null) {
      cache.put(varInstanceName,
          varInstance = new IntegerVariable(varInstanceName, width, value));
    }

    return varInstance;
  }
}
