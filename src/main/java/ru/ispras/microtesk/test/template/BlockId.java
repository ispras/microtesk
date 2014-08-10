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

final class BlockId
{
    private final List<Integer> indexes;
    private int childCount;

    /**
     * Constructs a identifier for a root block.
     */

    public BlockId()
    {
        this(Collections.singletonList(1));
    }

    private BlockId(List<Integer> indexes)
    {
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

        return new BlockId(childIndexes);
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
        return indexes.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;

        if (getClass() != obj.getClass())
            return false;

        final BlockId other = (BlockId) obj;
        return indexes.equals(other.indexes);
    }
}
