/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.TreeNodeStream;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.ReduceOptions;
import ru.ispras.fortress.transformer.Transformer;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.mmu.model.api.PolicyId;
import ru.ispras.microtesk.mmu.translator.ir.AbstractStorage;
import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.Attribute;
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Field;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Stmt;
import ru.ispras.microtesk.mmu.translator.ir.StmtAssign;
import ru.ispras.microtesk.mmu.translator.ir.StmtException;
import ru.ispras.microtesk.mmu.translator.ir.StmtIf;
import ru.ispras.microtesk.mmu.translator.ir.StmtTrace;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.TreeParserBase;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.errors.SymbolTypeMismatch;
import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;
import ru.ispras.microtesk.utils.FormatMarker;

public abstract class MmuTreeWalkerBase extends TreeParserBase {
  private Ir ir;

  public MmuTreeWalkerBase(final TreeNodeStream input, final RecognizerSharedState state) {
    super(input, state);
    this.ir = null;
  }

  public final void assignIR(final Ir ir) {
    this.ir = ir;
  }

  public final Ir getIR() {
    return ir;
  }

  private MmuTreeWalkerContext context = MmuTreeWalkerContext.GLOBAL;

  protected void resetContext() {
    this.context = MmuTreeWalkerContext.GLOBAL;
  }

  /**
   * Creates an Address IR object and adds it to the MMU IR.
   * 
   * @param addressId Address identifier.
   * @param widthExpr Address width expression.
   * @return New Address IR object.
   * @throws SemanticException (1) if the width expression is {@code null}; (2) if the width
   * expression cannot be reduced to a constant integer value; (3) if the width value is beyond
   * the Java Integer allowed range; (4) if the width value is less or equal 0. 
   */

  protected final Address newAddress(
      final CommonTree addressId, final Node widthExpr) throws SemanticException {

    checkNotNull(addressId, widthExpr);

    final Where w = where(addressId);
    final int addressSize = extractPositiveInt(w, widthExpr, "Address width");
    final Address address = new Address(addressId.getText(), addressSize);

    ir.addAddress(address);
    return address;
  }

  /**
   * Builder for a Type. Helps create a complex type from a sequence of fields.
   */

  protected final class TypeBuilder {
    private int currentPos;
    private Map<String, Field> fields;

    /**
     * Constructs a new type builder.
     */

    public TypeBuilder() {
      this.currentPos = 0;
      this.fields = new LinkedHashMap<>();
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
 
      final int bitSize = extractPositiveInt(w, sizeExpr, id + " field size");

      BitVector defValue = null;
      if (null != valueExpr) {
        final BigInteger value = extractBigInteger(w, valueExpr, id + " field value");
        defValue = BitVector.valueOf(value, bitSize);
      }

      final Field field = new Field(id, currentPos, bitSize, defValue);
      currentPos += bitSize;

      fields.put(field.getId(), field);
    }

    /**
     * Builds a Type from the collection of fields.
     * @return New Type.
     */

    public Type build() {
      return new Type(fields);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////
  // TODO: Review + comments are needed

  /**
   * Builder for Builder objects. Helps create a Buffer from attributes.
   */

  protected final class BufferBuilder {
    private final CommonTree id;
    private final Address address;
    private final Variable addressArg;
    private final Buffer parent;

    private Variable dataArg; // stores entries
    private BigInteger ways;
    private BigInteger sets;
    private Node index;
    private Node match;
    private PolicyId policy;

    /**
     * Constructs a builder for a Buffer object.
     *    
     * @param bufferId Buffer identifier.
     * @param addressArgId Address argument identifier. 
     * @param addressArgType Address argument type (identifier).
     * @throws SemanticException if the specified address type is not defined.
     */

    public BufferBuilder(
        final CommonTree id,
        final CommonTree addressArgId,
        final CommonTree addressArgType,
        final CommonTree parentBufferId) throws SemanticException {
      this.id = id;
      this.address = getAddress(addressArgType);
      this.addressArg = new Variable(addressArgId.getText(), address);
      this.parent = null != parentBufferId ? getBuffer(parentBufferId) : null;

      this.dataArg = null;
      this.ways = BigInteger.ZERO;
      this.sets = BigInteger.ZERO;
      this.index = null;
      this.match = null;
      this.policy = null;

      context = new MmuTreeWalkerContext(MmuTreeWalkerContext.Kind.BUFFER, id.getText());
      context.defineVariable(addressArg);
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
      checkRedefined(attrId, dataArg != null);
      dataArg = new Variable(attrId.getText(), attr);

      for (final Field field : attr.getFields()) {
        final NodeVariable valiable = dataArg.getVariableForField(field.getId());
        context.defineVariableAs(field.getId(), valiable);
      }
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
      checkRedefined(attrId, policy != null);
      try {
        final PolicyId value = PolicyId.valueOf(attr.getText());
        policy = value;
      } catch (Exception e) {
        raiseError(where(attr), "Unknown policy: " + attr.getText()); 
      }
    }

    public Buffer build() throws SemanticException {
      checkUndefined("ways", ways.equals(BigInteger.ZERO));
      checkUndefined("sets", sets.equals(BigInteger.ZERO));
      checkUndefined("entry", dataArg == null); 
      checkUndefined("index", index == null);
      checkUndefined("match", match == null);

      if (null == policy) {
        policy = PolicyId.NONE;
      }

      final Buffer buffer = new Buffer(
          id.getText(), address, addressArg, dataArg, ways, sets, index, match, policy, parent);

      ir.addBuffer(buffer);
      return buffer;
    }
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
                             addressArgId.getText(),
                             address,
                             dataArgId.getText(),
                             new Type(dataSize));
  }

  protected final CommonBuilder newSegmentBuilder(
      final CommonTree id,
      final CommonTree addressArgId,
      final CommonTree addressArgType,
      final CommonTree outputVarId,
      final CommonTree outputVarType) throws SemanticException {

    final Address address = getAddress(addressArgType);
    if (outputVarId == null || outputVarType == null) {
      return new CommonBuilder(where(id), id.getText(), addressArgId.getText(), address);
    }
    final Address outputAddr = getAddress(outputVarType);
    return new CommonBuilder(where(id),
                             id.getText(),
                             addressArgId.getText(),
                             address,
                             outputVarId.getText(),
                             outputAddr.getType());
  }

  protected final class CommonBuilder {
    private final Where where;

    private final String id;
    private final Address address;
    private final Variable addressArg;
    private final Variable outputVar;

    private final Map<String, Variable> variables;
    private final Map<String, Attribute> attributes;

    private CommonBuilder(
        final Where where,
        final String id,
        final String addressArgId,
        final Address addressArgType) {
      this(where, id, addressArgType, new Variable(addressArgId, addressArgType), null);
    }

    private CommonBuilder(
        final Where where,
        final String id,
        final String addressArgId,
        final Address addressArgType,
        final String outputVarId,
        final Type outputVarType) {
      this(where,
           id,
           addressArgType,
           new Variable(addressArgId, addressArgType),
           new Variable(outputVarId, outputVarType));
    }

    private CommonBuilder(
        final Where where,
        final String id,
        final Address address,
        final Variable addressVar,
        final Variable outputVar) {
      this.where = where;
      this.id = id;
      this.address = address;
      this.addressArg = addressVar;
      this.outputVar = outputVar;

      this.variables = new LinkedHashMap<>();
      this.attributes = new LinkedHashMap<>();

      context = new MmuTreeWalkerContext(MmuTreeWalkerContext.Kind.BUFFER, id);
      context.defineVariable(addressArg);
      context.defineVariable(outputVar);

      final NodeVariable memoryVar = new NodeVariable(id,
          DataType.MAP(DataType.UNKNOWN, outputVar.getDataType()));

      context.defineVariable(memoryVar);

      context.defineGlobalObjects(ir.getSegments().values());
      context.defineGlobalObjects(ir.getBuffers().values());
    }

    public void addVariable(final CommonTree varId, final Node sizeExpr) throws SemanticException {
      checkNotNull(varId, sizeExpr);

      final int bitSize = extractPositiveInt(
          where(varId), sizeExpr, String.format("Variable %s size", varId.getText()));

      final Variable variable = new Variable(varId.getText(), bitSize);

      variables.put(variable.getId(), variable);
      context.defineVariable(variable);
    }

    public void addVariable(
        final CommonTree varId, final CommonTree typeId) throws SemanticException {
      final ISymbol symbol = getSymbol(typeId);

      Variable variable = null;
      if (MmuSymbolKind.BUFFER == symbol.getKind()) {
        final Buffer buffer = getBuffer(typeId);
        variable = new Variable(varId.getText(), buffer);
      } else if (MmuSymbolKind.ADDRESS == symbol.getKind()) {
        final Address address = getAddress(typeId);
        variable = new Variable(varId.getText(), address);
      } else {
        raiseError(where(typeId), new SymbolTypeMismatch(symbol.getName(), symbol.getKind(),
            Arrays.<Enum<?>>asList(MmuSymbolKind.BUFFER, MmuSymbolKind.ADDRESS)));
      }

      variables.put(variable.getId(), variable);
      context.defineVariable(variable);
    }

    public void addAttribute(final CommonTree attrId, final List<Stmt> stmts) {
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

      final Memory memory = new Memory(
          id, address, addressArg, outputVar, variables, attributes);

      ir.addMemory(memory);
      return memory;
    }

    /**
     * Creates a segment IR object and adds it to the MMU IR.
     * 
     * @param segmentId Segment identifier.
     * @param addressArgId Address argument identifier. 
     * @param addressArgType Address argument type (identifier).
     * @param rangeStartExpr Range start expression.
     * @param rangeEndExpr Range and expression.
     * @return New Segment IR object.
     * @throws SemanticException (1) if the specified address type is not defined;
     * (2) if the range expressions equal to {@code null}, (3) if the range expressions
     * cannot be reduced to constant integer values; (4) if the range start
     * value is greater than the range end value.
     */

    public Segment buildSegment(final Node rangeStartExpr,
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
          outputVar,
          variables,
          attributes
          );

      ir.addSegment(segment);
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

    context.setAssignedValue(leftExpr, rightExpr);
    return new StmtAssign(leftExpr, rightExpr);
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
      ifBlocks.add(new Pair<>(cond, stmts));
    }

    public void addElseIf(
        final CommonTree where, final Node cond, final List<Stmt> stmts) throws SemanticException {
      checkNotNull(where, stmts);
      checkNotNull(where, cond);

      checkIsBoolean(where, cond);
      ifBlocks.add(new Pair<>(cond, stmts));
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
   * @return
   * @throws RecognitionException
   */

  protected final Node newExpression(
      final CommonTree operatorId, final Node ... operands) throws RecognitionException {
    final String ERR_NO_OPERATOR = "The %s operator is not supported.";
    final String ERR_NO_OPERATOR_FOR_TYPE = "The %s operator is not supported for the %s type.";

    final Operator op = Operator.fromText(operatorId.getText());
    final Where w = where(operatorId);

    if (null == op) {
      raiseError(w, String.format(ERR_NO_OPERATOR, operatorId.getText()));
    }

    // Trying to reduce all operands
    final Node[] substituted = new Node[operands.length];
    for (int i = 0; i < operands.length; i++) {
      substituted[i] = context.getAssignedValue(operands[i]);
    }

    // Calculating the type all argument are to be cast to
    final DataType firstOpType = substituted[0].getDataType();
    DataType type = firstOpType;

    for (int i = 1; i < substituted.length; i++) {
      final Node operand = substituted[i];
      final DataType currentType = operand.getDataType(); 

      // Size is always greater for bit vectors.
      if (currentType.getSize() > type.getSize()) { 
        type = currentType;
      }
    }

    if (type != firstOpType && type.getTypeId() == DataTypeId.BIT_VECTOR) {
      for (int i = 0; i < substituted.length; i++) {
        final Node operand = substituted[i];
        if ((operand instanceof NodeValue) && !type.equals(operand.getDataType())) {
          final BigInteger value = ((NodeValue) operand).getInteger();
          substituted[i] = new NodeValue(Data.newBitVector(value, type.getSize()));
        }
      }
    }

    final StandardOperation fortressOp = op.toFortressFor(type.getTypeId());
    if (null == fortressOp) {
      raiseError(w, String.format(ERR_NO_OPERATOR_FOR_TYPE, operatorId.getText(), type));
    }

    return Transformer.reduce(ReduceOptions.NEW_INSTANCE,
                              new NodeOperation(fortressOp, substituted));
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

    final Node from = context.getAssignedValue(fromExpr);
    final Node to = (null != toExpr) ? context.getAssignedValue(toExpr) : from;

    final Node reducedFrom = Transformer.reduce(ReduceOptions.NEW_INSTANCE, from);
    final Node reducedTo = Transformer.reduce(ReduceOptions.NEW_INSTANCE, to);

    return new NodeOperation(StandardOperation.BVEXTRACT, reducedFrom, reducedTo, variable);
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
    if (!addressArg.getDataType().equals(object.getAddressArg().getDataType())) {
      raiseError(where(id), String.format(
          "Wrong argument type. The %s object expects %s as an argument.",
          id.getText(), object.getAddressArg().getType()));
    }

    final AttributeRef attrRef = new AttributeRef(object, attr, addressArg);
    final Node variable = new NodeVariable(attrRef.getText(), attr.getDataType());
    variable.setUserData(attrRef);
    return variable;
  }

  protected final Node newVariable(final CommonTree id) throws SemanticException {
    return getVariable(id);
  }

  protected final Node newIndexedVariable(
      final CommonTree id, final Node indexExpr) throws SemanticException {
    checkNotNull(id, indexExpr);

    final NodeVariable variable = getVariable(id);
    return new NodeOperation(StandardOperation.SELECT, variable, indexExpr);
  }

  protected final Node newAttributeCall(
      final CommonTree id, final CommonTree attributeId) throws SemanticException {

    final NodeVariable variableNode = getVariable(id);
    if (!(variableNode.getUserData() instanceof Variable)) {
      raiseError(where(id), String.format(
          "Cannot access fields of the %s variable. No type information.", id.getText()));
    }

    final Variable variable = (Variable) variableNode.getUserData();
    final NodeVariable fieldNode = variable.getVariableForField(attributeId.getText());
    if (null == fieldNode) {
      raiseError(where(id), String.format(
          "The %s variable does not include the %s field.", id.getText(), attributeId.getText()));
    }

    return fieldNode;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Utility Methods
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private AbstractStorage getGlobalObject(final CommonTree objectId) throws SemanticException {
    final AbstractStorage object = context.getGlobalObject(objectId.getText());
    if (null == object) {
      raiseError(where(objectId), String.format(
          "%s is not defined in the current scope or is not a global object.", objectId.getText()));
    }
    return object;
  }

  private NodeVariable getVariable(final CommonTree variableId) throws SemanticException {
    final NodeVariable variable = context.getVariable(variableId.getText());
    if (null == variable) {
      raiseError(where(variableId), String.format(
          "%s is undefined in the current scope or is not a variable.", variableId.getText()));
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

  private BigInteger extractBigInteger(
      final Where w, final Node expr, final String exprDesc) throws SemanticException {

    if (expr.getKind() != Node.Kind.VALUE || !expr.isType(DataTypeId.LOGIC_INTEGER)) {
      raiseError(w, String.format("%s is not a constant integer expression.", exprDesc));
    }

    final NodeValue nodeValue = (NodeValue) expr;
    return nodeValue.getInteger();
  }

  private int extractInt(
      final Where w, final Node expr, final String exprDesc) throws SemanticException {

    final BigInteger value = extractBigInteger(w, expr, exprDesc);
    if (value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0 ||
        value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
      raiseError(w, String.format(
          "%s (=%d) is beyond the allowed integer value range.", exprDesc, value)); 
    }

    return value.intValue();
  }

  private int extractPositiveInt(
      final Where w, final Node expr, final String nodeName) throws SemanticException {

    final int value = extractInt(w, expr, nodeName);
    if (value <= 0) {
      raiseError(w, String.format("%s (%d) must be > 0.", nodeName, value));
    }

    return value;
  }

  private BigInteger extractPositiveBigInteger(
      final Where w, final Node expr, final String nodeName) throws SemanticException {

    final BigInteger value = extractBigInteger(w, expr, nodeName);
    if (value.compareTo(BigInteger.ZERO) <= 0) {
      raiseError(w, String.format("%s (%s) must be > 0.", nodeName, value));
    }

    return value;
  }
}
