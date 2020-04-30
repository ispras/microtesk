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
 * {@link Encoder} represents an incremental constraint encoder.
 *
 * @param <C> the encoded constraint.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface Encoder<C> {

  /**
   * Encodes the sub-constraint (node) and adds it to the constraint.
   *
   * @param node the node to be added.
   */
  void addNode(Node node);

  /**
   * Encodes the overall constraint.
   *
   * @return the encoded constraint.
   */
  C encode();

  /**
   * Clones the encoder.
   *
   * @return an encoder copy.
   */
  Encoder<C> clone();
}
