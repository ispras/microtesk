/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.whyml;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.castle.codegen.StringTemplateBuilder;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;

final class StbBvCastTheory implements StringTemplateBuilder {
  private final int smallBitSize;
  private final int bigBitSize;

  public StbBvCastTheory(final int smallBitSize, final int bigBitSize) {
    InvariantChecks.checkGreaterThanZero(smallBitSize);
    InvariantChecks.checkGreaterThanZero(bigBitSize);
    InvariantChecks.checkTrue(smallBitSize < bigBitSize);

    this.smallBitSize = smallBitSize;
    this.bigBitSize = bigBitSize;
  }

  @Override
  public ST build(final STGroup group) {
    InvariantChecks.checkNotNull(group);

    final ST st = group.getInstanceOf("bit_vector_cast_file");

    final int year = Calendar.getInstance ().get(Calendar.YEAR);
    st.add("year", year);

    st.add("small_size", smallBitSize);
    st.add("big_size", bigBitSize);

    final Set<Integer> importSizes = new LinkedHashSet<>();
    importSizes.add(smallBitSize);
    importSizes.add(bigBitSize);

    for (final int size : importSizes) {
      st.add("import_sizes", size);
    }

    final BitVector maxSmall = BitVector.newEmpty(smallBitSize);
    maxSmall.setAll();
    st.add("max_small", String.format("0x%s", maxSmall.toHexString()));

    return st;
  }
}
