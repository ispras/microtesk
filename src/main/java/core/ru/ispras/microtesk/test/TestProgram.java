/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.Block;

/**
 * This structure describes test program being generated.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class TestProgram {
  private TestSequence prologue;
  private Block epilogue;

  private final List<TestProgramEntry> entries;
  private final List<Map<String, TestSequence>> exceptionHandlers;

  public TestProgram() {
    this.prologue = null;
    this.epilogue = null;
    this.entries = new ArrayList<>();
    this.exceptionHandlers = new ArrayList<>();
  }

  public TestSequence getPrologue() {
    return prologue;
  }

  public void setPrologue(final TestSequence prologue) {
    InvariantChecks.checkNotNull(prologue);
    this.prologue = prologue;
  }

  public Block getEpilogue() {
    return epilogue;
  }

  public void setEpilogue(final Block epilogue) {
    InvariantChecks.checkNotNull(epilogue);
    this.epilogue = epilogue;
  }

  public List<Map<String, TestSequence>> getExceptionHandlers() {
    return exceptionHandlers;
  }

  public void addExceptionHandlers(final Map<String, TestSequence> handlers) {
    InvariantChecks.checkNotNull(handlers);
    exceptionHandlers.add(handlers);
  }

  public void addEntry(final TestProgramEntry entry) {
    InvariantChecks.checkNotNull(entry);
    entries.add(entry);
  }

  public int getEntryCount() {
    return entries.size();
  }

  public TestProgramEntry getEntry(final int index) {
    return entries.get(index);
  }

  public void clearEntries() {
    entries.clear();
  }
}
