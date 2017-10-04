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

package ru.ispras.microtesk.basis.solver.integer;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.expression.Node;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerFormulaBuilderSimple extends IntegerFormulaBuilder {
  private final List<Node> nodes;

  public IntegerFormulaBuilderSimple() {
    this.nodes = new ArrayList<>();
  }

  public IntegerFormulaBuilderSimple(final IntegerFormulaBuilderSimple r) {
    this.nodes = new ArrayList<>(r.nodes);
  }

  public Node build() {
    return IntegerUtils.makeNodeAnd(nodes);
  }

  @Override
  public void addFormula(final Node formula) {
    nodes.add(formula);
  }

  @Override
  public IntegerFormulaBuilderSimple clone() {
    return new IntegerFormulaBuilderSimple(this);
  }
}
