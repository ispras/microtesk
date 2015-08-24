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

import java.math.BigInteger;
import java.util.Arrays;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBStruct implements STBuilder {
  public static final Class<?> SPEC_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem.class;

  public static final Class<?> MMU_OPERATION_CLASS =
      ru.ispras.microtesk.mmu.basis.MemoryOperation.class;

  public static final Class<?> INTEGER_CLASS =
      ru.ispras.microtesk.basis.solver.integer.IntegerVariable.class;

  private final String packageName;
  private final Type type;

  public  STBStruct(final String packageName, final Type type) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(type);

    this.packageName = packageName;
    this.type = type;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");
    buildHeader(st);
    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", type.getId()); 
    st.add("pack", packageName);

    st.add("imps", Arrays.class.getName());
    st.add("imps", BigInteger.class.getName());
    st.add("imps", String.format("%s.*", INTEGER_CLASS.getPackage().getName()));
    st.add("imps", String.format("%s.*", MMU_OPERATION_CLASS.getPackage().getName()));
    st.add("imps", String.format("%s.*", SPEC_CLASS.getPackage().getName()));
  }
}
