/*
 * Copyright 2017-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.engine.memory;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link AccessesIteratorRandom} implements a random iterator of memory access skeletons,
 * i.e. sequences of memory accesses.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AccessesIteratorRandom implements Iterator<List<Access>> {
  final private List<Collection<AccessChooser>> accessChoosers;

  private List<Access> accesses = null;

  public AccessesIteratorRandom(final List<Collection<AccessChooser>> accessChoosers) {
    InvariantChecks.checkNotNull(accessChoosers);
    this.accessChoosers = accessChoosers;
  }

  @Override
  public void init() {
    next();
  }

  @Override
  public boolean hasValue() {
    return accesses != null;
  }

  @Override
  public List<Access> value() {
    return accesses;
  }

  @Override
  public void next() {
    final List<Access> result = new ArrayList<>(accessChoosers.size());

    for (final Collection<AccessChooser> choosers : accessChoosers) {
      while (!choosers.isEmpty()) {
        final AccessChooser chooser = Randomizer.get().choose(choosers);
        final Access access = chooser.get();

        if (access == null) {
          choosers.remove(chooser);
        } else {
          result.add(new Access(access));
          break;
        }
      }

      if (choosers.isEmpty()) {
        accesses = null;
        return;
      }
    }

    accesses = result;
  }

  @Override
  public void stop() {
    accesses = null;
  }

  @Override
  public AccessesIteratorExhaustive clone() {
    throw new UnsupportedOperationException();
  }
}
