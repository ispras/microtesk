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

import java.util.LinkedHashMap;
import java.util.Map;
import org.sat4j.specs.IProblem;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;

/**
 * {@link Sat4jDecoder} implements a decoder of SAT4J models to bit-vector values.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class Sat4jDecoder {

  private Sat4jDecoder() {}

  public static Map<Variable, BitVector> decode(
      final IProblem problem,
      final Map<Variable, Integer> indices) {
    final Map<Variable, BitVector> solution = new LinkedHashMap<>();

    for (final Map.Entry<Variable, Integer> entry : indices.entrySet()) {
      final Variable variable = entry.getKey();
      final int x = entry.getValue();

      final BitVector value = BitVector.newEmpty(variable.getType().getSize());
      for (int i = 0; i < variable.getType().getSize(); i++) {
        final int xi = x + i;
        value.setBit(i, problem.model(xi));
      }

      solution.put(variable, value);
    }

    return solution;
  }
}
