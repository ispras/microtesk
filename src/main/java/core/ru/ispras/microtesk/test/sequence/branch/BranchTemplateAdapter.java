/*
 * Copyright 2009-2015 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.microtesk.test.Adapter;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.template.Call;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchTemplateAdapter implements Adapter<BranchTemplateSolution> {
  @Override
  public Class<BranchTemplateSolution> getSolutionClass() {
    return BranchTemplateSolution.class;
  }

  @Override
  public TestSequence adapt(
      final Sequence<Call> abstractSequence, final BranchTemplateSolution solution) {
    // TODO:
    return null;
  }
}
