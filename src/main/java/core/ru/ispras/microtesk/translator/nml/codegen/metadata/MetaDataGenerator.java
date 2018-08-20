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

package ru.ispras.microtesk.translator.nml.codegen.metadata;

import ru.ispras.castle.codegen.FileGenerator;
import ru.ispras.castle.codegen.FileGeneratorStringTemplate;
import ru.ispras.castle.codegen.StringTemplateBuilder;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.codegen.PackageInfo;

import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalker;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAnd;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOr;

import java.io.IOException;

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

    generatePrimitives();
    generateModel();
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
      if (item.getModifier() == Primitive.Modifier.INTERNAL) {
        return;
      }

      if (item.isOrRule()) {
        generateGroup((PrimitiveOr) item);
        return;
      }

      if (item.getKind() == Primitive.Kind.MODE) {
        generateAddressingMode((PrimitiveAnd) item);
      } else if (item.getKind() == Primitive.Kind.OP) {
        generateOperation((PrimitiveAnd) item);
      } else {
        throw new IllegalArgumentException("Unknown kind: " + item.getKind());
      }
    }
  }

  private void generateGroup(final PrimitiveOr item) {
    generateFile(item.getName(), new StbGroup(getModelName(), item));
  }

  private void generateAddressingMode(final PrimitiveAnd item) {
    generateFile(item.getName(), new StbAddressingMode(getModelName(), item));
  }

  private void generateOperation(final PrimitiveAnd item) {
    generateFile(item.getName(), new StbOperation(getModelName(), item));
  }

  private void generateModel() {
    InvariantChecks.checkNotNull(ir);
    generateFile(StbModel.CLASS_NAME, new StbModel(ir));
  }

  private void generateFile(
      final String className,
      final StringTemplateBuilder templateBuilder) {
    final String[] templateGroups = new String[] {
        PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg",
        PackageInfo.NML_TEMPLATE_DIR + "MetaModel.stg"
        };

    final FileGenerator generator = new FileGeneratorStringTemplate(
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
        getModelName(),
        className
        );
  }

  private String getModelName() {
    InvariantChecks.checkNotNull(ir);
    return ir.getModelName();
  }
}
