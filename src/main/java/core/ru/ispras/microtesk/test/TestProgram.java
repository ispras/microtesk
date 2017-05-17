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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.DataSection;
import ru.ispras.microtesk.utils.AdjacencyList;

/**
 * This structure describes test program being generated.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class TestProgram {
  private TestSequence prologue;
  private TestSequence epilogue;

  private final AdjacencyList<TestSequence> entries;
  private final Map<TestSequence, Pair<Block, Integer>> postponedEntries;
  private final List<Pair<List<TestSequence>, Map<String, TestSequence>>> exceptionHandlers;

  private final Map<BigInteger, DataSection> dataSections;
  private final List<DataSection> globalDataSections;

  public TestProgram() {
    this.prologue = null;
    this.epilogue = null;
    this.entries = new AdjacencyList<>();
    this.postponedEntries = new IdentityHashMap<>();
    this.exceptionHandlers = new ArrayList<>();
    this.dataSections = new TreeMap<>();
    this.globalDataSections = new ArrayList<>();
  }

  public TestSequence getPrologue() {
    return prologue;
  }

  public void setPrologue(final TestSequence prologue) {
    InvariantChecks.checkNotNull(prologue);
    this.prologue = prologue;
  }

  public TestSequence getEpilogue() {
    return epilogue;
  }

  public void setEpilogue(final TestSequence epilogue) {
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

  public void addEntryAfter(final TestSequence previous, final TestSequence sequence) {
    InvariantChecks.checkNotNull(sequence);
    entries.addAfter(previous, sequence);
  }

  public void addPostponedEntry(final Block block) {
    addPostponedEntry(block, 1);
  }

  public void addPostponedEntry(final Block block, final int times) {
    InvariantChecks.checkNotNull(block);
    final TestSequence sequence = new TestSequence.Builder().build();

    postponedEntries.put(sequence, new Pair<>(block, times));
    addEntry(sequence);
  }

  public boolean isPostponedEntry(final TestSequence sequence) {
    return postponedEntries.containsKey(sequence);
  }

  public Pair<Block, Integer> getPostponedEntry(final TestSequence sequence) {
    return postponedEntries.get(sequence);
  }

  public void removePostponedEntry(final TestSequence sequence) {
    postponedEntries.remove(sequence);
  }

  public TestSequence getFirstEntry() {
    return entries.getFirst();
  }
  
  public TestSequence getLastEntry() {
    return entries.getLast();
  }

  public TestSequence getPrevEntry(final TestSequence sequence) {
    return entries.getPrevious(sequence);
  }

  public void replaceEntryWith(final TestSequence previous, final TestSequence current) {
    entries.replaceWith(previous, current);
  }

  public void addData(final DataSection data) {
    InvariantChecks.checkNotNull(data);
    registerData(data);

    if (data.isGlobal()) {
      globalDataSections.add(data);
    }
  }

  public void readdGlobalData() {
    InvariantChecks.checkTrue(dataSections.isEmpty());
    for (final DataSection data : globalDataSections) {
      registerData(data);
    }
  }

  private void registerData(final DataSection data) {
    final BigInteger address = data.getAllocationEndAddress();
    InvariantChecks.checkNotNull(address);
    dataSections.put(address, data);
  }

  public Collection<DataSection> getAllData() {
    return dataSections.values();
  }

  public Collection<DataSection> getGlobalData() {
    return globalDataSections;
  }

  public void reset() {
    entries.clear();
    postponedEntries.clear();
    dataSections.clear();
  }
}
