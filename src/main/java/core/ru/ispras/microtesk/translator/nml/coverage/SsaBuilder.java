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

package ru.ispras.microtesk.translator.nml.coverage;

import static ru.ispras.fortress.util.InvariantChecks.checkFalse;
import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.microtesk.translator.nml.coverage.Expression.AND;
import static ru.ispras.microtesk.translator.nml.coverage.Expression.CONCAT;
import static ru.ispras.microtesk.translator.nml.coverage.Expression.EQ;
import static ru.ispras.microtesk.translator.nml.coverage.Expression.EXTRACT;
import static ru.ispras.microtesk.translator.nml.coverage.Expression.NOT;
import static ru.ispras.microtesk.translator.nml.coverage.Expression.OR;
import static ru.ispras.microtesk.translator.nml.coverage.Expression.SELECT;
import static ru.ispras.microtesk.translator.nml.coverage.Expression.STORE;
import static ru.ispras.microtesk.translator.nml.coverage.Expression.TRUE;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import ru.ispras.microtesk.translator.nml.ir.IrInquirer;
import ru.ispras.microtesk.translator.nml.ir.expression.Converter;
import ru.ispras.microtesk.translator.nml.ir.expression.Expr;
import ru.ispras.microtesk.translator.nml.ir.expression.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.location.Location;
import ru.ispras.microtesk.translator.nml.ir.location.LocationAtom;
import ru.ispras.microtesk.translator.nml.ir.location.LocationConcat;
import ru.ispras.microtesk.translator.nml.ir.primitive.Instance;
import ru.ispras.microtesk.translator.nml.ir.primitive.InstanceArgument;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAssignment;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementCondition;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFunctionCall;
import ru.ispras.microtesk.translator.nml.ir.shared.Alias;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

final class SsaBuilder {
  private static final String ARRAY_INDEX = "index";
  private static final String ARRAY_VALUE = "value";
  private static final String ARRAY_EVENT_READ = "read";
  private static final String ARRAY_EVENT_WRITE = "write";

  private final IrInquirer inquirer;
  private final String prefix;
  private final String suffix;
  private final String extendedPrefix;
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

    // type of container object (array or scalar)
    public final DataType baseType;

    // type of complete self object (array element or scalar)
    public final DataType sourceType;

    // type after applying bitfields to self object
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

    public LValue stripBitfield() {
      return new LValue(base, macro, index, null, null, baseType, sourceType, targetType);
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
    return newUniqueTmp(String.format("__tmp_%d", numTemps++), data);
  }

  private NodeVariable newUniqueTmp(final String name, final DataType type) {
    return newUniqueTmp(name, type.valueUninitialized());
  }

  private NodeVariable newUniqueTmp(final String name, final Data data) {
    final NodeVariable var = scope.create(name, data);
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

  private Node createRValue(final LValue lvalue) {
    return createRValue(lvalue, true);
  }

  /**
   * Assemble LValue object into Node instance representing rvalue.
   * Named variables are considered latest in current builder state.
   * Variables are not updated by this method.
   */
  private Node createRValue(LValue lvalue, boolean signal) {
    Node root = null;
    if (lvalue.isMacro()) {
      root = macro(SsaOperation.EXPAND, lvalue.base);
    } else {
      root = storeVariable(lvalue.base);
    }

    if (lvalue.isArray()) {
      final Node element = SELECT(root, lvalue.index);
      if (signal) {
        signalArrayEvent(root, ARRAY_EVENT_READ, lvalue.index, element);
      }

      root = element;
    }

    if (lvalue.hasStaticBitfield()) {
      return EXTRACT(
          (NodeValue) lvalue.minorBit,
          (NodeValue) lvalue.majorBit,
          root);
    }

    if (lvalue.hasBitfield()) {
      return EXTRACT(0,
                     lvalue.targetType.getSize(),
                     new NodeOperation(StandardOperation.BVLSHR, root, lvalue.minorBit));
    }
    return root;
  }

  private void signalArrayEvent(final Node array,
                                final String event,
                                final Node index,
                                final Node value) {
    final NodeVariable var = (NodeVariable) array;
    final String fmt = String.format("%s.%s.%%s.%d", var.getName(), event, numTemps++);
    addToContext(EQ(newUniqueTmp(String.format(fmt, ARRAY_INDEX), index.getDataType()), index));
    addToContext(EQ(newUniqueTmp(String.format(fmt, ARRAY_VALUE), value.getDataType()), value));
  }

  private void signalBranchEvent(final Node address) {
    final String name = String.format("branch.address.%d", numTemps++);
    addToContext(EQ(newUniqueTmp(name, address.getDataType()), address));
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
    if (inquirer.isPC(lhs)) {
      final Node address = createRValue(lvalue.stripBitfield(), false);
      signalBranchEvent(address);
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

    signalArrayEvent(array.newer, ARRAY_EVENT_WRITE, lvalue.index, newer);

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


    final int lower = minor.getInteger().intValue() - 1;
    if (lower >= 0) {
      addToContext(EQ(EXTRACT(0, lower, newer), EXTRACT(0, lower, older)));
    }

    final int upper = major.getInteger().intValue() + 1;
    if (upper <= hibit) {
      addToContext(EQ(EXTRACT(upper, hibit, newer), EXTRACT(upper, hibit, older)));
    }
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
        EXTRACT(0,
                subtype.getSize(),
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

  private SsaForm convertNested(final List<Statement> statements) {
    final SsaBuilder builder =
        new SsaBuilder(inquirer, prefix, suffix, extendedPrefix, statements);
    builder.numBlocks = this.numBlocks;
    final SsaForm ssa = builder.build();
    this.numBlocks = builder.numBlocks;

    return ssa;
  }

  private String generateBlockName() {
    return String.format("%s.block_%d", extendedPrefix, numBlocks++);
  }

  private final class BranchPoint {
    public final List<String> names;
    public final List<Node> guards;

    public BranchPoint(int n) {
      this.names = new ArrayList<>(n);
      this.guards = new ArrayList<>(n);
    }

    public void addBranch(StatementCondition.Block block) {
      final NodeVariable guard = createMarks(block);

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
      names.add(guard.getName());
      guards.add(guard);
    }

    public Node[] negateGuards() {
      return guards.toArray(new Node[guards.size()]);
    }

    private NodeVariable createMarks(final StatementCondition.Block block) {
      final String blockId = generateBlockName();
      final NodeVariable guard = createGuard(blockId);
      final List<Node> special = new ArrayList<>();

      for (final Statement s : block.getStatements()) {
        if (s.getKind() == Statement.Kind.FUNCALL) {
          final StatementFunctionCall funcall = (StatementFunctionCall) s;
          final String callee = funcall.getName();

          final String markName;
          if (callee.equals("mark")) {
            markName = String.format("%s.%s", extendedPrefix, funcall.getArgument(0));
          } else if (callee.equals("undefined") || callee.equals("unpredicted")) {
            markName = String.format("%s.%s", blockId, callee);
          } else if (callee.equals("exception")) {
            markName = String.format("%s.exception.%s", extendedPrefix, funcall.getArgument(0));
          } else {
            continue;
          }

          final Node mark = createGuard(markName);
          if (!callee.equals("mark")) {
            special.add(mark);
          }
          addToContext(EQ(guard, mark));
        }
      }
      if (!special.isEmpty()) {
        addToContext(new NodeOperation(SsaOperation.MARK, special));
      }
      return guard;
    }

    private NodeVariable createGuard(final String name) {
      return scope.create(name, DataType.BOOLEAN.valueUninitialized());
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
    final NodeOperation node;
    if (isInstanceCall(s)) {
      acquireBlockBuilder();
      node = new NodeOperation(SsaOperation.CALL,
                               instanceReference(s.getCalleeInstance()),
                               newNamed(s.getAttributeName()));
    } else if (isThisCall(s)) {
      node = new NodeOperation(SsaOperation.THIS_CALL, newNamed(s.getAttributeName()));
    } else {
      node = new NodeOperation(SsaOperation.CALL,
                               newNamed(s.getCalleeName()),
                               newNamed(s.getAttributeName()));
    }
    finalizeBlock();
    pushConsecutiveBlock(BlockBuilder.createSingleton(node));
  }

  private Node instanceReference(final Instance instance) {
    final List<Node> operands = new ArrayList<>(instance.getArguments().size() + 1);
    final PrimitiveAND origin = (PrimitiveAND) instance.getPrimitive();
    operands.add(newNamed(origin.getName()));

    final Iterator<Primitive> parameters =
        origin.getArguments().values().iterator();

    for (final InstanceArgument arg : instance.getArguments()) {
      final Primitive parameter = parameters.next();

      switch (arg.getKind()) {
      case INSTANCE:
        operands.add(instanceReference(arg.getInstance()));
        break;

      case PRIMITIVE:
        if (parameter.getKind() == Primitive.Kind.IMM) {
          final LocationAtom atom =
              LocationAtom.createPrimitiveBased(arg.getName(), arg.getPrimitive());
          final Node value = createRValue(createLValue(atom));
          operands.add(value);
        } else {
          final Node link = new NodeOperation(SsaOperation.ARGUMENT_LINK,
                                              newNamed(arg.getName()));
          operands.add(link);
        }
        break;

      case EXPR:
        operands.add(convertExpression(arg.getExpr().getNode()));
        break;
      }
    }
    return new NodeOperation(SsaOperation.CLOSURE, operands);
  }

  private static NodeVariable newNamed(final String name) {
    return new NodeVariable(name, DataType.BOOLEAN);
  }

  private static boolean isThisCall(final StatementAttributeCall s) {
    return s.getCalleeName() == null && s.getCalleeInstance() == null;
  }

  private static boolean isInstanceCall(final StatementAttributeCall s) {
    return s.getCalleeName() == null && s.getCalleeInstance() != null;
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
    switch (atom.getSource().getSymbolKind()) {
    case ARGUMENT:
      name = Utility.dotConc(prefix, atom.getName());
      break;

    case MEMORY: // FIXME recursive processing required
      final MemoryExpr memory =
          ((LocationAtom.MemorySource) atom.getSource()).getMemory();
      final Alias alias = memory.getAlias();
      if (alias != null) {
        return createLValue((LocationAtom) alias.getLocation());
      }
      break;

    default:
    }

    final boolean macro = isModeArgument(atom);

    final DataType sourceType = Converter.getDataTypeForModel(atom.getSource().getType());
    final DataType targetType = Converter.getDataTypeForModel(atom.getType());

    Node index = null;
    DataType baseType = sourceType;

    if (atom.getIndex() != null) {
      // only ESymbolKind.MEMORY can be indexed
      final MemoryExpr memory =
          ((LocationAtom.MemorySource) atom.getSource()).getMemory();

      // get required bitsize of index
      final int size = memory.getSize().subtract(BigInteger.ONE).bitLength();

      // 'var x[type]' is considered as array of length 1 allowing x[0], skip
      if (size > 0) {
        index = convertExpression(atom.getIndex().getNode());
        baseType = DataType.MAP(DataType.BIT_VECTOR(size), sourceType);
      }
    }

    final Variable base = new Variable(name, baseType);
    if (atom.getBitfield() != null) {
      final Node major = convertExpression(atom.getBitfield().getFrom().getNode());
      final Node minor = convertExpression(atom.getBitfield().getTo().getNode());
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
        if (loc instanceof LocationAtom) {
          final Node rval = createRValue(createLValue((LocationAtom) loc));
          return IntegerCast.cast(rval, getCastType(in));
        } else if (loc instanceof LocationConcat)
          return CONCAT(createRValues(fetchConcatLValues((LocationConcat) loc)));
        else
          throw new UnsupportedOperationException();
      }
    };

    final TransformerRule int2bv = new TransformerRule() {
      @Override
      public boolean isApplicable(final Node node) {
        final NodeInfo info = getNodeInfo(node);
        return node.getKind() == Node.Kind.VALUE &&
               node.isType(DataType.INTEGER) &&
               info != null &&
               info.isCoersionApplied();
      }

      @Override
      public Node apply(final Node node) {
        final Data data =
            Converter.toFortressData(getNodeInfo(node).getValueInfo());
        return IntegerCast.cast(node, data.getType());
      }
    };

    final NodeTransformer transformer = new NodeTransformer();
    transformer.addRule(Node.Kind.VARIABLE, rule);
    transformer.addRule(Node.Kind.VALUE, int2bv);
    transformer.walk(expression);

    return transformer.getResult().iterator().next();
  }

  private static DataType getCastType(final Node node) {
    final NodeInfo info = getNodeInfo(node);
    if (info == null) {
      return node.getDataType();
    }
    return Converter.toFortressData(info.getValueInfo()).getType();
  }

  /**
   * Extract Location user-data from Node instance.
   *
   * @return Location object if correct instance is attached to node,
   * null otherwise.
   */
  private static Location locationFromNodeVariable(Node node) {
    final NodeInfo info = getNodeInfo(node);
    if (node.getKind() == Node.Kind.VARIABLE && info != null) {
      if (info.getSource() instanceof Location)
        return (Location) info.getSource();
    }
    return null;
  }

  private static NodeInfo getNodeInfo(final Node node) {
    if (node.getUserData() instanceof NodeInfo) {
      return (NodeInfo) node.getUserData();
    }
    return null;
  }

  public SsaBuilder(final IrInquirer inquirer, final String prefix, final String attribute, final List<Statement> code) {
    this(inquirer, prefix, attribute, Utility.dotConc(prefix, attribute), code);

    checkNotNull(prefix);
    checkNotNull(attribute);
    checkFalse(attribute.isEmpty());
    checkNotNull(code);
  }

  private SsaBuilder(final IrInquirer inquirer,
                     final String prefix,
                     final String suffix,
                     final String extended,
                     final List<Statement> code) {
    this.inquirer = inquirer;
    this.prefix = prefix;
    this.suffix = suffix;
    this.extendedPrefix = extended;
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

  public static SsaForm macroExpansion(final IrInquirer inquirer, final String prefix, final Expr expr) {
    final SsaBuilder builder = newConverter(inquirer, prefix);
    builder.acquireBlockBuilder();
    builder.addToContext(EQ(builder.convertExpression(expr.getNode()),
                            builder.createOutput(Converter.toFortressData(expr.getValueInfo()))));
    return builder.build();
  }

  public static SsaForm macroUpdate(final IrInquirer inquirer, final String prefix, final Expr expr) {
    final SsaBuilder builder = newConverter(inquirer, prefix);
    builder.acquireBlockBuilder();
    final Location loc = locationFromNodeVariable(expr.getNode());
    if (loc != null) {
      builder.assign(locationFromNodeVariable(expr.getNode()),
                     builder.createOutput(Converter.toFortressData(expr.getValueInfo())));
    }
    return builder.build();
  }

  public static SsaForm parametersList(final IrInquirer inquirer, final PrimitiveAND p) {
    final SsaBuilder builder = newConverter(inquirer, p.getName());
    builder.acquireBlockBuilder();
    final List<Node> parameters = new ArrayList<>(p.getArguments().size());
    for (final Map.Entry<String, Primitive> entry : p.getArguments().entrySet()) {
      final DataType type = convertType(entry.getValue().getReturnType());
      parameters.add(new NodeVariable(entry.getKey(), type));
    }
    builder.addToContext(new NodeOperation(SsaOperation.PARAMETERS, parameters));

    return builder.build();
  }

  private static DataType convertType(final Type type) {
    if (type == null) {
      return DataType.BOOLEAN;
    }
    return Converter.getDataTypeForModel(type);
  }

  private NodeOperation createOutput(Data data) {
    return new NodeOperation(SsaOperation.SUBSTITUTE, createTemporary(data));
  }

  private static SsaBuilder newConverter(final IrInquirer inquirer, final String prefix) {
    return new SsaBuilder(inquirer, prefix, "", prefix, Collections.<Statement>emptyList());
  }
}
