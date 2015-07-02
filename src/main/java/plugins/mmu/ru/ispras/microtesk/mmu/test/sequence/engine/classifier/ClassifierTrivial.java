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

package ru.ispras.microtesk.mmu.test.sequence.engine.classifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.Classifier;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryAccess;

/**
 * This class describes the policy of unification by execution paths.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class ClassifierTrivial implements Classifier<MemoryAccess> {
  @Override
  public List<Set<MemoryAccess>> classify(final Collection<MemoryAccess> executions) {
    InvariantChecks.checkNotNull(executions);

    final List<Set<MemoryAccess>> executionList = new ArrayList<>();

    for (final MemoryAccess execution : executions) {
      final Set<MemoryAccess> mmuExecutionClass = new HashSet<>();
      mmuExecutionClass.add(execution);
      executionList.add(mmuExecutionClass);
    }

    return executionList;
  }
}
