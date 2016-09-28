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

import java.io.IOException;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.generation.FileGenerator;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.generation.STBuilder;

import ru.ispras.microtesk.translator.generation.STFileGenerator;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalker;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;

public final class MetaDataGenerator implements TranslatorHandler<Ir> {
  private final Translator<Ir> translator;
  private Ir ir;

  public MetaDataGenerator(final Translator<Ir> translator) {
    InvariantChecks.checkNotNull(translator);
    this.translator = translator;
    this.ir = null;
  }

  private String getOutDir() {
    return translator.getOutDir() + "/src/java";
  }

  @Override
  public void processIr(final Ir ir) {
    InvariantChecks.checkNotNull(ir);
    this.ir = ir;

    // generatePrimitives();
    // generateModel();
  }

  private void generatePrimitives() {
    InvariantChecks.checkNotNull(ir);
    final IrWalker walker = new IrWalker(ir);
    final Visitor visitor = new Visitor();
    walker.visit(visitor, IrWalker.Direction.LINEAR);
  }

  private final class Visitor extends IrVisitorDefault {
    @Override
    public void onPrimitiveBegin(final Primitive item) {
      if (item.isOrRule()) {
        generateGroup((PrimitiveOR) item);
        return;
      }

      if (item.getKind() == Primitive.Kind.MODE) {
        generateAddressingMode((PrimitiveAND) item);
      } else if (item.getKind() == Primitive.Kind.OP) {
        generateOperation((PrimitiveAND) item);
      } else {
        throw new IllegalArgumentException("Unknown kind: " + item.getKind());
      }
    }
  }

  private void generateGroup(final PrimitiveOR item) {
    generateFile(item.getName(), new STBGroup(ir.getModelName(), item));
  }

  private void generateAddressingMode(final PrimitiveAND item) {
    //generateFile(item.getName(), new STBAddressingMode(item));
  }

  private void generateOperation(final PrimitiveAND item) {
    //generateFile(item.getName(), new STBOperation(item));
  }

  private void generateModel() {
    InvariantChecks.checkNotNull(ir);
    generateFile(STBModel.CLASS_NAME, new STBModel(ir));
  }

  private void generateFile(
      final String className,
      final STBuilder templateBuilder) {
    final String[] templateGroups = new String[] {
        PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg",
        PackageInfo.NML_TEMPLATE_DIR + "MetaModel.stg"
        };

    final FileGenerator generator = new STFileGenerator(
        getFileName(className), templateGroups, templateBuilder);

    try {
      generator.generate();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getFileName(final String className) {
    InvariantChecks.checkNotNull(ir);
    return String.format(
        "%s/%s/metadata/%s.java",
        PackageInfo.getModelOutDir(getOutDir()),
        ir.getModelName(),
        className
        );
  }
}
