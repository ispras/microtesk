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
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.ReduceOptions;
import ru.ispras.fortress.transformer.Transformer;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.TreeParserBase;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.mmu.ir.Address;
import ru.ispras.microtesk.translator.mmu.ir.Ir;
import ru.ispras.microtesk.translator.mmu.ir.Segment;

public class MmuTreeWalkerBase extends TreeParserBase {
  private static final String ERR_NO_OPERATOR = 
      "The %s operator is not supported.";

  private static final String ERR_NO_OPERATOR_FOR_TYPE =
      "The %s operator is not supported for the %s type.";

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

  /**
   * Creates an Address IR object and adds it to the MMU IR.
   * 
   * @param addressId Address identifier.
   * @param widthExpr Address width expression.
   * @return New Address IR object.
   * 
   * @throws NullPointerException if any of the arguments is {@code null}.
   * @throws SemanticException If the width expression is not a constant positive integer value. 
   */

  public Address newAddress(CommonTree addressId, Node widthExpr) throws SemanticException {
    InvariantChecks.checkNotNull(addressId);

    final Where w = where(addressId);
    final BigInteger widthValue = extractInteger(widthExpr, w, "Address width");

    final int value = widthValue.intValue();
    if (value <= 0) {
      raiseError(w, String.format("Illegal address width: %d", value));
    }

    final Address address = new Address(addressId.getText(), value);
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
   * 
   * @throws NullPointerException if any of the arguments is {@code null}.
   * @throws SemanticException if the specified address type is not defined;
   * if the range expressions are not constant integer values; if the range start
   * value is greater than the range end value.
   */

  public Segment newSegment(
      CommonTree segmentId,
      CommonTree addressArgId,
      CommonTree addressArgType,
      Node rangeStartExpr,
      Node rangeEndExpr) throws SemanticException {

    InvariantChecks.checkNotNull(segmentId);
    InvariantChecks.checkNotNull(addressArgId);
    InvariantChecks.checkNotNull(addressArgType);

    final Where w = where(segmentId);
    final String addressId = addressArgType.getText();

    final Address address = ir.getAddresses().get(addressId);
    if (null == address) {
      raiseError(w, String.format("%s is not defined or is not an address.", addressId));
    }

    final BigInteger rangeStart = extractInteger(rangeStartExpr, w, "Range start");
    final BigInteger rangeEnd = extractInteger(rangeEndExpr, w, "Range end");
    
    if (rangeStart.compareTo(rangeEnd) > 0) {
      raiseError(w, String.format(
          "Range start (%d) is greater than range end (%d).", rangeStart, rangeEnd));
    }

    final Segment segment = new Segment(
        segmentId.getText(),
        addressArgId.getText(),
        address,
        BitVector.valueOf(rangeStart, address.getWidth()),
        BitVector.valueOf(rangeEnd, address.getWidth())
        );

    ir.addSegment(segment);
    return segment;
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
    InvariantChecks.checkNotNull(operatorId);

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

  public Node newVariable(String id) {
    return null;
  }

  public Node newIndexedVariable(String id, Node index) {
    return null;
  }
  
  public Node newAttributeCall(String id, String attributeId) {
    return null;
  }
  
  private BigInteger extractInteger(Node node, Where where, String what) throws SemanticException {
    InvariantChecks.checkNotNull(node);

    if (node.getKind() != Node.Kind.VALUE || !node.isType(DataTypeId.LOGIC_INTEGER)) {
      raiseError(where, String.format("%s is not a constant integer expression.", what)); 
    }

    final NodeValue nodeValue = (NodeValue) node;
    return nodeValue.getInteger();
  }
}
