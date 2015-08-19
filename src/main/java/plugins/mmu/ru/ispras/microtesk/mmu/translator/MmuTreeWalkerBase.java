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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.TreeNodeStream;

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
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.mmu.model.api.PolicyId;
import ru.ispras.microtesk.mmu.translator.ir.AbstractStorage;
import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.Attribute;
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Stmt;
import ru.ispras.microtesk.mmu.translator.ir.StmtAssign;
import ru.ispras.microtesk.mmu.translator.ir.StmtException;
import ru.ispras.microtesk.mmu.translator.ir.StmtIf;
import ru.ispras.microtesk.mmu.translator.ir.StmtMark;
import ru.ispras.microtesk.mmu.translator.ir.StmtTrace;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.mmu.translator.ir.spec.builder.VariableStorage;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.TreeParserBase;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.errors.SymbolTypeMismatch;
import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;
import ru.ispras.microtesk.translator.nml.coverage.IntegerCast;
import ru.ispras.microtesk.utils.FormatMarker;

public abstract class MmuTreeWalkerBase extends TreeParserBase {
  private Ir ir;
  private VariableStorage storage = new VariableStorage();
  private Map<String, AbstractStorage> globals = new HashMap<>();
  protected ConstantPropagator propagator = new ConstantPropagator();

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
   * Adds a static constant (let expression) to the IR.
   * 
   * @param id Constant identifier.
   * @param value Constant value.
   * @throws SemanticException (1) if the value expression is {@code null};
   *                           (2) if value expression is not a constant value.
   */

  protected final void newConstant(final CommonTree id, final Node value) throws SemanticException {
    checkNotNull(id, value);

    if (value.getKind() != Node.Kind.VALUE) {
      raiseError(where(id), String.format(
          "Illegal let definition. A constant expression is required: %s", value));
    }

    ir.addConstant(id.getText(), (NodeValue) value);
  }

  /**
   * Returns the value of the specified constant. 
   * 
   * @param id Constant identifier.
   * @return Constant value.
   * @throws SemanticException if the constant is not defined.
   */

  protected final NodeValue getConstant(final CommonTree id) throws SemanticException {
    final NodeValue value = ir.getConstants().get(id.getText());

    if (null == value) {
      raiseError(where(id), "Constant is undefined: " + id.getText());
    }

    return value;
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
      final Type type = ir.getTypes().get(typeId.getText());
      if (type == null) {
        raiseError(w, String.format("Unkown type name '%s'.", typeId.getText()));
      }

      fields.put(id, type);
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

  //////////////////////////////////////////////////////////////////////////////
  // TODO: Review + comments are needed

  /**
   * Builder for Builder objects. Helps create a Buffer from attributes.
   */

  protected final class BufferBuilder {
    private final CommonTree id;
    private final Address address;
    private final String addressArgId;
    private final Variable addressArg;
    private final Buffer parent;

    private Variable dataArg = null; // stores entries
    private BigInteger ways = BigInteger.ZERO;
    private BigInteger sets = BigInteger.ZERO;
    private Node index = null;
    private Node match = null;
    private Node guard = null;
    private PolicyId policy = null;

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

      storage.newScope(id.getText());
      this.addressArgId = addressArgId.getText();
      this.addressArg = storage.declare(addressArgId.getText(),
                                        address.getContentType());
      storage.popScope();

      context = new MmuTreeWalkerContext(MmuTreeWalkerContext.Kind.BUFFER, id.getText());
      // context.defineVariable(addressArg);


      if (parentBufferId != null) {
        this.parent = getBuffer(parentBufferId);

        // setDataArg() depends on context, therefore the latter should be
        // created prior to
        setDataArg(id.getText(), this.parent.getEntry());
      } else {
        this.parent = null;
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

      if (parent != null) {
        raiseError(where(attrId), "Buffer view forbids 'entry' attribute redefinition.");
      }
      checkRedefined(attrId, dataArg != null);
      setDataArg(id.getText(), attr);
    }

    private void setDataArg(final String name, final Type type) {
      dataArg = storage.declare(name, type);
      final Map<String, Variable> scope = new HashMap<>(dataArg.getFields());
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

    public void setGuard(final CommonTree attrId, final Node attr) throws SemanticException {
      checkNotNull(attrId, attr);

      if (parent == null) {
        raiseError(where(attrId), "'guard' attribute is allowed only for buffer views.");
      }
      checkRedefined(attrId, guard != null);
      guard = attr;
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
      checkUndefined("entry", dataArg == null && parent == null); 
      checkUndefined("index", index == null);
      checkUndefined("match", match == null);

      if (null == policy) {
        policy = PolicyId.NONE;
      }

      final Buffer buffer = new Buffer(
          id.getText(), address, addressArg, dataArg, ways, sets, index, match, guard, policy, parent);

      ir.addBuffer(buffer);
      globals.put(id.getText(), buffer);
      storage.popScope();

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

  protected final class CommonBuilder {
    private final Where where;

    private final String id;
    private final Address address;
    private final Variable addressArg;
    private final Variable outputVar;
    private final Address outputVarAddress;

    private final Map<String, Variable> variables;
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

      context = new MmuTreeWalkerContext(MmuTreeWalkerContext.Kind.BUFFER, id);
    }

    public void addVariable(final CommonTree varId, final Node sizeExpr) throws SemanticException {
      checkNotNull(varId, sizeExpr);

      final int bitSize = extractPositiveInt(
          where(varId), sizeExpr, String.format("Variable %s size", varId.getText()));

      final Variable variable = storage.declare(varId.getText(), new Type(bitSize));
      variables.put(variable.getName(), variable);
    }

    public void addVariable(
        final CommonTree varId, final CommonTree typeId) throws SemanticException {
      final ISymbol symbol = getSymbol(typeId);

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
        type = null;
        source = null;
        raiseError(where(typeId), new SymbolTypeMismatch(symbol.getName(), symbol.getKind(),
            Arrays.<Enum<?>>asList(MmuSymbolKind.BUFFER, MmuSymbolKind.ADDRESS)));
      }

      final Variable v = storage.declare(varId.getText(), type, source);
      variables.put(v.getName(), v);
      // context.defineVariable(variable);
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
      storage.popScope();

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

    // context.setAssignedValue(leftExpr, rightExpr);
    propagator.assign(leftExpr, rightExpr);

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

  protected final Stmt newMark(final CommonTree text) {
    return new StmtMark(text.getText());
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
    final Node[] folded = new Node[operands.length];
    for (int i = 0; i < operands.length; i++) {
      folded[i] = propagator.get(operands[i]);
    }

    DataType type =
        IntegerCast.findCommonType(Arrays.asList(folded));
    if (op.toFortressFor(type.getTypeId()) == null) {
      type = IntegerCast.findCommonType(Arrays.asList(operands));
    }

    final StandardOperation fortressOp = op.toFortressFor(type.getTypeId());
    if (null == fortressOp) {
      raiseError(w, String.format(ERR_NO_OPERATOR_FOR_TYPE, operatorId.getText(), type));
    }

    final Node TRUE = NodeValue.newBoolean(true);
    final Node FALSE = NodeValue.newBoolean(false);

    final boolean isAnd = StandardOperation.AND == fortressOp; 
    final boolean isOr = StandardOperation.OR == fortressOp;

    for (int i = 0; i < folded.length; ++i) {
      final Node castNode = IntegerCast.cast(folded[i], type);
      folded[i] = castNode;

      if (isAnd && castNode.equals(FALSE) || isOr && castNode.equals(TRUE)) {
        return castNode;
      }
    }

    return Transformer.reduce(ReduceOptions.NEW_INSTANCE,
                              new NodeOperation(fortressOp, folded));
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

    final Where w = this.where(where);

    final Node reducedFrom = Transformer.reduce(ReduceOptions.NEW_INSTANCE, from);
    // FIXME
    //assertNodeInteger(w, reducedFrom);

    final Node reducedTo = Transformer.reduce(ReduceOptions.NEW_INSTANCE, to);
    // FIXME
    //assertNodeInteger(w, reducedTo);

    return new NodeOperation(StandardOperation.BVEXTRACT, reducedFrom, reducedTo, variable);
  }

  private void assertNodeInteger(final Where w, final Node node) throws SemanticException {
    if (node.getKind() != Node.Kind.VALUE ||
        !node.isType(DataType.INTEGER)) {
      raiseError(w, "Expecting constant integer expression, found " + node);
    }
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
    final Variable addressVar = (Variable) addressArg.getUserData();

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
      final CommonTree id, final List<CommonTree> memberChain) throws SemanticException {

    Variable variable = getVariableObject(id);
    for (final CommonTree member : memberChain) {
      final Variable field = variable.getFields().get(member.getText());
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
    final NodeVariable variable = getVariableObject(variableId).getNode(); // context.getVariable(variableId.getText());
    if (null == variable) {
      raiseError(where(variableId), String.format(
          "%s is undefined in the current scope or is not a variable.", variableId.getText()));
    }
    return variable;
  }

  private Variable getVariableObject(final CommonTree id) throws SemanticException {
    final Variable variable = storage.get(id.getText());
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
