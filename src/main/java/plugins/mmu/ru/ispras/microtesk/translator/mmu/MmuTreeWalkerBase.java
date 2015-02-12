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

package ru.ispras.microtesk.translator.mmu;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
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
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.ReduceOptions;
import ru.ispras.fortress.transformer.Transformer;

import ru.ispras.microtesk.model.api.mmu.PolicyId;

import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.TreeParserBase;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.errors.SymbolTypeMismatch;
import ru.ispras.microtesk.translator.antlrex.errors.UndeclaredSymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;

import ru.ispras.microtesk.translator.mmu.ir.Address;
import ru.ispras.microtesk.translator.mmu.ir.Attribute;
import ru.ispras.microtesk.translator.mmu.ir.Buffer;
import ru.ispras.microtesk.translator.mmu.ir.Field;
import ru.ispras.microtesk.translator.mmu.ir.Ir;
import ru.ispras.microtesk.translator.mmu.ir.Memory;
import ru.ispras.microtesk.translator.mmu.ir.Stmt;
import ru.ispras.microtesk.translator.mmu.ir.Type;
import ru.ispras.microtesk.translator.mmu.ir.Var;
import ru.ispras.microtesk.translator.mmu.ir.Segment;

public abstract class MmuTreeWalkerBase extends TreeParserBase {
  private Ir ir;

  public MmuTreeWalkerBase(TreeNodeStream input, RecognizerSharedState state) {
    super(input, state);
    this.ir = null;
  }

  public final void assignIR(Ir ir) {
    this.ir = ir;
  }

  public final Ir getIR() {
    return ir;
  }
 
  protected static final class Context {
    public static enum Kind {
      GLOBAL,
      BUFFER,
      MEMORY
    }

    private static final Context GLOBAL = new Context(Kind.GLOBAL, "");

    private final Kind kind;
    private final String id;
    private final Map<String, NodeVariable> variables;

    private Context(Kind kind, String id) {
      this.kind = kind;
      this.id = id;
      this.variables = new HashMap<String, NodeVariable>();
    }

    public Kind getKind() {
      return kind;
    }

    public String getId() {
      return id;
    }
    
    public void defineVariable(NodeVariable variable) {
      variables.put(variable.getName(), variable);
    }

    public NodeVariable getVariable(String variableId) {
      return variables.get(variableId);
    }
  }

  private Context context = Context.GLOBAL;

  protected void resetContext() {
    this.context = Context.GLOBAL;
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
      CommonTree addressId, Node widthExpr) throws SemanticException {

    checkNotNull(addressId, widthExpr);

    final Where w = where(addressId);
    final int addressSize = extractPositiveInt(w, widthExpr, "Address width");

    final Type type = new Type(addressSize); 
    final Address address = new Address(addressId.getText(), type);
    
    ir.addAddress(address);
    return address;
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

  protected final Segment newSegment(
      CommonTree segmentId,
      CommonTree addressArgId,
      CommonTree addressArgType,
      Node rangeStartExpr,
      Node rangeEndExpr) throws SemanticException {

    checkNotNull(segmentId, rangeStartExpr);
    checkNotNull(segmentId, rangeEndExpr);

    final Where w = where(segmentId);
    final Address address = getAddress(w, addressArgType.getText());

    final BigInteger rangeStart = extractBigInteger(w, rangeStartExpr, "Range start");
    final BigInteger rangeEnd = extractBigInteger(w, rangeEndExpr, "Range end");

    if (rangeStart.compareTo(rangeEnd) > 0) {
      raiseError(w, String.format(
          "Range start (%d) is greater than range end (%d).", rangeStart, rangeEnd));
    }
    
    final Var addressArg = new Var(addressArgId.getText(), address.getType(), address);

    final Segment segment = new Segment(
        segmentId.getText(),
        addressArg,
        BitVector.valueOf(rangeStart, address.getBitSize()),
        BitVector.valueOf(rangeEnd, address.getBitSize())
        );

    ir.addSegment(segment);
    return segment;
  }

  /**
   * Creates a builder for an Entry object.
   * @return Entry builder.
   */

  protected final EntryBuilder newEntryBuilder() {
    return new EntryBuilder();
  }

  /**
   * Builder for an Entry. Helps create an Entry from a sequence of Fields. 
   */

  protected final class EntryBuilder {
    private int currentPos;
    private Map<String, Field> fields;

    private EntryBuilder() {
      this.currentPos = 0;
      this.fields = new LinkedHashMap<>();
    }

    /**
     * Adds a field to Entry to be created.
     * 
     * @param fieldId Field identifier.
     * @param sizeExpr Field size expression.
     * @param valueExpr Field default value expression (optional, can be {@code null}).
     * @throws SemanticException (1) if the size expression is {@code null}, (2) if
     * the size expression cannot be evaluated to a positive integer value (Java int).
     */

    public void addField(CommonTree fieldId, Node sizeExpr, Node valueExpr) throws SemanticException {
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
     * Builds an Entry from the collection of fields.
     * @return New Entry.
     */

    public Type build() {
      return new Type(fields);
    }
  }

  /**
   * Creates a builder for a Buffer object.
   *    
   * @param bufferId Buffer identifier.
   * @param addressArgId Address argument identifier. 
   * @param addressArgType Address argument type (identifier).
   * @return New BufferBulder object.
   * @throws SemanticException if the specified address type is not defined.
   */

  protected final BufferBuilder newBufferBuilder(
      CommonTree bufferId,
      CommonTree addressArgId,
      CommonTree addressArgType) throws SemanticException {

    final Where w = where(bufferId);
    final Address address = getAddress(w, addressArgType.getText());
    return new BufferBuilder(w, bufferId.getText(), addressArgId.getText(), address);
  }

  //////////////////////////////////////////////////////////////////////////////
  // TODO: Review + comments are needed

  /**
   * Builder for Builder objects. Helps create a Buffer from attributes.
   */

  protected final class BufferBuilder {
    private final Where where;
    
    private final String id;
    private final String addressArgId;
    private final Address addressArgType;

    private int ways;
    private int sets;
    private Type entry;
    private Node index;
    private Node match;
    private PolicyId policy;

    private BufferBuilder(Where where, String id, String addressArgId, Address addressArgType) {
      this.where = where;

      this.id = id;
      this.addressArgId = addressArgId;
      this.addressArgType = addressArgType;
      
      this.ways = 0;
      this.sets = 0;
      this.entry = null;
      this.index = null;
      this.match = null;
      this.policy = null;

      context = new Context(Context.Kind.BUFFER, id);

      final Variable addressArg = new Variable(
          addressArgId, DataType.BIT_VECTOR(addressArgType.getBitSize()));

      final NodeVariable addressArgNode = new NodeVariable(addressArg);
      context.defineVariable(addressArgNode);
    }

    private void checkRedefined(CommonTree attrId, boolean isRedefined) throws SemanticException {
      if (isRedefined) {
        raiseError(where(attrId),
            String.format("The %s attribute is redefined.", attrId.getText()));
      }
    }

    private void checkUndefined(String attrId, boolean isUndefined) throws SemanticException {
      if (isUndefined) {
        raiseError(where, String.format("The %s attribute is undefined.", attrId));
      }
    }

    public void setWays(CommonTree attrId, Node attr) throws SemanticException {
      checkNotNull(attrId, attr);
      checkRedefined(attrId, ways != 0);
      ways = extractPositiveInt(where(attrId), attr, attrId.getText());
    }

    public void setSets(CommonTree attrId, Node attr) throws SemanticException {
      checkNotNull(attrId, attr);
      checkRedefined(attrId, sets != 0);
      sets = extractPositiveInt(where(attrId), attr, attrId.getText());
    }

    public void setEntry(CommonTree attrId, Type attr) throws SemanticException {
      checkNotNull(attrId, attr);
      checkRedefined(attrId, entry != null);
      entry = attr;

      for (Field f : attr.getFields()) {
        final Variable field = new Variable(f.getId(), f.getDataType());
        final NodeVariable fieldNode = new NodeVariable(field);
        context.defineVariable(fieldNode);
      }
    }

    public void setIndex(CommonTree attrId, Node attr) throws SemanticException {
      checkNotNull(attrId, attr);
      checkRedefined(attrId, index != null);
      index = attr;
    }

    public void setMatch(CommonTree attrId, Node attr) throws SemanticException {
      checkNotNull(attrId, attr);
      checkRedefined(attrId, match != null);
      match = attr;
    }

    public void setPolicyId(CommonTree attrId, CommonTree attr) throws SemanticException {
      checkRedefined(attrId, policy != null);
      try {
        final PolicyId value = PolicyId.valueOf(attr.getText());
        policy = value;
      } catch (Exception e) {
        raiseError(where(attr), "Unknown policy: " + attr.getText()); 
      }
    }

    public Buffer build() throws SemanticException {
      checkUndefined("ways", ways == 0);
      checkUndefined("sets", sets == 0);
      checkUndefined("entry", entry == null); 
      checkUndefined("index", index == null);
      checkUndefined("match", match == null);

      if (null == policy) {
        policy = PolicyId.NONE;
      }

      final Var addressArg = new Var(
          addressArgId, addressArgType.getType(), addressArgType);

      final Buffer buffer = new Buffer(
          id, addressArg, ways, sets, entry, index, match, policy);

      ir.addBuffer(buffer);
      return buffer;
    }
  }

  protected final MemoryBuilder newMemoryBuilder(
      CommonTree memoryId,
      CommonTree addressArgId,
      CommonTree addressArgType,
      CommonTree dataArgId,
      Node dataArgSizeExpr) throws SemanticException {

    final Where w = where(memoryId);
    final Address address = getAddress(w, addressArgType.getText());
    
    final int dataSize = extractPositiveInt(
        where(dataArgId), dataArgSizeExpr, "Data argument size");

    return new MemoryBuilder(w, 
        memoryId.getText(), addressArgId.getText(), address, dataArgId.getText(), dataSize);
  }

  protected final class MemoryBuilder {
    private final Where where;

    private final String id;
    private final String addressArgId;
    private final Address addressArgType;

    private final String dataArgId;
    private final int dataArgBitSize;

    private final Map<String, Var> variables;
    private final Map<String, Attribute> attributes;

    private MemoryBuilder(Where where, String id,
        String addressArgId, Address addressArgType, String dataArgId, int dataArgBitSize) {

      this.where = where;
      this.id = id;
      this.addressArgId = addressArgId;
      this.addressArgType = addressArgType;
      this.dataArgId = dataArgId;
      this.dataArgBitSize = dataArgBitSize;
      this.variables = new LinkedHashMap<>();
      this.attributes = new LinkedHashMap<>();
      
      context = new Context(Context.Kind.BUFFER, id);

      final Variable addressArg = new Variable(addressArgId, addressArgType.getDataType());
      final NodeVariable addressArgNode = new NodeVariable(addressArg);

      context.defineVariable(addressArgNode);
    }

    public void addVariable(CommonTree varId, Node sizeExpr) throws SemanticException {
      checkNotNull(varId, sizeExpr);

      final int bitSize = extractPositiveInt(
          where(varId), sizeExpr, String.format("Variable %s size", varId.getText()));

      final Var var = new Var(varId.getText(), new Type(bitSize));

      variables.put(var.getId(), var);
      context.defineVariable(var.getVariable());
    }

    public void addVariable(CommonTree varId, CommonTree typeId) throws SemanticException {
      final Where w = where(typeId);
      final ISymbol symbol = getSymbols().resolve(typeId.getText());
      if (null == symbol) {
        raiseError(w, new UndeclaredSymbol(typeId.getText()));
      }

      Var var = null;
      if (MmuSymbolKind.BUFFER == symbol.getKind()) {
        final Buffer buffer = getBuffer(w, typeId.getText());
        var = new Var(varId.getText(), buffer.getEntry(), buffer);
      } else if (MmuSymbolKind.ADDRESS == symbol.getKind()) {
        final Address address = getAddress(w, typeId.getText());
        var = new Var(varId.getText(), address.getType(), address);
      } else {
        raiseError(w, new SymbolTypeMismatch(symbol.getName(), symbol.getKind(),
            Arrays.<Enum<?>>asList(MmuSymbolKind.BUFFER, MmuSymbolKind.ADDRESS)));
      }

      variables.put(var.getId(), var);
      context.defineVariable(var.getVariable());
    }

    public void addAttribute(CommonTree attrId, List<Stmt> stmts) {
      final Attribute attr = new Attribute(attrId.getText(), attrId.getText().equals("read") ? DataType.BIT_VECTOR(dataArgBitSize) : null, stmts);
      attributes.put(attr.getId(), attr);
    }

    public Memory build() throws SemanticException {
      if (!attributes.containsKey("read")) {
        raiseError(where, "The 'read' action is not defined.");
      }

      if (!attributes.containsKey("write")) {
        raiseError(where, "The 'write' action is not defined.");
      }

      final Var addressArg = new Var(addressArgId, addressArgType.getType());
      final Var dataArg = new Var(dataArgId, new Type(dataArgBitSize));

      final Memory memory = new Memory(
          id, addressArg, dataArg, variables, attributes);

      ir.addMemory(memory);
      return memory;
    }
  }
 
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

  public Node newExpression(CommonTree operatorId, Node ... operands) throws RecognitionException {
    final String ERR_NO_OPERATOR = "The %s operator is not supported.";
    final String ERR_NO_OPERATOR_FOR_TYPE = "The %s operator is not supported for the %s type.";

    final Operator op = Operator.fromText(operatorId.getText());
    final Where w = where(operatorId);
    
    if (null == op) {
      raiseError(w, String.format(ERR_NO_OPERATOR, operatorId.getText()));
    }

    final DataType firstOpType = operands[0].getDataType();
    DataType type = firstOpType;

    final Node[] reducedOperands = new Node[operands.length];
    for (int i = 0; i < operands.length; i++) {
      final Node operand = Transformer.reduce(ReduceOptions.NEW_INSTANCE, operands[i]);
      final DataType currentType = operand.getDataType(); 

      // Size is always greater for bit vectors.
      if (currentType.getSize() > type.getSize()) { 
        type = currentType;
      }

      reducedOperands[i] = operand;
    }

    if (type != firstOpType && type.getTypeId() == DataTypeId.BIT_VECTOR) {
      for (int i = 0; i < reducedOperands.length; i++) {
        final Node operand = reducedOperands[i];
        if ((operand instanceof NodeValue) && !type.equals(operand.getDataType())) {
          final BigInteger value = ((NodeValue) operand).getInteger();
          reducedOperands[i] = new NodeValue(Data.newBitVector(value, type.getSize()));
        }
      }
    }

    final StandardOperation fortressOp = op.toFortressFor(type.getTypeId());
    if (null == fortressOp) {
      raiseError(w, String.format(ERR_NO_OPERATOR_FOR_TYPE, operatorId.getText(), type));
    }

    return new NodeOperation(fortressOp, reducedOperands);
  }

  public Node newConcat(CommonTree where, Node left, Node right) {
    return new NodeOperation(StandardOperation.BVCONCAT, left, right);
  }

  public Node newBitfield(CommonTree where, Node variable, Node fromExpr, Node toExpr) {
    return new NodeOperation(
        StandardOperation.BVEXTRACT, fromExpr, toExpr, variable);
  }

  public Node newVariable(CommonTree id) {
    return context.getVariable(id.getText());
  }

  public Node newIndexedVariable(CommonTree id, Node index) {
    final ISymbol symbol = getSymbols().resolve(id.getText());

    System.out.println(symbol);
    System.out.println(context.getVariable(id.getText()));

    return null;
  }
  
  public Node newAttributeCall(CommonTree id, CommonTree attributeId) {
    final ISymbol symbol = getSymbols().resolve(id.getText());
    

    System.out.println(symbol);
    
    return null;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Utility Methods
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private Address getAddress(Where w, String addressId) throws SemanticException {
    final Address address = ir.getAddresses().get(addressId);
    if (null == address) {
      raiseError(w, String.format("%s is not defined or is not an address.", addressId));
    }

    return address;
  }

  private Buffer getBuffer(Where w, String bufferId) throws SemanticException {
    final Buffer buffer = ir.getBuffers().get(bufferId);
    if (null == buffer) {
      raiseError(w, String.format("%s is not defined or is not a buffer.", bufferId));
    }

    return buffer;
  }

  private BigInteger extractBigInteger(
      Where w, Node expr, String exprDesc) throws SemanticException {

    if (expr.getKind() != Node.Kind.VALUE || !expr.isType(DataTypeId.LOGIC_INTEGER)) {
      raiseError(w, String.format("%s is not a constant integer expression.", exprDesc)); 
    }

    final NodeValue nodeValue = (NodeValue) expr;
    return nodeValue.getInteger();
  }

  private int extractInt(
      Where w, Node expr, String exprDesc) throws SemanticException {

    final BigInteger value = extractBigInteger(w, expr, exprDesc);
    if (value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0 ||
        value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
      raiseError(w, String.format(
          "%s (=%d) is beyond the allowed integer value range.", exprDesc, value)); 
    }

    return value.intValue();
  }

  private int extractPositiveInt(
      Where w, Node expr, String nodeName) throws SemanticException {

    final int value = extractInt(w, expr, nodeName);
    if (value <= 0) {
      raiseError(w, String.format("%s (%d) must be > 0.", nodeName, value));
    }

    return value;
  }
}
