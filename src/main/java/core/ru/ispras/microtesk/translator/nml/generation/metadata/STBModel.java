/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.generation.metadata;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.ArgumentMode;

import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.metadata.MetaModelBuilder;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.generation.STBuilder;
import ru.ispras.microtesk.translator.nml.generation.ExprPrinter;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalker;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExpr;

public final class STBModel implements STBuilder {
  public static final String CLASS_NAME = "ModelMetaData";

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

  private void buildHeader(ST st) {
    st.add("name", CLASS_NAME);
    st.add("pack", String.format(PackageInfo.MODEL_PACKAGE_FORMAT + ".metadata", ir.getModelName()));
    st.add("ext", MetaModelBuilder.class.getSimpleName());
    st.add("imps", String.format("%s.*", MetaModel.class.getPackage().getName()));
    st.add("imps", ru.ispras.microtesk.model.api.data.Type.class.getName());
    st.add("simps", String.format(PackageInfo.SHARED_CLASS_FORMAT, ir.getModelName()));
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("metamodel_constructor");
    stConstructor.add("name", CLASS_NAME);

    final IrWalker walker = new IrWalker(ir);
    final Visitor visitor = new Visitor(group, stConstructor);
    walker.visit(visitor, IrWalker.Direction.LINEAR);

    st.add("members", "");
    st.add("members", stConstructor);
  }

  private static final class Visitor extends IrVisitorDefault {
    private final STGroup group;
    private final ST stConstructor;

    private Visitor(final STGroup group, final ST stConstructor) {
      this.group = group;
      this.stConstructor = stConstructor;
    }

    @Override
    public void onMemory(final String name, final MemoryExpr memory) {
      if (memory.getKind() != Memory.Kind.REG && memory.getKind() != Memory.Kind.MEM) {
        return;
      }

      final boolean isReg = memory.getKind() == Memory.Kind.REG;
      final ST stField = group.getInstanceOf("location");

      stField.add("name", name);
      stField.add("type", memory.getType().getJavaText());
      stField.add("size", ExprPrinter.bigIntegerToString(memory.getSize(), isReg ? 10 : 16));

      stConstructor.add(isReg ? "registers" : "memories", stField);
    }

    @Override
    public void onPrimitiveBegin(final Primitive item) {
      if (item.getKind() == Primitive.Kind.MODE) {
        stConstructor.add("modes", item.getName()  + ".get()");
      } else if (item.getKind() == Primitive.Kind.OP) {
        stConstructor.add("operations", item.getName() + ".get()");
      } else {
        throw new IllegalArgumentException("Unknown kind: " + item.getKind());
      }
    }
  }
}
