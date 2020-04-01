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
 * {@link WritePolicyId} contains the cache write policies.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public enum WritePolicyId {
    /** Local Write, Data Allocation. */
    WN(true, false, false),

    /** Write-Through, No Data Allocation. */
    WT(false, true, false),

    /** Write-Through, Data Allocation. */
    WTA(true, true, false),

    /** Write-Back, Data Allocation. */
    WB(true, false, true);

    public final boolean alloc;
    public final boolean through;
    public final boolean back;

    WritePolicyId(final boolean alloc, final boolean through, final boolean back) {
      InvariantChecks.checkFalse(through && back);

      this.alloc = alloc;
      this.through = through;
      this.back = back;
    }
}
