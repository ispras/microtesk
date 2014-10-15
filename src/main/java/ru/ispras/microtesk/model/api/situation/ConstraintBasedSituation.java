/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.situation;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.ConstraintSolverException;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.solver.Solver;
import ru.ispras.fortress.solver.SolverResult;
import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.microtesk.model.api.situation.Situation;
import ru.ispras.microtesk.model.api.type.Type;

public abstract class ConstraintBasedSituation extends Situation {
  private final IConstraintFactory constraintFactory;

  public ConstraintBasedSituation(IInfo info, IConstraintFactory constraintFactory) {
    super(info);
    this.constraintFactory = constraintFactory;
  }

  public ConstraintBasedSituation(IInfo info, String xmlFileName) {
    this(info, new XMLBasedConstraintFactory(xmlFileName));
  }

  @Override
  public boolean setInput(String name, Data value) {
    return false;
  }

  @Override
  public boolean setOutput(String name) {
    return false;
  }

  @Override
  public final Map<String, Data> solve() throws ConfigurationException {
    final Constraint constraint = constraintFactory.create();
    final Solver solver = constraint.getKind().getDefaultSolverId().getSolver();

    final SolverResult solverResult = solver.solve(constraint);
    checkSolverResult(solverResult);

    final Map<String, Data> result = new HashMap<String, Data>();

    for (Variable variable : solverResult.getVariables()) {
      result.put(variable.getName(), variableToData(variable, Type.CARD(32)));
    }

    return result;
  }

  private void checkSolverResult(SolverResult solverResult) throws ConfigurationException {
    if (!solverResult.hasErrors()) {
      return;
    }

    final StringBuilder sb = new StringBuilder();

    sb.append(String.format("Unable to solve the %s test situation. ", getInfo().getName()));
    sb.append("Constraint solver failure. Reason: ");

    for (String error : solverResult.getErrors()) {
      sb.append(error);
    }

    throw new ConstraintSolverException(sb.toString());
  }

  private Data variableToData(Variable variable, Type targetType) {
    assert null != variable;
    assert null != targetType;

    assert variable.getData().getType().getTypeId() == DataTypeId.BIT_VECTOR;

    final int size = targetType.getBitSize();
    final BitVector value = (BitVector) variable.getData().getValue();

    assert 0 < size && size <= value.getBitSize();

    final BitVector rawData = (size == value.getBitSize()) ?
      value : BitVector.newMapping(value, 0, size);

    return new Data(rawData, targetType);
  }
}
