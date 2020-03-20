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

/**
 * {@link InclusionPolicyId} contains the cache inclusion policies.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public enum InclusionPolicyId {
    /** Inclusive policy. */
    INCLUSIVE(true, false, false),

    /** Exclusive policy. */
    EXCLUSIVE(false, true, false),

    /** Non-Inclusive Non-Exclusive. */
    NINE(false, false, true);

    public final boolean yes;
    public final boolean no;
    public final boolean dontCare;

    InclusionPolicyId(final boolean yes, final boolean no, final boolean dontCare) {
      this.yes = yes;
      this.no = no;
      this.dontCare = dontCare;
    }
}
