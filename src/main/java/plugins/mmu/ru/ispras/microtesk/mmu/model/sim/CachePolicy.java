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
 * {@link CachePolicy} contains all cache-related policies including:
 *
 * <ul>
 *  <li>an eviction policy;</li>
 *  <li>a write policy;</li>
 *  <li>an inclusion policy;</li>
 *  <li>a coherence policy.</li>
 * </ul>
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class CachePolicy {
  public static CachePolicy create(
      final EvictionPolicyId eviction,
      final WritePolicyId write,
      final InclusionPolicyId inclusion,
      final CoherenceProtocolId coherence) {
    return new CachePolicy(eviction, write, inclusion, coherence);
  }

  public final EvictionPolicyId eviction;
  public final WritePolicyId write;
  public final InclusionPolicyId inclusion;
  public final CoherenceProtocolId coherence;

  private CachePolicy(
        final EvictionPolicyId eviction,
        final WritePolicyId write,
        final InclusionPolicyId inclusion,
        final CoherenceProtocolId coherence) {
    InvariantChecks.checkNotNull(eviction);
    InvariantChecks.checkNotNull(write);
    InvariantChecks.checkNotNull(inclusion);
    InvariantChecks.checkNotNull(coherence);

    this.eviction = eviction;
    this.write = write;
    this.inclusion = inclusion;
    this.coherence = coherence;
  }

  @Override
  public String toString() {
    return String.format("%s:%s:%s:%s",
        eviction.name(), write.name(), inclusion.name(), coherence.name());
  }
}
