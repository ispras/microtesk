/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.BlockId.Distance;
import ru.ispras.microtesk.test.template.Label;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The role of the {@link LabelManager} class is resolving references to labels that have the same
 * names, but are defined in different blocks. It stores all labels defined by a sequence and their
 * relative positions grouped by name. When it is required to perform a jump to a label with a
 * specific name, it chooses the most suitable label depending on the block from which the jump is
 * performed. Here are the rules according to which the choice is made:
 * <ol>
 * <li>If there is only one such label (no other choice), choose it.</li>
 * <li>Choose a label defined in the current block, if there is such a label defined in the current
 * block.</li>
 * <li>Choose a label defined in the closest child, if there are such labels defined in child
 * blocks.</li>
 * <li>Choose a label defined in the closest parent, if there are such labels defined in parent
 * blocks.</li>
 * <li>Choose a label defined in the closest sibling.</li>
 * </ol>
 * Note: Labels that have different reference numbers are considered different. A reference number
 * is a way to distinguish labels with same names. This happens when some subsequences are created
 * using the same template representation (e.g. by instantiating the same preparator).
 *
 * For more implementation details, see the {@link LabelManager.TargetDistance} class.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class LabelManager {
  /**
   * The {@link Target} class stores information about the target the specified label points to.
   * It associates a label with an address where an instruction is located.
   *
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */
  public static final class Target {
    private final Label label;
    private final long address;

    Target(final Label label, final long address) {
      InvariantChecks.checkNotNull(label);

      this.label = label;
      this.address = address;
    }

    public Label getLabel() {
      return label;
    }

    public long getAddress() {
      return address;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + label.hashCode();
      result = prime * result + (int) (address >> 32);
      result = prime * result + (int) address;

      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final Target other = (Target) obj;
      return (address == other.address) && label.equals(other.label);
    }

    @Override
    public String toString() {
      return String.format("%s at 0x%016x", label, address);
    }
  }

  /**
   * The TargetDistance class associates label targets with their relative distances (in blocks)
   * from the reference point (the point a jump is performed from). Also, it provides a comparison
   * method which helps sort targets by their distance from the reference point. This is needed to
   * choose the most suitable target to perform a jump from the specified point (if there is an
   * ambiguity caused by labels that have the same name).
   *
   * <p>
   * Sorting criteria:
   * <ol>
   * <li>First - labels defined in the current block (zero distance).</li>
   * <li>Second - labels defined in child blocks (by the {@code down} path).</li>
   * <li>Third - labels defined in parents blocks (by the {@code up} path).</li>
   * <li>Finally - labels defined in sibling blocks (by the {@code up} path, the
   * {@code down} path is considered when up paths are equal).</li>
   * </ol>
   *
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */
  private static final class TargetDistance implements Comparable<TargetDistance> {
    private final Target target;
    private final Distance distance;

    private static final Distance ZERO = new Distance(0, 0);

    private TargetDistance(final Target target, final Distance distance) {
      this.target = target;
      this.distance = distance;
    }

    @Override
    public int compareTo(final TargetDistance other) {
      // /////////////////////////////////////////////////////////////////
      // This one and the other one refer to the same block.
      if (this.distance.equals(other.distance)) {
        return 0;
      }
      // /////////////////////////////////////////////////////////////////
      // This one is the current block.
      if (this.distance.equals(ZERO)) {
        return -1;
      }
      // If the other one is the current block (while this one it not)
      // it has more priority.
      if (other.distance.equals(ZERO)) {
        return 1;
      }
      // /////////////////////////////////////////////////////////////////
      // This one is a child block.
      if (0 == this.distance.getUp()) {
        // Other one is a child block too.
        if (0 == other.distance.getUp()) {
          return this.distance.getDown() - other.distance.getDown();
        } else {
          return -1; // Otherwise, this one has more priority.
        }
      }
      // If the other one is a child block (while this one is not),
      // it has more priority.
      if (0 == other.distance.getUp()) {
        return 1;
      }
      // /////////////////////////////////////////////////////////////////
      // This one is a parent block.
      if (0 == this.distance.getDown()) {
        // Other one is a parent block too.
        if (0 == other.distance.getDown()) {
          return this.distance.getUp() - other.distance.getUp();
        } else {
          return -1; // Otherwise, this one has more priority.
        }
      }
      // If the other one is a parent block (while this one is not),
      // it has more priority.
      if (0 == other.distance.getDown()) {
        return 1;
      }
      // /////////////////////////////////////////////////////////////////
      // This one and the other ones are sibling blocks.
      final int deltaUp = this.distance.getUp() - other.distance.getUp();

      // The up path is not the same.
      if (0 != deltaUp) {
        return deltaUp;
      }

      return this.distance.getDown() - other.distance.getDown();
    }
  }

  private final Map<String, List<Target>> table;

  /**
   * Constructs a new label manager that stores no information about labels.
   */
  public LabelManager() {
    this.table = new HashMap<>();
  }

  public LabelManager(final LabelManager other) {
    InvariantChecks.checkNotNull(other);

    this.table = new HashMap<>();
    for (final Map.Entry<String, List<Target>> e : other.table.entrySet()) {
      this.table.put(e.getKey(), new ArrayList<>(e.getValue()));
    }
  }

  /**
   * Adds information about a label to the table of label targets.
   *
   * @param label Label to be registered.
   * @param address Address associated with the label.
   *
   * @throws IllegalArgumentException if the parameter is {@code null}.
   */
  public void addLabel(final Label label, final long address) {
    InvariantChecks.checkNotNull(label);

    // Weak symbols always have address 0x0.
    final Target target = new Target(label, label.isWeak() ? 0x0 : address);

    final List<Target> targets;
    if (table.containsKey(label.getName())) {
      targets = table.get(label.getName());

      for (int index = 0; index < targets.size(); ++index) {
        final Target existingTarget = targets.get(index);
        final Label existingLabel = existingTarget.getLabel();

        if (existingLabel.equals(label)) {
          if (existingLabel.isWeak()) {
            targets.set(index, target);
            return;
          } else {
            throw new GenerationAbortedException(
                String.format("Incorrect template: label '%s' has been already defined",
                    label.getUniqueName()));
          }
        }
      }
    } else {
      targets = new ArrayList<>();
      table.put(label.getName(), targets);
    }

    targets.add(target);
  }

  /**
   * Adds information about label in the specified collection to the table of label targets.
   *
   * @param label Label to be registered.
   * @param address Address the label point to.
   * @param sequenceIndex Index of the sequence.
   *
   * @throws IllegalArgumentException if the {@code label} parameter is {@code null}.
   * @throws IllegalArgumentException if an object in the {@code labels} collection is not a
   *         Label object.
   */
  public void addLabel(final Label label, final long address, final int sequenceIndex) {
    InvariantChecks.checkNotNull(label);

    if (sequenceIndex != Label.NO_SEQUENCE_INDEX) {
      label.setSequenceIndex(sequenceIndex);
    }

    addLabel(label, address);
  }

  /**
   * Resolves a reference to a label having a specific name from a specific block and returns the
   * most suitable target (label and its position). The most suitable target is chosen depending on
   * the reference position (see the {@link LabelManager} class comment). If there are several
   * equally possible choices (ambiguity) a warning message is printed.
   *
   * @param referenceLabel A Label object that describes a reference to a label that has a specific
   *        name from a specific block.
   * @return The most suitable target (label and its position) for the given reference or
   *         {@code null} if no label having such name is found.
   *
   * @throws IllegalArgumentException if the parameter is {@code null}.
   */
  public Target resolve(final Label referenceLabel) {
    InvariantChecks.checkNotNull(referenceLabel);

    if (!table.containsKey(referenceLabel.getName())) {
      return null;
    }

    final List<Target> targets = table.get(referenceLabel.getName());
    if (1 == targets.size()) {
      return targets.get(0);
    }

    final List<TargetDistance> distances = new ArrayList<>(targets.size());
    for (int index = 0; index < targets.size(); ++index) {
      final Target target = targets.get(index);
      final Label targetLabel = target.getLabel();

      // If reference numbers or sequence indexes (except for global labels) do not match,
      // it is a different label that cannot be chosen.
      if (referenceLabel.getReferenceNumber() == targetLabel.getReferenceNumber()
          && (referenceLabel.getSequenceIndex() == targetLabel.getSequenceIndex()
              || Label.NO_SEQUENCE_INDEX == targetLabel.getSequenceIndex())) {
        final Distance distance = referenceLabel.getBlockId().getDistance(targetLabel.getBlockId());
        distances.add(new TargetDistance(target, distance));
      }
    }

    if (distances.isEmpty()) {
      return null;
    }

    Collections.sort(distances);
    checkAmbiguousChoice(distances);

    return distances.get(0).target;
  }

  private void checkAmbiguousChoice(final List<TargetDistance> items) {
    final StringBuilder sb = new StringBuilder();

    final TargetDistance chosenItem = items.get(0);
    for (int index = 1; index < items.size(); ++index) {
      final TargetDistance item = items.get(index);
      if (!chosenItem.distance.equals(item.distance)) {
        break;
      }
      if (sb.length() != 0) {
        sb.append(", ");
      }
      sb.append(item.target);
    }

    if (sb.length() == 0) {
      return;
    }

    Logger.warning(
        "Label %s was chosen, while there are other equally possible choices: %s.",
        chosenItem.target, sb
    );
  }

  /**
   * Clears all labels.
   */
  public void reset() {
    table.clear();
  }

  @Override
  public String toString() {
    return String.format("LabelManager [table=%s]", table);
  }
}
