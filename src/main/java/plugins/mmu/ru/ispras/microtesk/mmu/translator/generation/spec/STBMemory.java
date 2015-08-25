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
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBMemory implements STBuilder {
  public static final Class<?> INTEGER_CLASS =
      ru.ispras.microtesk.basis.solver.integer.IntegerVariable.class;

  private final String packageName;
  private final Memory memory;

  protected STBMemory(final String packageName, final Memory memory) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(memory);

    this.packageName = packageName;
    this.memory = memory;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildConstructor(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", memory.getId()); 
    st.add("pack", packageName);
    st.add("instance", "INSTANCE");
    st.add("imps", INTEGER_CLASS.getName());
  }

  private void buildConstructor(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("constructor_memory");
    stConstructor.add("name", memory.getId());

    for (final Variable variable : memory.getVariables()) {
      final String name = variable.getName().replaceFirst(memory.getId() + ".", "");
      final Type type = variable.getType();

      STBStruct.buildFieldDecl(
          name,
          type,
          st,
          stConstructor,
          group
          );
    }

    st.add("members", "");
    st.add("members", stConstructor);
  }
}
