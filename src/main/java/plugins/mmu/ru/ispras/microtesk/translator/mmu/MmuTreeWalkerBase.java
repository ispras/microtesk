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
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.ReduceOptions;
import ru.ispras.fortress.transformer.Transformer;

import ru.ispras.microtesk.translator.antlrex.TreeParserBase;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.mmu.ir.Ir;

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
   * Creates a new operations expression.
   * 
   * <ol>
   * <li>Find Fortress operator</li>
   * <li>Reduce all operands</li>
   * <li>Cast all value operands to common type (bit vector) if required</li>
   * <li>Make NodeOperation and return</li>
   * </ol>
   * 
   * @param operatorId
   * @param operands
   * @return
   * @throws RecognitionException
   */

  public Node newExpression(CommonTree operatorId, Node ... operands) throws RecognitionException {
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
}
