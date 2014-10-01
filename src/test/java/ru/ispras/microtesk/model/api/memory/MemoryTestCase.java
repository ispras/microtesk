/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MemoryTestCase.java, Oct 1, 2014 11:46:05 AM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api.memory;

import static org.junit.Assert.*;

import org.junit.Test;

import ru.ispras.microtesk.model.api.type.Type;

public class MemoryTestCase
{
    @Test
    public void testConstruction()
    {
        testBlockCount(1, 1);
        testBlockCount(1024, 1);
        testBlockCount(4096, 1);
        testBlockCount(4097, 2);
        testBlockCount(6000, 2);
        testBlockCount(8192, 2);
        testBlockCount(8193, 3);
        testBlockCount(12288, 3);
        testBlockCount(12289, 4);

        for (int powerOfTwo = 1; powerOfTwo < 32; powerOfTwo++)
        {
            final int length = (int) Math.pow(2, powerOfTwo);
            final int blockCount = 
                length / Memory.BLOCK_SIZE +
                (0 == length % Memory.BLOCK_SIZE ? 0 : 1);

            testBlockCount(length, blockCount);
        }
    }

    private static void testBlockCount(int length, int expectedBlockCount)
    {
        final Memory mem = Memory.newMEM("M", Type.CARD(8), length);
        assertEquals(length, mem.getLength());
        assertEquals(expectedBlockCount, mem.getBlockCount());
    }

    @Test
    public void testAccess()
    {
        final Type BYTE = Type.CARD(8);

        final Memory mem = Memory.newMEM("M", BYTE, 8200);
        assertEquals(8200, mem.getLength());
        assertEquals(3, mem.getBlockCount());

        mem.access();
        mem.access(4095);
        mem.access(4096);
        mem.access(4097);
        mem.access(8191);
        mem.access(8192);
        mem.access(8193);
        mem.access(8199);
    }
}
