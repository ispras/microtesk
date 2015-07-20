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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.classifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.Classifier;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;

/**
 * {@link ClassifierTrivial} implements the trivial policy for memory access path classification.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class ClassifierTrivial implements Classifier<MemoryAccessPath> {
  @Override
  public List<Set<MemoryAccessPath>> classify(final Collection<MemoryAccessPath> paths) {
    InvariantChecks.checkNotNull(paths);

    final List<Set<MemoryAccessPath>> pathClasses = new ArrayList<>();

    for (final MemoryAccessPath path : paths) {
      final Set<MemoryAccessPath> pathClass = new HashSet<>();

      pathClass.add(path);
      pathClasses.add(pathClass);
    }

    return pathClasses;
  }
}
