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

import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBBuffer implements STBuilder {
  private static final Class<?> BASE_INTF =
      ru.ispras.microtesk.mmu.model.api.Buffer.class; 

  private final String packageName;
  private final Buffer buffer;

  public STBBuffer(final String packageName, final Buffer buffer) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(buffer);

    this.packageName = packageName;
    this.buffer = buffer;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("buffer");

    buildHeader(st);
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

    final String intfName = String.format("%s<%s, %s>",
        BASE_INTF.getSimpleName(),
        "Object",
        buffer.getAddress().getId());

    st.add("name", buffer.getId()); 
    st.add("intf", intfName);
  }

  private void buildConstructor(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("constructor");
    stConstructor.add("name", buffer.getId());

    st.add("members", "");
    st.add("members", stConstructor);
  }

  private void buildIsHit(final ST st, final STGroup group) {
    final ST stMethod = group.getInstanceOf("is_hit");

    stMethod.add("addr_type", buffer.getAddress().getId());
    stMethod.add("addr_name", buffer.getAddressArg().getName().replace('.', '_'));

    st.add("members", "");
    st.add("members", stMethod);
  }

  private void buildGetData(final ST st, final STGroup group) {
    final ST stMethod = group.getInstanceOf("get_data");

    stMethod.add("addr_type", buffer.getAddress().getId());
    stMethod.add("addr_name", buffer.getAddressArg().getName().replace('.', '_'));
    stMethod.add("data_type", "Object");

    st.add("members", "");
    st.add("members", stMethod);
  }

  private void buildSetData(final ST st, final STGroup group) {
    final ST stMethod = group.getInstanceOf("set_data");

    stMethod.add("addr_type", buffer.getAddress().getId());
    stMethod.add("addr_name", buffer.getAddressArg().getName().replace('.', '_'));
    stMethod.add("data_type", "Object");
    stMethod.add("data_name", buffer.getDataArg().getName().replace('.', '_'));

    st.add("members", "");
    st.add("members", stMethod);
  }
}
