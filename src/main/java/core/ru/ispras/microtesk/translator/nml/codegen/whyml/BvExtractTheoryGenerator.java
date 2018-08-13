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
import ru.ispras.microtesk.codegen.StringTemplateBuilder;

final class BvExtractTheoryGenerator extends BvTheoryGeneratorBase {
  private static final String THEORY_FORMAT  = "bvextract_%d_%d";
  private static final String THEORY_REGEXPR = "bvextract_[1-9][0-9]*_[1-9][0-9]*";

  private static BvExtractTheoryGenerator instance = null;

  public static BvExtractTheoryGenerator get() {
    if (null == instance) {
      instance = new BvExtractTheoryGenerator();
    }
    return instance;
  }

  private BvExtractTheoryGenerator() {
    super(THEORY_REGEXPR);
  }

  public boolean generate(final int sourceSize, final int fieldSize) {
    InvariantChecks.checkGreaterThanZero(sourceSize);
    InvariantChecks.checkGreaterThanZero(fieldSize);
    InvariantChecks.checkTrue(sourceSize > fieldSize);

    final String theoryName = String.format(THEORY_FORMAT, sourceSize, fieldSize);
    if (theoryExists(theoryName)) {
      return false;
    }

    final StringTemplateBuilder templateBuilder = new StbBvExtractTheory(sourceSize, fieldSize);
    generateTheoryFile(theoryName, templateBuilder);

    return true;
  }
}
