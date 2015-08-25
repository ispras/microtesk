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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.translator.generation.STBuilder;

public final class STBSegment implements STBuilder {
  public static final Class<?> SEGMENT_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuSegment.class;

  public static final Class<?> INTEGER_CLASS =
      ru.ispras.microtesk.basis.solver.integer.IntegerVariable.class;

  private final String packageName;
  private final Segment segment;

  protected STBSegment(final String packageName, final Segment segment) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(segment);

    this.packageName = packageName;
    this.segment = segment;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildConstructor(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", segment.getId()); 
    st.add("pack", packageName);
    st.add("ext", SEGMENT_CLASS.getSimpleName());
    st.add("instance", "INSTANCE");

    st.add("imps", INTEGER_CLASS.getName());
    st.add("imps", SEGMENT_CLASS.getName());
  }

  private void buildConstructor(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("constructor");

    stConstructor.add("name", segment.getId());
    stConstructor.add("va", segment.getAddress().getId());
    stConstructor.add("pa", segment.getDataArgAddress().getId());
    stConstructor.add("start", String.format("0x%xL", segment.getMin()));
    stConstructor.add("end", String.format("0x%xL", segment.getMax()));
    stConstructor.add("mapped", "false");
    stConstructor.add("va_expr", "null");
    stConstructor.add("pa_expr", "null");

    st.add("members", "");
    st.add("members", stConstructor);
  }
}
