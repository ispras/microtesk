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

package ru.ispras.microtesk.test.engine;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.IdentityHashMap;
import java.util.Map;

final class AbstractSequenceIterator implements Iterator<AbstractSequence> {
  private final Iterator<AbstractSequence> sequenceIterator;
  private AbstractSequence abstractSequence;

  public AbstractSequenceIterator(final Iterator<AbstractSequence> sequenceIterator) {
    InvariantChecks.checkNotNull(sequenceIterator);

    this.sequenceIterator = sequenceIterator;
    this.abstractSequence = null;
  }

  @Override
  public void init() {
    sequenceIterator.init();
    this.abstractSequence = null;
  }

  @Override
  public boolean hasValue() {
    return sequenceIterator.hasValue();
  }

  @Override
  public AbstractSequence value() {
    if (null == abstractSequence) {
      abstractSequence = sequenceIterator.value();
      abstractSequence = resolveDependencies(abstractSequence);
      abstractSequence = cloneAbstractSequence(abstractSequence);
    }

    return abstractSequence;
  }

  @Override
  public void next() {
    sequenceIterator.next();
    this.abstractSequence = null;
  }

  @Override
  public void stop() {
    sequenceIterator.stop();
  }

  @Override
  public Iterator<AbstractSequence> clone() {
    throw new UnsupportedOperationException();
  }

  private static AbstractSequence resolveDependencies(final AbstractSequence abstractSequence) {
    final Map<AbstractCall, Integer> abstractCalls = new IdentityHashMap<>();

    for (int index = 0; index < abstractSequence.getSequence().size(); index++) {
      final AbstractCall abstractCall = abstractSequence.getSequence().get(index);
      abstractCalls.put(abstractCall, index);
    }

    for (int index = 0; index < abstractSequence.getSequence().size(); index++) {
      final AbstractCall abstractCall =
          abstractSequence.getSequence().get(index);

      final AbstractCall dependencyAbstractCall =
          (AbstractCall) abstractCall.getAttributes().get("dependsOn");

      if (null != dependencyAbstractCall) {
        final int dependencyIndex = abstractCalls.get(dependencyAbstractCall);
        abstractCall.getAttributes().put("dependsOnIndex", dependencyIndex);
      }
    }

    return abstractSequence;
  }

  private static AbstractSequence cloneAbstractSequence(final AbstractSequence abstractSequence) {
    return new AbstractSequence(
        abstractSequence.getSection(),
        AbstractCall.copyAll(abstractSequence.getSequence())
    );
  }
}
