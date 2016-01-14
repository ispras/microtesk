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

package ru.ispras.microtesk.translator.nml.ir.valueinfo;

import java.util.List;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.antlrex.SemanticError;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.symbols.Where;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.nml.ir.expression.Operator;

public final class ValueInfoCalculator extends WalkerFactoryBase {
  public ValueInfoCalculator(WalkerContext context) {
    super(context);
  }

  public ValueInfo cast(Where w, ValueInfo.Kind target, List<ValueInfo> values)
      throws SemanticException {
    final ValueInfo castValueInfo = ValueInfoCast.getCast(target, values);

    if (null == castValueInfo) {
      raiseError(w, new IncompatibleTypes(values));
    }

    return castValueInfo;
  }

  public ValueInfo calculate(Where w, Operator op, ValueInfo castValueInfo, List<ValueInfo> values)
      throws SemanticException {
    final OperatorLogic operatorLogic = OperatorLogic.forOperator(op);
    if (!operatorLogic.isSupportedFor(castValueInfo)) {
      raiseError(w, new UnsupportedOperandType(op, castValueInfo));
    }

    return operatorLogic.calculate(castValueInfo, values);
  }
}


final class IncompatibleTypes implements ISemanticError {
  private static final String FORMAT = "Incompatible types: %s.";

  private final List<ValueInfo> values;

  public IncompatibleTypes(List<ValueInfo> values) {
    this.values = values;
  }

  @Override
  public String getMessage() {
    final StringBuilder sb = new StringBuilder();

    for (ValueInfo vi : values) {
      if (sb.length() != 0) {
        sb.append(", ");
      }
      sb.append(vi.getTypeName());
    }

    return String.format(FORMAT, sb.toString());
  }
}


final class UnsupportedOperandType extends SemanticError {
  private static final String FORMAT = 
    "The %s type is not supported by the %s operator.";

  public UnsupportedOperandType(Operator op, ValueInfo vi) {
    super(String.format(FORMAT, vi.getTypeName(), op.text()));
  }
}
