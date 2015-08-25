/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.translator.generation.FileGenerator;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.generation.STBuilder;
import ru.ispras.microtesk.translator.generation.STFileGenerator;

final class SpecGeneratorFactory {
  private static final String MMU_STG_DIR = "stg/mmu/spec/";

  private static final String JAVA_COMMON_STG =
      PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg";

  private static final String COMMON_STG = MMU_STG_DIR + "Common.stg";
  private static final String[] COMMON_STGS = new String[] {JAVA_COMMON_STG, COMMON_STG};

  private static final String STRUCT_STG = MMU_STG_DIR + "Struct.stg";
  private static final String[] STRUCT_STGS = new String[] {JAVA_COMMON_STG, STRUCT_STG};

  private static final String BUFFER_STG = MMU_STG_DIR + "Buffer.stg";
  private static final String[] BUFFER_STGS = new String[] {JAVA_COMMON_STG, BUFFER_STG};

  private static final String CF_STG = MMU_STG_DIR + "ControlFlow.stg";
  private static final String SEGMENT_STG = MMU_STG_DIR + "Segment.stg";
  private static final String[] SEGMENT_STGS = new String[] {JAVA_COMMON_STG, SEGMENT_STG, CF_STG};

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

  public FileGenerator newStructGenerator(final Type structType) {
    InvariantChecks.checkNotNull(structType);

    final String outputFileName = getOutputFileName(structType.getId());
    final STBuilder builder = new STBStruct(packageName, structType);

    return new STFileGenerator(outputFileName, STRUCT_STGS, builder);
  }

  public FileGenerator newAddressGenerator(final Address address) {
    InvariantChecks.checkNotNull(address);

    final String outputFileName = getOutputFileName(address.getId());
    final STBuilder builder = new STBAddress(packageName, address);

    return new STFileGenerator(outputFileName, STRUCT_STGS, builder);
  }

  public FileGenerator newBufferGenerator(final Buffer buffer) {
    InvariantChecks.checkNotNull(buffer);

    final String outputFileName = getOutputFileName(buffer.getId());
    final STBuilder builder = new STBBuffer(packageName, buffer);

    return new STFileGenerator(outputFileName, BUFFER_STGS, builder);
  }

  public FileGenerator newSegmentGenerator(final Segment segment) {
    InvariantChecks.checkNotNull(segment);

    final String outputFileName = getOutputFileName(segment.getId());
    final STBuilder builder = new STBSegment(packageName, segment);

    return new STFileGenerator(outputFileName, SEGMENT_STGS, builder);
  }

  public FileGenerator newMemoryGenerator(final Memory memory) {
    InvariantChecks.checkNotNull(memory);

    final String outputFileName = getOutputFileName(memory.getId());
    final STBuilder builder = new STBMemory(packageName, memory);

    return new STFileGenerator(outputFileName, SEGMENT_STGS, builder);
  }

  public FileGenerator newSpecificationGenerator(final Ir ir) {
    InvariantChecks.checkNotNull(ir);

    final String outputFileName = getOutputFileName(STBSpecification.CLASS_NAME);
    final STBuilder builder = new STBSpecification(packageName, ir);

    return new STFileGenerator(outputFileName, COMMON_STGS, builder);
  }
}
