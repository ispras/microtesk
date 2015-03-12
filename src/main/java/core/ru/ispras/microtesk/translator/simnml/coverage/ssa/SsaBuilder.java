/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.simnml.coverage.ssa;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.NodeTransformer;
import ru.ispras.fortress.transformer.TransformerRule;

import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.ir.location.*;
import ru.ispras.microtesk.translator.simnml.ir.primitive.*;
import ru.ispras.microtesk.translator.simnml.ir.expression.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ru.ispras.microtesk.translator.simnml.coverage.ssa.Expression.*;

final class SsaBuilder {
  private final String tag;
  private final List<Statement> code;

  private SsaScope scope;

  private int numBlocks;
  private int numTemps;
  private BlockBuilder blockBuilder;
  private Deque<Block> stack;
  private List<Block> blocks;

  private static final class LValue {
    public final Variable base;
    public final boolean macro;

    public final Node index;
    public final Node minorBit;
    public final Node majorBit;

    public final DataType baseType;
    public final DataType sourceType;
    public final DataType targetType;

    public LValue(Variable base,
                  boolean macro,
                  Node index,
                  Node minorBit,
                  Node majorBit,
                  DataType baseType,
                  DataType sourceType,
                  DataType targetType) {
      this.base = base;
      this.macro = macro;
      this.index = index;
      this.minorBit = minorBit;
      this.majorBit = majorBit;
      this.baseType = baseType;
      this.sourceType = sourceType;
      this.targetType = targetType;
    }

    public boolean isArray() {
      return index != null;
    }

    public boolean hasBitfield() {
      return minorBit != null && majorBit != null;
    }

    public boolean hasStaticBitfield() {
      return hasBitfield() &&
          minorBit.getKind() == Node.Kind.VALUE &&
          majorBit.getKind() == Node.Kind.VALUE;
    }

    public boolean isMacro() {
        return macro;
    }
  }

  private void addToContext(NodeOperation node) {
    blockBuilder.add(node);
  }

  private void addToContext(List<NodeOperation> list) {
    blockBuilder.addAll(list);
  }

  private NodeVariable createTemporary(DataType type) {
    return createTemporary(type.valueUninitialized());
  }

  private NodeVariable createTemporary(Data data) {
    final NodeVariable var = scope.create(String.format("__tmp_%d", numTemps++),
                                          data);
    var.setUserData(2);
    return var;
  }

  private NodeVariable storeVariable(Variable var) {
    if (scope.contains(var.getName())) {
      return scope.fetch(var.getName());
    }
    return scope.create(var.getName(), var.getData());
  }

  private NodeVariable updateVariable(Variable var) {
    if (!scope.contains(var.getName())) {
      scope.create(var.getName(), var.getData());
    }
    return scope.update(var.getName());
  }

  private static Node macro(Enum<?> op, Variable v) {
    return new NodeOperation(op, new NodeVariable(v));
  }

  /**
   * Assemble LValue object into Node instance representing rvalue.
   * Named variables are considered latest in current builder state.
   * Variables are not updated by this method.
   */
  private Node createRValue(LValue lvalue) {
    Node root = null;
    if (lvalue.isMacro()) {
      root = macro(SsaOperation.EXPAND, lvalue.base);
    } else {
      root = storeVariable(lvalue.base);
    }

    if (lvalue.isArray()) {
      root = SELECT(root, lvalue.index);
    }

    if (lvalue.hasStaticBitfield()) {
      return EXTRACT(
          (NodeValue) lvalue.minorBit,
          (NodeValue) lvalue.majorBit,
          root);
    }

    if (lvalue.hasBitfield()) {
      return EXTRACT(NodeValue.newInteger(0),
                     lvalue.targetType.getSize(),
                     new NodeOperation(StandardOperation.BVLSHR, root, lvalue.minorBit));
    }
    return root;
  }

  private Node[] createRValues(LValue[] lhs) {
    final Node[] arg = new Node[lhs.length];
    for (int i = 0; i < arg.length; ++i) {
      arg[i] = createRValue(lhs[i]);
    }
    return arg;
  }

  private LValue[] fetchConcatLValues(LocationConcat conc) {
    final LValue[] lhs = new LValue[conc.getLocations().size()];
    for (int i = 0; i < lhs.length; ++i)
      lhs[i] = createLValue(conc.getLocations().get(i));

    return lhs;
  }

  private void convertAssignment(StatementAssignment s) {
    assign(s.getLeft(), s.getRight().getNode());
  }

  private void assign(Location loc, Node value) {
    acquireBlockBuilder();
    final Node rhs = convertExpression(value);
    if (loc instanceof LocationAtom) {
      assignToAtom((LocationAtom) loc, rhs);
    } else if (loc instanceof LocationConcat) {
      assignToConcat((LocationConcat) loc, rhs);
    } else {
      throw new IllegalArgumentException("Unknown Location subtype");
    }
  }

  private void assignToAtom(LocationAtom lhs, Node value) {
    final LValue lvalue = createLValue(lhs);
    if (lvalue.isArray()) {
      addToContext(EQ(updateArrayElement(lvalue), value));
    } else {
      addToContext(EQ(updateScalar(lvalue), value));
    }
  }

  private final class SsaValue {
    public final Node older;
    public final Node newer;

    private SsaValue(LValue lvalue) {
      if (lvalue.isMacro()) {
        this.older = macro(SsaOperation.EXPAND, lvalue.base);
        this.newer = macro(SsaOperation.UPDATE, lvalue.base);
      } else {
        this.older = storeVariable(lvalue.base);
        this.newer = updateVariable(lvalue.base);
      }
    }
  }

  private Node updateLValue(LValue lvalue) {
    if (lvalue.isMacro()) {
      return macro(SsaOperation.UPDATE, lvalue.base);
    }
    return updateVariable(lvalue.base);
  }

  private Node updateScalar(LValue lvalue) {
    if (!lvalue.hasBitfield()) {
      return updateLValue(lvalue);
    }
    final SsaValue scalar = new SsaValue(lvalue);
    if (lvalue.hasStaticBitfield()) {
      return updateStaticSubvector(scalar.newer, scalar.older, lvalue);
    }
    return updateDynamicSubvector(scalar.newer, scalar.older, lvalue);
  }

  private Node updateArrayElement(LValue lvalue) {
    final SsaValue array = new SsaValue(lvalue);

    final NodeVariable newer = createTemporary(lvalue.sourceType);
    addToContext(EQ(array.newer, STORE(array.older, lvalue.index, newer)));

    if (!lvalue.hasBitfield()) {
      return newer;
    }

    final NodeVariable older = createTemporary(lvalue.sourceType);
    addToContext(EQ(older, SELECT(array.older, lvalue.index)));

    if (lvalue.hasStaticBitfield()) {
      return updateStaticSubvector(newer, older, lvalue);
    }
    return updateDynamicSubvector(newer, older, lvalue);
  }

  private Node updateStaticSubvector(Node newer, Node older, LValue lvalue) {
    final int olderHiBit = older.getDataType().getSize() - 1;
    final int newerHiBit = newer.getDataType().getSize() - 1;

    if (olderHiBit != newerHiBit) {
      throw new IllegalArgumentException("Overlapping variables with different sizes is forbidden");
    }

    final int hibit = olderHiBit;
    final NodeValue minor = (NodeValue) lvalue.minorBit;
    final NodeValue major = (NodeValue) lvalue.majorBit;

    addToContext(EQ(EXTRACT(0, minor, newer), EXTRACT(0, minor, older)));
    addToContext(EQ(EXTRACT(major, hibit, newer), EXTRACT(major, hibit, older)));

    return EXTRACT(minor, major, newer);
  }

  private Node updateDynamicSubvector(Node newer, Node older, LValue lvalue) {
    final int olderSize = older.getDataType().getSize();
    final int newerSize = newer.getDataType().getSize();

    if (olderSize != newerSize)
      throw new IllegalArgumentException("Overlapping variables with different sizes is forbidden");

    final int bitsize = olderSize;

    final NodeOperation shLeftAmount =
        new NodeOperation(StandardOperation.BVSUB,
            NodeValue.newBitVector(BitVector.valueOf(bitsize, bitsize)),
            lvalue.minorBit);

    addToContext(EQ(
        new NodeOperation(StandardOperation.BVLSHL, older, shLeftAmount),
        new NodeOperation(StandardOperation.BVLSHL, newer, shLeftAmount)));

    final NodeOperation shRightAmount =
        new NodeOperation(StandardOperation.BVADD,
                          lvalue.majorBit,
                          NodeValue.newBitVector(BitVector.valueOf(1, bitsize)));

    addToContext(EQ(
        new NodeOperation(StandardOperation.BVLSHR, older, shRightAmount),
        new NodeOperation(StandardOperation.BVLSHR, newer, shRightAmount)));

    final DataType subtype = lvalue.targetType;
    final NodeVariable subvector = createTemporary(subtype);

    addToContext(EQ(
        EXTRACT(
            NodeValue.newInteger(0),
            NodeValue.newInteger(subtype.getSize()),
            new NodeOperation(StandardOperation.BVLSHR, newer, lvalue.minorBit)),
        subvector));

    return subvector;
  }

  private void assignToConcat(LocationConcat lhs, Node value) {
    final LValue[] lvalues = fetchConcatLValues(lhs);
    final Node[] arg = new Node[lvalues.length];

    for (int i = 0; i < lvalues.length; ++i) {
      if (lvalues[i].isArray()) {
        arg[i] = updateArrayElement(lvalues[i]);
      } else {
        arg[i] = updateScalar(lvalues[i]);
      }
    }
    addToContext(EQ(CONCAT(arg), value));
  }

  private void convertCondition(StatementCondition s) {
    acquireBlockBuilder();
    final BranchPoint branchPoint = collectConditions(s);
    finalizeBlock();

    final Block phi = BlockBuilder.createPhi();
    final String phiName = generateBlockName();
    final List<GuardedBlock> mergePoint =
        Collections.singletonList(new GuardedBlock(phiName, TRUE, phi));
    final List<GuardedBlock> children = new ArrayList<>(s.getBlockCount());

    for (int i = 0; i < s.getBlockCount(); ++i) {
      final StatementCondition.Block codeBlock = s.getBlock(i);
      final SsaForm ssa = convertNested(codeBlock.getStatements());
      children.add(new GuardedBlock(branchPoint.names.get(i),
                                    branchPoint.guards.get(i),
                                    ssa.getEntryPoint()));
      for (Block block : ssa.getExitPoints()) {
        block.setChildren(mergePoint);
      }
      blocks.addAll(ssa.getBlocks());
    }
    if (!s.getBlock(s.getBlockCount() - 1).isElseBlock()) {
      children.add(new GuardedBlock(phiName,
                                    NOT(OR(branchPoint.negateGuards())),
                                    phi));
    }
    final Block block = stack.pop();
    block.setChildren(children);
    block.setSuccessor(phi);

    stack.push(phi);
    blocks.add(phi);
  }

  private SsaForm convertNested(List<Statement> statements) {
    final SsaBuilder builder = new SsaBuilder(this.tag, statements);
    builder.numBlocks = this.numBlocks;
    final SsaForm ssa = builder.build();
    this.numBlocks = builder.numBlocks;

    return ssa;
  }

  private String generateBlockName() {
    return String.format("%s.block_%d", tag, numBlocks++);
  }

  private final class BranchPoint {
    public final List<String> names;
    public final List<Node> guards;

    public BranchPoint(int n) {
      this.names = new ArrayList<>(n);
      this.guards = new ArrayList<>(n);
    }

    public void addBranch(StatementCondition.Block block) {
      // TODO get user-defined name whenever specified
      final String name = generateBlockName();
      final Node guard = scope.create(name, DataType.BOOLEAN.valueUninitialized());

      if (!block.isElseBlock()) {
        final Node condition =
            convertExpression(block.getCondition().getNode());
        if (guards.isEmpty()) {
          addToContext(EQ(guard, condition));
        } else {
          addToContext(EQ(guard, AND(NOT(OR(negateGuards())), condition)));
        }
      } else {
        addToContext(EQ(guard, NOT(OR(negateGuards()))));
      }
      names.add(name);
      guards.add(guard);
    }

    public Node[] negateGuards() {
      return guards.toArray(new Node[guards.size()]);
    }
  }

  private BranchPoint collectConditions(StatementCondition cond) {
    final BranchPoint point = new BranchPoint(cond.getBlockCount());
    for (int i = 0; i < cond.getBlockCount(); ++i) {
      point.addBranch(cond.getBlock(i));
    }
    return point;
  }

  private void convertCall(StatementAttributeCall s) {
    finalizeBlock();
    pushConsecutiveBlock(
        BlockBuilder.createCall(s.getCalleeName(), s.getAttributeName()));
  }

  private void acquireBlockBuilder() {
    if (blockBuilder == null) {
      blockBuilder = new BlockBuilder();
      scope = SsaScopeFactory.createScope();
      numTemps = 0;
    }
  }

  private void finalizeBlock() {
    if (blockBuilder != null) {
      pushConsecutiveBlock(blockBuilder.build());
      blockBuilder = null;
    }
  }

  private void pushConsecutiveBlock(Block block) {
    if (!stack.isEmpty()) {
      stack.pop().setChildren(
          Collections.singletonList(new GuardedBlock(generateBlockName(), TRUE, block)));
    }
    stack.push(block);
    blocks.add(block);
  }

  private void convertCode(List<Statement> code) {
    for (Statement s : code) {
      switch (s.getKind()) {
        case ASSIGN:
          convertAssignment((StatementAssignment) s);
          break;

        case COND:
          convertCondition((StatementCondition) s);
          break;

        case CALL:
          convertCall((StatementAttributeCall) s);
          break;

        case FUNCALL: // FIXME
        case FORMAT:
        case STATUS: // skip
          break;

        default:
          throw new IllegalArgumentException("Unexpected statement: " + s.getKind());
      }
    }
  }

  private LValue createLValue(LocationAtom atom) {
    String name = atom.getName();
    if (atom.getSource().getSymbolKind() == ESymbolKind.ARGUMENT)
      name = tag + "." + atom.getName();
    final boolean macro = isModeArgument(atom);

    final DataType sourceType = Converter.getDataTypeForModel(atom.getSource().getType());
    final DataType targetType = Converter.getDataTypeForModel(atom.getType());

    Node index;
    DataType baseType;

    if (atom.getIndex() != null) {
      // Array type with Integer indices represented with BitVector 32
      index = convertExpression(atom.getIndex().getNode());
      baseType = DataType.MAP(DataType.BIT_VECTOR(32), sourceType);
    } else {
      index = null;
      baseType = sourceType;
    }

    final Variable base = new Variable(name, baseType);
    if (atom.getBitfield() != null) {
      final Node minor = convertExpression(atom.getBitfield().getFrom().getNode());
      final Node major = convertExpression(atom.getBitfield().getTo().getNode());
      return new LValue(base, macro, index, minor, major, baseType, sourceType, targetType);
    }
    return new LValue(base, macro, index, null, null, baseType, sourceType, targetType);
  }

  private static boolean isModeArgument(LocationAtom atom) {
    if (atom.getSource() instanceof LocationAtom.PrimitiveSource) {
      final LocationAtom.PrimitiveSource source =
        (LocationAtom.PrimitiveSource) atom.getSource();
      return source.getPrimitive().getKind() == Primitive.Kind.MODE;
    }
    return false;
  }

  /**
   * Convert given expression accordingly to current builder state.
   * Replaces named variables with versioned equivalents. Current builder
   * state versions are used. Context and variables are not updated.
   *
   * @param expression Expression to be converted.
   */
  private Node convertExpression(Node expression) {
    final TransformerRule rule = new TransformerRule() {
      @Override
      public boolean isApplicable(Node in) {
        return locationFromNodeVariable(in) != null;
      }

      @Override
      public Node apply(Node in) {
        final Location loc = locationFromNodeVariable(in);
        if (loc instanceof LocationAtom)
          return createRValue(createLValue((LocationAtom) loc));
        else if (loc instanceof LocationConcat)
          return CONCAT(createRValues(fetchConcatLValues((LocationConcat) loc)));
        else
          throw new UnsupportedOperationException();
      }
    };

    final NodeTransformer transformer = new NodeTransformer();
    transformer.addRule(Node.Kind.VARIABLE, rule);
    transformer.walk(expression);

    return transformer.getResult().iterator().next();
  }

  /**
   * Extract Location user-data from Node instance.
   *
   * @return Location object if correct instance is attached to node,
   * null otherwise.
   */
  private static Location locationFromNodeVariable(Node node) {
    if (node.getKind() == Node.Kind.VARIABLE && node.getUserData() instanceof NodeInfo) {
      final NodeInfo info = (NodeInfo) node.getUserData();
      if (info.getSource() instanceof Location)
        return (Location) info.getSource();
    }
    return null;
  }

  public SsaBuilder(String tag, List<Statement> code) {
    notnull(tag);
    notnull(code);

    this.tag = tag;
    this.code = code;
    this.scope = null;
    this.numBlocks = 0;
    this.numTemps = 0;
    this.blockBuilder = null;
    this.stack = new ArrayDeque<>();
    this.blocks = new ArrayList<>();
  }

  public SsaForm build() {
    // probably never built
    if (blocks.isEmpty()) {
      convertCode(code);
      finalizeBlock();
    }
    // still empty?
    if (blocks.isEmpty()) {
      final Block empty = BlockBuilder.createEmpty();
      return new SsaForm(empty, empty, Collections.singleton(empty));
    }
    return new SsaForm(blocks.get(0),
                       blocks.get(blocks.size() - 1),
                       blocks);
  }

  public static SsaForm macroExpansion(String tag, Expr expr) {
    final SsaBuilder builder =
        new SsaBuilder(tag, Collections.<Statement>emptyList());
    builder.acquireBlockBuilder();
    builder.addToContext(EQ(builder.convertExpression(expr.getNode()),
                            builder.createOutput(Converter.toFortressData(expr.getValueInfo()))));
    return builder.build();
  }

  public static SsaForm macroUpdate(String tag, Expr expr) {
    final SsaBuilder builder =
        new SsaBuilder(tag, Collections.<Statement>emptyList());
    builder.acquireBlockBuilder();
    final Location loc = locationFromNodeVariable(expr.getNode());
    if (loc != null) {
      builder.assign(locationFromNodeVariable(expr.getNode()),
                     builder.createOutput(Converter.toFortressData(expr.getValueInfo())));
    }
    return builder.build();
  }

  private NodeOperation createOutput(Data data) {
    return new NodeOperation(SsaOperation.SUBSTITUTE, createTemporary(data));
  }

  private static void notnull(Object o) {
    if (o == null) {
      throw new NullPointerException();
    }
  }
}

final class FlatScope implements SsaScope {
  private static final String TEMPORARY_NAME = "tmp";

  private int numTemporaries;
  private final Map<String, NodeVariable> variables;
  
  FlatScope() {
    this.numTemporaries = 0;
    this.variables = new LinkedHashMap<>();
  }

  @Override
  public boolean contains(String name) {
    notnull(name);
    return variables.containsKey(name);
  }

  @Override
  public NodeVariable create(String name, Data data) {
    notnull(name);
    notnull(data);

    if (this.contains(name)) {
      throw new IllegalArgumentException("Attempt to override variable " + name);
    }
    final NodeVariable node = new NodeVariable(new Variable(name, data));
    variables.put(name, node);
    return node;
  }

  @Override
  public NodeVariable fetch(String name) {
    notnull(name);
    if (variables.containsKey(name)) {
      return variables.get(name);
    }
    throw new IllegalArgumentException("Attempt to access undeclared variable " + name);
  }

  @Override
  public NodeVariable update(String name) {
    return fetch(name);
  }

  public NodeVariable createTemporary(Data data) {
    return create(String.format("%s!%d", TEMPORARY_NAME, numTemporaries++), data);
  }

  private static void notnull(Object o) {
    if (o == null) {
      throw new NullPointerException();
    }
  }
}
