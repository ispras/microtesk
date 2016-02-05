/*
 * Copyright 2009-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence.engine.branch;

import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.testbase.knowledge.iterator.BooleanIterator;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link BranchExecution} represents a single execution of a branch instruction.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchExecution implements Iterator<Boolean> {
  private static final boolean RANDOM = true;

  private final BooleanIterator iterator;

  /** Set of basic blocks (with counters) to be executed before the branch execution. */
  private Map<Integer, Integer> preBlocks;
  /** Set of delay slots (with counters) to be executed before the branch execution. */
  private Map<Integer, Integer> preSlots;

  /** Set of basic blocks (with counters) to be executed after the branch execution. */
  private Map<Integer, Integer> postBlocks;
  /** Set of delay slots (with counters) to be executed after the branch execution. */
  private Map<Integer, Integer> postSlots;

  /**
   * Constructs a branch execution;
   * 
   * @param conditionalBranch the flag that indicates if the branch is conditional or not.
   */
  public BranchExecution(final boolean conditionalBranch) {
    iterator = new BooleanIterator(
        conditionalBranch && RANDOM ? Randomizer.get().nextBoolean() : false);

    iterator.init();

    // There is only one possibility for unconditional branches.
    if (!conditionalBranch) {
      iterator.next();
    }
  }

  public BranchExecution() {
    this(true);
  }

  private BranchExecution(final BranchExecution r) {
    InvariantChecks.checkNotNull(r);

    iterator = r.iterator.clone();

    preBlocks = new LinkedHashMap<>(r.preBlocks);
    preSlots = new LinkedHashMap<>(r.preSlots);
    postBlocks = new LinkedHashMap<>(r.postBlocks);
    postSlots = new LinkedHashMap<>(r.postSlots);
  }

  public Map<Integer, Integer> getPreBlocks() {
    return preBlocks;
  }

  public void setPreBlocks(final Map<Integer, Integer> preBlocks) {
    InvariantChecks.checkNotNull(preBlocks);
    this.preBlocks = preBlocks;
  }

  public Map<Integer, Integer> getPostBlocks() {
    return postBlocks;
  }

  public void setPostBlocks(final Map<Integer, Integer> postBlocks) {
    InvariantChecks.checkNotNull(postBlocks);
    this.postBlocks = postBlocks;
  }

  public Map<Integer, Integer> getPreSlots() {
    return preSlots;
  }

  public void setPreSlots(final Map<Integer, Integer> preSlots) {
    InvariantChecks.checkNotNull(preSlots);
    this.preSlots = preSlots;
  }

  public Map<Integer, Integer> getPostSlots() {
    return postSlots;
  }

  public void setPostSlots(final Map<Integer, Integer> postSlots) {
    InvariantChecks.checkNotNull(postSlots);
    this.postSlots = postSlots;
  }

  @Override
  public void init() {
    iterator.init();
  }

  @Override
  public boolean hasValue() {
    return iterator.hasValue();
  }

  @Override
  public Boolean value() {
    return iterator.value();
  }

  @Override
  public void next() {
    iterator.next();
  }

  @Override
  public void stop() {
    iterator.stop();
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    builder.append(value());
    builder.append(", ");
    builder.append(String.format("pre=%s", preBlocks));
    builder.append(", ");
    builder.append(String.format("post=%s", postBlocks));

    return builder.toString();
  }

  @Override
  public BranchExecution clone() {
    return new BranchExecution(this);
  }
}
