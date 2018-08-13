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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.codegen.StringTemplateBuilder;

import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;

final class StbBvConcatTheory implements StringTemplateBuilder {
  private final int firstBitSize;
  private final int secondBitSize;
  private final int resultBitSize;

  public StbBvConcatTheory(final int firstBitSize, final int secondBitSize) {
    InvariantChecks.checkGreaterThanZero(firstBitSize);
    InvariantChecks.checkGreaterThanZero(secondBitSize);

    this.firstBitSize = firstBitSize;
    this.secondBitSize = secondBitSize;
    this.resultBitSize = firstBitSize + secondBitSize;
  }

  @Override
  public ST build(final STGroup group) {
    InvariantChecks.checkNotNull(group);

    final ST st = group.getInstanceOf("bit_vector_concat_file");

    final int year = Calendar.getInstance ().get(Calendar.YEAR);
    st.add("year", year);

    st.add("first_size", firstBitSize);
    st.add("second_size", secondBitSize);
    st.add("result_size", resultBitSize);

    final Set<Integer> importSizes = new LinkedHashSet<>();
    importSizes.add(firstBitSize);
    importSizes.add(secondBitSize);
    importSizes.add(resultBitSize);

    for (final int size : importSizes) {
      st.add("import_sizes", size);
    }

    return st;
  }
}
