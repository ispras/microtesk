/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

import java.util.LinkedList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.Model;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.ExceptionHandler;
import ru.ispras.microtesk.test.template.Template;
import ru.ispras.microtesk.test.template.Template.Section;

public final class Engine implements Template.Processor {
  private final Model model;
  private final Options options;

  private final Code code;
  private final List<Block> delayed;

  private CodeBlock prologue = null;
  private Block epilogue = null;

  public Engine(final Model model, final Options options) {
    this.model = model;
    this.options = options;

    this.code = new Code();
    this.delayed = new LinkedList<>();
  }

  @Override
  public void defineExceptionHandler(final ExceptionHandler handler) {
    // TODO Auto-generated method stub
  }

  @Override
  public void finish() {
    // TODO Auto-generated method stub
  }

  @Override
  public void process(final Section section, final Block block) {
    InvariantChecks.checkNotNull(section);
    InvariantChecks.checkNotNull(block);

    if (section == Section.PRE) {
      processPrologue(block);
    } else if (section == Section.POST) {
      processEpilogue(block);
    } else if (block.isExternal()) {
      processExternal(block);
    } else {
      processBlock(block);
    }
  }

  private void processPrologue(final Block block) {
    InvariantChecks.checkTrue(null == prologue);
    prologue = null;
  }

  private void processEpilogue(final Block block) {
    InvariantChecks.checkTrue(null == epilogue);
    epilogue = block;
  }

  private void processExternal(final Block block) {
    // TODO Auto-generated method stub
  }

  private void processBlock(final Block block) {
    // TODO Auto-generated method stub
  }
}
