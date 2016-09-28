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

import ru.ispras.microtesk.translator.generation.STFileGenerator;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalker;

public class MetaDataGenerator implements TranslatorHandler<Ir> {
  private static final String[] TEMPLATE_GROUPS  = new String[] { 
    PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg",
    PackageInfo.NML_TEMPLATE_DIR + "MetaModel.stg"
    }; 

  private final Translator<Ir> translator;

  public MetaDataGenerator(final Translator<Ir> translator) {
    InvariantChecks.checkNotNull(translator);
    this.translator = translator;
  }

  private String getOutDir() {
    return translator.getOutDir() + "/src/java";
  }

  @Override
  public void processIr(final Ir ir) {
    InvariantChecks.checkNotNull(ir);

    generatePrimitives(ir);
    generateModel(ir);
  }

  private void generatePrimitives(final Ir ir) {
    final IrWalker walker = new IrWalker(ir);
    final Visitor visitor = new Visitor();
    walker.visit(visitor, IrWalker.Direction.LINEAR);
  }

  private static final class Visitor extends IrVisitorDefault {
    
  }

  private void generateModel(final Ir ir) {
    final String outputFile =
        getFileName(ir.getModelName(), STBModel.CLASS_NAME);

    final FileGenerator generator =
        new STFileGenerator(outputFile, TEMPLATE_GROUPS, new STBModel(ir));

    try {
      generator.generate();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getFileName(final String modelName, final String className) {
    return String.format(
        "%s/%s/metadata/%s.java",
        PackageInfo.getModelOutDir(getOutDir()),
        modelName,
        className
        );
  }
}
