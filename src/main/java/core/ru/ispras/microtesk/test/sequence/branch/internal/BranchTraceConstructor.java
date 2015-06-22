/*
 * Copyright 2009-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence.branch.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.randomizer.Randomizer;

/**
 * {@link BranchTraceConstructor} implements a constructor of a branch trace.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchTraceConstructor {
  /**
   * Trace segment constructor.
   */
  private static final class SegmentConstructor extends BranchEntryVisitor {
    /** Maps a branch entry index into the block segment. */
    private Map<Integer, Set<Integer>> blockSegments = new LinkedHashMap<>();

    /** Maps a branch entry index into the slot segment. */
    private Map<Integer, Set<Integer>> slotSegments = new LinkedHashMap<>();

    @Override
    public void onBranch(
        final int index, final BranchEntry entry, final BranchExecution execution) {
      // Process the block segments.
      final Set<Integer> newBlockSegment = execution.getBlockSegment();
      blockSegments.put(index, newBlockSegment);

      // Process the slot segments.
      final Set<Integer> newSlotSegment = execution.getSlotSegment();
      slotSegments.put(index, newSlotSegment);
    }

    @Override
    public void onDelaySlot(final int index, final BranchEntry entry) {
      for (final Set<Integer> segment : slotSegments.values()) {
        segment.add(index);
      }
    }

    @Override
    public void onBasicBlock(final int index, final BranchEntry entry) {
      for (final Set<Integer> segment : blockSegments.values()) {
        segment.add(index);
      }
    }
  }

  /**
   * Block coverage counter.
   */
  private static final class CoverageCounter extends BranchEntryVisitor {
    private final int index;
    private final BranchEntry entry;

    private int blockCount = 0;
    private int slotCount = 0;

    public CoverageCounter(final int index, final BranchEntry entry) {
      this.index = index;
      this.entry = entry;
    }

    @Override
    public void onBranch(int index, BranchEntry entry, BranchExecution execution) {
      if (this.entry == entry) {
        execution.setBlockCoverageCount(blockCount);
        blockCount = 0;

        execution.setSlotCoverageCount(slotCount);
        slotCount = 0;
      }
    }

    @Override
    public void onDelaySlot(final int index, final BranchEntry entry) {
      // Only single-instruction delay slots are supported.
      if (index == this.index + 1) {
        slotCount++;
      }
    }

    @Override
    public void onBasicBlock(final int index, final BranchEntry entry) {
      final Set<Integer> blockCoverage = this.entry.getBlockCoverage();

      if (blockCoverage == null) {
        return;
      }

      if (blockCoverage.contains(index)) {
        blockCount++;
      }
    }
  }

  public static enum Flags {
    /** Do not use delay slots. */
    DO_NOT_USE_DELAY_SLOTS
  }

  private EnumSet<Flags> flags;

  /** Branch structure. */
  private BranchStructure branchStructure;

  /**
   * Construct a branch trace constructor.
   * 
   * @param branchStructure the branch structure.
   * @param flags the heuristics flags.
   */
  public BranchTraceConstructor(
      final BranchStructure branchStructure, final EnumSet<Flags> flags) {
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
    for (int i = 0; i < branchStructure.size(); i++) {
      final BranchEntry entry = branchStructure.get(i);
      final BranchTrace trace = entry.getBranchTrace();

      for (int j = 0; j < trace.size(); j++) {
        BranchExecution execution = trace.get(j);
        execution.clear();
      }
    }

    final BranchStructureWalker walker =
        new BranchStructureWalker(branchStructure, new SegmentConstructor());

    walker.start();
  }

  /**
   * Returns the union of the blocks in the segments of the branch entry.
   * 
   * @param entry the branch entry.
   * @return the union of the block segments.
   */
  private Set<Integer> getBlockUnion(final BranchEntry entry) {
    final Set<Integer> segment = new LinkedHashSet<>();
    final BranchTrace trace = entry.getBranchTrace();

    for (int i = 0; i < trace.size(); i++) {
      final BranchExecution execution = trace.get(i);
      segment.addAll(execution.getBlockSegment());
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
    final Set<Integer> intersection = new LinkedHashSet<>();
    final BranchTrace trace = entry.getBranchTrace();

    if (trace.isEmpty()) {
      return intersection;
    }

    final BranchExecution execution = trace.get(0);
    intersection.addAll(execution.getSlotSegment());

    final Set<Integer> remove = new LinkedHashSet<>();

    for (final int block : intersection) {
      boolean contains = true;
      for (int i = 1; i < trace.size(); i++) {
        final Set<Integer> segment = trace.get(i).getSlotSegment();

        if (!segment.contains(block)) {
          contains = false;
          break;
        }
      }

      if (!contains) {
        remove.add(block);
      }
    }

    intersection.removeAll(remove);

    return intersection;
  }

  /**
   * Returns the trace segments where the branch condition changes.
   * 
   * @param entry the branch entry.
   * @return the list of trace segments.
   */
  private List<Set<Integer>> getChangeSegments(final BranchEntry entry) {
    final List<Set<Integer>> segments = new ArrayList<>();
    final BranchTrace trace = entry.getBranchTrace();

    for (int i = 0; i < trace.size() - 1; i++) {
      final BranchExecution pre = trace.get(i);
      final BranchExecution post = trace.get(i + 1);

      final Set<Integer> segment = pre.getBlockSegment();

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
   * @return {@code true} if construction is successful; {@code false} otherwise.
   */
  private boolean constructCoverage(final BranchEntry entry) {
    // Get all blocks from all segments of the branch.
    final Set<Integer> blocks = getBlockUnion(entry);

    // Get the set of segments to be covered.
    final List<Set<Integer>> segments = getChangeSegments(entry);

    entry.setBlockCoverage(null);
    entry.setSlotCoverage(null);

    // Unreachable or fictitious branching.
    if (segments.isEmpty()) {
      entry.setBlockCoverage(Collections.<Integer>emptySet());
      return true;
    }

    for (int i = 0; i < segments.size(); i++) {
      final Set<Integer> segment = segments.get(i);

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
      final Set<Integer> segment = segments.get(0);
      final int block = Randomizer.get().choose(segment);

      // Add the block to the coverage.
      coverage.add(block);
      entry.setBlockCoverage(coverage);

      return true;
    }

    // Complex branching.
    final Map<Integer, Integer> counts = new LinkedHashMap<>();

    int maxCount = 0;

    // While there exist uncovered segments.
    while (!segments.isEmpty()) {
      // Calculate the block coverage count.
      for (final int block : blocks) {
        for (int i = 0; i < segments.size(); i++) {
          final Set<Integer> segment = segments.get(i);

          if (segment.contains(block)) {
            Integer count = counts.get(block);

            if (count == null) {
              count = new Integer(0);
            } else {
              count = new Integer(count + 1);
            }

            if (count > maxCount) {
              maxCount = count;
            }

            counts.put(block, count);
          }
        }
      }

      // Choose a block with the maximal coverage count.
      final Set<Integer> bests = new LinkedHashSet<>();

      for (final Map.Entry<Integer, Integer> pair : counts.entrySet()) {
        if (pair.getValue() == maxCount) {
          bests.add(pair.getKey());
        }
      }

      final int block = Randomizer.get().choose(bests);
      coverage.add(block);

      // Change the coverage.
      for (int i = 0; i < segments.size(); i++) {
        final Set<Integer> segment = segments.get(i);

        if (segment.contains(block)) {
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
   * Calculates the block coverage counts for the branch executions.
   * 
   * @param index the branch entry index.
   * @param entry the branch entry.
   */
  private void calculateCoverageCounts(int index, BranchEntry entry) {
    final BranchStructureWalker walker =
        new BranchStructureWalker(branchStructure, new CoverageCounter(index, entry));

    walker.start();
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

      calculateCoverageCounts(i, entry);
    }

    return true;
  }
}
