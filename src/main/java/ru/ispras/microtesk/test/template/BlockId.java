/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BlockId.java, Aug 7, 2014 2:53:27 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.test.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The BlockId class describes unique identifiers for instruction call blocks.
 * The identifiers help uniquely identify elements that belong to different
 * blocks, but have the same name. The identifier reflects the hierarchical
 * structure of instruction call blocks. 
 * 
 * @author Andrei Tatarnikov
 */

public final class BlockId
{
    /**
     * The Distance class describe a distance between blocks.
     * I.e. the path from one block to another including directions.
     * First we go up (by the specified number of steps, if needed)
     * and then go down (by the specified number of steps, if needed).
     * 
     * @author Andrei Tatarnikov
     */

    public static final class Distance
    {
        private final int up;
        private final int down;

        private Distance(int up, int down)
        {
            this.up = up;
            this.down = down;
        }

        public int getUp()    { return up; }
        public int getDown()  { return down; }
        public int getTotal() { return up + down; }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;

            result = prime * result + up;
            result = prime * result + down;

            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;

            if (getClass() != obj.getClass())
                return false;

            final Distance other = (Distance) obj;
            return (up == other.up) && (down == other.down);
        }

        @Override
        public String toString()
        {
            return String.format(
                "Distance [up=%d, down=%d]", up, down);
        }
    }

    private final BlockId parent;
    private final List<Integer> indexes;
    private int childCount;

    /**
     * Constructs a identifier for a root block.
     */

    public BlockId()
    {
        this(null, Collections.singletonList(1));
    }

    private BlockId(BlockId parent, List<Integer> indexes)
    {
        this.parent = parent;
        this.indexes = indexes;
        this.childCount = 0;
    }

    /**
     * Creates an new identifier for a child block.
     * 
     * @return Next child identifier.
     */

    public BlockId nextChildId()
    {
        childCount++;

        final List<Integer> childIndexes =
            new ArrayList<Integer>(indexes.size() + 1);

        childIndexes.addAll(indexes);
        childIndexes.add(childCount);

        return new BlockId(this, childIndexes);
    }

    /**
     * Returns the identifier of the parent block or <code>null</code>
     * if there is no parent block (the current one is root).
     * 
     * @return Parent identifier or <code>null</code> if there is no parent.
     */

    public BlockId parentId()
    {
        return parent;
    }

    /**
     * Checks whether the specified block identifier refers to
     * a block which is a parent (not necessary immediate) of
     * the block marked by the current identifier. Note: a block
     * is not a parent to itself.
     * 
     * @param parentId Identifier of the candidate parent block.
     * @return <code>true</code> if the specified block identifier
     * refers to a parent block or <code>false<code> otherwise.
     * 
     * @throws NullPointerException if the parameter is <code>null<code>.
     */

    public boolean isParent(BlockId parentId)
    {
        if (null == parentId)
            throw new NullPointerException();

        if (parentId.indexes.size() >= indexes.size())
            return false;

        return parentId.indexes.size() == 
            getEqualSize(indexes, parentId.indexes);
    }

    /**
     * Checks whether the specified block identifier refers to
     * a block which is a child (not necessary immediate) of
     * the block marked by the current identifier. Note: a block
     * is not a child to itself.
     * 
     * @param childId Identifier of the candidate child block.
     * @return <code>true</code> if the specified block identifier
     * refers to a child block or <code>false<code> otherwise.
     * 
     * @throws NullPointerException if the parameter is <code>null<code>.
     */

    public boolean isChild(BlockId childId)
    {
        if (null == childId)
            throw new NullPointerException();

        if (childId.indexes.size() <= indexes.size())
            return false;

        return indexes.size() == 
            getEqualSize(indexes, childId.indexes);
    }
    
    /**
     * Returns the depth of nesting for the block described by
     * the current identifier.
     * 
     * @return Nesting depth of the block described by the identifier.  
     */

    public int getDepth()
    {
       return indexes.size() - 1; 
    }

    /**
     * Calculates the distance between the current block
     * and the target block (the path you need to pass to
     * get from the current one to the target one).
     * 
     * @param target Target block.
     * @return Distance from the current block to the target block.
     * 
     * @throws NullPointerException if the parameter is <code>null<code>.
     */

    public Distance getDistance(BlockId target)
    {
        if (null == target)
            throw new NullPointerException();

        final int forkDepth =
           getEqualSize(indexes, target.indexes) - 1;

        final int up = getDepth() - forkDepth;
        final int down = target.getDepth() - forkDepth; 

        return new Distance(up, down);
    }

    /**
     * Returns textual representation of the identifier.
     * 
     * @return Textual representation of the identifier.
     */

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        for (Integer index : indexes)
        {
            sb.append('_');
            sb.append(index);
        }
        return sb.toString();
    }

    /**
     * Calculates the hash code of the block identifier based on 
     * the elements of the indexes list which describes the 
     * relative location of the block. 
     * 
     * @return Hash code for the block identifier.  
     */

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;

        for (int index : indexes)
            result = prime * result + index;    

        return result;
    }

    /**
     * Checks whether the specified object is a block
     * identifier that is equal to the current one. 
     * 
     * @return <code>true</code> the object refers to an equal block
     * identifier or <code>false</code> other wise.
     */

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;

        if (getClass() != obj.getClass())
            return false;

        final BlockId other = (BlockId) obj;

        if (indexes.size() != other.indexes.size())
            return false;

        return indexes.size() == 
            getEqualSize(indexes, other.indexes);
    }

    /**
     * Returns the size of a common sequence that presents in both
     * lists and starts from the 0th position. E.g. two lists are equal,
     * if their sizes are equal and the size of the common sequence equals
     * the size of the lists. 
     * 
     * @param a First list.
     * @param b Second list.
     * @return Size of a common sequence starting from the 0th position.
     */

    private static int getEqualSize(List<Integer> a, List<Integer> b)
    {
        final int maxEqualSize = Math.min(a.size(), b.size());

        int index = 0;
        while(index < maxEqualSize && a.get(index) == b.get(index))
            index++;

        assert index > 1:
            "Invariant: not empty, first elements (0-index) equal.";

        return index;
    }
}
