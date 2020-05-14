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
import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.constraint.ConstraintUtils;
import ru.ispras.microtesk.basis.solver.Encoder;

/**
 * {@link EncoderTrivial} implements a trivial constraint/solution encoder/decoder.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class EncoderTrivial implements Encoder {

  private final Collection<Node> nodes;

  public EncoderTrivial() {
    this.nodes = new ArrayList<>();
  }

  public EncoderTrivial(final EncoderTrivial r) {
    this.nodes = new ArrayList<>(r.nodes);
  }

  @Override
  public void addNode(final Node node) {
    nodes.add(node);
  }

  @Override
  public Constraint encode() {
    return ConstraintUtils.newConstraint(Nodes.and(nodes));
  }

  @Override
  public EncoderTrivial clone() {
    return new EncoderTrivial(this);
  }
}
