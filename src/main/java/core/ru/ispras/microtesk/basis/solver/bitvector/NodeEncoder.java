/*
 * Copyright 2015-2020 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.expression.Node;

/**
 * {@link NodeEncoder} represents an abstract formula encoder.
 *
 * @param <F> the encoded form.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface NodeEncoder<F> {

  /**
   * Encodes the sub-formula (node) and adds it to the formula.
   *
   * @param node the node to be added.
   */
  void addNode(Node node);

  /**
   * Returns the encoded representation of the formula.
   *
   * @return the encoded representation.
   */
  F getEncodedForm();

  /**
   * Clones the encoder.
   *
   * @return an encoder copy.
   */
  NodeEncoder clone();
}
