/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.whyml;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.codegen.FileGeneratorStringTemplate;
import ru.ispras.microtesk.codegen.StringTemplateBuilder;
import ru.ispras.microtesk.translator.codegen.PackageInfo;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class BVTheoryGenerator extends BVTheoryGeneratorBase {
  private static final Pattern FILE_NAME_PATTERN = Pattern.compile("bv([1-9][0-9]*).why");
  private static BVTheoryGenerator instance = null;

  public static BVTheoryGenerator getInstance() {
    if (null == instance) {
      instance = new BVTheoryGenerator();
    }
    return instance;
  }

  private final Set<Integer> bitVectorSizes;

  private BVTheoryGenerator() {
    this.bitVectorSizes = readBitVectorSizes(theoryDir);
  }

  private static Set<Integer> readBitVectorSizes(final File dir) {
    final Set<Integer> result = new HashSet<>();
    for (final File file : dir.listFiles()) {
      final Matcher matcher = FILE_NAME_PATTERN.matcher(file.getName());
      if (matcher.matches()) {
        final int bitVectorSize = Integer.parseInt(matcher.group(1));
        result.add(bitVectorSize);
      }
    }
    return result;
  }

  public boolean generate(final int bitVectorSize) {
    InvariantChecks.checkGreaterThanZero(bitVectorSize);

    if (bitVectorSizes.contains(bitVectorSize)) {
      return false;
    }

    generateTheoryFile(bitVectorSize);
    bitVectorSizes.add(bitVectorSize);

    return true;
  }

  private void generateTheoryFile(final int bitVectorSize) {
    final String[] templateGroups =
        new String[] { PackageInfo.COMMON_TEMPLATE_DIR + "WhyBitVector.stg" };

    final String fileName = getFileName(bitVectorSize);
    final StringTemplateBuilder templateBuilder = new StbBitVectorTheory(bitVectorSize);

    FileGeneratorStringTemplate.generateFile(
        fileName, templateGroups, templateBuilder);
  }

  private String getFileName(int bitVectorSize) {
    return new File(theoryDir, String.format("bv%d.why", bitVectorSize)).toString();
  }
}
