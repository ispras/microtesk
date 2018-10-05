/*
 * Copyright 2017-2018 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.test.template.DataSection;
import ru.ispras.microtesk.utils.AdjacencyList;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This structure describes test program being generated.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class TestProgram {
  private List<ConcreteSequence> prologue;
  private List<ConcreteSequence> epilogue;

  private final AdjacencyList<ConcreteSequence> entries;
  private final List<Pair<List<ConcreteSequence>, Map<String, ConcreteSequence>>> exceptionHandlers;

  private final Map<BigInteger, List<DataSection>> dataSections;
  private final List<DataSection> globalDataSections;

  public TestProgram() {
    this.prologue = new ArrayList<>();
    this.epilogue = new ArrayList<>();
    this.entries = new AdjacencyList<>();
    this.exceptionHandlers = new ArrayList<>();
    this.dataSections = new TreeMap<>();
    this.globalDataSections = new ArrayList<>();
  }

  public List<ConcreteSequence> getPrologue() {
    return prologue;
  }

  public void addPrologue(final ConcreteSequence prologue) {
    InvariantChecks.checkNotNull(prologue);
    this.prologue.add(prologue);
  }

  public List<ConcreteSequence> getEpilogue() {
    return epilogue;
  }

  public void addEpilogue(final ConcreteSequence epilogue) {
    InvariantChecks.checkNotNull(epilogue);
    this.epilogue.add(epilogue);
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

  public void removeEntry(final ConcreteSequence sequence) {
    entries.remove(sequence);
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

    List<DataSection> dataList = dataSections.get(address);
    if (null == dataList) {
      dataList = new ArrayList<>();
      dataSections.put(address, dataList);
    }

    dataList.add(data);
  }

  public Collection<DataSection> getAllData() {
    final List<DataSection> result = new ArrayList<>();
    for (final List<DataSection> dataList : dataSections.values()) {
      result.addAll(dataList);
    }
    return result;
  }

  public Collection<DataSection> getGlobalData() {
    return globalDataSections;
  }

  public void reset() {
    entries.clear();
    dataSections.clear();
  }
}
