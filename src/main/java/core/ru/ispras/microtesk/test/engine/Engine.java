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

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.Printer;
import ru.ispras.microtesk.test.TestEngineUtils;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.DataSection;
import ru.ispras.microtesk.test.template.ExceptionHandler;
import ru.ispras.microtesk.test.template.Template;
import ru.ispras.microtesk.test.template.Template.Section;

/**
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Engine implements Template.Processor {
  private final EngineContext context;
  private final List<ThreadState> threadStates;

  private Printer printer = null;
  private TestSequence prologue = null;
  private Block epilogue = null;

  public Engine(final EngineContext context) {
    InvariantChecks.checkNotNull(context);

    this.context = context;
    this.threadStates = newInitialThreadStates(context.getModel().getPENumber());
  }

  private static List<ThreadState> newInitialThreadStates(final int count) {
    InvariantChecks.checkGreaterThanZero(count); 

    final List<ThreadState> result = new ArrayList<>(count);
    for (int index = 0; index < count; index++) {
      result.set(index, new ThreadState(ThreadState.Kind.START, 0, null));
    }

    return result;
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

  @Override
  public void process(final DataSection data) {
    // TODO Auto-generated method stub
  }

  @Override
  public void defineExceptionHandler(final ExceptionHandler handler) {
    InvariantChecks.checkNotNull(handler);
    // TODO Auto-generated method stub
  }

  @Override
  public void finish() {
    // TODO Auto-generated method stub
  }

  private void processPrologue(final Block block) {
    InvariantChecks.checkTrue(null == prologue);
    prologue = TestEngineUtils.makeTestSequenceForExternalBlock(context, block);
  }

  private void processEpilogue(final Block block) {
    InvariantChecks.checkTrue(null == epilogue);
    epilogue = block;
  }

  private void processExternal(final Block block) {
    startFile();

    /*
    final List<Call> abstractSequence = TestEngineUtils.getSingleSequence(block);
    final BigInteger origin = getOrigin(block);
    
    
    if (null == origin) {
      unallocated.add(block);
      return;
    }

    
    if (unallocated.isEmpty()) {
      final TestSequence sequence =
          TestEngineUtils.makeTestSequenceForExternalBlock(engineContext, block);
      
      
    } else {
      unallocated.add(block);
    }*/

    // If has no unallocated || has fixed origin
      // make test sequence 
      // allocate test sequence
      // For each thread to enter execute the test sequence
    // Else
      // Add to unallocated
  }

  private void processBlock(final Block block) {
    startFile();

    // startFile();
    // TODO Auto-generated method stub
  }

  private void startFile() {
    if (null != printer) {
      return;
    }
  }
}
