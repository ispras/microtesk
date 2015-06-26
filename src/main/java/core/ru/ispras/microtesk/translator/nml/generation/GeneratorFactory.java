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

import static ru.ispras.microtesk.translator.generation.PackageInfo.COMMON_TEMPLATE_DIR;
import static ru.ispras.microtesk.translator.generation.PackageInfo.SIMNML_TEMPLATE_DIR;
import static ru.ispras.microtesk.translator.generation.PackageInfo.getModeFileFormat;
import static ru.ispras.microtesk.translator.generation.PackageInfo.getModelFileFormat;
import static ru.ispras.microtesk.translator.generation.PackageInfo.getOpFileFormat;
import static ru.ispras.microtesk.translator.generation.PackageInfo.getSharedFileFormat;
import ru.ispras.microtesk.translator.generation.ClassGenerator;
import ru.ispras.microtesk.translator.generation.IClassGenerator;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;
import ru.ispras.microtesk.translator.nml.ir.IR;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;

final class GeneratorFactory {
  private final String outDir;
  private final String specFileName;
  private final String modelName;

  public GeneratorFactory(String outDir, String modelName, String specFileName) {
    this.outDir = outDir;
    this.specFileName = specFileName;
    this.modelName = modelName.toLowerCase();
  }

  public IClassGenerator createModelGenerator(IR ir) {
    final String outputFileName = String.format(getModelFileFormat(outDir), modelName);

    final String[] templateGroups = new String[] { 
      COMMON_TEMPLATE_DIR + "JavaCommon.stg",
      SIMNML_TEMPLATE_DIR + "Model.stg"
    };

    final ITemplateBuilder modelBuilder = new STBModel(specFileName, modelName, ir);
    return new ClassGenerator(outputFileName, templateGroups, modelBuilder);
  }

  public IClassGenerator createSharedGenerator(IR ir) {
    final String outputFileName = String.format(getSharedFileFormat(outDir), modelName);

    final String[] templateGroups = new String[] {
      COMMON_TEMPLATE_DIR + "JavaCommon.stg",
      SIMNML_TEMPLATE_DIR + "Shared.stg"
    };

    final ITemplateBuilder builder = new STBShared(ir, specFileName, modelName);
    return new ClassGenerator(outputFileName, templateGroups, builder);
  }

  public IClassGenerator createAddressingModeOr(PrimitiveOR mode) {
    final String outputFileName =
      String.format(getModeFileFormat(outDir), modelName, mode.getName());

    final String[] templateGroups = new String[] {
      COMMON_TEMPLATE_DIR + "JavaCommon.stg",
      SIMNML_TEMPLATE_DIR + "AddressingModeOr.stg"
    };

    final ITemplateBuilder builder = new STBAddressingModeOr(specFileName, modelName, mode);
    return new ClassGenerator(outputFileName, templateGroups, builder);
  }

  public IClassGenerator createAddressingMode(PrimitiveAND mode) {
    final String outputFileName =
      String.format(getModeFileFormat(outDir), modelName, mode.getName());

    final String[] templateGroups = new String[] {
      COMMON_TEMPLATE_DIR + "JavaCommon.stg",
      SIMNML_TEMPLATE_DIR + "AddressingMode.stg"
    };

    final ITemplateBuilder builder = new STBAddressingMode(specFileName, modelName, mode);
    return new ClassGenerator(outputFileName, templateGroups, builder);
  }

  public IClassGenerator createOperationOr(PrimitiveOR op) {
    final String outputFileName = 
      String.format(getOpFileFormat(outDir), modelName, op.getName());

    final String[] templateGroups = new String[] {
      COMMON_TEMPLATE_DIR + "JavaCommon.stg",
      SIMNML_TEMPLATE_DIR + "OperationOr.stg"
    };

    final ITemplateBuilder builder = new STBOperationOr(specFileName, modelName, op);
    return new ClassGenerator(outputFileName, templateGroups, builder);
  }

  public IClassGenerator createOperation(PrimitiveAND op) {
    final String outputFileName = String.format(getOpFileFormat(outDir), modelName, op.getName());

    final String[] templateGroups = new String[] {
      COMMON_TEMPLATE_DIR + "JavaCommon.stg",
      SIMNML_TEMPLATE_DIR + "Operation.stg"
    };

    final ITemplateBuilder builder = new STBOperation(specFileName, modelName, op);
    return new ClassGenerator(outputFileName, templateGroups, builder);
  }
}
