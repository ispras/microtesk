package ru.ispras.microtesk.translator.simnml.coverage.ssa;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class GuardedBlock {
  public final String name;
  public final Node guard;
  public final Block block;

  GuardedBlock(String name, Node guard, Block block) {
    this.name = name;
    this.guard = guard;
    this.block = block;
  }
}

public final class Block {
  private final List<NodeOperation> statements;
  private final Map<String, NodeVariable> inputs;
  private final Map<String, NodeVariable> outputs;
  private final List<NodeVariable> intermediates;
  private List<GuardedBlock> children;
  private Block successor;

  Block(
      List<NodeOperation> statements,
      Map<String, NodeVariable> inputs,
      Map<String, NodeVariable> outputs,
      List<NodeVariable> intermediates) {
    this.statements = statements;
    this.inputs = inputs;
    this.outputs = outputs;
    this.intermediates = intermediates;
    this.children = Collections.emptyList();
    this.successor = null;
  }

  Block(List<NodeOperation> statements) {
    this.statements = statements;
    this.inputs = Collections.emptyMap();
    this.outputs = Collections.emptyMap();
    this.intermediates = Collections.emptyList();
    this.children = Collections.emptyList();
    this.successor = null;
  }

  void setChildren(List<GuardedBlock> children) {
    this.children = children;
  }

  public List<GuardedBlock> getChildren() {
    return Collections.unmodifiableList(children);
  }

  void setSuccessor(Block block) {
    this.successor = block;
  }

  public Block getSuccessor() {
    return this.successor;
  }

  public List<NodeOperation> getStatements() {
    return Collections.unmodifiableList(statements);
  }
}

final class BlockBuilder {
  private static final List<NodeOperation> PHI_STATEMENTS =
    Collections.singletonList(new NodeOperation(SsaOperation.PHI));

  private List<NodeOperation> statements;
  private Map<String, NodeVariable> inputs;
  private Map<String, NodeVariable> outputs;
  private List<NodeVariable> intermediates;

  BlockBuilder() {
    this.statements = new ArrayList<>();
    this.inputs = new TreeMap<>();
    this.outputs = new TreeMap<>();
    this.intermediates = new ArrayList<>();
  }

  void add(NodeOperation s) {
    statements.add(s);
  }

  void addAll(Collection<NodeOperation> nodes) {
    statements.addAll(nodes);
  }

  public List<NodeOperation> getStatements() {
    return statements;
  }

  public Block build() {
    collectData(statements);
    return new Block(statements, inputs, outputs, intermediates);
  }

  private void collectData(List<NodeOperation> statements) {
    /* TODO populate input/output maps and intermediates list */
  }

  public static Block createCall(String callee, String attribute) {
    final NodeOperation call = new NodeOperation(SsaOperation.CALL);
    call.setUserData(new Pair<>(callee, attribute));
    return new Block(Collections.singletonList(call));
  }

  public static Block createPhi() {
    return new Block(PHI_STATEMENTS);
  }

  public static Block createEmpty() {
    return new Block(Collections.<NodeOperation>emptyList());
  }
}
