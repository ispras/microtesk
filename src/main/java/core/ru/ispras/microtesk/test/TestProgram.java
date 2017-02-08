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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.DataSection;

/**
 * This structure describes test program being generated.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class TestProgram {
  private TestSequence prologue;
  private Block epilogue;

  private final AdjacencyList<TestSequence> entries;
  private final Map<TestSequence, Block> postponedEntries;
  private final List<Pair<List<TestSequence>, Map<String, TestSequence>>> exceptionHandlers;

  private final List<DataSection> globalData;
  private final List<DataSection> localData;

  public TestProgram() {
    this.prologue = null;
    this.epilogue = null;
    this.entries = new AdjacencyList<>();
    this.postponedEntries = new IdentityHashMap<>();
    this.exceptionHandlers = new ArrayList<>();
    this.globalData = new ArrayList<>();
    this.localData = new ArrayList<>();
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

  public List<Pair<List<TestSequence>, Map<String, TestSequence>>> getExceptionHandlers() {
    return exceptionHandlers;
  }

  public void addExceptionHandlers(
      final Pair<List<TestSequence>, Map<String, TestSequence>> handlers) {
    InvariantChecks.checkNotNull(handlers);
    exceptionHandlers.add(handlers);
  }

  public Iterable<TestSequence> getEntries() {
    return entries;
  }

  public void addEntry(final TestSequence sequence) {
    InvariantChecks.checkNotNull(sequence);
    entries.add(sequence);
  }

  public void addPostponedEntry(final Block block) {
    InvariantChecks.checkNotNull(block);
    final TestSequence sequence = new TestSequence.Builder().build();

    postponedEntries.put(sequence, block);
    addEntry(sequence);
  }

  public boolean isPostponedEntry(final TestSequence sequence) {
    return postponedEntries.containsKey(sequence);
  }

  public Block extractPostponedBlock(final TestSequence sequence) {
    return postponedEntries.remove(sequence);
  }

  public TestSequence getLastEntry() {
    return entries.getLast();
  }

  public TestSequence getLastAllocatedEntry() {
    return !entries.isEmpty() ? findAllocatedEntry(entries.getLast()) : null;
  }

  public TestSequence getPrevAllocatedEntry(final TestSequence sequence) {
    return findAllocatedEntry(entries.getPrevious(sequence));
  }

  public void replaceEntryWith(final TestSequence previous, final TestSequence current) {
    entries.replaceWith(previous, current);
  }

  private TestSequence findAllocatedEntry(final TestSequence sequence) {
    TestSequence entry = sequence;
    while (null != entry && entry.isEmpty()) {
      entry = entries.getPrevious(entry);
    }

    return entry;
  }

  public void addData(final DataSection data) {
    InvariantChecks.checkNotNull(data);
    if (data.isGlobal()) {
      globalData.add(data);
    } else {
      localData.add(data);
    }
  }

  public List<DataSection> getGlobalData() {
    return globalData;
  }

  public List<DataSection> getLocalData() {
    return localData;
  }

  public void reset() {
    entries.clear();
    postponedEntries.clear();
    localData.clear();
  }
}
