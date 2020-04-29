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

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.Nodes;

/**
 * {@link NodeEncoderTrivial} implements a simple formula builder.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class NodeEncoderTrivial implements NodeEncoder<Node> {
  private final Collection<Node> nodes;

  public NodeEncoderTrivial() {
    this.nodes = new ArrayList<>();
  }

  public NodeEncoderTrivial(final NodeEncoderTrivial r) {
    this.nodes = new ArrayList<>(r.nodes);
  }

  @Override
  public Node getEncodedForm() {
    return Nodes.and(nodes);
  }

  @Override
  public void addNode(final Node node) {
    nodes.add(node);
  }

  @Override
  public NodeEncoderTrivial clone() {
    return new NodeEncoderTrivial(this);
  }
}
