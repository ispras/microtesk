/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.generation;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBSegment implements STBuilder {
  private static final Class<?> BASE_INTF =
      ru.ispras.microtesk.mmu.model.api.Buffer.class; 

  private final String packageName;
  private final Segment segment;

  public STBSegment(final String packageName, final Segment segment) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(segment);

    this.packageName = packageName;
    this.segment = segment;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("segment");

    buildHeader(st);
    buildRange(st, group);
    buildConstructor(st, group);
    buildIsHit(st, group);
    buildGetData(st, group);
    buildSetData(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("pack", packageName);
    st.add("imps", BASE_INTF.getName());
    st.add("imps", BitVector.class.getName());
    st.add("imps", ru.ispras.fortress.util.Pair.class.getName());

    final String intfName = String.format("%s<%s, %s>",
        BASE_INTF.getSimpleName(),
        segment.getDataArgAddress().getId(),
        segment.getAddress().getId());

    st.add("name", segment.getId()); 
    st.add("intf", intfName);
  }

  private void buildRange(final ST st, final STGroup group) {
    final ST stRange = group.getInstanceOf("range");
    final int bitSize = segment.getAddress().getAddressType().getBitSize();

    stRange.add("start", BitVector.valueOf(segment.getMin(), bitSize).toHexString());
    stRange.add("end", BitVector.valueOf(segment.getMax(), bitSize).toHexString());
    stRange.add("radix", 16);
    stRange.add("size", bitSize);

    st.add("members", stRange);
  }

  private void buildConstructor(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("constructor");
    stConstructor.add("name", segment.getId());

    st.add("members", "");
    st.add("members", stConstructor);
  }

  private void buildIsHit(final ST st, final STGroup group) {
    final ST stMethod = group.getInstanceOf("is_hit");

    stMethod.add("addr_type", segment.getAddress().getId());
    stMethod.add("addr_name", segment.getAddressArg().getName());

    st.add("members", "");
    st.add("members", stMethod);
  }

  private void buildGetData(final ST st, final STGroup group) {
    final ST stMethod = group.getInstanceOf("get_data_empty");

    stMethod.add("addr_type", segment.getAddress().getId());
    stMethod.add("addr_name", segment.getAddressArg().getName());
    stMethod.add("data_type", segment.getDataArgAddress().getId());

    st.add("members", "");
    st.add("members", stMethod);
  }

  private void buildSetData(final ST st, final STGroup group) {
    final ST stMethod = group.getInstanceOf("set_data_empty");

    stMethod.add("addr_type", segment.getAddress().getId());
    stMethod.add("addr_name", segment.getAddressArg().getName());
    stMethod.add("data_type", segment.getDataArgAddress().getId());
    stMethod.add("data_name", segment.getDataArg().getName());

    st.add("members", "");
    st.add("members", stMethod);
  }
}
