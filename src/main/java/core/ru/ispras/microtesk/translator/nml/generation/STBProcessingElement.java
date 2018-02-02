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

package ru.ispras.microtesk.translator.nml.generation;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.memory.Memory;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.generation.STBuilder;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.shared.Alias;
import ru.ispras.microtesk.translator.nml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

import java.math.BigInteger;

final class STBProcessingElement implements STBuilder {
  public static final String CLASS_NAME = "PE";
  private final Ir ir;

  public STBProcessingElement(final Ir ir) {
    this.ir = ir;
  }

  private void buildHeader(final ST st) {
    st.add("name", CLASS_NAME);

    st.add("pack", String.format(PackageInfo.MODEL_PACKAGE_FORMAT, ir.getModelName()));
    st.add("ext", ru.ispras.microtesk.model.ProcessingElement.class.getSimpleName());

    st.add("imps", BigInteger.class.getName());
    st.add("imps", ru.ispras.microtesk.model.ProcessingElement.class.getName());
    st.add("imps", ru.ispras.microtesk.model.data.Type.class.getName());
    st.add("imps", ru.ispras.microtesk.model.memory.Label.class.getName());
    st.add("imps", ru.ispras.microtesk.model.memory.Memory.class.getName());
  }

  private void buildBody(final STGroup group, final ST st) {
    final ST tCore = group.getInstanceOf("processing_element");
    tCore.add("class", CLASS_NAME);

    for (final MemoryExpr memory : ir.getMemory().values()) {
      if (memory.getKind() == Memory.Kind.VAR) {
        continue;
      }

      tCore.add("names", memory.getName());

      final ST stMemoryDef = buildMemoryLine(group, memory);
      tCore.add("defs", stMemoryDef);

      if (null != memory.getAlias()) {
        tCore.add("copies", stMemoryDef);
      } else if (memory.isShared()){
        tCore.add("copies",
            String.format("shared ? other.%1$s : other.%1$s.copy()", memory.getName()));
      } else {
        tCore.add("copies",
            String.format("other.%s.copy()", memory.getName()));
      }
    }

    for (final LetLabel label : ir.getLabels().values()) {
      final ST tNewLabel = group.getInstanceOf("new_label");

      tNewLabel.add("name", label.getName());
      tNewLabel.add("memory", label.getMemoryName());
      tNewLabel.add("index", label.getIndex());

      tCore.add("labels", tNewLabel);
    }

    st.add("members", tCore);
  }

  public static ST buildMemoryLine(final STGroup group, final MemoryExpr memory) {
    final ST tMemory = group.getInstanceOf("new_memory");

    tMemory.add("name", memory.getName());
    tMemory.add("kind", memory.getKind());

    final Type typeExpr = memory.getType();
    if (null != typeExpr.getAlias()) {
      tMemory.add("type", "TypeDefs." + typeExpr.getAlias());
    } else {
      final ST tNewType = group.getInstanceOf("new_type");
      tNewType.add("typeid", typeExpr.getTypeId());
      tNewType.add("size", typeExpr.getBitSize());
      tMemory.add("type", tNewType);
    }

    final BigInteger memorySize = memory.getSize();
    if (memorySize.compareTo(BigInteger.ONE.shiftLeft(10)) > 0) {
      tMemory.add("size", ExprPrinter.bigIntegerToString(memorySize, 16));
    } else {
      tMemory.add("size", memorySize);
    }

    final Alias alias = memory.getAlias();
    if (null == alias) {
      tMemory.add("alias", false);
    } else {
      if (Alias.Kind.LOCATION == alias.getKind()) {
        PrinterLocation.addPE = false;
        tMemory.add("alias", PrinterLocation.toString(alias.getLocation()));
      } else {
        tMemory.add("alias", String.format("%s, %d, %d",
            alias.getName(), alias.getMin(), alias.getMax()));
      }
    }

    return tMemory;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildBody(group, st);

    return st;
  }
}
