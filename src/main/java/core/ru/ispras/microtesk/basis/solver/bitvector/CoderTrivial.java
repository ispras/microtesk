/*
 * Copyright 2017-2020 ISP RAS (http://www.ispras.ru)
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
import java.util.Collection;
import java.util.Map;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.microtesk.basis.solver.Coder;

/**
 * {@link CoderTrivial} implements a trivial constraint/solution encoder/decoder.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class CoderTrivial implements Coder<Map<Variable, BitVector>> {

  private final Collection<Node> nodes;

  public CoderTrivial() {
    this.nodes = new ArrayList<>();
  }

  public CoderTrivial(final CoderTrivial r) {
    this.nodes = new ArrayList<>(r.nodes);
  }

  @Override
  public void addNode(final Node node) {
    nodes.add(node);
  }

  @Override
  public Node encode() {
    return Nodes.and(nodes);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<Variable, BitVector> decode(final Object encoded) {
    return (Map<Variable, BitVector>) encoded;
  }

  @Override
  public CoderTrivial clone() {
    return new CoderTrivial(this);
  }
}
