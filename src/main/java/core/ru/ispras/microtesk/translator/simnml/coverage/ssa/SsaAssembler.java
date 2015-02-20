package ru.ispras.microtesk.translator.simnml.coverage.ssa;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.NodeTransformer;
import ru.ispras.fortress.transformer.TransformerRule;

import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Primitive;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static ru.ispras.microtesk.translator.simnml.coverage.ssa.Expression.AND;
import static ru.ispras.microtesk.translator.simnml.coverage.ssa.Expression.OR;

public final class SsaAssembler {
  final Map<String, SsaForm> buildingBlocks;
  final Primitive spec;
  SsaScope scope;
  int numTemps;

  Deque<Primitive> context;
  ArrayList<Node> statements;
  Deque<Integer> batchSize;
  Map<Enum<?>, TransformerRule> ruleset;

  public SsaAssembler(Map<String, SsaForm> buildingBlocks, Primitive spec) {
    this.buildingBlocks = buildingBlocks;
    this.spec = spec;
    this.scope = SsaScopeFactory.createScope();
    this.numTemps = 0;

    this.context = null;
    this.ruleset = null;
  }

  public Node assemble() {
    this.context = new ArrayDeque<>();
    this.statements = new ArrayList<>();
    this.batchSize = new ArrayDeque<>();
    this.ruleset = createRuleset();

    newBatch();
    embedSsa(buildingBlocks.get(spec.getName() + ".action"), spec);
    return endBatch();
  }

  private void embedSsa(SsaForm ssa, Primitive spec) {
    context.push(spec);
    embedBlock(ssa.getEntryPoint(), spec);
    context.pop();
  }

  private Block embedBlock(Block block, Primitive spec) {
    walkStatements(block, spec);
    if (blockIsOp(block, SsaOperation.PHI) ||
        blockIsOp(block, SsaOperation.CALL)) {
      return block;
    }
    if (block.getChildren().size() > 1) {
      return embedBranches(block, spec);
    }
    return embedSequence(block, spec);
  }

  private Block embedSequence(Block block, Primitive spec) {
    if (block.getChildren().size() > 0) {
      return embedBlock(block.getChildren().get(0).block, spec);
    }
    return null;
  }

  private Block embedBranches(Block block, Primitive spec) {
    Block fence = null;
    final List<NodeOperation> branches = new ArrayList();
    for (GuardedBlock guard : block.getChildren()) {
      newBatch(guard.guard);
      fence = sameNotNull(fence, embedBlock(guard.block, spec));
      branches.add(endBatch());
    }
    addToBatch(OR(branches));

    return embedSequence(fence, spec);
  }

  private static <T> T sameNotNull(T stored, T input) {
    if (input == null) {
      throw new NullPointerException();
    }
    if (stored != null && stored != input) {
      throw new IllegalArgumentException();
    }
    return input;
  }

  private static boolean blockIsOp(Block block, Enum<?> id) {
    final List<NodeOperation> stmts = block.getStatements();
    return stmts.size() == 1 &&
           nodeIsOperation(stmts.get(0), id);
  }

  private final class ModeRule implements TransformerRule {
    private final Enum<?> operation;
    private final String suffix;

    ModeRule(Enum<?> operation, String suffix) {
      this.operation = operation;
      this.suffix = suffix;
    }

    @Override
    public boolean isApplicable(Node node) {
      return nodeIsOperation(node, this.operation);
    }

    @Override
    public Node apply(Node node) {
      final Pair<String, String> name =
          Utility.splitOnLast(variableOperand(0, node).getName(), '.');
      final Primitive mode = contextArgument(name.second);

      embedSsa(buildingBlocks.get(mode.getName() + suffix), mode);

      return scope.fetch(String.format("__tmp_%d", numTemps - 1));
    }
  }

  private Map<Enum<?>, TransformerRule> createRuleset() {
    final TransformerRule call = new TransformerRule() {
      @Override
      public boolean isApplicable(Node node) {
        return nodeIsOperation(node, SsaOperation.CALL);
      }

      @Override
      public Node apply(Node node) {
        final Pair<String, String> pair = (Pair<String, String>) node.getUserData();
        if (spec.getArguments().keySet().contains(pair.first)) {
          final Primitive callee = contextArgument(pair.first);
          embedSsa(buildingBlocks.get(String.format("%s.%s", callee.getName(), pair.second)),
                   callee);

          return node;
        }
        throw new IllegalArgumentException("Underspecified call found");
      }
    };

    final TransformerRule substitute = new TransformerRule() {
      @Override
      public boolean isApplicable(Node node) {
        return nodeIsOperation(node, SsaOperation.SUBSTITUTE);
      }

      @Override
      public Node apply(Node node) {
        return createTemporary(variableOperand(0, node).getDataType());
      }
    };

    final Map<Enum<?>, TransformerRule> rules = new IdentityHashMap<>();
    rules.put(SsaOperation.CALL, call);
    rules.put(SsaOperation.SUBSTITUTE, substitute);
    rules.put(SsaOperation.EXPAND, new ModeRule(SsaOperation.EXPAND, ".expand"));
    rules.put(SsaOperation.UPDATE, new ModeRule(SsaOperation.UPDATE, ".update"));

    final TransformerRule rebase = new TransformerRule() {
      @Override
      public boolean isApplicable(Node node) {
        return node.getKind() == Node.Kind.VARIABLE &&
               node.getUserData() == null;
      }

      @Override
      public Node apply(Node node) {
        node.setUserData(1);
        return node;
      }
    };
    rules.put(Node.Kind.VARIABLE, rebase);

    return rules;
  }

  private Primitive contextArgument(String name) {
    return (Primitive) context.peek().getArguments().get(name).getValue();
  }

  private void walkStatements(final Block block, final Primitive spec) {
    final NodeTransformer transformer = new NodeTransformer(this.ruleset);
    transformer.walk(block.getStatements());

    // It is known that resulting sequence will be inverted on block granularity
    for (Node node : transformer.getResult()) {
      addToBatch(node);
    }
  }

  private void addToBatch(Node node) {
    if (nodeIsOperation(node, SsaOperation.CALL) ||
        nodeIsOperation(node, SsaOperation.PHI) ||
        nodeIsTrue(node)) {
      return;
    }
    this.statements.add(node);
    this.batchSize.push(this.batchSize.pop() + 1);
  }

  private boolean nodeIsTrue(Node node) {
    if (node.equals(Expression.TRUE)) {
      return true;
    }
    if (!nodeIsOperation(node, StandardOperation.AND)) {
      return false;
    }
    final NodeOperation op = (NodeOperation) node;
    return op.getOperandCount() == 1 &&
           op.getOperand(0).equals(Expression.TRUE);
  }

  private void newBatch() {
    this.batchSize.push(0);
  }

  private void newBatch(Node node) {
    newBatch();
    addToBatch(node);
  }

  private NodeOperation endBatch() {
    final List<Node> operands =
        this.statements.subList(this.statements.size() - this.batchSize.pop(),
                                this.statements.size());
    final NodeOperation batch = AND(operands);
    operands.clear();

    return batch;
  }

  private NodeVariable createTemporary(DataType type) {
    return scope.create(String.format("__tmp_%d", numTemps++),
                        type.valueUninitialized());
  }

  private static NodeVariable variableOperand(int i, Node op) {
    return (NodeVariable) ((NodeOperation) op).getOperand(i);
  }

  private static boolean nodeIsOperation(Node node, Enum<?> opId) {
    if (node.getKind() != Node.Kind.OPERATION) {
      return false;
    }
    return ((NodeOperation) node).getOperationId() == opId;
  }
}
