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
  public static Policy create(final EvictPolicyId evict, final WritePolicyId write) {
    return new Policy(evict, write);
  }

  public final EvictPolicyId evict;
  public final WritePolicyId write;

  private Policy(final EvictPolicyId evictPolicyId, final WritePolicyId writePolicyId) {
    InvariantChecks.checkNotNull(evictPolicyId);
    InvariantChecks.checkNotNull(writePolicyId);

    this.evict = evictPolicyId;
    this.write = writePolicyId;
  }
}
