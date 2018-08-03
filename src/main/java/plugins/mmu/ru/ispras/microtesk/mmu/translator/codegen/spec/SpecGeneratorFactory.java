/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.codegen.spec;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.codegen.FileGenerator;
import ru.ispras.microtesk.codegen.FileGeneratorStringTemplate;
import ru.ispras.microtesk.codegen.StringTemplateBuilder;
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
import ru.ispras.microtesk.translator.generation.PackageInfo;

final class SpecGeneratorFactory {
  private static final String MMU_STG_DIR = "stg/mmu/spec/";

  private static final String JAVA_COMMON_STG =
      PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg";

  private static final String SPEC_STG = MMU_STG_DIR + "Specification.stg";
  private static final String[] SPEC_STGS = new String[] {JAVA_COMMON_STG, SPEC_STG};

  private static final String STRUCT_STG = MMU_STG_DIR + "Struct.stg";
  private static final String[] STRUCT_STGS = new String[] {JAVA_COMMON_STG, STRUCT_STG};

  private static final String BUFFER_STG = MMU_STG_DIR + "Buffer.stg";
  private static final String[] BUFFER_STGS =
      new String[] {JAVA_COMMON_STG, STRUCT_STG, BUFFER_STG};

  private static final String CF_STG = MMU_STG_DIR + "ControlFlow.stg";
  private static final String SEGMENT_STG = MMU_STG_DIR + "Segment.stg";
  private static final String[] SEGMENT_STGS =
      new String[] {JAVA_COMMON_STG, SEGMENT_STG, CF_STG, STRUCT_STG};

  private static final String FUNCTION_STG = MMU_STG_DIR + "Function.stg";
  private static final String[] FUNCTION_STGS =
      new String[] {JAVA_COMMON_STG, FUNCTION_STG, CF_STG, STRUCT_STG};

  private static final String CONSTANT_STG = MMU_STG_DIR + "Constant.stg";
  private static final String[] CONSTANT_STGS = new String[] {JAVA_COMMON_STG, CONSTANT_STG};

  private static final String OPERATION_STG = MMU_STG_DIR + "Operation.stg";
  private static final String[] OPERATION_STGS = new String[] {JAVA_COMMON_STG, OPERATION_STG};

  private final String outDir;
  private final String packageName;

  public SpecGeneratorFactory(final String outDir, final String modelName) {
    InvariantChecks.checkNotNull(outDir);
    InvariantChecks.checkNotNull(modelName);

    this.outDir = String.format("%s/%s/mmu/spec", PackageInfo.getModelOutDir(outDir), modelName);
    this.packageName = String.format("%s.%s.mmu.spec", PackageInfo.MODEL_PACKAGE, modelName);
  }

  private String getOutputFileName(final String name) {
    return String.format("%s/%s%s", outDir, name, PackageInfo.JAVA_EXT);
  }

  public FileGenerator newExternGenerator(final Var extern) {
    InvariantChecks.checkNotNull(extern);

    final String outputFileName = getOutputFileName(extern.getName());
    final StringTemplateBuilder builder = new StbExtern(packageName, extern);

    return new FileGeneratorStringTemplate(outputFileName, CONSTANT_STGS, builder);
  }

  public FileGenerator newConstantGenerator(final Constant constant) {
    InvariantChecks.checkNotNull(constant);

    final String outputFileName = getOutputFileName(constant.getId());
    final StringTemplateBuilder builder = new StbConstant(packageName, constant);

    return new FileGeneratorStringTemplate(outputFileName, CONSTANT_STGS, builder);
  }

  public FileGenerator newStructGenerator(final Type structType) {
    InvariantChecks.checkNotNull(structType);

    final String outputFileName = getOutputFileName(structType.getId());
    final StringTemplateBuilder builder = new StbStruct(packageName, structType);

    return new FileGeneratorStringTemplate(outputFileName, STRUCT_STGS, builder);
  }

  public FileGenerator newAddressGenerator(final Address address) {
    InvariantChecks.checkNotNull(address);

    final String outputFileName = getOutputFileName(address.getId());
    final StringTemplateBuilder builder = new StbAddress(packageName, address);

    return new FileGeneratorStringTemplate(outputFileName, STRUCT_STGS, builder);
  }

  public FileGenerator newFunctionGenerator(final Ir ir, final Callable func) {
    InvariantChecks.checkNotNull(func);

    final String outputFileName = getOutputFileName(func.getName());
    final StringTemplateBuilder builder = new StbFunction(packageName, ir, func);

    return new FileGeneratorStringTemplate(outputFileName, FUNCTION_STGS, builder);
  }

  public FileGenerator newOperationGenerator(final Ir ir, final Operation operation) {
    InvariantChecks.checkNotNull(operation);

    final String outputFileName = getOutputFileName(operation.getId());
    final StringTemplateBuilder builder = new StbOperation(packageName, ir, operation);

    return new FileGeneratorStringTemplate(outputFileName, OPERATION_STGS, builder);
  }

  public FileGenerator newBufferGenerator(final Buffer buffer) {
    InvariantChecks.checkNotNull(buffer);

    final String outputFileName = getOutputFileName(buffer.getId());
    final StringTemplateBuilder builder = new StbBuffer(packageName, buffer);

    return new FileGeneratorStringTemplate(outputFileName, BUFFER_STGS, builder);
  }

  public FileGenerator newSegmentGenerator(final Ir ir, final Segment segment) {
    InvariantChecks.checkNotNull(segment);

    final String outputFileName = getOutputFileName(segment.getId());
    final StringTemplateBuilder builder = new StbSegment(packageName, ir, segment);

    return new FileGeneratorStringTemplate(outputFileName, SEGMENT_STGS, builder);
  }

  public FileGenerator newMemoryGenerator(final Ir ir, final Memory memory) {
    InvariantChecks.checkNotNull(memory);

    final String outputFileName = getOutputFileName(memory.getId());
    final StringTemplateBuilder builder = new StbMemory(packageName, ir, memory);

    return new FileGeneratorStringTemplate(outputFileName, SEGMENT_STGS, builder);
  }

  public FileGenerator newSpecificationGenerator(final Ir ir) {
    InvariantChecks.checkNotNull(ir);

    final String outputFileName = getOutputFileName(StbSpecification.CLASS_NAME);
    final StringTemplateBuilder builder = new StbSpecification(packageName, ir);

    return new FileGeneratorStringTemplate(outputFileName, SPEC_STGS, builder);
  }
}
