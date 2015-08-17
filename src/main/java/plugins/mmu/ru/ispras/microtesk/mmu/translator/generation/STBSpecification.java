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

import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.translator.generation.STBuilder;

public final class STBSpecification implements STBuilder {
  public static final String CLASS_NAME = "Specification";

  private static final Class<?> SPEC_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem.class;

  private static final Class<?> INTEGER_CLASS = 
      ru.ispras.microtesk.basis.solver.integer.IntegerVariable.class;

  private final String packageName;
  private final Ir ir;

  public STBSpecification(final String packageName, final Ir ir) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(ir);

    this.packageName = packageName;
    this.ir = ir;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");
    buildHeader(st);
    buildBody(st, group);
    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", CLASS_NAME); 
    st.add("pack", packageName);
    st.add("imps", String.format("%s.*", INTEGER_CLASS.getPackage().getName()));
    st.add("imps", String.format("%s.*", SPEC_CLASS.getPackage().getName()));
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stBody = group.getInstanceOf("body");

    stBody.add("name", CLASS_NAME);
    stBody.add("spec", SPEC_CLASS.getSimpleName());
    st.add("members", stBody);

    buildAddresses(stBody, group);
    buildSegments(stBody, group);
  }

  private void buildAddresses(final ST st, final STGroup group) {
    final ST stSeparator = group.getInstanceOf("separator");
    stSeparator.add("text", "Addresses");
    st.add("members", stSeparator);
    st.add("stmts", "");

    for(final Address address : ir.getAddresses().values()) {
      final ST stDef = group.getInstanceOf("type_def");
      stDef.add("name", address.getId());
      stDef.add("var_name", listToString(address.getAccessChain()));
      stDef.add("var_size", address.getAddressType().getBitSize());
      st.add("members", stDef);

      final ST stReg = group.getInstanceOf("type_reg");
      stReg.add("name", address.getId());
      st.add("stmts", stReg);
    }
  }

  private void buildSegments(final ST st, final STGroup group) {
    final ST stSeparator = group.getInstanceOf("separator");
    stSeparator.add("text", "Segments");
    st.add("members", "");
    st.add("members", stSeparator);
    st.add("stmts", "");

    for(final Segment segment : ir.getSegments().values()) {
      final ST stDef = group.getInstanceOf("segment_def");
      stDef.add("name", segment.getId());
      stDef.add("va", segment.getAddress().getId());
      stDef.add("pa", segment.getDataArgAddress().getId());
      stDef.add("start", String.format("0x%xL", segment.getMin()));
      stDef.add("end", String.format("0x%xL", segment.getMax()));
      stDef.add("mapped", "false");
      stDef.add("va_expr", "null");
      stDef.add("pa_expr", "null");
      st.add("members", stDef);

      final ST stReg = group.getInstanceOf("segment_reg");
      stReg.add("name", segment.getId());
      st.add("stmts", stReg);
    }
  }

  private static String listToString(final List<String> list) {
    final StringBuilder sb = new StringBuilder();
    for (final String string : list) {
      if (sb.length() != 0) {
        sb.append('.');
      }
      sb.append(string);
    }
    return sb.toString();
  }
}
