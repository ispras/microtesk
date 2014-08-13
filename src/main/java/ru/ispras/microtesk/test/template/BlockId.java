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

        return isEqual(
            indexes, parentId.indexes, parentId.indexes.size());
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

        return isEqual(
            indexes, childId.indexes, indexes.size());
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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;

        for (int index : indexes)
            result = prime * result + index;    

        return result;
    }

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

        return isEqual(indexes, other.indexes, indexes.size());
    }

    private static boolean isEqual(List<Integer> a, List<Integer> b, int count)
    {
        assert a.size() <= count;
        assert b.size() <= count;

        for (int index = 0; index < count; ++index)
            if (a.get(index) != b.get(index))
                return false;

        return true;
    }
}
