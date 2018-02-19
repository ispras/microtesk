/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.generation.sim;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Callable;
import ru.ispras.microtesk.mmu.translator.ir.Constant;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.mmu.translator.ir.Operation;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Var;
import ru.ispras.microtesk.translator.generation.FileGenerator;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.generation.STBuilder;
import ru.ispras.microtesk.translator.generation.STFileGenerator;

import java.util.Map;

final class SimGeneratorFactory {
  private static final String MMU_STG_DIR = "stg/mmu/sim/";

  private static final String JAVA_COMMON_STG =
      PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg";

  private static final String MMU_COMMON_STG = MMU_STG_DIR + "Common.stg";

  private static final String[] COMMON_STGS =
      new String[] {JAVA_COMMON_STG, MMU_COMMON_STG};

  private static final String   MODEL_STG = MMU_STG_DIR + "Model.stg";
  private static final String[] MODEL_STGS =
      new String[] {JAVA_COMMON_STG, MODEL_STG};

  private final String outDir;
  private final String packageName;

  public SimGeneratorFactory(final String outDir, final String modelName) {
    InvariantChecks.checkNotNull(outDir);
    InvariantChecks.checkNotNull(modelName);

    this.outDir = String.format("%s/%s/mmu/sim", PackageInfo.getModelOutDir(outDir), modelName);
    this.packageName = String.format("%s.%s.mmu.sim", PackageInfo.MODEL_PACKAGE, modelName);
  }

  private String getOutputFileName(final String name) {
    return String.format("%s/%s%s", outDir, name, PackageInfo.JAVA_EXT);
  }

  public FileGenerator newExternGenerator(final Map<String, Var> externs) {
    InvariantChecks.checkNotNull(externs);

    final String outputFileName = getOutputFileName(STBExtern.CLASS_NAME);
    final STBuilder builder = new STBExtern(packageName, externs);

    return new STFileGenerator(outputFileName, COMMON_STGS, builder);
  }

  public FileGenerator newConstantGenerator(final Constant constant) {
    InvariantChecks.checkNotNull(constant);

    final String outputFileName = getOutputFileName(constant.getId());
    final STBuilder builder = new STBConstant(packageName, constant);

    return new STFileGenerator(outputFileName, COMMON_STGS, builder);
  }

  public FileGenerator newAddressGenerator(final Address address) {
    InvariantChecks.checkNotNull(address);

    final String outputFileName = getOutputFileName(address.getId());
    final STBuilder builder = new STBStruct(packageName, address);

    return new STFileGenerator(outputFileName, COMMON_STGS, builder);
  }

  public FileGenerator newOperationGenerator(final Operation operation) {
    InvariantChecks.checkNotNull(operation);

    final String outputFileName = getOutputFileName(operation.getId());
    final STBuilder builder = new STBOperation(packageName, operation);

    return new STFileGenerator(outputFileName, COMMON_STGS, builder);
  }

  public FileGenerator newFunctionGenerator(final Ir ir, final Callable function) {
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(function);

    final String outputFileName = getOutputFileName(function.getName());
    final STBuilder builder = new STBFunction(packageName, function);

    return new STFileGenerator(outputFileName, COMMON_STGS, builder);
  }

  public FileGenerator newStructGenerator(final Type structType) {
    InvariantChecks.checkNotNull(structType);

    final String outputFileName = getOutputFileName(structType.getId());
    final STBuilder builder = new STBStruct(packageName, structType);

    return new STFileGenerator(outputFileName, COMMON_STGS, builder);
  }

  public FileGenerator newBufferGenerator(
      final Ir ir,
      final Buffer buffer,
      final boolean isTargetBuffer) {
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(buffer);

    final String outputFileName = getOutputFileName(buffer.getId());
    final STBuilder builder = new STBBuffer(packageName, ir, buffer, isTargetBuffer);

    return new STFileGenerator(outputFileName, COMMON_STGS, builder);
  }

  public FileGenerator newSegmentGenerator(final Segment segment) {
    InvariantChecks.checkNotNull(segment);

    final String outputFileName = getOutputFileName(segment.getId());
    final STBuilder builder = new STBSegment(packageName, segment);

    return new STFileGenerator(outputFileName, COMMON_STGS, builder);
  }

  public FileGenerator newMemoryGenerator(final Memory memory) {
    InvariantChecks.checkNotNull(memory);

    final String outputFileName = getOutputFileName(memory.getId());
    final STBuilder builder = new STBMemory(packageName, memory);

    return new STFileGenerator(outputFileName, COMMON_STGS, builder);
  }

  public FileGenerator newModelGenerator(final Ir ir, final Buffer targetBuffer) {
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(targetBuffer);

    final String outputFileName = getOutputFileName(STBModel.CLASS_NAME);
    final STBuilder builder = new STBModel(packageName, ir, targetBuffer);

    return new STFileGenerator(outputFileName, MODEL_STGS, builder);
  }
}
