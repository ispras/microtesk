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

package ru.ispras.microtesk.test.sequence.branch;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.sequence.branch.internal.BranchStructure;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchTemplateSolution {
  /** Branch structure containing execution traces. */
  private BranchStructure branchStructure;

  public BranchStructure getBranchStructure() {
    return branchStructure;
  }

  public void setBranchStructure(final BranchStructure branchStructure) {
    InvariantChecks.checkNotNull(branchStructure);
    this.branchStructure = branchStructure;
  }
}
