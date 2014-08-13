/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BlockIdTestCase.java, Aug 13, 2014 6:26:12 PM Andrei Tatarnikov
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

import static org.junit.Assert.*;

import org.junit.Test;

public class BlockIdTestCase
{
    @Test
    public void testEquals()
    {
        final BlockId root1 = new BlockId();
        final BlockId root2 = new BlockId();

        assertEquals(root1, root2);

        final BlockId child11 = root1.nextChildId();
        final BlockId child21 = root2.nextChildId();

        assertEquals(child11, child21);
        assertNotSame(child11, child21);

        assertEquals(child11.parentId(), child21.parentId());
        assertNotSame(child11.parentId(), child21.parentId());

        final BlockId child12 = root1.nextChildId();
        final BlockId child22 = root2.nextChildId();

        assertEquals(child12.parentId(), child22.parentId());
        assertNotSame(child12.parentId(), child22.parentId());

        assertEquals(child12.parentId(), child11.parentId());
        assertEquals(child22.parentId(), child21.parentId());

        final BlockId child121 = child12.nextChildId();
        final BlockId child221 = child22.nextChildId();

        assertEquals(child121, child221);
        assertEquals(child121.parentId().parentId(), child221.parentId().parentId());
        assertEquals(child121.parentId().parentId(), root2);
        assertEquals(child221.parentId().parentId(), root1);

    }
    
    @Test
    public void testParentChild()
    {
        // TODO
    }
    
    @Test
    public void testDistance()
    {
        // TODO
    }
}

