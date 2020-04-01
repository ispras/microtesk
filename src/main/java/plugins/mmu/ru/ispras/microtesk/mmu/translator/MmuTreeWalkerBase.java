/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.TreeNodeStream;

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.NodeTransformer;
import ru.ispras.fortress.transformer.ReduceOptions;
import ru.ispras.fortress.transformer.Reducer;
import ru.ispras.fortress.transformer.Transformer;
import ru.ispras.fortress.transformer.TransformerRule;
import ru.ispras.fortress.transformer.TypeConversion;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;

import ru.ispras.microtesk.mmu.model.sim.*;
import ru.ispras.microtesk.mmu.translator.ir.AbstractStorage;
import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.Attribute;
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Callable;
import ru.ispras.microtesk.mmu.translator.ir.Constant;
import ru.ispras.microtesk.mmu.translator.ir.ExternalSource;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.mmu.translator.ir.Operation;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Stmt;
import ru.ispras.microtesk.mmu.translator.ir.StmtAssert;
import ru.ispras.microtesk.mmu.translator.ir.StmtAssign;
import ru.ispras.microtesk.mmu.translator.ir.StmtCall;
import ru.ispras.microtesk.mmu.translator.ir.StmtException;
import ru.ispras.microtesk.mmu.translator.ir.StmtIf;
import ru.ispras.microtesk.mmu.translator.ir.StmtMark;
import ru.ispras.microtesk.mmu.translator.ir.StmtReturn;
import ru.ispras.microtesk.mmu.translator.ir.StmtTrace;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Var;
import ru.ispras.microtesk.mmu.model.spec.MmuBuffer;
import ru.ispras.microtesk.translator.TranslatorContext;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.TreeParserBase;
import ru.ispras.microtesk.translator.antlrex.symbols.Symbol;
import ru.ispras.microtesk.translator.antlrex.symbols.Where;
import ru.ispras.microtesk.translator.nml.coverage.IntegerCast;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryResource;
import ru.ispras.microtesk.utils.FormatMarker;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The {@link MmuTreeWalkerBase} class provides factory methods to create MMU IR.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public abstract class MmuTreeWalkerBase extends TreeParserBase {
  private Ir ir;
  private TranslatorContext context;

  private final VariableStorage storage = new VariableStorage();
  private final Map<String, AbstractStorage> globals = new HashMap<>();
  protected final ConstantPropagator propagator = new ConstantPropagator();

  private final NodeTransformer equalityExpander;

  private static final class ExpandEqualityRule implements TransformerRule {
    @Override
    public boolean isApplicable(final Node expr) {
      if (Node.Kind.OPERATION != expr.getKind()) {
        return false;
      }

      final NodeOperation op = (NodeOperation) expr;
      return op.getOperationId() == StandardOperation.EQ
          && op.getOperandCount() > 2;
    }

    @Override
    public Node apply(final Node expr) {
      final NodeOperation op = (NodeOperation) expr;
      Node reference = op.getOperand(0);

      for (final Node operand : op.getOperands()) {
        if (Node.Kind.VALUE == operand.getKind()) {
          reference = operand;
          break;
        }
      }

      final List<Node> operands = new ArrayList<>(op.getOperandCount() - 1);
      for (final Node operand : op.getOperands()) {
        if (operand != reference) {
          operands.add(new NodeOperation(StandardOperation.EQ, operand, reference));
        }
      }

      return new NodeOperation(StandardOperation.AND, operands);
    }
  }

  /**
   * The {@link ReduceNegationRule} rule reduces expressions
   * {@code (NOT(EQ A B))} to {@code (NEQ A B)} and {@code (NOT(NEQ A B))} to {@code (EQ A B)}.
   */
  private static final class ReduceNegationRule implements TransformerRule {
    private final List<StandardOperation> operations =
        Arrays.asList(StandardOperation.EQ, StandardOperation.NOTEQ);

    @Override
    public boolean isApplicable(final Node node) {
      if (!ExprUtils.isOperation(node, StandardOperation.NOT)) {
        return false;
      }

      final Node expr = ((NodeOperation) node).getOperand(0);
      return ExprUtils.isOperation(expr, operations)
          && ((NodeOperation) expr).getOperandCount() == 2;
    }

    @Override
    public Node apply(final Node node) {
      final NodeOperation expr = (NodeOperation)((NodeOperation) node).getOperand(0);
      final boolean isEq = ExprUtils.isOperation(expr, StandardOperation.EQ);

      return new NodeOperation(
          isEq ? StandardOperation.NOTEQ : StandardOperation.EQ, expr.getOperands());
    }
  }

  private Node standardize(final Node cond) {
    final Node stdCond = Transformer.standardize(cond);

    equalityExpander.walk(stdCond);
    final Node result = equalityExpander.getResult().iterator().next();

    equalityExpander.reset();
    return result;
  }

  public MmuTreeWalkerBase(final TreeNodeStream input, final RecognizerSharedState state) {
    super(input, state);
    this.ir = null;
    this.context = null;

    this.equalityExpander = new NodeTransformer();
    this.equalityExpander.addRule(StandardOperation.EQ, new ExpandEqualityRule());
    this.equalityExpander.addRule(StandardOperation.NOT, new ReduceNegationRule());
  }

  public final void assignIR(final Ir ir) {
    InvariantChecks.checkNotNull(ir);
    this.ir = ir;
  }

  public final Ir getIR() {
    return ir;
  }

  public final void assignContext(final TranslatorContext context) {
    // Context is allowed to be null (e.g., when mmuSL is translated independently).
    this.context = context;
  }

  public final TranslatorContext getContext() {
    return context;
  }

  /**
   * Adds a static constant (let expression) to the IR.
   *
   * @param id Constant identifier.
   * @param value Constant value.
   * @throws SemanticException if the value expression is {@code null}.
   */
  protected final void newConstant(
      final CommonTree id,
      final Node value) throws SemanticException {
    checkNotNull(id, value);

    final Constant constant = new Constant(id.getText(), value);
    ir.addConstant(constant);
  }

  /**
   * Returns the value of the specified constant.
   *
   * @param id Constant identifier.
   * @return Constant value.
   * @throws SemanticException if the constant is not defined.
   */
  protected final Node getConstant(final CommonTree id) throws SemanticException {
    final Constant constant = ir.getConstants().get(id.getText());

    if (null == constant) {
      raiseError(where(id), "Constant is undefined: " + id.getText());
    }

    return constant.isValue() ? constant.getExpression() : constant.getVariable();
  }

  /**
   * Adds an external variable linked to the specified source entity defined
   * in the ISA specification to the IR.
   *
   * @param id Variable name.
   * @param aliasId Name of the source entity.
   * @param args Arguments used to access the source entity (e.g. register index,
   *        or addressing mode arguments).
   *
   * @throws SemanticException if the variable cannot be linked to the specified ISA entity.
   */
  protected void newExtern(
      final CommonTree id,
      final CommonTree aliasId,
      final List<Node> args) throws SemanticException {
    checkNotNull(id, args);

    final ru.ispras.microtesk.translator.nml.ir.Ir isaIr =
        context.getIr(ru.ispras.microtesk.translator.nml.ir.Ir.class);

    final String name = id.getText();
    final String sourceName = aliasId.getText();

    if (!isaIr.getMemory().containsKey(sourceName)
        && !isaIr.getModes().containsKey(sourceName)) {
      raiseError(where(id), String.format(
          "%s is not defined in the ISA specification or cannot be used as an external variable. "
              + "It must be a reg, mem or mode element.", sourceName
      ));
    }

    final Type type;
    final ExternalSource.Kind sourceKind;
    if (isaIr.getMemory().containsKey(sourceName)) {
      final MemoryResource memory = isaIr.getMemory().get(sourceName);
      if (memory.getKind() == ru.ispras.microtesk.model.memory.Memory.Kind.VAR) {
        raiseError(where(id), String.format(
            "External variable must not be defined as var: %s", sourceName));
      }

      final int bitSize = memory.getType().getBitSize();
      type = new Type(bitSize);
      sourceKind = ExternalSource.Kind.MEMORY;
    } else {
      final Primitive mode = isaIr.getModes().get(sourceName);
      if (null == mode.getReturnType()) {
        raiseError(where(id), String.format(
            "Addressing mode %s does not have a return type and cannot be used "
                + "as an external variable.", sourceName));
      }

      final int bitSize = mode.getReturnType().getBitSize();
      type = new Type(bitSize);
      sourceKind = ExternalSource.Kind.MODE;
    }

    final List<BigInteger> argValues = new ArrayList<>(args.size());
    for (final Node arg : args) {
      if (arg.getKind() != Node.Kind.VALUE) {
        raiseError(where(id),
            "References to external elements can be parameterized only with constant values.");
      }

      final BigInteger value = ((NodeValue) arg).getInteger();
      argValues.add(value);
    }

    final ExternalSource source = new ExternalSource(sourceKind, sourceName, argValues);
    final Var variable = storage.declare(name, type, source);

    ir.addExtern(variable);
  }

  /**
   * Creates an Address IR object and adds it to the MMU IR.
   *
   * @param addressId Address identifier.
   * @param type Address Address type.
   * @param memberChain Member chain.
   * @return New Address IR object.
   * @throws SemanticException (1) if the width expression is {@code null}; (2) if the width
   * expression cannot be reduced to a constant integer value; (3) if the width value is beyond
   * the Java Integer allowed range; (4) if the width value is less or equal 0.
   */
  protected final Address newAddress(final CommonTree addressId,
                                     final Type type,
                                     final List<CommonTree> memberChain) throws SemanticException {
    checkNotNull(addressId, type);

    final ArrayList<String> path = new ArrayList<>();
    if (memberChain != null) {
      path.ensureCapacity(memberChain.size());
      for (final CommonTree member : memberChain) {
        path.add(member.getText());
      }
    }
    if (!path.isEmpty()) {
      final Type nested = type.searchNested(path);
      if (nested == null) {
        raiseError(where(addressId), "Invalid value member specified.");
      }
      if (nested.isStruct()) {
        raiseError(where(addressId), "Address value member should be bit vector.");
      }
    }
    if (path.isEmpty() && type.isStruct()) {
      final String name = type.getFields().keySet().iterator().next();
      final Type nested = type.getFields().get(name);

      if (nested.isStruct()) {
        raiseError(where(addressId), "Address value member needs to be specified.");
      }
      path.add(name);
    }
    final Address address;
    if (type.isStruct()) {
      address = new Address(addressId.getText(), type, Collections.unmodifiableList(path));
    } else {
      address = new Address(addressId.getText(), type.getBitSize());
    }
    ir.addAddress(address);
    // newType(addressId, type);

    return address;
  }

  /**
   * Builder for a Type. Helps create a complex type from a sequence of fields.
   */

  protected final class StructBuilder {
    private final String id;
    private final Map<String, Type> fields = new LinkedHashMap<>();

    public StructBuilder(final String id) {
      InvariantChecks.checkNotNull(id);
      this.id = id;
    }

    /**
     * Adds a field to Type to be created.
     *
     * @param fieldId Field identifier.
     * @param sizeExpr Field size expression.
     * @param valueExpr Field default value expression (optional, can be {@code null}).
     * @throws SemanticException (1) if the size expression is {@code null}, (2) if
     * the size expression cannot be evaluated to a positive integer value (Java int).
     */
    public void addField(
        final CommonTree fieldId,
        final Node sizeExpr,
        final Node valueExpr) throws SemanticException {
      checkNotNull(fieldId, sizeExpr);

      final Where w = where(fieldId);
      final String id = fieldId.getText();

      if (fields.containsKey(id)) {
        raiseError(w, String.format("Duplicate member '%s'.", id));
      }

      final int bitSize = extractPositiveInt(w, sizeExpr, id + " field size");

      final BitVector defValue;
      if (null != valueExpr) {
        final BigInteger value = extractBigInteger(w, valueExpr, id + " field value");
        defValue = BitVector.valueOf(value, bitSize);
      } else {
        defValue = null;
      }

      final Type fieldType = new Type(bitSize, defValue);
      fields.put(id, fieldType);
    }

    public void addField(
        final CommonTree fieldId,
        final CommonTree typeId) throws SemanticException {
      checkNotNull(fieldId, typeId);

      final Where w = where(fieldId);
      final String id = fieldId.getText();

      if (fields.containsKey(id)) {
        raiseError(w, String.format("Duplicate member '%s'.", id));
      }
      fields.put(id, resolveTypeName(typeId));
    }

    /**
     * Builds a Type from the collection of fields.
     * @return New Type.
     */

    public Type build() {
      return new Type(id, fields);
    }
  }

  protected final Type newType(
      final CommonTree typeId, final Type type) throws SemanticException {
    checkNotNull(typeId, type);

    final String typeName = typeId.getText();
    if (ir.getTypes().containsKey(typeName)) {
      raiseError(where(typeId), String.format("Redefinition of '%s'.", typeName));
    }

    ir.addType(type, typeName);
    return type;
  }

  protected final Type findType(final CommonTree typeId) throws SemanticException {
    checkNotNull(typeId, typeId);

    final Type type = ir.getTypes().get(typeId.getText());
    if (type == null) {
      raiseError(where(typeId),
                 String.format("'%s' is not defined or is not a type.", typeId.getText()));
    }
    return type;
  }

  protected final Type resolveTypeName(final CommonTree typeId) throws SemanticException {
    checkNotNull(typeId, typeId);

    final Where w = where(typeId);

    final Type type = ir.getTypes().get(typeId.getText());
    final Constant constant = ir.getConstants().get(typeId.getText());

    final Node size = null != constant && constant.isValue() ? constant.getExpression() : null;

    if (type == null && size == null) {
      raiseError(w, String.format("Unknown type name '%s'.", typeId.getText()));
    }

    if (type != null && size != null) {
      raiseError(w, "Ambiguous type in variable declaration: " + typeId.getText());
    }

    if (type != null) {
      return type;
    }

    final int bitSize = extractPositiveInt(w, size, "Type size");
    return new Type(bitSize);
  }

  protected final class OperationBuilder {
    private static final String ATTRIBUTE_NAME = "init";

    private final CommonTree id;
    private final Address address;
    private final Var addressArg;
    private List<Stmt> stmts;
    private final Set<Node> assigned;

    public OperationBuilder(
        final CommonTree id,
        final CommonTree addressArgId,
        final CommonTree addressArgType) throws SemanticException {
      this.id = id;
      this.address = getAddress(addressArgType);

      storage.newScope(id.getText());
      this.addressArg = storage.declare(addressArgId.getText(), address.getContentType());

      this.stmts = null;
      this.assigned = new HashSet<>();
    }

    public void addAttribute(
        final CommonTree attrId,
        final List<Stmt> stmts) throws SemanticException {
      checkNotNull(attrId, stmts);

      if (!ATTRIBUTE_NAME.equals(attrId.getText())) {
        raiseError(where(attrId), String.format(
            "The %s attribute is not supported for the %s operation.",
            attrId.getText(),
            id.getText())
        );
      }

      if (null != this.stmts) {
        raiseError(where(attrId), String.format(
            "The %s attribute is already defined for the %s operation.",
            attrId.getText(),
            id.getText())
        );
      }

      for (final Stmt stmt : stmts) {
        checkValidStmt(where(attrId), stmt);
      }

      this.stmts = stmts;
    }

    private void checkValidStmt(final Where where, final Stmt stmt) throws SemanticException {
      if (stmt.getKind() == Stmt.Kind.TRACE) {
        // Trace statements are ignored (they work only in the simulator)
        return;
      }

      if (stmt.getKind() != Stmt.Kind.ASSIGN) {
        raiseError(where, String.format(
            "%s statements are not allowed in operations.", stmt.getKind()));
      }

      final StmtAssign assignment = (StmtAssign) stmt;

      final Node left = assignment.getLeft();
      final Node right = assignment.getRight();

      if (!isAddressField(left)) {
        raiseError(where, String.format(
            "Only assignments to individual fields of %s are allowed in the %s operation.",
            addressArg.getName(),
            id.getText())
        );
      }

      if (!isConstant(right)) {
        raiseError(where,
            "Only constants are allowed in right side of assignments in operations.");
      }

      if (assigned.contains(left)) {
        raiseError(where,
            left + " is already assigned. Reassignments are not allowed in operations.");
      }

      assigned.add(left);
    }

    private boolean isAddressField(final Node node) {
      if (node.getKind() == Node.Kind.OPERATION) {
        final NodeOperation op = (NodeOperation) node;

        if (op.getOperationId() == StandardOperation.BVEXTRACT
            && op.getOperandCount() == 3) {
          return isAddressField(op.getOperand(2));
        }

        return false;
      }

      if (node.getKind() == Node.Kind.VARIABLE
          && node.getUserData() instanceof Var) {
        final Var variable = (Var) node.getUserData();
        return variable.isParent(addressArg) && !variable.isStruct();
      }

      return false;
    }

    private boolean isConstant(final Node node) {
      if (node.getKind() == Node.Kind.VALUE) {
        return true;
      }

      if (node.getKind() == Node.Kind.VARIABLE) {
        final NodeVariable variable = (NodeVariable) node;
        final Object userData = variable.getUserData();

        if (userData instanceof Constant) {
          return true;
        }

        if (userData instanceof Var
            && ((Var) userData).getTypeSource() instanceof ExternalSource) {
          return true;
        }

        return false;
      }

      if (node.getKind() == Node.Kind.OPERATION) {
        final NodeOperation op = (NodeOperation) node;

        if (op.getOperationId() == StandardOperation.BVEXTRACT
            && op.getOperandCount() == 3) {
          return isConstant(op.getOperand(2));
        }

        if (op.getOperationId() == StandardOperation.BVCONCAT) {
          for (int index = 0; index < op.getOperandCount(); ++index) {
            if (!isConstant(op.getOperand(index))) {
              return false;
            }
          }
          return true;
        }

        return false;
      }

      return false;
    }

    public Operation build() throws SemanticException {
      if (null == stmts) {
        raiseError(where(id), String.format(
            "The %s attribute is undefined for the %s operation.",
            ATTRIBUTE_NAME,
            id.getText())
        );
      }

      for (final Memory memory : ir.getMemories().values()) {
        if (!memory.getAddress().equals(address)) {
          raiseError(where(id), String.format(
              "The %s operation is not compatible with the %s definition: address type mismatch.",
              id.getText(), memory.getId())
          );
        }
      }

      final Operation operation = new Operation(
          id.getText(),
          address,
          addressArg,
          stmts
          );

      ir.addOperation(operation);
      storage.popScope();

      return operation;
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  // TODO: Review + comments are needed

  /**
   * Builder for {@link Buffer} objects. Helps create a Buffer from attributes.
   */
  protected final class BufferBuilder {
    private final MmuBuffer.Kind kind;
    private final CommonTree id;
    private final Address address;
    private final String addressArgId;
    private final Var addressArg;
    private final boolean isView;

    private Var dataArg = null; // stores entries
    private BigInteger ways = BigInteger.ZERO;
    private BigInteger sets = BigInteger.ZERO;
    private Node index = null;
    private Node match = null;
    private EvictionPolicyId evictionPolicy = null;
    private WritePolicyId writePolicy = null;
    private InclusionPolicyId inclusionPolicy = null;
    private CoherenceProtocolId coherenceProtocol = null;
    private Buffer next = null;

    /**
     * Constructs a builder for a Buffer object.
     *
     * @param id Buffer identifier.
     * @param addressArgId Address argument identifier.
     * @param addressArgType Address argument type (identifier).
     * @param parentBufferId Identifier of the parent buffer.
     * @param qualifiers List of qualifiers.
     *
     * @throws SemanticException if the specified address type is not defined.
     */
    public BufferBuilder(
        final CommonTree id,
        final CommonTree addressArgId,
        final CommonTree addressArgType,
        final CommonTree parentBufferId,
        final List<String> qualifiers) throws SemanticException {

      this.id = id;
      this.address = getAddress(addressArgType);

      storage.newScope(id.getText());
      this.addressArgId = addressArgId.getText();
      this.addressArg = storage.declare(addressArgId.getText(),
                                        address.getContentType());
      storage.popScope();

      if (parentBufferId != null) {
        this.isView = true;
        this.next = getBuffer(parentBufferId);

        // setDataArg() depends on context, therefore the latter should be
        // created prior to
        setDataArg(id.getText(), next.getEntry());
      } else {
        this.isView = false;
      }

      if (qualifiers.contains(MmuBuffer.Kind.MEMORY.getText())) {
        kind = MmuBuffer.Kind.MEMORY;
      } else if (qualifiers.contains(MmuBuffer.Kind.REGISTER.getText())) {
        final ru.ispras.microtesk.translator.nml.ir.Ir isaIr =
            context.getIr(ru.ispras.microtesk.translator.nml.ir.Ir.class);

        final MemoryResource register =
            isaIr.getMemory().get(id.getText());

        if (null == register) {
          raiseError(where(id), String.format("Register %s is not defined.", id.getText()));
        }

        if (register.getKind() != ru.ispras.microtesk.model.memory.Memory.Kind.REG) {
          raiseError(where(id), String.format("%s is not a register.", id.getText()));
        }

        kind = MmuBuffer.Kind.REGISTER;
      } else {
        kind = MmuBuffer.Kind.UNMAPPED;
      }
    }

    public void setWays(final CommonTree attrId, final Node attr) throws SemanticException {
      checkNotNull(attrId, attr);
      checkRedefined(attrId, !ways.equals(BigInteger.ZERO));
      ways = extractPositiveBigInteger(where(attrId), attr, attrId.getText());
    }

    public void setSets(final CommonTree attrId, final Node attr) throws SemanticException {
      checkNotNull(attrId, attr);
      checkRedefined(attrId, !sets.equals(BigInteger.ZERO));
      sets = extractPositiveBigInteger(where(attrId), attr, attrId.getText());
    }

    public void setEntry(final CommonTree attrId, final Type attr) throws SemanticException {
      checkNotNull(attrId, attr);

      if (isView) {
        raiseError(where(attrId), "Buffer view forbids 'entry' attribute redefinition.");
      }
      checkRedefined(attrId, dataArg != null);
      setDataArg(id.getText(), attr);
    }

    private void setDataArg(final String name, final Type type) {
      dataArg = storage.declare(name, type);
      final Map<String, Var> scope = new HashMap<>(dataArg.getFields());
      scope.put(addressArgId, addressArg);
      storage.newScope(scope);
    }

    public void setIndex(final CommonTree attrId, final Node attr) throws SemanticException {
      checkNotNull(attrId, attr);
      checkRedefined(attrId, index != null);
      index = attr;
    }

    public void setMatch(final CommonTree attrId, final Node attr) throws SemanticException {
      checkNotNull(attrId, attr);
      checkRedefined(attrId, match != null);
      match = attr;
    }

    public void setPolicyId(
        final CommonTree attrId, final CommonTree attr) throws SemanticException {
      final EvictionPolicyId eviction = getEvictionPolicy(attr.getText());
      checkRedefined(attrId, eviction != null && evictionPolicy != null);
      evictionPolicy = eviction;

      final WritePolicyId write = getWritePolicy(attr.getText());
      checkRedefined(attrId, write != null && writePolicy != null);
      writePolicy = write;

      final InclusionPolicyId inclusion = getInclusionPolicy(attr.getText());
      checkRedefined(attrId, inclusion != null && inclusionPolicy != null);
      inclusionPolicy = inclusion;

      final CoherenceProtocolId coherence = getCoherenceProtocol(attr.getText());
      checkRedefined(attrId, coherence != null && coherenceProtocol != null);
      coherenceProtocol = coherence;

      if (eviction == null && write == null && inclusion == null && coherence == null) {
        raiseError(where(attr), "Unknown policy: " + attr.getText());
      }
    }

    public void setNextBufferId(
            final CommonTree attrId, final CommonTree attr) throws SemanticException {
      checkRedefined(attrId, next != null);
      try {
        next = getBuffer(attr);
      } catch (Exception e) {
        raiseError(where(attr), "Unknown next buffer: " + attr.getText());
      }
    }

    private EvictionPolicyId getEvictionPolicy(final String id) {
      try {
        return EvictionPolicyId.valueOf(id);
      } catch (Exception e) {
        return null;
      }
    }

    private WritePolicyId getWritePolicy(final String id) {
      try {
        return WritePolicyId.valueOf(id);
      } catch (Exception e) {
        return null;
      }
    }

    private InclusionPolicyId getInclusionPolicy(final String id) {
      try {
        return InclusionPolicyId.valueOf(id);
      } catch (Exception e) {
        return null;
      }
    }

    private CoherenceProtocolId getCoherenceProtocol(final String id) {
      try {
        return CoherenceProtocolId.valueOf(id);
      } catch (Exception e) {
        return null;
      }
    }

    private void checkRedefined(
        final CommonTree attrId, final boolean isRedefined) throws SemanticException {
      if (isRedefined) {
        raiseError(where(attrId),
            String.format("The %s attribute is redefined.", attrId.getText()));
      }
    }

    private void checkUndefined(
        final String attrId, final boolean isUndefined) throws SemanticException {
      if (isUndefined) {
        raiseError(where(id), String.format("The %s attribute is undefined.", attrId));
      }
    }

    public Buffer build() throws SemanticException {
      checkUndefined("ways", ways.equals(BigInteger.ZERO));
      checkUndefined("sets", sets.equals(BigInteger.ZERO));
      checkUndefined("entry", dataArg == null && !isView);
      checkUndefined("index", index == null);
      checkUndefined("match", match == null);

      final Buffer buffer = new Buffer(
          id.getText(),
          kind,
          isView,
          address,
          addressArg,
          dataArg,
          ways,
          sets,
          index,
          match,
          CachePolicy.create(
            evictionPolicy != null ? evictionPolicy : EvictionPolicyId.NONE,
            writePolicy != null ? writePolicy : WritePolicyId.WN,
            inclusionPolicy != null ? inclusionPolicy : InclusionPolicyId.NINE,
            coherenceProtocol != null ? coherenceProtocol : CoherenceProtocolId.NONE
          ),
          next);

      ir.addBuffer(buffer);
      globals.put(id.getText(), buffer);
      storage.popScope();

      return buffer;
    }
  }

  protected final List<String> checkContextKeywords(
      final MmuLanguageContext langCtx,
      Collection<CommonTree> nodes) throws SemanticException {
    InvariantChecks.checkNotNull(langCtx);

    if (nodes == null) {
      nodes = Collections.emptyList();
    }
    final MmuLanguageContext.CheckResult result = langCtx.checkKeywords(nodes);
    if (!result.isSuccess()) {
      raiseError(where(result.getSource()), result.getMessage());
    }
    return result.getResult();
  }

  protected final CommonBuilder newMemoryBuilder(
      final CommonTree memoryId,
      final CommonTree addressArgId,
      final CommonTree addressArgType,
      final CommonTree dataArgId,
      final Node dataArgSizeExpr) throws SemanticException {

    final Address address = getAddress(addressArgType);

    final int dataSize = extractPositiveInt(
        where(dataArgId), dataArgSizeExpr, "Data argument size");

    return new CommonBuilder(where(memoryId),
                             memoryId.getText(),
                             address,
                             addressArgId.getText(),
                             null,
                             new Type(dataSize),
                             dataArgId.getText());
  }

  protected final CommonBuilder newSegmentBuilder(
      final CommonTree id,
      final CommonTree addressArgId,
      final CommonTree addressArgType,
      final CommonTree outputVarId,
      final CommonTree outputVarType) throws SemanticException {

    final Address address = getAddress(addressArgType);
    if (outputVarId == null || outputVarType == null) {
      return new CommonBuilder(where(id), id.getText(), address, addressArgId.getText());
    }

    final Address outputAddr = getAddress(outputVarType);
    return new CommonBuilder(where(id),
                             id.getText(),
                             address,
                             addressArgId.getText(),
                             outputAddr,
                             outputVarId.getText());
  }

  protected final class CallableBuilder {
    private final Where location;
    private final String name;
    private final Map<String, Var> parameters = new LinkedHashMap<>();
    private final Map<String, Local> locals = new HashMap<>();
    private List<Stmt> body = null;
    private Type retType = null;
    private Var output = null;

    private final class Local {
      public final Var var;
      public final Where loc;

      public Local(final Var var, final Where loc) {
        this.var = var;
        this.loc = loc;
      }
    }

    public CallableBuilder(final CommonTree node) {
      this.location = where(node);
      this.name = node.getText();

      storage.newScope(name);
    }

    public void setRetType(final Type retType) {
      this.retType = retType;
    }

    public void setOutput(final Var var) {
      this.output = var;
      this.retType = var.getType();
    }

    public void setBody(final List<Stmt> body) {
      this.body = body;
    }

    public Map<String, Var> addParameters(
        final Collection<CommonTree> nodes,
        final Collection<Type> types) throws SemanticException {
      final Map<String, Var> variables = addLocalVariables(nodes, types);
      parameters.putAll(variables);
      return variables;
    }

    public Map<String, Var> addLocalVariables(
        final Collection<CommonTree> nodes,
        final Collection<Type> types) throws SemanticException {
      InvariantChecks.checkNotNull(nodes);
      InvariantChecks.checkNotNull(types);
      InvariantChecks.checkTrue(nodes.size() == types.size());

      final Iterator<CommonTree> nodeIt = nodes.iterator();
      final Iterator<Type> typeIt = types.iterator();

      final Map<String, Var> variables = new LinkedHashMap<>(nodes.size());
      while (nodeIt.hasNext() && typeIt.hasNext()) {
        final CommonTree node = nodeIt.next();
        final Var var = addVariable(node, typeIt.next());
        variables.put(node.getText(), var);
      }
      return variables;
    }

    private Var addVariable(final CommonTree node,
                                 final Type type) throws SemanticException {
      final String name = node.getText();
      final Local local = locals.get(name);
      if (name.equals(this.name) || local != null) {
        final Where loc = (local != null) ? local.loc : this.location;
        final String msg = String.format(
            "Redeclaration of '%s', previous declaration was here:%n%s",
            name,
            loc);
        raiseError(where(node), msg);
      }

      final Var var = storage.declare(name, type);
      locals.put(name, new Local(var, where(node)));

      return var;
    }

    public Callable build() throws SemanticException {
      final Map<String, Var> locals = new HashMap<>(this.locals.size());
      for (final Map.Entry<String, Local> entry : this.locals.entrySet()) {
        locals.put(entry.getKey(), entry.getValue().var);
      }
      locals.keySet().removeAll(parameters.keySet());

      if (output == null && retType != null) {
        output = storage.declare(name, retType);
      }
      final int nexits = setExit(location, body, output);
      if (output != null && nexits == 0) {
        raiseError(location, "missing 'return' statement in function returning non-void");
      }

      final List<Var> params = new ArrayList<>(parameters.values());
      final Callable c = new Callable(name, params, locals, body, output);
      storage.popScope();
      return c;
    }

    private int setExit(
        final Where w,
        final List<Stmt> body,
        final Var output) throws SemanticException {
      int nexits = 0;
      for (final Stmt s : body) {
        switch (s.getKind()) {
          case RETURN:
            final StmtReturn ret = (StmtReturn) s;
            final boolean retVoid = output == null;
            final boolean valVoid = ret.getExpr() == null;

            if (retVoid != valVoid) {
              raiseError(w, String.format(
                  "'return' with %s value, in function returning %svoid",
                  (valVoid) ? "no" : "a",
                  (retVoid) ? "" : "non-"));
            }
            if (!retVoid) {
              ret.setStorage(output);
              nexits += 1;
            }
            break;

          case IF:
            final StmtIf cond = (StmtIf) s;
            for (final Pair<Node, List<Stmt>> branch : cond.getIfBlocks()) {
              nexits += setExit(w, branch.second, output);
            }
            nexits += setExit(w, cond.getElseBlock(), output);
            break;
          default:
            break;
        }
      }
      return nexits;
    }
  }

  protected final void registerFunction(final Callable func) {
    ir.addFunction(func);
  }

  protected final NodeOperation newCall(
      final CommonTree node,
      final List<Node> args) throws SemanticException {
    checkNotNull(node, node);
    checkNotNull(node, args);

    final Callable callee = ir.getFunctions().get(node.getText());
    if (callee == null) {
      raiseError(where(node), String.format("Call to undefined symbol '%s'", node.getText()));
    }

    final int ndiff = callee.getParameters().size() - args.size();
    if (ndiff != 0) {
      raiseError(where(node), String.format(
          "Too %s arguments to function '%s'",
          (ndiff < 0) ? "many" : "few",
          node.getText()));
    }

    final List<Node> castArgs = new ArrayList<>(args.size());
    for (int index = 0; index < callee.getParameters().size(); ++index) {
      final Node arg = args.get(index);
      final Var param = callee.getParameter(index);

      if (arg.getKind() == Node.Kind.VALUE) {
        final Node castArg = IntegerCast.cast(arg, param.getDataType());
        castArgs.add(castArg);
      } else {
        checkTypes(where(node), param.getDataType(), arg.getDataType());
        castArgs.add(arg);
      }
    }

    final NodeOperation call;
    if (null != callee.getOutput()) {
      final DataType dataType = callee.getOutput().getDataType();
      call = new NodeOperation(MmuSymbolKind.FUNCTION, dataType, castArgs);
    } else {
      call = new NodeOperation(MmuSymbolKind.FUNCTION, castArgs);
    }

    call.setUserData(callee);
    return call;
  }

  protected final Node newCallExpr(final Where w, final NodeOperation call)
      throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(call);
    InvariantChecks.checkTrue(call.getUserData() instanceof Callable);

    final Callable callee = (Callable) call.getUserData();
    if (callee.getOutput() == null) {
      raiseError(w, "void value not ignored as it ought to be");
    }
    return call;
  }

  protected final Stmt newCallStmt(final NodeOperation call) {
    InvariantChecks.checkNotNull(call);
    InvariantChecks.checkTrue(call.getUserData() instanceof Callable);

    return new StmtCall((Callable) call.getUserData(), call.getOperands());
  }

  protected final class CommonBuilder {
    private final Where where;

    private final String id;
    private final Address address;
    private final Var addressArg;
    private final Var outputVar;
    private final Address outputVarAddress;

    private final Map<String, Var> variables;
    private final Map<String, Attribute> attributes;

    private CommonBuilder(
        final Where where,
        final String id,
        final Address address,
        final String inputName) {
      this(where, id, address, inputName, null, null);
    }

    private CommonBuilder(
        final Where where,
        final String id,
        final Address address,
        final String inputName,
        final Address outputAddr,
        final String outputName) {
      this(where,
           id,
           address,
           inputName,
           outputAddr,
           outputAddr.getContentType(),
           outputName);
    }

    private CommonBuilder(
        final Where where,
        final String id,
        final Address address,
        final String inputName,
        final Address outputAddr,
        final Type outputType,
        final String outputName) {
      this.where = where;
      this.id = id;
      this.address = address;
      this.outputVarAddress = outputAddr;

      storage.newScope(id);
      this.addressArg = storage.declare(inputName, address.getContentType());
      if (outputName != null && outputType != null) {
        this.outputVar = storage.declare(outputName, outputType);
      } else {
        this.outputVar = null;
      }

      this.variables = new LinkedHashMap<>();
      this.attributes = new LinkedHashMap<>();
    }

    public void addVariable(final CommonTree varId, final Node sizeExpr) throws SemanticException {
      checkNotNull(varId, sizeExpr);

      final int bitSize = extractPositiveInt(
          where(varId), sizeExpr, String.format("Variable %s size", varId.getText()));

      final Var variable = storage.declare(varId.getText(), new Type(bitSize));
      variables.put(variable.getName(), variable);
    }

    public void addVariable(
        final CommonTree varId, final CommonTree typeId) throws SemanticException {
      final Symbol symbol = getSymbol(typeId);

      final Type type;
      final Object source;
      if (MmuSymbolKind.BUFFER == symbol.getKind()) {
        final Buffer buffer = getBuffer(typeId);
        type = buffer.getEntry();
        source = buffer;
      } else if (MmuSymbolKind.ADDRESS == symbol.getKind()) {
        final Address address = getAddress(typeId);
        type = address.getContentType();
        source = address;
      } else {
        type = resolveTypeName(typeId);
        source = null;
        /*
        raiseError(where(typeId), new SymbolTypeMismatch(symbol.getName(), symbol.getKind(),
            Arrays.<Enum<?>>asList(MmuSymbolKind.BUFFER, MmuSymbolKind.ADDRESS)));
        */
      }
      final Var v = storage.declare(varId.getText(), type, source);
      variables.put(v.getName(), v);
    }

    public void addAttribute(
        final CommonTree attrId, final List<Stmt> stmts) throws SemanticException {
      checkNotNull(attrId, stmts);

      final Attribute attr = new Attribute(attrId.getText(), outputVar.getDataType(), stmts);
      attributes.put(attr.getId(), attr);
    }

    public Memory buildMemory() throws SemanticException {
      if (!attributes.containsKey("read")) {
        raiseError(where, "The 'read' action is not defined.");
      }

      if (!attributes.containsKey("write")) {
        raiseError(where, "The 'write' action is not defined.");
      }

      for (final Operation operation : ir.getOperations().values()) {
        if (!operation.getAddress().equals(address)) {
          raiseError(where, String.format(
              "The %s operation is not compatible with the %s definition: address type mismatch.",
              operation.getId(), id)
          );
        }
      }

      final Memory memory = new Memory(
          id, address, addressArg, outputVar, variables, attributes);

      ir.addMemory(memory);
      storage.popScope();

      return memory;
    }

    /**
     * Creates a segment IR object and adds it to the MMU IR.
     *
     * @param rangeStartExpr Range start expression.
     * @param rangeEndExpr Range and expression.
     * @return New Segment IR object.
     * @throws SemanticException (1) if the specified address type is not defined;
     * (2) if the range expressions equal to {@code null}, (3) if the range expressions
     * cannot be reduced to constant integer values; (4) if the range start
     * value is greater than the range end value.
     */
    public Segment buildSegment(
        final Node rangeStartExpr,
        final Node rangeEndExpr) throws SemanticException {
      final BigInteger rangeStart = extractBigInteger(where, rangeStartExpr, "Range start");
      final BigInteger rangeEnd = extractBigInteger(where, rangeEndExpr, "Range end");

      if (rangeStart.compareTo(rangeEnd) > 0) {
        raiseError(where, String.format(
            "Range start (%d) is greater than range end (%d).", rangeStart, rangeEnd));
      }

      final Segment segment = new Segment(
          id,
          address,
          addressArg,
          rangeStart,
          rangeEnd,
          outputVarAddress,
          outputVar,
          variables,
          attributes
          );

      ir.addSegment(segment);
      globals.put(id, segment);
      storage.popScope();

      return segment;
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Methods for creating statements
  //////////////////////////////////////////////////////////////////////////////////////////////////

  protected final Stmt newAssignment(
      final CommonTree where,
      final Node leftExpr,
      final Node rightExpr) throws SemanticException {
    checkNotNull(where, leftExpr, "The left hand side expression is not recognized.");
    checkNotNull(where, rightExpr, "The right hand side expression is not recognized.");

    /*
    // TODO: Workaround Node.isType throws exceptions
    // when field indexes used by BVEXTRACT are non-constant expressions.
    // Need to improve Node.isType.
    */

    try {
      if (rightExpr.isType(DataTypeId.BIT_VECTOR)) {
        final DataType ldt = leftExpr.getDataType();
        final DataType rdt = rightExpr.getDataType();
        checkTypes(where(where), ldt, rdt);
      }
    } catch (final IllegalStateException e) {
      Logger.warning(String.format(
          "%s: Unable to determine size of assignment operands.", where(where)));
    }

    final Node right;
    if (rightExpr.getUserData() instanceof Constant
        && !((Constant) rightExpr.getUserData()).isValue()
        && !((Constant) rightExpr.getUserData()).getVariable().isType(DataTypeId.BIT_VECTOR)) {
      right = new NodeVariable(((NodeVariable) rightExpr).getName(), leftExpr.getDataType());
      right.setUserData(rightExpr.getUserData());
    } else {
      right = rightExpr;
    }

    final Node left = leftExpr;
    propagator.assign(left, right);

    return new StmtAssign(left, right);
  }

  private void checkTypes(
      final Where where,
      final DataType destType,
      final DataType srcType) throws SemanticException {
    if (!srcType.equals(destType)) {
      Logger.warning("%s: Type mismatch: %s cannot be assigned to %s.", where, srcType, destType);
    }
  }

  protected final Stmt newException(final CommonTree message) {
    return new StmtException(message.getText());
  }

  protected final Stmt newTrace(
      final CommonTree format,
      final List<Node> fargs) throws SemanticException {
    checkNotNull(format, fargs);

    if (fargs.isEmpty()) {
      return new StmtTrace(format.getText());
    }

    final List<FormatMarker> markers = FormatMarker.extractMarkers(format.getText());
    if (markers.size() != fargs.size()) {
      raiseError(where(format), String.format(
          "Incorrect format: %d arguments are specified while %d are actually passed.",
          markers.size(), fargs.size()));
    }

    return new StmtTrace(format.getText(), markers, fargs);
  }

  protected final Stmt newMark(final CommonTree text) {
    return new StmtMark(text.getText());
  }

  protected final Stmt newAssert(final CommonTree place, final Node condition)
      throws SemanticException {
    if (!condition.isType(DataTypeId.LOGIC_BOOLEAN)) {
      raiseError(where(place), "Assertion is not a logical expression.");
    }

    return new StmtAssert(condition);
  }

  protected final class IfBuilder {
    private final List<Pair<Node, List<Stmt>>> ifBlocks;
    private List<Stmt> elseBlock;

    public IfBuilder(
        final CommonTree where,
        final Node cond,
        final List<Stmt> stmts) throws SemanticException {
      checkNotNull(where, stmts);
      checkNotNull(where, cond);

      this.ifBlocks = new ArrayList<>();
      this.elseBlock = Collections.emptyList();

      checkIsBoolean(where, cond);
      ifBlocks.add(new Pair<>(standardize(cond), stmts));
    }

    public void addElseIf(
        final CommonTree where, final Node cond, final List<Stmt> stmts) throws SemanticException {
      checkNotNull(where, stmts);
      checkNotNull(where, cond);

      checkIsBoolean(where, cond);
      ifBlocks.add(new Pair<>(standardize(cond), stmts));
    }

    public void setElse(final CommonTree where, final List<Stmt> stmts) throws SemanticException {
      checkNotNull(where, stmts);
      if (!stmts.isEmpty()) {
        elseBlock = stmts;
      }
    }

    public Stmt build() {
      return new StmtIf(ifBlocks, elseBlock);
    }

    private void checkIsBoolean(final CommonTree where, final Node cond) throws SemanticException {
      if (!cond.isType(DataTypeId.LOGIC_BOOLEAN)) {
        raiseError(where(where),
            "Incorrect conditional expression: only boolean expressions are accepted.");
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Expression Methods
  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new operator-based expression. Works in the following steps:
   *
   * <ol><li>Find Fortress operator</li>
   * <li>Reduce all operands</li>
   * <li>Cast all value operands to common type (bit vector) if required</li>
   * <li>Make NodeOperation and return</li></ol>
   *
   * @param operatorId Operator identifier.
   * @param operands Array of operands.
   * @return New expression.
   *
   * @throws SemanticException if the specified operator is not defined or does not support
   *         operands of the specified types.
   */
  protected final Node newExpression(
      final CommonTree operatorId,
      final Node... operands) throws RecognitionException {
    final Operator op = Operator.fromText(operatorId.getText());
    final Where w = where(operatorId);

    if (null == op) {
      raiseError(w, String.format("The %s operator is not supported.", operatorId.getText()));
    }

    // Trying to reduce all operands
    final Node[] folded = new Node[operands.length];
    for (int i = 0; i < operands.length; i++) {
      folded[i] = propagator.get(operands[i]);
    }

    DataType type = IntegerCast.findCommonType(Arrays.asList(folded));
    if (op.toFortressFor(type.getTypeId()) == null) {
      type = IntegerCast.findCommonType(Arrays.asList(operands));
    }

    final StandardOperation fortressOp = op.toFortressFor(type.getTypeId());
    if (null == fortressOp) {
      raiseError(w, String.format(
          "The %s operator is not supported for the %s type.", operatorId.getText(), type));
    }

    for (int i = 0; i < folded.length; ++i) {
      final Node castNode = IntegerCast.cast(folded[i], type);
      folded[i] = castNode;
    }

    return Reducer.reduce(
        ReduceOptions.NEW_INSTANCE,
        new NodeOperation(fortressOp, folded)
        );
  }

  /**
   * Creates a conditional expression of the following kind:
   *
   * <p>{@code if C1 then V1 (elif Ci then Vi)* else Vn endif}.
   *
   * @param id Token that marks location of the construction in code.
   * @param blocks Pairs [code condition expression, value expression].
   * @return New expression.
   */
  protected final Node newCondExpression(
      final CommonTree id,
      final List<Pair<Node, Node>> blocks) {
    InvariantChecks.checkNotNull(id);
    InvariantChecks.checkNotNull(blocks);
    InvariantChecks.checkTrue(blocks.size() >= 2);

    final List<Node> values = new ArrayList<>(blocks.size());
    for (final Pair<Node, Node> block : blocks) {
      values.add(block.second);
    }

    final DataType type = IntegerCast.findCommonType(values);
    InvariantChecks.checkNotNull(type);

    final Pair<Node, Node> elseBlock = blocks.get(blocks.size() - 1);
    InvariantChecks.checkTrue(elseBlock.first.equals(NodeValue.newBoolean(true)));

    Node result = elseBlock.second;
    for (int index = blocks.size() - 2; index >= 0; index--) {
      final Pair<Node, Node> currentBlock = blocks.get(index);

      final Node condition = standardize(currentBlock.first);
      final Node value = IntegerCast.cast(currentBlock.second, type);

      if (condition.equals(NodeValue.newBoolean(true))) {
        result = value;
      } else if (condition.equals(NodeValue.newBoolean(false))) {
        // result stays the same
      } else {
        result = Nodes.ite(condition, value, result);
      }
    }

    return result;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Concatenation and bitfield methods
  //////////////////////////////////////////////////////////////////////////////////////////////////

  protected final Node newConcat(
      final CommonTree where, final Node left, final Node right) throws SemanticException {

    checkNotNull(where, left);
    checkNotNull(where, right);

    return new NodeOperation(StandardOperation.BVCONCAT, left, right);
  }

  protected final Node newBitfield(
      final CommonTree where,
      final Node variable,
      final Node fromExpr,
      final Node toExpr) throws SemanticException {
    checkNotNull(where, variable);
    checkNotNull(where, fromExpr);

    final Node from = propagator.get(fromExpr);
    final Node to = (null != toExpr) ? propagator.get(toExpr) : from;

    final Node reducedFrom = Reducer.reduce(ReduceOptions.NEW_INSTANCE, from);
    final Node reducedTo = Reducer.reduce(ReduceOptions.NEW_INSTANCE, to);
    final Where w = this.where(where);

    if (!ExprUtils.isConstant(reducedFrom) || !ExprUtils.isConstant(reducedTo)) {
      raiseError(w, "Bit field expressions must be statically calculated constants.");
    }

    final Node distance = DistanceCalculator.get().distance(reducedTo, reducedFrom);
    if (!ExprUtils.isValue(distance)) {
      raiseError(w, "Unable to calculate bit field size.");
    }

    final BigInteger fieldSize =
        BigInteger.ONE.add(TypeConversion.integerValue((NodeValue) distance, false));

    return new NodeOperation(
        StandardOperation.BVEXTRACT,
        DataType.bitVector(fieldSize.intValue()),
        reducedFrom,
        reducedTo,
        variable
        );
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Methods to create variable atoms
  //////////////////////////////////////////////////////////////////////////////////////////////////

  protected final Node newAttributeRef(
      final CommonTree id,
      final boolean isLhs,
      final List<Node> args,
      final CommonTree attrId) throws SemanticException {
    checkNotNull(id, args);

    final AbstractStorage object = getGlobalObject(id);

    final String attrName;
    if (null != attrId) {
      attrName = attrId.getText();
    } else {
      attrName = isLhs ? AbstractStorage.WRITE_ATTR_NAME : AbstractStorage.READ_ATTR_NAME;
    }

    final Attribute attr = object.getAttribute(attrName);
    if (null == attr) {
      raiseError(where(id), String.format(
          "The %s attribute is not defined for the %s object.", attrName, id.getText()));
    }

    if (args.size() != 1) {
      raiseError(where(id), String.format(
          "Wrong number of arguments. The %s object requires one argument.", id.getText()));
    }

    final Node addressArg = args.get(0);
    if (null == addressArg) {
      raiseError(where(id), "Address argument is null.");
    }

    if (!ExprUtils.isVariable(addressArg)) {
      // A structure is a variable, an expression is a bit vector (which is invalid in this case).
      raiseError(where(id), "Address argument is not an address structure.");
    }

    final Var addressVar = (Var) addressArg.getUserData();

    final Type expectedType = object.getAddressArg().getType();
    if (!addressVar.getType().equals(expectedType)) {
      raiseError(where(id), String.format(
          "Wrong argument type. The %s object expects %s as an argument.",
          id.getText(), expectedType));
    }

    final AttributeRef attrRef = new AttributeRef(object, attr, addressArg);
    final Node variable = new NodeVariable(attrRef.getText(), attr.getDataType());
    variable.setUserData(attrRef);
    return variable;
  }

  protected final Node newVariable(
      final boolean isLhs, final CommonTree id) throws SemanticException {
    if (isLhs && ir.getExterns().containsKey(id.getText())) {
      raiseError(where(id), "Assigning extern variables is not allowed: " + id.getText());
    }

    return getVariable(id);
  }

  protected final Node newIndexedVariable(
      final CommonTree id, final Node indexExpr) throws SemanticException {
    checkNotNull(id, indexExpr);

    final NodeVariable variable = getVariable(id);
    return new NodeOperation(StandardOperation.SELECT, variable, indexExpr);
  }

  protected final Node newAttributeCall(
      final CommonTree id, final List<CommonTree> memberChain) throws SemanticException {

    if (isAddressId(id) && memberChain.size() == 1) {
      return newImmediateAttribute(id, memberChain.get(0));
    }

    Var variable = getVariableObject(id);
    for (final CommonTree member : memberChain) {
      final Var field = variable.getFields().get(member.getText());
      if (null == field) {
        raiseError(where(member),
                   String.format("The variable '%s' does not have the field '%s'.",
                                 variable.getName(),
                                 member.getText()));
      }
      variable = field;
    }
    return variable.getNode();
  }

  private boolean isAddressId(final CommonTree id) {
    return ir.getAddresses().containsKey(id.getText());
  }

  private Node newImmediateAttribute(final CommonTree idNode,
                                     final CommonTree attrNode) throws SemanticException {
    checkNotNull(idNode, attrNode);

    // FIXME generalize
    final Address addr = ir.getAddresses().get(idNode.getText());
    if (addr == null) {
      raiseError(where(idNode), "Immediate attributes being supported for addresses only");
    }

    final String attrName = attrNode.getText();
    if (!attrName.equals("width")) {
      raiseError(where(idNode), "Unknown immediate attribute: " + attrName);
    }
    return NodeValue.newInteger(addr.getAddressType().getBitSize());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Utility Methods
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private AbstractStorage getGlobalObject(final CommonTree objectId) throws SemanticException {
    final AbstractStorage object = globals.get(objectId.getText());
    if (null == object) {
      raiseError(where(objectId), String.format(
          "%s is not defined in the current scope or is not a global object.", objectId.getText()));
    }
    return object;
  }

  private NodeVariable getVariable(final CommonTree variableId) throws SemanticException {
    final NodeVariable variable = getVariableObject(variableId).getNode();
    if (null == variable) {
      raiseError(where(variableId), String.format(
          "%s is undefined in the current scope or is not a variable.", variableId.getText()));
    }
    return variable;
  }

  private Var getVariableObject(final CommonTree id) throws SemanticException {
    final Var variable = storage.get(id.getText());
    if (null == variable) {
      raiseError(where(id), String.format(
          "%s is undefined in the current scope or is not a variable.", id.getText()));
    }
    return variable;
  }

  private Address getAddress(final CommonTree addressId) throws SemanticException {
    final Address address = ir.getAddresses().get(addressId.getText());
    if (null == address) {
      raiseError(where(addressId), String.format(
          "%s is not defined or is not an address.", addressId.getText()));
    }
    return address;
  }

  private Buffer getBuffer(final CommonTree bufferId) throws SemanticException {
    final Buffer buffer = ir.getBuffers().get(bufferId.getText());
    if (null == buffer) {
      raiseError(where(bufferId), String.format(
          "%s is not defined or is not a buffer.", bufferId.getText()));
    }
    return buffer;
  }

  protected BigInteger extractBigInteger(
      final Where w, final Node expr, final String exprDesc) throws SemanticException {

    if (expr.getKind() != Node.Kind.VALUE || !expr.isType(DataTypeId.LOGIC_INTEGER)) {
      raiseError(w, String.format("%s is not a constant integer expression.", exprDesc));
    }

    final NodeValue nodeValue = (NodeValue) expr;
    return nodeValue.getInteger();
  }

  protected int extractInt(
      final Where w, final Node expr, final String exprDesc) throws SemanticException {

    final BigInteger value = extractBigInteger(w, expr, exprDesc);
    if (value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0
        || value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
      raiseError(w, String.format(
          "%s (=%d) is beyond the allowed integer value range.", exprDesc, value));
    }

    return value.intValue();
  }

  protected int extractPositiveInt(
      final Where w, final Node expr, final String nodeName) throws SemanticException {

    final int value = extractInt(w, expr, nodeName);
    if (value <= 0) {
      raiseError(w, String.format("%s (%d) must be > 0.", nodeName, value));
    }

    return value;
  }

  protected BigInteger extractPositiveBigInteger(
      final Where w, final Node expr, final String nodeName) throws SemanticException {

    final BigInteger value = extractBigInteger(w, expr, nodeName);
    if (value.compareTo(BigInteger.ZERO) <= 0) {
      raiseError(w, String.format("%s (%s) must be > 0.", nodeName, value));
    }

    return value;
  }
}
