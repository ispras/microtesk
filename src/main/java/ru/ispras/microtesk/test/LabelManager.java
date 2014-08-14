/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LabelManager.java, Aug 14, 2014 11:51:02 AM Andrei Tatarnikov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.BlockId.Distance;

/**
 * The role of the LabelManager class is resolving references to labels 
 * that have the same names, but are defined in different blocks. It stores
 * all labels defined by a sequence and their relative positions grouped
 * by name. When it is required to perform a jump to a label with a specific
 * name, it chooses the most suitable label depending on the block from 
 * which the jump is performed. Here are the rules according to which
 * the choice is made: 
 * <ol>
 * <li>If there is only one such label (no other choice), choose it.</li>
 * <li>Choose a label defined in the current block,
 *     if there is such a label defined in the current block.</li>
 * <li>Choose a label defined in the closest child,
 *     if there are such labels defined in child blocks.</li>
 * <li>Choose a label defined in the closest parent,
 *     if there are such labels defined in parent blocks.</li>
 * <li>Choose a label defined in the closest sibling.</li>
 * </ol>
 * For more implementation details, see the {@link LabelManager.TargetDistance}
 * class.
 * 
 * @author Andrei Tatarnikov
 */

final class LabelManager
{
    /**
     * The Target class stores information about the target
     * the specified label points to. It associates a label
     * with a position in an instruction call sequence.
     *  
     * @author Andrei Tatarnikov
     */

    public static final class Target
    {
        private final Label label;
        private final int position;

        Target(Label label, int position)
        {
            if (null == label)
                throw new NullPointerException();

            if (position < 0)
                throw new IllegalArgumentException();

            this.label = label;
            this.position = position;
        }

        public Label getLabel()  { return label; }
        public int getPosition() { return position; }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;

            result = prime * result + label.hashCode();
            result = prime * result + position;

            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;

            if (getClass() != obj.getClass())
                return false;

            final Target other = (Target) obj;
            return (position == other.position) && label.equals(other.label);
        }

        @Override
        public String toString()
        {
            return String.format("%s at %d", label, position);
        }
    }

    /**
     * The TargetDistance class associates label targets with their
     * relative distances (in blocks) from the reference point (the point
     * a jump is performed from). Also, it provides a comparison method
     * which helps sort targets by their distance from the reference point.
     * This is needed to choose the most suitable target to perform a jump
     * from the specified point (if there is an ambiguity caused by labels
     * that have the same name).
     * 
     * <p>Sorting criteria:
     * <ol>
     * <li>First - labels defined in the current block (zero distance).</li>
     * <li>Second - labels defined in child blocks 
     *     (by the <code>down</code> path).</li>
     * <li>Third - labels defined in parents blocks
     *     (by the <code>up</code> path).</li>
     * <li>Finally - labels defined in sibling blocks (by the
     *     <code>up</code> path, the <code>down</code> path is considered
     *     when up paths are equal).</li>
     * </ol>
     * 
     * @author Andrei Tatarnikov
     */

    private static final class TargetDistance
        implements Comparable<TargetDistance>
    {
        private final Target target;
        private final Distance distance;

        private static final Distance ZERO = new Distance(0, 0);

        private TargetDistance(Target target, Distance distance)
        {
            this.target = target;
            this.distance = distance;
        }

        @Override
        public int compareTo(TargetDistance other)
        {
            ///////////////////////////////////////////////////////////////////
            // This one and the other one refer to the same block.
            if (this.distance.equals(other.distance))
                return 0;

            ///////////////////////////////////////////////////////////////////
            // This one is the current block.
            if (this.distance.equals(ZERO))
                return -1;

            // If the other one is the current block (while this one it not)
            // it has more priority.
            if (other.distance.equals(ZERO))
                return 1;

            ///////////////////////////////////////////////////////////////////
            // This one is a child block.
            if (0 == this.distance.getUp())
            {
                // Other one is a child block too.
                if (0 == other.distance.getUp())
                    return this.distance.getDown() - other.distance.getDown();
                else
                    return -1; // Otherwise, this one has more priority. 
            }

            // If the other one is a child block (while this one is not), 
            // it has more priority.
            if (0 == other.distance.getUp())
                return 1;

            ///////////////////////////////////////////////////////////////////
            // This one is a parent block.
            if (0 == this.distance.getDown())
            {
                // Other one is a parent block too.
                if (0 == other.distance.getDown())
                    return this.distance.getUp() - other.distance.getUp();
                else
                    return -1; // Otherwise, this one has more priority.
            }

            // If the other one is a parent block (while this one is not), 
            // it has more priority.
            if (0 == other.distance.getDown())
                return 1;

            ///////////////////////////////////////////////////////////////////
            // This one and the other ones are sibling blocks.

            final int deltaUp =
                this.distance.getUp() - other.distance.getUp();

            // The up path is not the same. 
            if (0 != deltaUp)
                return deltaUp;

            return this.distance.getDown() - other.distance.getDown();
        }
    }

    private final Map<String, List<Target>> table;

    /**
     * Constructs a new label manager that stores no information about labels.
     */

    public LabelManager()
    {
        this.table = new HashMap<String, List<Target>>();
    }

    /**
     * Adds information about a label to the table of label targets.
     * 
     * @param label Label to be registered.
     * @param position Position in the sequence of the instruction
     * the label points to.
     * 
     * @throws NullPointerException if the parameter is <code>null</code>.
     */

    public void addLabel(Label label, int position)
    {
        if (null == label)
            throw new NullPointerException();

        final Target target = new Target(label, position);

        final List<Target> targets;
        if (table.containsKey(label.getName()))
        {
            targets = table.get(label.getName());
        }
        else
        {
            targets = new ArrayList<Target>();
            table.put(label.getName(), targets);
        }

        targets.add(target);
    }

    /**
     * Adds information about label in the specified collection to
     * the table of label targets. It is supposed that all labels
     * in the collection point to the same address (position).
     * 
     * @param labels Collection of labels to be registered.
     * @param position Position in the sequence of the instruction
     * the labels in the collection point to.
     * 
     * @throws NullPointerException if the <code>label</code>
     * parameter is <code>null</code>.
     * @throws IllegalArgumentException if an object in the <code>labels</code>
     * collection is not a Label object.
     */

    public void addAllLabels(Collection<?> labels, int position)
    {
        if (null == labels)
            throw new NullPointerException();

        for (Object item : labels)
        {
            if (!(item instanceof Label))
                throw new IllegalArgumentException(
                    item + " is not a Label object!");

            addLabel((Label) item, position);
        }
    }

    /**
     * Resolves a reference to a label having a specific name from a specific
     * block and returns the most suitable target (label and its position). 
     * The most suitable target is chosen depending on the reference position
     * (see the {@link LabelManager} class comment). If there are several
     * equally possible choices (ambiguity) a warning message is printed.  
     * 
     * @param referenceLabel A Label object that describes a reference to
     * a label that has a specific name from a specific block.
     * @return The most suitable target (label and its position) for the given
     * reference or <code>null</code> if no label having such name is found. 
     * 
     * @throws NullPointerException if the parameter is <code>null</code>.
     */

    public Target resolve(Label referenceLabel)
    {
        if (null == referenceLabel)
            throw new NullPointerException();

        if (!table.containsKey(referenceLabel.getName()))
            return null;

        final List<Target> targets =
            table.get(referenceLabel.getName());

        if (1 == targets.size())
            return targets.get(0);

        final List<TargetDistance> distances =
             new ArrayList<TargetDistance>(targets.size());

        for (int index = 0; index < targets.size(); ++index)
        {
            final Target target = targets.get(index);
            final Label targetLabel = target.getLabel();

            final Distance distance = 
                referenceLabel.getBlockId().getDistance(targetLabel.getBlockId());

            distances.add(new TargetDistance(target, distance));
        }

        Collections.sort(distances);
        printAmbiguousTargets(distances);

        return distances.get(0).target;
    }

    private void printAmbiguousTargets(List<TargetDistance> items)
    {
        final StringBuilder sb = new StringBuilder();

        final TargetDistance chosenItem = items.get(0);
        for (int index = 1; index < items.size(); ++index)
        {
            final TargetDistance item = items.get(index);
            if (!chosenItem.distance.equals(item.distance))
                break;

            if (sb.length() != 0) sb.append(", ");
            sb.append(item.target);
        }

        if (sb.length() == 0)
            return;

        final String MESSAGE_FRMT =
            "Warning: Label %s was chosen, while there are other " +
            "equally possible choices: %s.%n";

        System.out.printf(MESSAGE_FRMT, chosenItem.target, sb);
    }
}
