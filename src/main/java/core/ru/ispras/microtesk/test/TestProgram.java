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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.model.memory.Section;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.DataSection;
import ru.ispras.microtesk.utils.AdjacencyList;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This structure describes test program being generated.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class TestProgram {
  private ConcreteSequence prologue;
  private ConcreteSequence epilogue;

  private final AdjacencyList<ConcreteSequence> entries;
  private final Map<ConcreteSequence, Pair<Block, Integer>> postponedEntries;
  private final List<Pair<List<ConcreteSequence>, Map<String, ConcreteSequence>>> exceptionHandlers;

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

  public ConcreteSequence getPrologue() {
    return prologue;
  }

  public void setPrologue(final ConcreteSequence prologue) {
    InvariantChecks.checkNotNull(prologue);
    this.prologue = prologue;
  }

  public ConcreteSequence getEpilogue() {
    return epilogue;
  }

  public void setEpilogue(final ConcreteSequence epilogue) {
    InvariantChecks.checkNotNull(epilogue);
    this.epilogue = epilogue;
  }

  public List<Pair<List<ConcreteSequence>, Map<String, ConcreteSequence>>> getExceptionHandlers() {
    return exceptionHandlers;
  }

  public void addExceptionHandlers(
      final Pair<List<ConcreteSequence>, Map<String, ConcreteSequence>> handlers) {
    InvariantChecks.checkNotNull(handlers);
    exceptionHandlers.add(handlers);
  }

  public Iterable<ConcreteSequence> getEntries() {
    return entries;
  }

  public boolean hasEntry(final ConcreteSequence sequence) {
    return entries.contains(sequence);
  }

  public void addEntry(final ConcreteSequence sequence) {
    InvariantChecks.checkNotNull(sequence);
    entries.add(sequence);
  }

  public void addEntryAfter(final ConcreteSequence previous, final ConcreteSequence sequence) {
    InvariantChecks.checkNotNull(sequence);
    entries.addAfter(previous, sequence);
  }

  public void addPostponedEntry(final Block block) {
    addPostponedEntry(block, 1);
  }

  public void addPostponedEntry(final Block block, final int times) {
    InvariantChecks.checkNotNull(block);
    final ConcreteSequence sequence = new ConcreteSequence.Builder(block.getSection()).build();

    postponedEntries.put(sequence, new Pair<>(block, times));
    addEntry(sequence);
  }

  public boolean isPostponedEntry(final ConcreteSequence sequence) {
    return postponedEntries.containsKey(sequence);
  }

  public Pair<Block, Integer> getPostponedEntry(final ConcreteSequence sequence) {
    return postponedEntries.get(sequence);
  }

  public void removePostponedEntry(final ConcreteSequence sequence) {
    postponedEntries.remove(sequence);
  }

  public ConcreteSequence getLastEntry(final Section section) {
    ConcreteSequence last = entries.getLast();
    while (null != last && section != last.getSection()) {
      last = entries.getPrevious(last);
    }
    return last;
  }

  public ConcreteSequence getLastNonEmptyEntry() {
    ConcreteSequence last = entries.getLast();
    while (null != last && last.isEmpty()) {
      last = entries.getPrevious(last);
    }
    return last;
  }

  public ConcreteSequence getPrevEntry(final ConcreteSequence sequence) {
    ConcreteSequence previous = entries.getPrevious(sequence);
    while (null != previous && sequence.getSection() != previous.getSection()) {
      previous = entries.getPrevious(previous);
    }
    return previous;
  }

  public void replaceEntryWith(final ConcreteSequence previous, final ConcreteSequence current) {
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
