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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link BranchTraceConstructor} implements a branch trace constructor, a component that gathers
 * useful information for a given execution trace.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class BranchTraceConstructor {
  /**
   * {@link SegmentConstructor} implements a segment constructor.
   * 
   * <p>A segment is a set of basic blocks (delay slots) executed between two subsequent calls of a
   * given branch instruction or before its first call.</p>
   * 
   * <p>For each basic block (delay slot), a segment contains a counter that shows how many times
   * the basic block (delay slot) is executed.</p>
   */
  private static final class SegmentConstructor extends BranchEntryVisitor {
    /** Contains a set of basic blocks having been executed. */
    private Map<Integer, Integer> preBlocks = new LinkedHashMap<>();
    /** Contains a set of delay slots having been executed. */
    private Map<Integer, Integer> preSlots = new LinkedHashMap<>();

    /** Maps a branch index into the current basic block segment. */
    private Map<Integer, Map<Integer, Integer>> postBlocks = new LinkedHashMap<>();
    /** Maps a branch index into the current delay slot segment. */
    private Map<Integer, Map<Integer, Integer>> postSlots = new LinkedHashMap<>();

    @Override
    public void onBranch(
        final int index, final BranchEntry entry, final BranchExecution execution) {
      InvariantChecks.checkNotNull(entry);
      InvariantChecks.checkNotNull(execution);

      // Previous basic blocks.
      if (!postBlocks.containsKey(index)) {
        execution.setPreBlocks(preBlocks);
      } else {
        execution.setPreBlocks(postBlocks.get(index));
      }

      // Previous delay slots.
      if (!postSlots.containsKey(index)) {
        execution.setPreSlots(preSlots);
      } else {
        execution.setPreSlots(postSlots.get(index));
      }

      // Next basic blocks.
      final Map<Integer, Integer> newPostBlocks = new LinkedHashMap<>();
      execution.setPostBlocks(newPostBlocks);
      postBlocks.put(index, newPostBlocks);

      // Next delay slots.
      final Map<Integer, Integer> newPostSlots = new LinkedHashMap<>();
      execution.setPostSlots(newPostSlots);
      postSlots.put(index, newPostSlots);
    }

    @Override
    public void onBasicBlock(final int index, final BranchEntry entry) {
      InvariantChecks.checkNotNull(entry);

      incrementCounter(index, preBlocks);

      for (final Map<Integer, Integer> segment : postBlocks.values()) {
        incrementCounter(index, segment);
      }
    }

    @Override
    public void onDelaySlot(final int index, final BranchEntry entry) {
      InvariantChecks.checkNotNull(entry);

      incrementCounter(index, preSlots);

      for (final Map<Integer, Integer> segment : postSlots.values()) {
        incrementCounter(index, segment);
      }
    }

    private void incrementCounter(final int index, final Map<Integer, Integer> segment) {
      final Integer count = segment.get(index);
      segment.put(index, count != null ? count + 1 : 1);
    }
  }

  public static enum Flags {
    /** Do not use delay slots. */
    DO_NOT_USE_DELAY_SLOTS
  }

  /** Branch structure. */
  private final BranchStructure branchStructure;

  /** Control flags. */
  private final EnumSet<Flags> flags;

  /**
   * Construct a branch trace constructor.
   * 
   * @param branchStructure the branch structure.
   * @param flags the heuristics flags.
   */
  public BranchTraceConstructor(
      final BranchStructure branchStructure, final EnumSet<Flags> flags) {
    InvariantChecks.checkNotNull(branchStructure);
    InvariantChecks.checkNotNull(flags);

    this.branchStructure = branchStructure;
    this.flags = flags;
  }

  /**
   * Construct a branch trace constructor.
   * 
   * @param branchStructure the branch structure.
   */
  public BranchTraceConstructor(final BranchStructure branchStructure) {
    this(branchStructure, EnumSet.noneOf(Flags.class));
  }

  /** Constructs trace segments. */
  private void constructSegments() {
    final BranchStructureWalker walker =
        new BranchStructureWalker(branchStructure, new SegmentConstructor());

    walker.start();
  }

  /**
   * Returns the union of the basic blocks in the segments of the branch entry.
   * 
   * @param entry the branch entry.
   * @return the union of the basic block segments.
   */
  private Set<Integer> getBlockUnion(final BranchEntry entry) {
    InvariantChecks.checkNotNull(entry);

    final Set<Integer> segment = new LinkedHashSet<>();
    final BranchTrace trace = entry.getBranchTrace();

    for (int i = 0; i < trace.size(); i++) {
      final BranchExecution execution = trace.get(i);
      segment.addAll(execution.getPostBlocks().keySet());
    }

    return segment;
  }

  /**
   * Returns the intersection of the slots in the segments of the branch entry.
   * 
   * @param entry the branch entry.
   * @return the intersection of the slot segments.
   */
  private Set<Integer> getSlotIntersection(final BranchEntry entry) {
    InvariantChecks.checkNotNull(entry);

    final BranchTrace trace = entry.getBranchTrace();

    if (trace.isEmpty()) {
      return Collections.emptySet();
    }

    final BranchExecution first = trace.get(0);
    final Set<Integer> intersection = new LinkedHashSet<>(first.getPostSlots().keySet());

    for (int i = 1; i < trace.size() && !intersection.isEmpty(); i++) {
      final BranchExecution execution = trace.get(i);
      final Map<Integer, Integer> segment = execution.getPostSlots();

      intersection.retainAll(segment.keySet());
    }

    return intersection;
  }

  /**
   * Returns the trace segments where the branch condition changes.
   * 
   * @param entry the branch entry.
   * @return the list of trace segments.
   */
  private List<Map<Integer, Integer>> getChangeSegments(final BranchEntry entry) {
    InvariantChecks.checkNotNull(entry);

    final List<Map<Integer, Integer>> segments = new ArrayList<>();
    final BranchTrace trace = entry.getBranchTrace();

    for (int i = 0; i < trace.size() - 1; i++) {
      final BranchExecution pre = trace.get(i);
      final BranchExecution post = trace.get(i + 1);

      final Map<Integer, Integer> segment = pre.getPostBlocks();

      if (pre.value() != post.value()) {
        segments.add(segment);
      }
    }

    return segments;
  }

  /**
   * Constructs the block and the slot coverage of the branch entry.
   * 
   * @param entry the branch entry.
   * @return {@code true} if the construction is successful; {@code false} otherwise.
   */
  private boolean constructCoverage(final BranchEntry entry) {
    InvariantChecks.checkNotNull(entry);

    // Get all blocks from all segments of the branch.
    final Set<Integer> blocks = getBlockUnion(entry);
    // Get the set of segments to be covered.
    final List<Map<Integer, Integer>> segments = getChangeSegments(entry);

    // Reset the block and the slot coverage.
    entry.setBlockCoverage(null);
    entry.setSlotCoverage(null);

    // Unreachable or fictitious branching.
    if (segments.isEmpty()) {
      entry.setBlockCoverage(Collections.<Integer>emptySet());
      return true;
    }

    for (final Map<Integer, Integer> segment : segments) {
      // Cannot cover the empty segment.
      if (segment.isEmpty()) {
        if (flags.contains(Flags.DO_NOT_USE_DELAY_SLOTS)) {
          return false;
        } else {
          entry.setSlotCoverage(getSlotIntersection(entry));
          return true;
        }
      }
    }

    final Set<Integer> coverage = new LinkedHashSet<Integer>();

    // Simple branching.

    if (segments.size() == 1) {
      // Get a random block from the segment.
      final Map<Integer, Integer> segment = segments.iterator().next();
      final int block = Randomizer.get().choose(segment.keySet());

      // Add the block to the coverage.
      coverage.add(block);
      entry.setBlockCoverage(coverage);

      return true;
    }

    // Complex branching.

    // Maps a block to the number of segments it belongs to.
    final Map<Integer, Integer> counts = new LinkedHashMap<>();

    int maxCount = 0;

    // While there exist uncovered segments.
    while (!segments.isEmpty()) {
      // Calculate the block coverage count.
      for (final int block : blocks) {
        for (final Map<Integer, Integer> segment : segments) {
          if (segment.containsKey(block)) {
            Integer count = counts.get(block);
            count = count == null ? new Integer(0) : new Integer(count + 1);

            if (count > maxCount) {
              maxCount = count;
            }

            counts.put(block, count);
          }
        }
      }

      // Choose a block with the maximal coverage count.
      final Set<Integer> bestBlocks = new LinkedHashSet<>();

      for (final Map.Entry<Integer, Integer> pair : counts.entrySet()) {
        if (pair.getValue() == maxCount) {
          bestBlocks.add(pair.getKey());
        }
      }

      final int block = Randomizer.get().choose(bestBlocks);
      coverage.add(block);

      // Change the coverage.
      for (int i = 0; i < segments.size(); i++) {
        final Map<Integer, Integer> segment = segments.get(i);

        if (segment.containsKey(block)) {
          segments.remove(segment);
          i--;
        }
      }

      counts.clear();
      maxCount = 0;
    }

    // Set the block coverage.
    entry.setBlockCoverage(coverage);

    return true;
  }

  /**
   * Constructs the coverage for all branch entries.
   * 
   * @return {@code true} if construction is successful; {@code false} otherwise.
   */
  public boolean construct() {
    constructSegments();

    for (int i = 0; i < branchStructure.size(); i++) {
      final BranchEntry entry = branchStructure.get(i);

      if (!constructCoverage(entry)) {
        return false;
      }
    }

    return true;
  }
}
