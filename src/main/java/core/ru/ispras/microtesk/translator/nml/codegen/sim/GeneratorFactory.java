/*
 * Copyright 2012-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.sim;

import ru.ispras.castle.codegen.FileGenerator;
import ru.ispras.castle.codegen.FileGeneratorStringTemplate;
import ru.ispras.castle.codegen.StringTemplateBuilder;
import ru.ispras.microtesk.translator.codegen.PackageInfo;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;

final class GeneratorFactory {
  private final String outDir;
  private final String modelName;

  public GeneratorFactory(String outDir, String modelName) {
    this.outDir = outDir;
    this.modelName = modelName;
  }

  public FileGenerator createModelGenerator(Ir ir) {
    final String outputFileName = String.format(
        "%s/%s/%s.java", PackageInfo.getModelOutDir(outDir), modelName, StbModel.CLASS_NAME);

    final String[] templateGroups = new String[] {
        PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg",
        PackageInfo.NML_TEMPLATE_DIR + "Model.stg"
    };

    final StringTemplateBuilder modelBuilder = new StbModel(ir);
    return new FileGeneratorStringTemplate(outputFileName, templateGroups, modelBuilder);
  }

  public FileGenerator createTypesGenerator(final Ir ir) {
    final String outputFileName = String.format(
        "%s/%s/%s.java", PackageInfo.getModelOutDir(outDir), modelName, StbTypes.CLASS_NAME);

    final String[] templateGroups = new String[] {
        PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg",
        PackageInfo.NML_TEMPLATE_DIR + "Shared.stg"
    };

    final StringTemplateBuilder builder = new StbTypes(ir);
    return new FileGeneratorStringTemplate(outputFileName, templateGroups, builder);
  }

  public FileGenerator createPEGenerator(final Ir ir) {
    final String outputFileName = String.format("%s/%s/%s.java",
        PackageInfo.getModelOutDir(outDir), modelName, StbProcessingElement.CLASS_NAME);

    final String[] templateGroups = new String[] {
        PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg",
        PackageInfo.NML_TEMPLATE_DIR + "Shared.stg"
    };

    final StringTemplateBuilder builder = new StbProcessingElement(ir);
    return new FileGeneratorStringTemplate(outputFileName, templateGroups, builder);
  }

  public FileGenerator createTempVarGenerator(final Ir ir) {
    final String outputFileName = String.format("%s/%s/%s.java",
        PackageInfo.getModelOutDir(outDir), modelName, StbTemporaryVariables.CLASS_NAME);

    final String[] templateGroups = new String[] {
        PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg",
        PackageInfo.NML_TEMPLATE_DIR + "Shared.stg"
    };

    final StringTemplateBuilder builder = new StbTemporaryVariables(ir);
    return new FileGeneratorStringTemplate(outputFileName, templateGroups, builder);
  }

  public FileGenerator createAddressingModeOr(PrimitiveOR mode) {
    final String outputFileName =
        String.format(PackageInfo.getModeFileFormat(outDir), modelName, mode.getName());

    final String[] templateGroups = new String[] {
        PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg",
        PackageInfo.NML_TEMPLATE_DIR + "AddressingModeOr.stg"
    };

    final StringTemplateBuilder builder = new StbAddressingModeOr(modelName, mode);
    return new FileGeneratorStringTemplate(outputFileName, templateGroups, builder);
  }

  public FileGenerator createAddressingMode(PrimitiveAND mode) {
    final String outputFileName =
        String.format(PackageInfo.getModeFileFormat(outDir), modelName, mode.getName());

    final String[] templateGroups = new String[] {
        PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg",
        PackageInfo.NML_TEMPLATE_DIR + "AddressingMode.stg"
    };

    final StringTemplateBuilder builder = new StbAddressingMode(modelName, mode);
    return new FileGeneratorStringTemplate(outputFileName, templateGroups, builder);
  }

  public FileGenerator createOperationOr(PrimitiveOR op) {
    final String outputFileName =
        String.format(PackageInfo.getOpFileFormat(outDir), modelName, op.getName());

    final String[] templateGroups = new String[] {
        PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg",
        PackageInfo.NML_TEMPLATE_DIR + "OperationOr.stg"
    };

    final StringTemplateBuilder builder = new StbOperationOr(modelName, op);
    return new FileGeneratorStringTemplate(outputFileName, templateGroups, builder);
  }

  public FileGenerator createOperation(PrimitiveAND op) {
    final String outputFileName = String.format(
        PackageInfo.getOpFileFormat(outDir), modelName, op.getName());

    final String[] templateGroups = new String[] {
        PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg",
        PackageInfo.NML_TEMPLATE_DIR + "Operation.stg"
    };

    final StringTemplateBuilder builder = new StbOperation(modelName, op);
    return new FileGeneratorStringTemplate(outputFileName, templateGroups, builder);
  }
}
