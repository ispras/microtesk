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

package ru.ispras.microtesk.mmu.translator.generation;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.translator.generation.FileGenerator;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.generation.STBuilder;
import ru.ispras.microtesk.translator.generation.STFileGenerator;

final class GeneratorFactory {
  private static final String MMU_STG_DIR = "stg/mmu/";

  private static final String JAVA_COMMON_STG =
      PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg";

  private static final String MMU_COMMON_STG = MMU_STG_DIR + "Common.stg";

  private static final String   ADDRESS_STG = MMU_STG_DIR + "Address.stg";
  private static final String[] ADDRESS_STGS =
      new String[] {JAVA_COMMON_STG, MMU_COMMON_STG, ADDRESS_STG};

  private static final String   BUFFER_STG = MMU_STG_DIR + "Buffer.stg";
  private static final String[] BUFFER_STGS =
      new String[] {JAVA_COMMON_STG, MMU_COMMON_STG, BUFFER_STG};

  private static final String   SEGMENT_STG = MMU_STG_DIR + "Segment.stg";
  private static final String[] SEGMENT_STGS =
      new String[] {JAVA_COMMON_STG, MMU_COMMON_STG, SEGMENT_STG};

  private static final String   MEMORY_STG = MMU_STG_DIR + "Memory.stg";
  private static final String[] MEMORY_STGS =
      new String[] {JAVA_COMMON_STG, MMU_COMMON_STG, MEMORY_STG};

  private final String outDir;
  private final String packageName;

  public GeneratorFactory(final String outDir, final String modelName) {
    InvariantChecks.checkNotNull(outDir);
    InvariantChecks.checkNotNull(modelName);

    this.outDir = String.format("%s/%s/mmu", PackageInfo.getModelOutDir(outDir), modelName);
    this.packageName = String.format("%s.%s.mmu", PackageInfo.MODEL_PACKAGE, modelName);
  }

  private String getOutputFileName(final String name) {
    return String.format("%s/%s%s", outDir, name, PackageInfo.JAVA_EXT);
  }

  public FileGenerator newAddressGenerator(final Address address) {
    InvariantChecks.checkNotNull(address);

    final String outputFileName = getOutputFileName(address.getId());
    final STBuilder builder = new STBAddress(packageName, address);

    return new STFileGenerator(outputFileName, ADDRESS_STGS, builder);
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

  public FileGenerator newMemoryGenerator(final Ir ir, final Memory memory) {
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(memory);

    final String outputFileName = getOutputFileName(memory.getId());
    final STBuilder builder = new STBMemory(packageName, memory);

    return new STFileGenerator(outputFileName, MEMORY_STGS, builder);
  }
}
