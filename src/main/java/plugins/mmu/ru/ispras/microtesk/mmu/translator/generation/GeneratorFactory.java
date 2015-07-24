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
import ru.ispras.microtesk.translator.generation.FileGenerator;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.generation.STBuilder;
import ru.ispras.microtesk.translator.generation.STFileGenerator;

final class GeneratorFactory {
  private static final String MMU_STG_DIR = "stg/mmu/";

  private static final String JAVA_COMMON_STG =
      PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg";

  private static final String   ADDRESS_STG  = MMU_STG_DIR + "Address.stg";
  private static final String[] ADDRESS_STGS = new String[] {JAVA_COMMON_STG, ADDRESS_STG};

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
}
