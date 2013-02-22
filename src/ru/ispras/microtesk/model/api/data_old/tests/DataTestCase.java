/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * $Id: DataTestCase.java, Oct 17, 2012 2:36:01 PM Andrei Tatarnikov Exp $
 */

package ru.ispras.microtesk.model.api.data_old.tests;

import ru.ispras.microtesk.model.api.data_old.Data;
import ru.ispras.microtesk.model.api.data_old.Int;
import org.junit.Test;

import static ru.ispras.microtesk.model.api.rawdata.tests.TestUtils.*;

public class DataTestCase
{
    @Test
    public void testNOT()
    {
        final Data i = new Int(0x00FF00FF00L, 35);
        Trace(i.toBinString());

        final Data ii = i.bitNOT();
        Trace(ii.toBinString());

        checkRawData(ii.getRawData(), 0xFF00FF00FFL);
    }

    @Test
    public void testBitAND()
    {
        final Data i1 = new Int(0xFF00FF00, 35);
        Trace(i1.toBinString());

        final Data i2 = new Int(0x0000FF00, 35);
        Trace(i2.toBinString());

        final Data ii = i1.bitAND(i2);
        Trace(ii.toBinString());

        checkRawData(ii.getRawData(), 0x0000FF00);
    }
    
    @Test
    public void testBitOR()
    {
        final Data i1 = new Int(0xFF00FF00, 35);
        Trace(i1.toBinString());

        final Data i2 = new Int(0x0000FF00, 35);
        Trace(i2.toBinString());

        final Data ii = i1.bitOR(i2);
        Trace(ii.toBinString());
        
        checkRawData(ii.getRawData(), 0xFF00FF00);
    }

    @Test
    public void testBitXOR()
    {
        final Data i1 = new Int(Integer.valueOf("0101", 2), 9);
        Trace(i1.toBinString());

        final Data i2 = new Int(Integer.valueOf("0011", 2), 9);
        Trace(i2.toBinString());

        final Data ii = i1.bitXOR(i2);
        Trace(ii.toBinString());

        checkRawData(ii.getRawData(), Integer.valueOf("0110", 2));
    }

    @Test
    public void testShiftLeft()
    {
        final Data i = new Int(0xFFFF, 32);
        Trace(i.toBinString());

        final Data ii = i.shiftLeft(3);
        Trace(ii.toBinString());

        checkRawData(ii.getRawData(), Integer.valueOf("00000000000001111111111111111000", 2));
    }

    @Test
    public void testShiftRight()
    {
        final Data i = new Int(0xFFFF, 32);
        Trace(i.toBinString());

        final Data ii = i.shiftRight(3);
        Trace(ii.toBinString());

        checkRawData(ii.getRawData(), Integer.valueOf("00000000000000000001111111111111", 2));
    } 

    @Test
    public void testRotateLeft()
    {
        final Data i = new Int( Integer.valueOf("111000111000", 2), 12);
        Trace(i.toBinString());

        final Data ii = i.rotateLeft(5);
        Trace(ii.toBinString());

        checkRawData(ii.getRawData(), Integer.valueOf("011100011100", 2));
    }

    @Test
    public void testRotateRight()
    {
        final Data i = new Int( Integer.valueOf("111000111000", 2), 12);
        Trace(i.toBinString());

        final Data ii = i.rotateRight(5);
        Trace(ii.toBinString());

        checkRawData(ii.getRawData(), Integer.valueOf("110001110001", 2));
    }
}
