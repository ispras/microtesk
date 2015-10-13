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

package ru.ispras.microtesk.mmu.translator.generation.sim;

import java.math.BigInteger;
import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.generation.spec.Utils;
import ru.ispras.microtesk.mmu.translator.ir.ExternalSource;
import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBExtern implements STBuilder {
  public static final String CLASS_NAME = "Extern";

  public static final Class<?> READER_CLASS =
      ru.ispras.microtesk.model.api.state.Reader.class;

  private final String packageName;
  private final Map<String, Variable> externs;

  public STBExtern(
      final String packageName,
      final Map<String, Variable> externs) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(externs);

    this.packageName = packageName;
    this.externs = externs;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");
    st.add("instance", "instance");

    buildHeader(st);
    buildBody(st, group);

    for (final Variable variable : externs.values()) {
      ExprPrinter.get().addVariableMappings(
          variable, String.format("%s.get().%s()", CLASS_NAME, variable.getName()));
    }

    return st;
  }

  protected final void buildHeader(final ST st) {
    st.add("name", CLASS_NAME); 
    st.add("pack", packageName);

    st.add("imps", BigInteger.class.getName());
    st.add("imps", STBCommon.BIT_VECTOR_CLASS.getName());
    st.add("imps", READER_CLASS.getName());
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stBody = group.getInstanceOf("extern_body");
    stBody.add("fnames", externs.keySet());

    for (final Variable variable : externs.values()) {
      final ExternalSource source = (ExternalSource) variable.getTypeSource();

      final String method;
      final StringBuilder args = new StringBuilder();

      if (source.getKind() == ExternalSource.Kind.MEMORY) {
        method = "forMemory";
        InvariantChecks.checkTrue(source.getArgs().size() <= 1);
      } else if (source.getKind() == ExternalSource.Kind.MODE) {
        method = "forAddressingMode";
      } else {
        InvariantChecks.checkTrue(false);
        method = "null";
      }

      for (final BigInteger arg : source.getArgs()) {
        args.append(", ");
        args.append(Utils.toString(arg));
      }

      final String value = String.format(
          "Reader.%s(\"%s\"%s)", method, source.getName(), args);

      stBody.add("fvalues", value);
    }

    st.add("members", stBody);
  }
}
