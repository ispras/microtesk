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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.integer.IntegerClause;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerFormulaBuilder;
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

  /** Allows updating the formula, i.e. performing symbolic execution. */
  private final IntegerFormulaBuilder<IntegerField> builder;

  /** Enables recursive memory calls. */
  private final Map<Integer, MemoryAccessStack> stacks;

  /** Contains original variables. */
  private final Collection<IntegerVariable> originals;

  /**
   * Maps a variable name to the variable version number.
   * 
   * <p>The version number is increased upon each assignment.</p>
   */
  private final Map<String, Integer> versions;

  /** Maps a name to the corresponding variable (original or version). */
  private final Map<String, IntegerVariable> cache;

  /** Maps a variable to the derived values (constant propagation). */
  private final Map<IntegerVariable, BigInteger> constants;

  private MemorySymbolicResult(
      final IntegerFormulaBuilder<IntegerField> builder,
      final Map<Integer, MemoryAccessStack> stacks,
      final Collection<IntegerVariable> originals,
      final Map<String, Integer> versions,
      final Map<String, IntegerVariable> cache,
      final Map<IntegerVariable, BigInteger> constants) {
    InvariantChecks.checkNotNull(builder);
    InvariantChecks.checkNotNull(stacks);
    InvariantChecks.checkNotNull(originals);
    InvariantChecks.checkNotNull(versions);
    InvariantChecks.checkNotNull(cache);
    InvariantChecks.checkNotNull(constants);

    this.builder = builder;
    this.stacks = stacks;
    this.originals = originals;
    this.versions = versions;
    this.cache = cache;
    this.constants = constants;
  }

  public MemorySymbolicResult(final IntegerFormulaBuilder<IntegerField> builder) {
    this(
        builder,
        new HashMap<Integer, MemoryAccessStack>(),
        new LinkedHashSet<IntegerVariable>(),
        new HashMap<String, Integer>(),
        new HashMap<String, IntegerVariable>(),
        new HashMap<IntegerVariable, BigInteger>());
  }

  public MemorySymbolicResult(final MemorySymbolicResult r) {
    this(
        r.builder.clone(),
        new HashMap<>(r.stacks),
        new LinkedHashSet<>(r.originals),
        new HashMap<>(r.versions),
        new HashMap<>(r.cache),
        new HashMap<>(r.constants));

    // Clone the memory access stacks.
    for (final Map.Entry<Integer, MemoryAccessStack> entry : r.stacks.entrySet()) {
      stacks.put(entry.getKey(), new MemoryAccessStack(entry.getValue()));
    }
  }

  public boolean hasConflict() {
    return hasConflict;
  }

  public void setConflict(final boolean hasConflict) {
    this.hasConflict = hasConflict;
  }

  public IntegerFormulaBuilder<IntegerField> getBuilder() {
    return builder;
  }

  public Map<Integer, MemoryAccessStack> getStacks() {
    return stacks;
  }

  public Collection<IntegerVariable> getOriginalVariables() {
    return originals;
  }

  public boolean containsOriginalVariable(final IntegerVariable var) {
    return originals.contains(var);
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
      final IntegerVariable version = getVersion(original.getName());
      final BigInteger constant = constants.get(version);

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
    builder.addEquation(lhs, rhs, true);
  }

  public void addClause(final IntegerClause<IntegerField> clause) {
    builder.addClause(clause);
  }

  public MemoryAccessStack getStack() {
    return getStack(-1);
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
      Logger.debug("CALL: %d", stack.size());
    } else if (entry.isReturn()) {
      stack.ret();
      Logger.debug("RETURN: %d", stack.size());
    }
  }

  public void includeOriginalVariables() {
    // Add the constraints of the kind V = V(n), where n is the last version number of V.
    for (final IntegerVariable original : originals) {
      final IntegerVariable version = getVersion(original.getName());

      final IntegerField lhs = new IntegerField(original);
      final IntegerField rhs = new IntegerField(version);

      addEquation(lhs, rhs);

      // Propagate the constant if applicable.
      final BigInteger constant = getConstant(version);

      if (constant != null) {
        addConstant(original, constant);
      }
    }
  }

  public IntegerVariable getOriginal(final IntegerVariable variable, final int pathIndex) {
    InvariantChecks.checkNotNull(variable);

    final String originalName = getOriginalName(variable, pathIndex);
    return getVariable(originalName, variable.getWidth(), variable.getValue());
  }

  public IntegerVariable getVersion(final IntegerVariable variable, final int pathIndex) {
    InvariantChecks.checkNotNull(variable);

    final String originalName = getOriginalName(variable, pathIndex);
    final int versionNumber = getVersionNumber(originalName);
    final String versionName = getVersionName(originalName, versionNumber);

    return getVariable(versionName, variable.getWidth(), variable.getValue());
  }

  public IntegerField getVersion(final IntegerField field, final int pathIndex) {
    InvariantChecks.checkNotNull(field);

    final IntegerVariable version = getVersion(field.getVariable(), pathIndex);

    final int lo = field.getLoIndex();
    final int hi = field.getHiIndex();

    return new IntegerField(version, lo, hi);
  }

  public IntegerVariable getNextVersion(final IntegerVariable variable, final int pathIndex) {
    InvariantChecks.checkNotNull(variable);

    final String originalName = getOriginalName(variable, pathIndex);
    final int versionNumber = getVersionNumber(originalName);
    final String nextVersionName = getVersionName(originalName, versionNumber + 1);

    return getVariable(nextVersionName, variable.getWidth(), variable.getValue());
  }

  public void defineVersion(final IntegerVariable variable, final int pathIndex) {
    InvariantChecks.checkNotNull(variable);

    final String originalName = getOriginalName(variable, pathIndex);
    final int versionNumber = getVersionNumber(originalName);

    versions.put(originalName, versionNumber + 1);
  }

  private static String getOriginalName(final IntegerVariable variable, final int pathIndex) {
    InvariantChecks.checkNotNull(variable);

    final String name = variable.getName();
    return pathIndex == -1 ? name : String.format("%s$%d", name, pathIndex);
  }

  private static String getVersionName(final String originalName, final int versionNumber) {
    return String.format("%s(%d)", originalName, versionNumber);
  }

  private int getVersionNumber(final String originalName) {
    InvariantChecks.checkNotNull(originalName);
    return versions.containsKey(originalName) ? versions.get(originalName) : 0;
  }

  private IntegerVariable getVersion(final String originalName) {
    InvariantChecks.checkNotNull(originalName);

    final int versionNumber = getVersionNumber(originalName);
    final String versionName = getVersionName(originalName, versionNumber);

    return cache.get(versionName);
  }

  private IntegerVariable getVariable(
      final String variableName, final int width, final BigInteger value) {
    InvariantChecks.checkNotNull(variableName);

    IntegerVariable variable = cache.get(variableName);
    if (variable == null) {
      cache.put(variableName, variable = new IntegerVariable(variableName, width, value));
    }

    return variable;
  }
}
