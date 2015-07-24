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
import static ru.ispras.microtesk.translator.generation.PackageInfo.NML_TEMPLATE_DIR;
import static ru.ispras.microtesk.translator.generation.PackageInfo.getModeFileFormat;
import static ru.ispras.microtesk.translator.generation.PackageInfo.getModelFileFormat;
import static ru.ispras.microtesk.translator.generation.PackageInfo.getOpFileFormat;
import static ru.ispras.microtesk.translator.generation.PackageInfo.getSharedFileFormat;
import ru.ispras.microtesk.translator.generation.STFileGenerator;
import ru.ispras.microtesk.translator.generation.FileGenerator;
import ru.ispras.microtesk.translator.generation.STBuilder;
import ru.ispras.microtesk.translator.nml.ir.Ir;
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

  public FileGenerator createModelGenerator(Ir ir) {
    final String outputFileName = String.format(getModelFileFormat(outDir), modelName);

    final String[] templateGroups = new String[] { 
      COMMON_TEMPLATE_DIR + "JavaCommon.stg",
      NML_TEMPLATE_DIR + "Model.stg"
    };

    final STBuilder modelBuilder = new STBModel(specFileName, modelName, ir);
    return new STFileGenerator(outputFileName, templateGroups, modelBuilder);
  }

  public FileGenerator createSharedGenerator(Ir ir) {
    final String outputFileName = String.format(getSharedFileFormat(outDir), modelName);

    final String[] templateGroups = new String[] {
      COMMON_TEMPLATE_DIR + "JavaCommon.stg",
      NML_TEMPLATE_DIR + "Shared.stg"
    };

    final STBuilder builder = new STBShared(ir, specFileName, modelName);
    return new STFileGenerator(outputFileName, templateGroups, builder);
  }

  public FileGenerator createAddressingModeOr(PrimitiveOR mode) {
    final String outputFileName =
      String.format(getModeFileFormat(outDir), modelName, mode.getName());

    final String[] templateGroups = new String[] {
      COMMON_TEMPLATE_DIR + "JavaCommon.stg",
      NML_TEMPLATE_DIR + "AddressingModeOr.stg"
    };

    final STBuilder builder = new STBAddressingModeOr(specFileName, modelName, mode);
    return new STFileGenerator(outputFileName, templateGroups, builder);
  }

  public FileGenerator createAddressingMode(PrimitiveAND mode) {
    final String outputFileName =
      String.format(getModeFileFormat(outDir), modelName, mode.getName());

    final String[] templateGroups = new String[] {
      COMMON_TEMPLATE_DIR + "JavaCommon.stg",
      NML_TEMPLATE_DIR + "AddressingMode.stg"
    };

    final STBuilder builder = new STBAddressingMode(specFileName, modelName, mode);
    return new STFileGenerator(outputFileName, templateGroups, builder);
  }

  public FileGenerator createOperationOr(PrimitiveOR op) {
    final String outputFileName = 
      String.format(getOpFileFormat(outDir), modelName, op.getName());

    final String[] templateGroups = new String[] {
      COMMON_TEMPLATE_DIR + "JavaCommon.stg",
      NML_TEMPLATE_DIR + "OperationOr.stg"
    };

    final STBuilder builder = new STBOperationOr(specFileName, modelName, op);
    return new STFileGenerator(outputFileName, templateGroups, builder);
  }

  public FileGenerator createOperation(PrimitiveAND op) {
    final String outputFileName = String.format(getOpFileFormat(outDir), modelName, op.getName());

    final String[] templateGroups = new String[] {
      COMMON_TEMPLATE_DIR + "JavaCommon.stg",
      NML_TEMPLATE_DIR + "Operation.stg"
    };

    final STBuilder builder = new STBOperation(specFileName, modelName, op);
    return new STFileGenerator(outputFileName, templateGroups, builder);
  }
}
