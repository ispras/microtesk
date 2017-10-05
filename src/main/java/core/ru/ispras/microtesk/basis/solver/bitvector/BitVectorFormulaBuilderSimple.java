/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.basis.solver.bitvector;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.expression.Node;
import ru.ispras.microtesk.utils.FortressUtils;

/**
 * {@link BitVectorFormulaBuilderSimple} implements a simple formula builder.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BitVectorFormulaBuilderSimple extends BitVectorFormulaBuilder {
  private final List<Node> nodes;

  public BitVectorFormulaBuilderSimple() {
    this.nodes = new ArrayList<>();
  }

  public BitVectorFormulaBuilderSimple(final BitVectorFormulaBuilderSimple r) {
    this.nodes = new ArrayList<>(r.nodes);
  }

  public Node build() {
    return FortressUtils.makeNodeAnd(nodes);
  }

  @Override
  public void addFormula(final Node formula) {
    nodes.add(formula);
  }

  @Override
  public BitVectorFormulaBuilderSimple clone() {
    return new BitVectorFormulaBuilderSimple(this);
  }
}
