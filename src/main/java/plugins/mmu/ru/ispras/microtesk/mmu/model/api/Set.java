/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.api;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a cache set, which is a fully associative buffer consisting of cache lines.
 * 
 * @param <D> the data type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:leonsia@ispras.ru">Tatiana Sergeeva</a>
 */

public final class Set<D, A extends Address> implements Buffer<D, A> {
  /** The array of cache lines. */
  private final List<Line<D, A>> lines = new ArrayList<>();

  /** The data replacement policy. */
  private Policy policy;

  /**
   * Constructs a cache set of the given associativity.
   * 
   * @param associativity the number of lines in the set.
   * @param policyId the identifier of the data replacement policy.
   * @param matcher the data-address matcher.
   */

  public Set(int associativity, final PolicyId policyId, final Matcher<D, A> matcher) {
    // Fill the set with the default (invalid) lines.
    for (int i = 0; i < associativity; i++) {
      lines.add(new Line<D, A>(matcher));
    }

    this.policy = policyId.newPolicy(associativity);
  }

  @Override
  public boolean isHit(final A address) {
    return getLine(address) != null;
  }

  @Override
  public D getData(final A address) {
    final Line<D, A> line = getLine(address);
    return line != null ? line.getData(address) : null;
  }

  @Override
  public D setData(final A address, final D data) {
    Line<D, A> line = getLine(address);

    // If there is a miss, choose a victim.
    if (line == null) {
      line = lines.get(policy.chooseVictim()); 
    }

    return line.setData(address, data);
  }

  /**
   * Returns the line associated with the given address.
   *  
   * @param address the data address.
   * @return the line associated with the given address if it exists; <code>null</code> otherwise.
   */

  private Line<D, A> getLine(final A address) {
    int index = -1;

    for (int i = 0; i < lines.size(); i++) {
      final Line<D, A> line = lines.get(i);

      if (line.isHit(address)) {
        if (index != -1) {
          throw new IllegalStateException("Multiple hits in a cache set");
        }

        index = i;
      }
    }

    if (index != -1) {
      policy.accessLine(index);
    }

    return index == -1 ? null : lines.get(index);
  }
}
