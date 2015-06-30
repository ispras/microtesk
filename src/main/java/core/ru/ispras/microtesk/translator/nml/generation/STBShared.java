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

import static ru.ispras.microtesk.translator.generation.PackageInfo.SHARED_PACKAGE_FORMAT;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.ProcessorModel;
import ru.ispras.microtesk.model.api.memory.Label;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.state.Resetter;
import ru.ispras.microtesk.model.api.state.Status;
import ru.ispras.microtesk.model.api.type.TypeId;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.shared.Alias;
import ru.ispras.microtesk.translator.nml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.nml.ir.shared.LetString;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

final class STBShared implements ITemplateBuilder {
  public final String specFileName;
  public final String modelName;
  public final Ir ir;

  public STBShared(Ir ir, String specFileName, String modelName) {
    this.specFileName = specFileName;
    this.modelName = modelName;
    this.ir = ir;
  }

  private void insertEmptyLine(ST t) {
    t.add("members", "");
  }

  private void buildHeader(ST t) {
    t.add("file", specFileName);
    t.add("pack", String.format(SHARED_PACKAGE_FORMAT, modelName));

    t.add("imps", BigInteger.class.getName());

    if (!ir.getTypes().isEmpty() || !ir.getMemory().isEmpty()) {
      t.add("imps", TypeId.class.getName());
      t.add("imps", ru.ispras.microtesk.model.api.type.Type.class.getName());
    }

    t.add("imps", Memory.class.getName());
    t.add("imps", Label.class.getName());
    t.add("imps", Status.class.getName());
    t.add("imps", Resetter.class.getName());
  }

  private void buildLetStrings(STGroup group, ST t) {
    if (!ir.getStrings().isEmpty()) {
      insertEmptyLine(t);
    }

    for (LetString string : ir.getStrings().values()) {
      final ST tLet = group.getInstanceOf("let");

      tLet.add("name", string.getName());
      tLet.add("type", String.class.getSimpleName());
      tLet.add("value", String.format("\"%s\"", string.getText()));

      t.add("members", tLet);
    }
  }

  private void buildTypes(STGroup group, ST t) {
    if (!ir.getTypes().isEmpty()) {
      insertEmptyLine(t);
    }

    for (Map.Entry<String, Type> type : ir.getTypes().entrySet()) {
      final ST tType = group.getInstanceOf("type_alias");
      
      final String name = type.getKey();
      final String javaText = type.getValue().getJavaText();

      tType.add("name", name);
      tType.add("alias", String.format("Type.def(\"%s\", %s)", name, javaText));

      t.add("members", tType);
    }
  }

  private void buildMemory(STGroup group, ST t) {
    if (!ir.getMemory().isEmpty()) {
      insertEmptyLine(t);
    }

    final List<String> registers = new ArrayList<String>();
    final List<String> memory = new ArrayList<String>();
    final List<String> variables = new ArrayList<String>();

    for (Map.Entry<String, MemoryExpr> mem : ir.getMemory().entrySet()) {
      buildMemoryLine(group, t, mem.getKey(), mem.getValue());

      switch (mem.getValue().getKind()) {
        case REG:
          registers.add(mem.getKey());
          break;

        case MEM:
          memory.add(mem.getKey());
          break;

        case VAR:
          variables.add(mem.getKey());
          break;

        default:
          assert false : "Unknown kind!";
          break;
      }
    }

    insertEmptyLine(t);

    buildMemoryLineArray(group, t, ProcessorModel.SHARED_REGISTERS, registers);
    buildMemoryLineArray(group, t, ProcessorModel.SHARED_MEMORY, memory);
    buildMemoryLineArray(group, t, ProcessorModel.SHARED_VARIABLES, variables);
  }

  private void buildMemoryLine(STGroup group, ST t, String name, MemoryExpr memory) {
    final ST tMemory = group.getInstanceOf("memory");

    tMemory.add("name", name);
    tMemory.add("kind", memory.getKind());

    final Type typeExpr = memory.getType();
    if (null != typeExpr.getAlias()) {
      tMemory.add("type", typeExpr.getAlias());
    } else {
      final ST tNewType = group.getInstanceOf("new_type");
      tNewType.add("typeid", typeExpr.getTypeId());
      tNewType.add("size", new PrinterExpr(typeExpr.getBitSizeExpr()));
      tMemory.add("type", tNewType);
    }

    tMemory.add("size", new PrinterExpr(memory.getSizeExpr()));

    final Alias alias = memory.getAlias();
    if (null == alias) {
      tMemory.add("alias", false);
    } else {
      if (Alias.Kind.LOCATION == alias.getKind()) {
        tMemory.add("alias", PrinterLocation.toString(alias.getLocation()));
      } else {
        tMemory.add("alias", String.format("%s, %d, %d",
            alias.getName(), alias.getMin(), alias.getMax()));
      }
    }

    t.add("members", tMemory);
  }

  private void buildMemoryLineArray(STGroup group, ST t, String name, List<String> items) {
    final ST tArray = group.getInstanceOf("memory_array");

    tArray.add("type", Memory.class.getSimpleName() + "[]");
    tArray.add("name", name);
    tArray.add("items", items);

    t.add("members", tArray);
  }

  private void buildLabels(STGroup group, ST t) {
    final ST tLabels = group.getInstanceOf("memory_array");

    tLabels.add("type", Label.class.getSimpleName() + "[]");
    tLabels.add("name", ProcessorModel.SHARED_LABELS);

    for (LetLabel label : ir.getLabels().values()) {
      final ST tNewLabel = group.getInstanceOf("new_label");

      tNewLabel.add("name", label.getName());
      tNewLabel.add("memory", label.getMemoryName());
      tNewLabel.add("index", label.getIndex());

      tLabels.add("items", tNewLabel);
    }

    t.add("members", tLabels);
  }

  private void buildStatuses(STGroup group, ST t) {
    insertEmptyLine(t);

    final ST tStatuses = group.getInstanceOf("memory_array");

    tStatuses.add("type", Status.class.getSimpleName() + "[]");
    tStatuses.add("name", ProcessorModel.SHARED_STATUSES);

    for (Status status : Status.STANDARD_STATUSES.values()) {
      final ST tStatus = group.getInstanceOf("status");

      tStatus.add("name", status.getName());
      tStatus.add("def_value", status.getDefault());
      t.add("members", tStatus);

      tStatuses.add("items", status.getName());
    }

    t.add("members", tStatuses);
  }

  private void buildResetter(STGroup group, ST t) {
    insertEmptyLine(t);

    final ST tResetter = group.getInstanceOf("resetter");

    tResetter.add("type", Resetter.class.getSimpleName());
    tResetter.add("name", ProcessorModel.SHARED_RESETTER);

    tResetter.add("items", ProcessorModel.SHARED_VARIABLES);
    tResetter.add("items", ProcessorModel.SHARED_STATUSES);

    t.add("members", tResetter);
  }

  @Override
  public ST build(STGroup group) {
    final ST t = group.getInstanceOf("shared");

    buildHeader(t);
    buildLetStrings(group, t);
    //buildLetConstants(group, t);
    buildTypes(group, t);
    buildMemory(group, t);
    buildLabels(group, t);
    buildStatuses(group, t);
    buildResetter(group, t);

    return t;
  }
}
