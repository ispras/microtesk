/*
 * Copyright 2014-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.InvariantChecks;

final class Block {
  private static final List<NodeOperation> PHI_STATEMENTS =
      Collections.singletonList(new NodeOperation(SsaOperation.PHI));

  public static Block newSingleton(final NodeOperation node) {
    return new Block(Collections.singletonList(node));
  }

  public static Block newPhi() {
    return new Block(PHI_STATEMENTS);
  }

  public static Block newEmpty() {
    return new Block(Collections.<NodeOperation>emptyList());
  }

  public static final class Builder {
    private List<NodeOperation> statements;
    private Map<String, NodeVariable> inputs;
    private Map<String, NodeVariable> outputs;
    private List<NodeVariable> intermediates;

    public Builder() {
      this.statements = new ArrayList<>();
      this.inputs = new TreeMap<>();
      this.outputs = new TreeMap<>();
      this.intermediates = new ArrayList<>();
    }

    public void add(final NodeOperation s) {
      statements.add(s);
    }

    public void addAll(final Collection<NodeOperation> nodes) {
      statements.addAll(nodes);
    }

    public Block build() {
      collectData(statements);
      return new Block(statements, inputs, outputs, intermediates);
    }

    private void collectData(final List<NodeOperation> statements) {
      /* TODO populate input/output maps and intermediates list */
    }
  }

  private final List<NodeOperation> statements;
  private final Map<String, NodeVariable> inputs;
  private final Map<String, NodeVariable> outputs;
  private final List<NodeVariable> intermediates;

  private List<GuardedBlock> children;
  private Block successor;

  private Block(
      final List<NodeOperation> statements,
      final Map<String, NodeVariable> inputs,
      final Map<String, NodeVariable> outputs,
      final List<NodeVariable> intermediates) {
    InvariantChecks.checkNotNull(statements);
    InvariantChecks.checkNotNull(inputs);
    InvariantChecks.checkNotNull(outputs);
    InvariantChecks.checkNotNull(intermediates);

    this.statements = Collections.unmodifiableList(statements);
    this.inputs = Collections.unmodifiableMap(inputs);
    this.outputs = Collections.unmodifiableMap(outputs);
    this.intermediates = Collections.unmodifiableList(intermediates);

    this.children = Collections.emptyList();
    this.successor = null;
  }

  private Block(final List<NodeOperation> statements) {
    this.statements = statements;
    this.inputs = Collections.emptyMap();
    this.outputs = Collections.emptyMap();
    this.intermediates = Collections.emptyList();

    this.children = Collections.emptyList();
    this.successor = null;
  }

  public void setChildren(final List<GuardedBlock> children) {
    this.children = Collections.unmodifiableList(children);
  }

  public List<GuardedBlock> getChildren() {
    return children;
  }

  public void setSuccessor(final Block block) {
    this.successor = block;
  }

  public Block getSuccessor() {
    return this.successor;
  }

  public List<NodeOperation> getStatements() {
    return statements;
  }

  public Map<String, NodeVariable> getInputs() {
    return inputs;
  }

  public Map<String, NodeVariable> getOutputs() {
    return outputs;
  }

  public List<NodeVariable> getIntermediates() {
    return intermediates;
  }
}
