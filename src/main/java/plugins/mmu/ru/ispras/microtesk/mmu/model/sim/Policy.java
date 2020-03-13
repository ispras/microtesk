/*
 * Copyright 2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.sim;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link Policy} contains all cache-related policies.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class Policy {
  public static Policy create(
      final EvictPolicyId evict,
      final WritePolicyId write,
      final InclusionPolicyId inclusion) {
    return new Policy(evict, write, inclusion);
  }

  public final EvictPolicyId evict;
  public final WritePolicyId write;
  public final InclusionPolicyId inclusion;

  private Policy(
        final EvictPolicyId evict,
        final WritePolicyId write,
        final InclusionPolicyId inclusion) {
    InvariantChecks.checkNotNull(evict);
    InvariantChecks.checkNotNull(write);
    InvariantChecks.checkNotNull(inclusion);

    this.evict = evict;
    this.write = write;
    this.inclusion = inclusion;
  }

  @Override
  public String toString() {
    return String.format("%s:%s:%s", evict.name(), write.name(), inclusion.name());
  }
}
