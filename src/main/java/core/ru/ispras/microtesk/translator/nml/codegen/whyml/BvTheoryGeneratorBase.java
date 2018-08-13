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

import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.codegen.FileGeneratorStringTemplate;
import ru.ispras.microtesk.codegen.StringTemplateBuilder;
import ru.ispras.microtesk.translator.codegen.PackageInfo;
import ru.ispras.microtesk.utils.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class BvTheoryGeneratorBase {
  private static final String[] TEMPLATE_GROUPS =
      new String[] { PackageInfo.COMMON_TEMPLATE_DIR + "WhyBitVector.stg" };

  private final File theoryDir;
  private final Set<String> existingTheories;

  protected BvTheoryGeneratorBase(final String theoryRegExpr) {
    this.theoryDir = getTheoryDir();
    this.existingTheories = getExistingTheories(theoryRegExpr);
  }

  private static File getTheoryDir() {
    final Path path = Paths.get(SysUtils.getHomeDir(), "lib", "why3", "theories", "ispras");
    return path.toFile();
  }

  private Set<String> getExistingTheories(final String theoryRegExpr) {
    final Pattern theoryPattern = Pattern.compile(getTheoryFileName(theoryRegExpr));

    final Set<String> result = new HashSet<>();
    for (final File file : theoryDir.listFiles()) {
      final Matcher matcher = theoryPattern.matcher(file.getName());
      if (matcher.matches()) {
        final String theoryName = FileUtils.getShortFileNameNoExt(file.getName());
        result.add(theoryName);
      }
    }
    return result;
  }

  protected final boolean theoryExists(final String theoryName) {
    return existingTheories.contains(theoryName);
  }

  protected final void generateTheoryFile(
      final String theoryName,
      final StringTemplateBuilder templateBuilder) {
    final String fileName = getTheoryPath(theoryName);
    FileGeneratorStringTemplate.generateFile(fileName, TEMPLATE_GROUPS, templateBuilder);
    existingTheories.add(theoryName);
  }

  private String getTheoryPath(final String theoryName) {
    return new File(theoryDir, getTheoryFileName(theoryName)).toString();
  }

  private String getTheoryFileName(final String theoryName) {
    return String.format("%s.why", theoryName);
  }
}
