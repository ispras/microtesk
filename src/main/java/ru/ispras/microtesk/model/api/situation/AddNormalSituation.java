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

import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.constraint.ConstraintBuilder;
import ru.ispras.fortress.solver.constraint.ConstraintKind;
import ru.ispras.fortress.solver.constraint.Formulas;
import ru.ispras.microtesk.model.api.situation.ConstraintBasedSituation;
import ru.ispras.microtesk.model.api.situation.ISituation;

public final class AddNormalSituation extends ConstraintBasedSituation {
  private static final String NAME = "normal";
  private static final IFactory FACTORY = new IFactory() {
    @Override
    public ISituation create() {
      return new AddNormalSituation();
    }
  };

  public static final IInfo INFO = new Info(NAME, FACTORY);

  public AddNormalSituation() {
    super(INFO, new AddNormalConstraintBuilder());
  }
}


final class AddNormalConstraintBuilder extends OverflowConstraintFactory {
  public Constraint create() {
    final ConstraintBuilder builder = new ConstraintBuilder();

    builder.setName("AddNormal");
    builder.setKind(ConstraintKind.FORMULA_BASED);
    builder.setDescription("AddNormal constraint");

    // Unknown variables
    final NodeVariable rs = new NodeVariable(builder.addVariable("src2", BIT_VECTOR_TYPE));
    final NodeVariable rt = new NodeVariable(builder.addVariable("src3", BIT_VECTOR_TYPE));

    final Formulas formulas = new Formulas();
    builder.setInnerRep(formulas);

    formulas.add(IsValidSignedInt(rs));
    formulas.add(IsValidSignedInt(rt));

    formulas.add(IsValidSignedInt(new NodeOperation(StandardOperation.BVADD, rs, rt)));
    formulas.add(isNotEqual(rs, rt));

    return builder.build();
  }
}
