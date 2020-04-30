/*
 * Copyright 2015-2020 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.transformer.Transformer;
import ru.ispras.fortress.transformer.VariableProvider;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.Coder;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;
import ru.ispras.microtesk.mmu.basis.MemoryAccessStack;
import ru.ispras.microtesk.mmu.model.spec.MmuAction;
import ru.ispras.microtesk.mmu.model.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.model.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.model.spec.MmuProgram;
import ru.ispras.microtesk.mmu.model.spec.MmuTransition;
import ru.ispras.microtesk.utils.HierarchicalMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * {@link SymbolicExecutor} represents a result of symbolic execution.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class SymbolicResult {
  /**
   * Indicates whether a contradiction is detected during symbolic execution.
   *
   * <p>If it is {@code true}, further symbolic execution does not make sense.</p>
   */
  private boolean hasConflict = false;

  /** Allows updating the formula, i.e. performing symbolic execution. */
  private final Coder<Map<Variable, BitVector>> coder;

  /** Enables recursive memory calls. */
  private final Map<Integer, MemoryAccessContext> contexts;

  /** Contains original variables. */
  private final Collection<Variable> originals;

  /**
   * Maps a variable name to the variable version number.
   *
   * <p>The version number is increased upon each assignment.</p>
   */
  private final Map<String, Integer> versions;

  /** Maps a name to the corresponding variable (original or version). */
  private final Map<String, Variable> cache;

  /** Maps a variable to the derived values (constant propagation). */
  private final Map<Variable, BitVector> constants;

  private SymbolicResult(
      final Coder<Map<Variable, BitVector>> coder,
      final Map<Integer, MemoryAccessContext> contexts,
      final Collection<Variable> originals,
      final Map<String, Integer> versions,
      final Map<String, Variable> cache,
      final Map<Variable, BitVector> constants) {
    InvariantChecks.checkNotNull(coder);
    InvariantChecks.checkNotNull(contexts);
    InvariantChecks.checkNotNull(originals);
    InvariantChecks.checkNotNull(versions);
    InvariantChecks.checkNotNull(cache);
    InvariantChecks.checkNotNull(constants);

    this.coder = coder;
    this.contexts = contexts;
    this.originals = originals;
    this.versions = versions;
    this.cache = cache;
    this.constants = constants;
  }

  public SymbolicResult(final Coder<Map<Variable, BitVector>> coder) {
    this(
        coder,
        new HashMap<>(),
        new LinkedHashSet<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>()
    );
  }

  public SymbolicResult(final Coder<Map<Variable, BitVector>> coder, final SymbolicResult other) {
    this(
        coder,
        new HashMap<>(other.contexts.size()),
        new LinkedHashSet<>(other.originals),
        new HierarchicalMap<>(other.versions, new HashMap<>()),
        new HierarchicalMap<>(other.cache, new HashMap<>()),
        new HierarchicalMap<>(other.constants, new HashMap<>())
    );

    // Clone the memory access contexts.
    for (final Map.Entry<Integer, MemoryAccessContext> entry : other.contexts.entrySet()) {
      this.contexts.put(entry.getKey(), new MemoryAccessContext(entry.getValue()));
    }
  }

  public SymbolicResult(final SymbolicResult other) {
    this(other.coder.clone(), other);
  }

  public boolean hasConflict() {
    return hasConflict;
  }

  public void setConflict(final boolean hasConflict) {
    this.hasConflict = hasConflict;
  }

  public Coder<Map<Variable, BitVector>> getCoder() {
    return coder;
  }

  public Map<Integer, MemoryAccessContext> getContexts() {
    return contexts;
  }

  public Collection<Variable> getOriginalVariables() {
    return originals;
  }

  public boolean containsOriginalVariable(final Variable var) {
    return originals.contains(var);
  }

  public void addOriginalVariable(final Variable var) {
    originals.add(var);
  }

  public void addOriginalVariables(final Collection<Variable> vars) {
    originals.addAll(vars);
  }

  public BitVector getConstant(final Variable var) {
    return constants.get(var);
  }

  public Map<Variable, BitVector> getConstants() {
    return constants;
  }

  public void addConstant(final Variable var, final BitVector constant) {
    constants.put(var, constant);
  }

  public void addNode(final Node formula) {
    coder.addNode(formula);
  }

  public MemoryAccessContext getContext() {
    return getContext(-1);
  }

  public MemoryAccessContext getContext(final int pathIndex) {
    MemoryAccessContext context = contexts.get(pathIndex);

    if (context == null) {
      final String id = pathIndex != -1 ? String.format("%d", pathIndex) : "";
      contexts.put(pathIndex, context = new MemoryAccessContext(id));
    }

    return context;
  }

  public void accessBuffer(final AccessPath.Entry entry, final int pathIndex) {
    InvariantChecks.checkNotNull(entry);

    final MemoryAccessContext context = getContext(pathIndex);
    final MmuProgram program = entry.getProgram();

    for (final MmuTransition transition : program.getTransitions()) {
      for (final MmuBufferAccess bufferAccess : transition.getBufferAccesses(context)) {
        context.doAccess(bufferAccess);
      }
    }
  }

  public MemoryAccessStack.Frame updateStack(final AccessPath.Entry entry, final int pathIndex) {
    InvariantChecks.checkNotNull(entry);

    final MemoryAccessContext context = getContext(pathIndex);

    if (entry.getKind() == AccessPath.Entry.Kind.CALL) {
      final MmuProgram program = entry.getProgram();
      final MmuTransition transition = program.getTransition();
      final MmuAction sourceAction = transition.getSource();
      final MmuAction targetAction = transition.getTarget();
      final MmuBufferAccess bufferAccess = targetAction.getBufferAccess(context);
      final MmuBuffer buffer = bufferAccess != null ? bufferAccess.getBuffer() : null;

      final String frameId = String.format("%s_%s_%d",
          sourceAction.getName(),
          buffer.getName(),
          context.getBufferAccessId(buffer));

      return context.doCall(frameId, transition);
    }

    if (entry.getKind() == AccessPath.Entry.Kind.RETURN) {
      return context.doReturn();
    }

    return null;
  }

  public void includeOriginalVariables() {
    // Add the constraints of the kind V = V(n), where n is the last version number of V.
    for (final Variable original : originals) {
      final Variable version = getVersion(original.getName());
      InvariantChecks.checkNotNull(version,
          String.format("Version of %s has not been found", original.getName()));

      addNode(Nodes.eq(
          new NodeVariable(original),
          new NodeVariable(version)));

      // Propagate the constant if applicable.
      final BitVector constant = getConstant(version);

      if (constant != null) {
        addConstant(original, constant);
      }
    }
  }

  public Variable getOriginal(final Variable variable, final int pathIndex) {
    InvariantChecks.checkNotNull(variable);

    final String originalName = getOriginalName(variable, pathIndex);
    return getVariable(originalName, variable.getType(), variable.getData());
  }

  public Variable getVersion(final Variable variable, final int pathIndex) {
    InvariantChecks.checkNotNull(variable);

    final String originalName = getOriginalName(variable, pathIndex);
    final int versionNumber = getVersionNumber(originalName);
    final String versionName = getVersionName(originalName, versionNumber);

    return getVariable(versionName, variable.getType(), variable.getData());
  }

  public Node getVersion(final Node node, final int pathIndex) {
    InvariantChecks.checkNotNull(node);

    return Transformer.substitute(node, new VariableProvider() {
      @Override
      public Variable getVariable(final Variable variable) {
        return getVersion(variable, pathIndex);
      }
    });
  }

  public Variable getVersion(final Variable originalVariable) {
    InvariantChecks.checkNotNull(originalVariable);

    final String originalName = originalVariable.getName();
    final int versionNumber = getVersionNumber(originalName);
    final String versionName = getVersionName(originalName, versionNumber);

    return getVariable(versionName, originalVariable.getType(), originalVariable.getData());
  }

  private Variable getVersion(final String originalName) {
    final int versionNumber = getVersionNumber(originalName);
    final String versionName = getVersionName(originalName, versionNumber);

    return cache.get(versionName);
  }

  public Variable getNextVersion(final Variable variable, final int pathIndex) {
    InvariantChecks.checkNotNull(variable);

    final String originalName = getOriginalName(variable, pathIndex);
    final int versionNumber = getVersionNumber(originalName);
    final String nextVersionName = getVersionName(originalName, versionNumber + 1);

    return getVariable(nextVersionName, variable.getType(), variable.getData());
  }

  public void defineVersion(final Variable variable, final int pathIndex) {
    InvariantChecks.checkNotNull(variable);

    final String originalName = getOriginalName(variable, pathIndex);
    final int versionNumber = getVersionNumber(originalName);

    versions.put(originalName, versionNumber + 1);
  }

  //------------------------------------------------------------------------------------------------

  public int getVersionNumber(final Variable originalVariable) {
    InvariantChecks.checkNotNull(originalVariable);
    return getVersionNumber(originalVariable.getName());
  }

  private int getVersionNumber(final String originalName) {
    return versions.containsKey(originalName) ? versions.get(originalName) : 0;
  }

  public void setVersionNumber(final Variable originalVariable, final int versionNumber) {
    final String originalName = originalVariable.getName();
    versions.put(originalName, versionNumber);

    final String versionName = getVersionName(originalName, versionNumber);

    if (!cache.containsKey(versionName)) {
      cache.put(versionName, new Variable(versionName, originalVariable.getData()));
    }
  }

  private static String getOriginalName(final Variable variable, final int pathIndex) {
    InvariantChecks.checkNotNull(variable);

    final String name = variable.getName();
    return pathIndex == -1 ? name : String.format("%s$%d", name, pathIndex);
  }

  private static String getVersionName(final String originalName, final int versionNumber) {
    return String.format("%s(%d)", originalName, versionNumber);
  }

  private Variable getVariable(final String variableName, final DataType type, final Data data) {
    InvariantChecks.checkNotNull(variableName);

    Variable variable = cache.get(variableName);

    if (variable == null) {
      variable = (data != null)
          ? new Variable(variableName, data)
          : new Variable(variableName, type);

      cache.put(variableName, variable);
    }

    return variable;
  }
}
