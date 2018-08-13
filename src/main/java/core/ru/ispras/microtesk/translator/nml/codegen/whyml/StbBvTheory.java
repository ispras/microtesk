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

import java.math.BigInteger;
import java.util.Calendar;

final class StbBvTheory implements StringTemplateBuilder {
  private final int bitVectorSize;

  public StbBvTheory(final int bitVectorSize) {
    InvariantChecks.checkGreaterThanZero(bitVectorSize);
    this.bitVectorSize = bitVectorSize;
  }

  @Override
  public ST build(final STGroup group) {
    InvariantChecks.checkNotNull(group);

    final int year = Calendar.getInstance ().get(Calendar.YEAR);
    final BigInteger twoPowerSize = BigInteger.ONE.shiftLeft(bitVectorSize);
    final BigInteger halfTwoPowerSize = BigInteger.ONE.shiftLeft(bitVectorSize - 1);
    final BigInteger maxInt = twoPowerSize.subtract(BigInteger.ONE);

    final ST st = group.getInstanceOf("bit_vector_file");

    st.add("year", year);
    st.add("bit_size", bitVectorSize);
    st.add("two_power_size", String.format("0x%x", twoPowerSize));
    st.add("half_two_power_size", String.format("0x%x", halfTwoPowerSize));
    st.add("max_int", String.format("0x%x", maxInt));

    return st;
  }
}
