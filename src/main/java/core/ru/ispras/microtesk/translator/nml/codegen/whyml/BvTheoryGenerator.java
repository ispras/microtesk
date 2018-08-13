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

final class BvTheoryGenerator extends BvTheoryGeneratorBase {
  private static final String THEORY_FORMAT  = "bv%d";
  private static final String THEORY_REGEXPR = "bv[1-9][0-9]*";

  private static BvTheoryGenerator instance = null;

  public static BvTheoryGenerator get() {
    if (null == instance) {
      instance = new BvTheoryGenerator();
    }
    return instance;
  }

  private BvTheoryGenerator() {
    super(THEORY_REGEXPR);
  }

  public boolean generate(final int bitSize) {
    InvariantChecks.checkGreaterThanZero(bitSize);

    final String theoryName = String.format(THEORY_FORMAT, bitSize);
    if (theoryExists(theoryName)) {
      return false;
    }

    final StringTemplateBuilder templateBuilder = new StbBvTheory(bitSize);
    generateTheoryFile(theoryName, templateBuilder);

    return true;
  }
}
