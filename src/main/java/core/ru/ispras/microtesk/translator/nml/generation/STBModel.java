/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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
import static ru.ispras.microtesk.translator.generation.PackageInfo.SHARED_CLASS_FORMAT;

import java.util.ArrayList;
import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.ProcessorModel;
import ru.ispras.microtesk.model.api.debug.MetaModelPrinter;
import ru.ispras.microtesk.model.api.debug.ModelStatePrinter;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.instruction.IOperation;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;

final class STBModel implements ITemplateBuilder {
  private final String specFileName;
  private final String modelName;
  private final Ir ir;

  public STBModel(
      final String specFileName,
      final String modelName,
      final Ir ir) {
    this.specFileName = specFileName;
    this.modelName = modelName;
    this.ir = ir;
  }

  @Override
  public ST build(final STGroup group) {
    final ST t = group.getInstanceOf("model");

    t.add("file", specFileName);
    t.add("pack", String.format(MODEL_PACKAGE_FORMAT, modelName));

    t.add("imps", ProcessorModel.class.getName());
    t.add("imps", MetaModelPrinter.class.getName());
    t.add("imps", ModelStatePrinter.class.getName());
    t.add("imps", IAddressingMode.class.getName());
    t.add("imps", IOperation.class.getName());

    t.add("simps", String.format(SHARED_CLASS_FORMAT, modelName));

    t.add("base", ProcessorModel.class.getSimpleName());

    final ST tc = group.getInstanceOf("constructor");

    tc.add("name", modelName);
    tc.add("reg", ProcessorModel.SHARED_REGISTERS);
    tc.add("mem", ProcessorModel.SHARED_MEMORY);
    tc.add("lab", ProcessorModel.SHARED_LABELS);
    tc.add("stat", ProcessorModel.SHARED_STATUSES);
    tc.add("reset", ProcessorModel.SHARED_RESETTER);

    addAddressingModes(t, tc);
    addOperations(t, tc);

    t.add("members", tc);
    t.add("members", group.getInstanceOf("debug_block"));

    return t;
  }

  private void addOperations(final ST t, final ST tc) {
    final List<String> opNames = new ArrayList<>();
    final List<String> opGroupNames = new ArrayList<>();

    for (final Primitive op : ir.getOps().values()) {
      if (op.isOrRule()) {
        opGroupNames.add(op.getName());
      } else {
        opNames.add(op.getName());
      }
    }

    tc.add("ops", opNames);
    tc.add("ogs", opGroupNames);

    if (!opNames.isEmpty() || !opGroupNames.isEmpty()) {
      t.add("imps", String.format(OP_CLASS_FORMAT, modelName, "*"));
    }
  }

  private void addAddressingModes(final ST t, final ST tc) {
    final List<String> modeNames = new ArrayList<>();
    final List<String> modeGroupNames = new ArrayList<>();

    for (final Primitive m : ir.getModes().values()) {
      if (m.isOrRule()) {
        modeGroupNames.add(m.getName());
      } else {
        modeNames.add(m.getName());
      }
    }

    tc.add("modes", modeNames);
    tc.add("mgs", modeGroupNames);

    if (!modeNames.isEmpty() || !modeGroupNames.isEmpty()) {
      t.add("imps", String.format(MODE_CLASS_FORMAT, modelName, "*"));
    }
  }
}
