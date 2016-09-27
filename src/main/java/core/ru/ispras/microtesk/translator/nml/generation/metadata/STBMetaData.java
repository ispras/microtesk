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
import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaArgument;
import ru.ispras.microtesk.model.api.metadata.MetaGroup;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.metadata.MetaModelBuilder;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.generation.STBuilder;
import ru.ispras.microtesk.translator.nml.generation.ExprPrinter;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalker;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExpr;

public final class STBMetaData implements STBuilder {
  public static final String CLASS_NAME = "ModelMetaData";

  private final Ir ir;

  public STBMetaData(final Ir ir) {
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
    st.add("imps", ArgumentMode.class.getName());
    st.add("imps", ru.ispras.microtesk.model.api.data.Type.class.getName());
    st.add("simps", String.format(PackageInfo.SHARED_CLASS_FORMAT, ir.getModelName()));
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("metamodel_constructor");
    stConstructor.add("name", CLASS_NAME);

    final IrWalker walker = new IrWalker(ir);
    final Visitor visitor = new Visitor(group, st, stConstructor); 
    walker.visit(visitor, IrWalker.Direction.LINEAR);

    st.add("members", "");
    st.add("members", stConstructor);
  }

  private static final class Visitor extends IrVisitorDefault {
    private final STGroup group;
    private final ST st;
    private final ST stConstructor;
    private ST stArgumentList;

    private Visitor(final STGroup group, final ST st, final ST stConstructor) {
      this.group = group;
      this.st = st;
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
      if (item.isOrRule()) {
        onGroup((PrimitiveOR) item);
        return;
      }

      if (item.getKind() == Primitive.Kind.MODE) {
        onAddressingMode((PrimitiveAND) item);
      } else if (item.getKind() == Primitive.Kind.OP) {
        onOperation((PrimitiveAND) item);
      } else {
        throw new IllegalArgumentException("Unknown kind: " + item.getKind());
      }
    }

    @Override
    public void onArgumentBegin(
        final PrimitiveAND andRule,
        final String argName,
        final Primitive argType) {
      final PrimitiveInfo info = andRule.getInfo();
      final ArgumentMode argumentMode = info.getArgUsage(argName);

      final ST stArgument = group.getInstanceOf("new_object");

      stArgument.add("type", MetaArgument.class.getSimpleName());
      stArgument.add("args", "\"" + argName + "\"");
      stArgument.add("args", argType.getName());

      if (argType.getKind() != Primitive.Kind.IMM) {
        stArgument.add("args", String.format("%s.%s",
            ArgumentMode.class.getSimpleName(), argumentMode));
      }

      stArgumentList.add("args", stArgument);
    }

    private void onAddressingMode(final PrimitiveAND primitive) {
      InvariantChecks.checkNotNull(primitive);
      InvariantChecks.checkTrue(primitive.getKind() == Primitive.Kind.MODE);

      final PrimitiveInfo info = primitive.getInfo();

      final ST stField = group.getInstanceOf("field_object");
      stArgumentList = group.getInstanceOf("arg_list");

      stField.add("name", primitive.getName());
      stField.add("type", getPrimitiveClass(primitive).getSimpleName());
      stField.add("args", "\"" + primitive.getName() + "\"");
      stField.add("args",
          primitive.getReturnType() != null ? primitive.getReturnType().getJavaText() : "null");
      stField.add("args", stArgumentList);
      stField.add("args", info.canThrowException());
      stField.add("args", info.isMemoryReference());
      stField.add("args", info.isLoad());
      stField.add("args", info.isStore());
      stField.add("args", info.getBlockSize());

      st.add("members", stField);
      stConstructor.add("modes", primitive.getName());
    }

    private void onOperation(final PrimitiveAND primitive) {
      InvariantChecks.checkNotNull(primitive);
      InvariantChecks.checkTrue(primitive.getKind() == Primitive.Kind.OP);

      final PrimitiveInfo info = primitive.getInfo();

      final ST stField = group.getInstanceOf("field_object");
      stArgumentList = group.getInstanceOf("arg_list");

      stField.add("name", primitive.getName());
      stField.add("type", getPrimitiveClass(primitive).getSimpleName());
      stField.add("args", "\"" + primitive.getName() + "\"");
      stField.add("args", "\"" + primitive.getName() + "\"");
      stField.add("args", primitive.isRoot());

      stField.add("args", stArgumentList);
      stField.add("args", "null");

      stField.add("args", info.isBranch());
      stField.add("args", info.isConditionalBranch());
      stField.add("args", info.canThrowException());
      stField.add("args", info.isLoad());
      stField.add("args", info.isStore());
      stField.add("args", info.getBlockSize());

      st.add("members", stField);
      stConstructor.add("operations", primitive.getName());
    }

    private void onGroup(final PrimitiveOR primitive) {
      InvariantChecks.checkNotNull(primitive);

      final ST stField = group.getInstanceOf("field_object");

      stField.add("name", primitive.getName());
      stField.add("type", MetaGroup.class.getSimpleName());
      stField.add("args", String.format("%s.%s.%s",
          MetaGroup.class.getSimpleName(),
          MetaGroup.Kind.class.getSimpleName(),
          primitive.getKind() == Primitive.Kind.OP ? MetaGroup.Kind.OP : MetaGroup.Kind.MODE
          ));
      stField.add("args", "\"" + primitive.getName() + "\"");

      for(final Primitive item : primitive.getORs()) {
        stField.add("args", item.getName());
      }

      st.add("members", stField);
    }
  }

  private static Class<?> getPrimitiveClass(final Primitive primitive) {
    if (primitive.isOrRule()) {
      return MetaGroup.class;
    }

    if (primitive.getKind() == Primitive.Kind.MODE) {
      return MetaAddressingMode.class;
    }

    if (primitive.getKind() == Primitive.Kind.OP) {
      return MetaOperation.class;
    }

    throw new IllegalArgumentException(
        "Unsupported kind: " + primitive.getKind());
  }
}
