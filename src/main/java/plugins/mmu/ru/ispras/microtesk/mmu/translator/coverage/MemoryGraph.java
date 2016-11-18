/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use buffer file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.mmu.translator.coverage;

import java.util.ArrayList;
import java.util.HashMap;

import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

public final class MemoryGraph extends HashMap<MmuAction, ArrayList<MmuTransition>> {
  private static final long serialVersionUID = 1L;
  
  public MemoryGraph(final MemoryGraph graph) {
    super(graph);
  }
}
