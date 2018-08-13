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

final class BvCastTheoryGenerator extends BvTheoryGeneratorBase {
  private static final String THEORY_FORMAT  = "bvcast_%d_%d";
  private static final String THEORY_REGEXPR = "bvcast_[1-9][0-9]*_[1-9][0-9]*";

  private static BvCastTheoryGenerator instance = null;

  public static BvCastTheoryGenerator getInstance() {
    if (null == instance) {
      instance = new BvCastTheoryGenerator();
    }
    return instance;
  }

  private BvCastTheoryGenerator() {
    super(THEORY_REGEXPR);
  }

  public boolean generate(final int sourceBitSize, final int targetBitSize) {
    InvariantChecks.checkGreaterThanZero(sourceBitSize);
    InvariantChecks.checkGreaterThanZero(targetBitSize);

    final String theoryName = String.format(THEORY_FORMAT, sourceBitSize, targetBitSize);
    if (theoryExists(theoryName)) {
      return false;
    }

    //final StringTemplateBuilder templateBuilder = new StbBvCastTheory(firstBitSize, secondBitSize);
    //generateTheoryFile(theoryName, templateBuilder);

    return true;
  }
}
