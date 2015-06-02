/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.coverage;

import java.util.Collection;
import java.util.Collections;

import ru.ispras.fortress.util.InvariantChecks;

public final class SsaForm {
  private final Block entry;
  private final Block exit;
  private final Collection<Block> blocks;

  SsaForm(Block entry, Block exit, Collection<Block> blocks) {
    InvariantChecks.checkNotNull(entry);
    InvariantChecks.checkNotNull(exit);
    InvariantChecks.checkNotNull(blocks);

    this.entry = entry;
    this.exit = exit;
    this.blocks = blocks;
  }

  public Block getEntryPoint() {
    return entry;
  }

  public Collection<Block> getExitPoints() {
    return Collections.singleton(exit);
  }

  public Collection<Block> getBlocks() {
    return blocks;
  }
}
