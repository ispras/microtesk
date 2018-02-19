/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.NodeTransformer;
import ru.ispras.fortress.transformer.Transformer;
import ru.ispras.fortress.transformer.TransformerRule;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.utils.NamePath;
import ru.ispras.microtesk.utils.StringUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class Parameters {
  private final NodeOperation node;

  public Parameters(final Node node) {
    InvariantChecks.checkTrue(ExprUtils.isOperation(node, SsaOperation.PARAMETERS));
    this.node = (NodeOperation) node;
  }

  public String getName(final int i) {
    return Utility.literalOperand(i, node);
  }

  final DataType getType(final int i) {
    return node.getOperand(i).getDataType();
  }

  final NodeVariable find(final String name) {
    for (final Node p : node.getOperands()) {
      final NodeVariable v = (NodeVariable) p;
      if (v.getName().equals(name)) {
        return v;
      }
    }
    return null;
  }
}

final class Closure {
  private final NodeOperation node;

  public Closure(final Node node) {
    InvariantChecks.checkTrue(ExprUtils.isOperation(node, SsaOperation.CLOSURE));
    this.node = (NodeOperation) node;
  }

  public Node getOriginRef() {
    return node.getOperand(0);
  }

  public String getOriginName() {
    return Utility.literalOperand(0, node);
  }

  public List<Node> getArguments() {
    return node.getOperands().subList(1, node.getOperandCount());
  }

  public Node getArgument(final int i) {
    return node.getOperand(i + 1);
  }
}

public final class SsaAssembler {
  private final Map<String, SsaForm> buildingBlocks;
  private Map<String, Object> buildingContext;

  private Map<NamePath, String> context;

  private SsaScope scope;
  private int numTemps;

  private Map<NamePath, Integer> contextEnum;
  private List<Node> statements;
  private Deque<Integer> batchSize;

  private Deque<Changes> changesStack;
  private Changes changes;

  private NamePath actualPrefix;

  public SsaAssembler(Map<String, SsaForm> buildingBlocks) {
    this.buildingBlocks = buildingBlocks;

    this.scope = new SsaScopeVariable();
    this.numTemps = 0;

    final Map<String, NodeVariable> changesStore = new HashMap<>();
    this.changes = new Changes(changesStore, changesStore);
  }

  public Node assemble(final Map<String, Object> context, final String entry) {
    return assemble(context, entry, entry);
  }

  public Node assemble(final Map<String, Object> context, final String entry, final String tag) {
    this.buildingContext = new HashMap<>(context);
    this.context = parseQuery(context);

    this.contextEnum = new HashMap<>();
    this.statements = new ArrayList<>();
    this.batchSize = new ArrayDeque<>();

    this.changesStack = new ArrayDeque<>();
    this.actualPrefix = NamePath.get(tag);

    newBatch();
    step(NamePath.get(entry), "action");

    return endBatch();
  }

  private static Map<NamePath, String> parseQuery(final Map<String, Object> query) {
    final Map<NamePath, String> ctx = new HashMap<>();
    for (final Map.Entry<String, Object> entry : query.entrySet()) {
      ctx.put(parseName(entry.getKey()), entry.getValue().toString());
    }
    return ctx;
  }

  private static NamePath parseName(final String name) {
    final String[] parts = name.split("\\.");
    return NamePath.get(parts[0], Arrays.copyOfRange(parts, 1, parts.length));
  }

  private void step(final NamePath path, final String method) {
    final String name = context.get(path);
    final SsaForm ssa = buildingBlocks.get(StringUtils.dotConc(name, method));
    embedBlock(path, ssa.getEntryPoint());
  }

  private void stepArgument(final NamePath path, final String name, final String method) {
    step(path.resolve(name), method);
  }

  private Block embedBlock(final NamePath path, final Block block) {
    InvariantChecks.checkNotNull(block);

    walkStatements(path, block.getStatements());
    if (blockIsOp(block, SsaOperation.PHI)) {
      return block;
    }
    if (block.getChildren().size() > 1) {
      return embedBranches(path, block);
    }
    return embedSequence(path, block);
  }

  private Block embedSequence(final NamePath path, final Block block) {
    InvariantChecks.checkNotNull(block);

    if (block.getChildren().size() > 0) {
      changes.commit();
      return embedBlock(path, block.getChildren().get(0).block);
    }
    return null;
  }

  private Block embedBranches(final NamePath path, final Block block) {
    InvariantChecks.checkNotNull(block);
    changes.commit();

    Block fence = null;
    final int size = block.getChildren().size();
    final List<NodeOperation> branches = new ArrayList<>(size);
    final Collection<Changes> containers = changes.fork(size);
    final Iterator<Changes> rebasers = containers.iterator();

    changesStack.push(changes);

    final NodeTransformer xform = new NodeTransformer(createRuleset(path));
    for (GuardedBlock guard : block.getChildren()) {
      changes = rebasers.next();

      newBatch(Transformer.transform(guard.guard, xform));
      fence = sameNotNull(fence, embedBlock(path, guard.block));
      branches.add(endBatch());
    }
    changes = changesStack.pop();
    join(changes, block.getChildren(), containers, xform);

    addToBatch(Nodes.or(branches));

    newBatch();
    for (Map.Entry<String, Node> entry : changes.getSummary().entrySet()) {
      final Node node = entry.getValue();
      if (ExprUtils.isOperation(node, StandardOperation.ITE)) {
        addToBatch(Nodes.eq(changes.newLatest(entry.getKey()), node));
      }
    }
    addToBatch(endBatch());
    changes.getSummary().clear();

    return embedSequence(path, fence);
  }

  private static void join(
      Changes repo,
      Collection<GuardedBlock> blocks,
      Collection<Changes> containers, NodeTransformer xform) {
    final Iterator<GuardedBlock> block = blocks.iterator();
    for (Changes diff : containers) {
      final Node guard = Transformer.transform(block.next().guard, xform);
      for (Map.Entry<String, Node> entry : diff.getSummary().entrySet()) {
        final Node fallback = getJointFallback(entry.getKey(), repo, diff);
        if (!fallback.equals(entry.getValue())
            || fallback.getUserData() != entry.getValue().getUserData()) {
          final Node ite = Nodes.ite(guard, entry.getValue(), fallback);
          repo.getSummary().put(entry.getKey(), ite);
        }
      }
    }
  }

  private static Node getJointFallback(String name, Changes master, Changes branch) {
    if (master.getSummary().containsKey(name)) {
      return master.getSummary().get(name);
    }
    final NodeVariable base = branch.getBase(name);
    if (base != null) {
      return base;
    }
    return branch.getLatest(name);
  }

  private static <T> T sameNotNull(T stored, T input) {
    InvariantChecks.checkNotNull(input);
    InvariantChecks.checkTrue(stored == null || stored == input);

    return input;
  }

  private static boolean blockIsOp(Block block, Enum<?> id) {
    final List<NodeOperation> stmts = block.getStatements();
    return stmts.size() == 1 && ExprUtils.isOperation(stmts.get(0), id);
  }

  private final class ModeRule implements TransformerRule {
    private final Enum<?> operation;
    private final NamePath path;
    private final String suffix;
    private final int version;

    ModeRule(final Enum<?> operation, final NamePath path, final String suffix) {
      this.operation = operation;
      this.path = path;
      this.suffix = suffix;
      this.version = (operation == SsaOperation.UPDATE) ? 2 : 1;
    }

    @Override
    public boolean isApplicable(Node node) {
      return ExprUtils.isOperation(node, this.operation);
    }

    @Override
    public Node apply(Node node) {
      final Pair<String, String> pair =
          StringUtils.splitOnLast(Utility.literalOperand(0, node), '.');

      return linkMacro(path, pair.second, suffix, version);
    }
  }

  private NodeVariable linkMacro(
      final NamePath path,
      final String name,
      final String method,
      final int version) {
    stepArgument(path, name, method);

    final NodeVariable tmp =
        scope.fetch(String.format("__tmp_%d", numTemps - 1));

    final NodeVariable var =
        changes.rebase(getVariableName(path, name), tmp.getData(), version);
    addToBatch(Nodes.eq(var, tmp));

    return tmp;
  }

  private String getVariableName(final NamePath path, final String... tail) {
    return NamePath.get(this.actualPrefix.resolve(path.subpath(1)), tail).toString();
  }

  private void linkClosure(final NamePath callerPath,
                           final NamePath calleePath,
                           final Closure closure) {
    final Parameters parameters = getParameters(closure.getOriginName());

    context.put(calleePath, closure.getOriginName());

    for (int i = 0; i < closure.getArguments().size(); ++i) {
      final Node operand = closure.getArgument(i);
      final NamePath paramPath = calleePath.resolve(parameters.getName(i));

      if (ExprUtils.isOperation(operand, SsaOperation.CLOSURE)) {
        linkClosure(callerPath, paramPath, new Closure(operand));
      } else if (ExprUtils.isOperation(operand, SsaOperation.ARGUMENT_LINK)) {
        final String argName = Utility.literalOperand(0, operand);
        final NamePath srcPath = callerPath.resolve(argName);

        final Map<NamePath, String> extension = new HashMap<>();
        for (final Map.Entry<NamePath, String> entry : context.entrySet()) {
          final NamePath path = entry.getKey();
          if (path.startsWith(srcPath)) {
            final NamePath tail = path.subpath(srcPath.getNameCount());
            final NamePath argPath = paramPath.resolve(tail);

            extension.put(argPath, entry.getValue());
            if (entry.getValue().equals("#IMM")) {
              linkArgument(argPath, path);
            }
          }
        }
        extension.put(paramPath, context.get(srcPath));
        context.putAll(extension);

        linkArgument(paramPath, srcPath);
      } else {
        final NodeVariable arg =
            changes.rebase(
              //inner.expression, //.substring(origin.expression.length()),
              getVariableName(paramPath),
              parameters.getType(i).valueUninitialized(),
              1);
        addToBatch(Nodes.eq(arg, operand));
//        walkStatements(origin, Collections.singleton(EQ(arg, operand)));
      }
    }
  }

  private void linkArgument(final NamePath dstPath, final NamePath srcPath) {
    final String argType =
        context.get(srcPath.subpath(0, srcPath.getNameCount() - 1));
    final String localName =
        srcPath.getName(srcPath.getNameCount() - 1).toString();

    final NodeVariable image = getParameters(argType).find(localName);
    final Data data = image.getDataType().valueUninitialized();

    final NodeVariable target =
        changes.rebase(getVariableName(dstPath), data, 1);
    final NodeVariable source =
        changes.rebase(getVariableName(srcPath), data, 1);

    addToBatch(Nodes.eq(target, source));
  }

  private Parameters getParameters(final String callee) {
    final SsaForm ssa = buildingBlocks.get(StringUtils.dotConc(callee, "parameters"));
    return new Parameters(ssa.getEntryPoint().getStatements().get(0));
  }

  private static boolean isArgumentCall(final Node node) {
    final NodeOperation op = (NodeOperation) node;
    return op.getOperand(0).getKind() == Node.Kind.VARIABLE
        && op.getOperand(1).getKind() == Node.Kind.VARIABLE;
  }

  private static Pair<String, String> getArgumentCall(final Node node) {
    return new Pair<>(Utility.variableOperand(0, node).getName(),
        Utility.variableOperand(1, node).getName());
  }

  private Map<Enum<?>, TransformerRule> createRuleset(final NamePath path) {
    final TransformerRule call = new TransformerRule() {
      @Override
      public boolean isApplicable(Node node) {
        return ExprUtils.isOperation(node, SsaOperation.CALL);
      }

      @Override
      public Node apply(Node node) {
        if (isArgumentCall(node)) {
          final Pair<String, String> pair = getArgumentCall(node);
          stepArgument(path, pair.first, pair.second);
        } else {
          final NodeOperation call = (NodeOperation) node;
          final NodeOperation instance = (NodeOperation) call.getOperand(0);

          final String callee = Utility.literalOperand(0, instance);
          final NamePath ctxKey = path.resolve(callee);
          Integer num = contextEnum.get(ctxKey);
          if (num == null) {
            num = 0;
          }
          contextEnum.put(ctxKey, num + 1);

          final NamePath innerPath =
              path.resolve(String.format("%s_%d", callee, num));

          linkClosure(path, innerPath, new Closure(instance));
          step(innerPath, Utility.literalOperand(1, call));
        }
        // Prune custom SSA operation
        return Nodes.TRUE;
      }
    };

    final TransformerRule thisCall = new TransformerRule() {
      @Override
      public boolean isApplicable(Node node) {
        return ExprUtils.isOperation(node, SsaOperation.THIS_CALL);
      }

      @Override
      public Node apply(Node node) {
        step(path, Utility.literalOperand(0, node));
        // Prune custom SSA operation
        return Nodes.TRUE;
      }
    };

    final TransformerRule substitute = new TransformerRule() {
      @Override
      public boolean isApplicable(Node node) {
        return ExprUtils.isOperation(node, SsaOperation.SUBSTITUTE);
      }

      @Override
      public Node apply(Node node) {
        return createTemporary(Utility.variableOperand(0, node).getDataType());
      }
    };

    final Map<Enum<?>, TransformerRule> rules = new IdentityHashMap<>();
    rules.put(SsaOperation.CALL, call);
    rules.put(SsaOperation.THIS_CALL, thisCall);
    rules.put(SsaOperation.SUBSTITUTE, substitute);

    rules.put(SsaOperation.EXPAND,
              new ModeRule(SsaOperation.EXPAND, path, "expand"));

    rules.put(SsaOperation.UPDATE,
              new ModeRule(SsaOperation.UPDATE, path, "update"));

    final TransformerRule rebase = new TransformerRule() {
      @Override
      public boolean isApplicable(Node node) {
        return node.getKind() == Node.Kind.VARIABLE && node.getUserData() != null;
      }

      @Override
      public Node apply(Node node) {
        final NodeVariable var = (NodeVariable) node;
        if (var.getName().indexOf('.') >= 0) {
          return rebaseLocal(var);
        }
        return changes.rebase(var);
      }

      private Node rebaseLocal(NodeVariable node) {
        // drop first entry in name for local variables
        final NamePath name = parseName(node.getName()).subpath(1);

        return changes.rebase(getVariableName(path.resolve(name)),
                              node.getData(),
                              (Integer) node.getUserData());
      }
    };
    rules.put(Node.Kind.VARIABLE, rebase);

    final TransformerRule rotate = new TransformerRule() {
      @Override
      public boolean isApplicable(final Node node) {
        return ExprUtils.isOperation(node, StandardOperation.BVROR);
      }

      @Override
      public Node apply(final Node node) {
        final NodeOperation rotate = (NodeOperation) node;
        final List<Node> operands = rotate.getOperands();

        final Node amount = operands.get(1);
        final Node origin = operands.get(0);

        if (ExprUtils.isValue(amount) && amount.isType(DataType.INTEGER)) {
          // reverse argument order
          return new NodeOperation(rotate.getOperationId(), amount, origin);
        }
        // mask = 2^n - 1
        // --> (bvsub (bvshl 1 n) 1)
        //
        // x >>> n
        // --> (x >> n) | ((x & mask) << (size(x) - n)
        // --> (bvor (bvshr x n) (bvshl (bvand x mask) (bvsub size(x) n)))
        final int bitsize = origin.getDataType().getSize();
        final Node one = NodeValue.newBitVector(BitVector.valueOf(1, bitsize));
        final Node n = castBitVector(amount, origin);
        final Node size = NodeValue.newBitVector(BitVector.valueOf(bitsize, bitsize));

        final Node sizeMinusN = Nodes.bvsub(size, n);
        final Node pow2n = Nodes.bvlshl(one, n);
        final Node mask = Nodes.bvsub(pow2n, one);
        final Node shrX = Nodes.bvlshr(origin, n);
        final Node maskX = Nodes.bvand(origin, mask);
        final Node shlMasked = Nodes.bvlshl(maskX, sizeMinusN);

        return Nodes.bvor(shrX, shlMasked);
      }
    };

    rules.put(StandardOperation.BVROR, rotate);
    return rules;
  }

  private static Node castBitVector(final Node src, final Node dst) {
    final int srcSize = src.getDataType().getSize();
    final int dstSize = dst.getDataType().getSize();

    if (srcSize < dstSize) {
      return Nodes.bvzeroext(dstSize - srcSize, src);
    }

    if (srcSize > dstSize) {
      return Nodes.bvextract(dstSize - 1, 0, src);
    }

    return src;
  }

  private void walkStatements(final NamePath path, final Collection<? extends Node> statements) {
    final NodeTransformer transformer =
        new NodeTransformer(createRuleset(path));
    transformer.walk(statements);

    // It is known that resulting sequence will be inverted on block granularity
    for (Node node : transformer.getResult()) {
      addToBatch(node);
    }
  }

  private void addToBatch(Node node) {
    if (ExprUtils.isOperation(node, SsaOperation.PHI) || nodeIsTrue(node)) {
      return;
    }
    this.statements.add(node);
    this.batchSize.push(this.batchSize.pop() + 1);
  }

  private void addToBatch(Collection<? extends Node> batch) {
    this.statements.addAll(batch);
    this.batchSize.push(this.batchSize.pop() + batch.size());
  }

  private boolean nodeIsTrue(Node node) {
    if (node.equals(Nodes.TRUE)) {
      return true;
    }
    if (!ExprUtils.isOperation(node, StandardOperation.AND)) {
      return false;
    }
    final NodeOperation op = (NodeOperation) node;
    return op.getOperandCount() == 1 && op.getOperand(0).equals(Nodes.TRUE);
  }

  private void newBatch() {
    this.batchSize.push(0);
  }

  private void newBatch(final Node node) {
    newBatch();
    addToBatch(node);
  }

  private NodeOperation endBatch() {
    final List<Node> operands =
        this.statements.subList(this.statements.size() - this.batchSize.pop(),
                                this.statements.size());

    if (operands.isEmpty()) {
      return Nodes.and(Nodes.TRUE);
    }
    final NodeOperation batch = Nodes.and(operands);
    operands.clear();

    return batch;
  }

  private NodeVariable createTemporary(DataType type) {
    return scope.create(String.format("__tmp_%d", numTemps++),
                        type.valueUninitialized());
  }
}
