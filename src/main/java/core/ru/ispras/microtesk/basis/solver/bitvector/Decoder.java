/*
 * Copyright 2020 ISP RAS (http://www.ispras.ru)
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

import java.util.Map;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;

/**
 * {@link Decoder} represents a solution decoder.
 *
 * @param <S> the encoded solution.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface Decoder<S> {

  /**
   * Decodes the solution.
   *
   * @param encoded the encoded solution.
   * @return the decoded representation.
   */
  Map<Variable, BitVector> decode(S encoded);

  /**
   * Clones the decoder.
   *
   * @return an decoder copy.
   */
  Decoder<S> clone();
}
