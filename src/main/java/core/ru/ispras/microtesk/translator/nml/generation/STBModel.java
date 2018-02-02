/*
 * Copyright 2012-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.generation;

import static ru.ispras.microtesk.translator.generation.PackageInfo.MODEL_PACKAGE_FORMAT;
import static ru.ispras.microtesk.translator.generation.PackageInfo.MODE_CLASS_FORMAT;
import static ru.ispras.microtesk.translator.generation.PackageInfo.OP_CLASS_FORMAT;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.generation.STBuilder;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;

import java.util.ArrayList;
import java.util.List;

final class STBModel implements STBuilder {
  public final static String CLASS_NAME = "Model";
  private final Ir ir;

  public STBModel(final Ir ir) {
    InvariantChecks.checkNotNull(ir);
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
    st.add("pack", String.format(MODEL_PACKAGE_FORMAT, ir.getModelName()));
    st.add("ext", ru.ispras.microtesk.model.ModelBuilder.class.getSimpleName());

    st.add("imps", ru.ispras.microtesk.model.ModelBuilder.class.getName());
    st.add("imps", String.format(
        MODEL_PACKAGE_FORMAT + ".metadata.MetaModelFactory", ir.getModelName()));
    st.add("imps", String.format(
        MODEL_PACKAGE_FORMAT + ".decoder.Decoder", ir.getModelName()));
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("model_constructor");

    stConstructor.add("class", CLASS_NAME);
    stConstructor.add("name", ir.getModelName());

    addModes(st, stConstructor);
    addOperations(st, stConstructor);

    st.add("members", stConstructor);
  }

  private void addModes(final ST st, final ST stConstructor) {
    final List<String> modeNames = new ArrayList<>();
    for (final Primitive m : ir.getModes().values()) {
      if (!m.isOrRule()) {
        modeNames.add(m.getName());
      }
    }

    stConstructor.add("modes", modeNames);
    if (!modeNames.isEmpty()) {
      st.add("imps", String.format(MODE_CLASS_FORMAT, ir.getModelName(), "*"));
    }
  }

  private void addOperations(final ST st, final ST stConstructor) {
    final List<String> opNames = new ArrayList<>();
    for (final Primitive op : ir.getOps().values()) {
      if (!op.isOrRule()) {
        opNames.add(op.getName());
      }
    }

    stConstructor.add("ops", opNames);
    if (!opNames.isEmpty()) {
      st.add("imps", String.format(OP_CLASS_FORMAT, ir.getModelName(), "*"));
    }
  }
}
