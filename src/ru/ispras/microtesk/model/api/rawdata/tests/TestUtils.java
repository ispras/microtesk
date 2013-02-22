/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TestUtils.java, Oct 19, 2012 11:42:28 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.rawdata.tests;

import ru.ispras.microtesk.model.api.rawdata.RawData;
import org.junit.Assert;

public final class TestUtils
{
    private TestUtils() {}
    
    private static final boolean OUTPUT_DEBUG_STRINGS = true;

    public static void Trace(String text)
    {
        if (OUTPUT_DEBUG_STRINGS)
            System.out.println(text);
    }

    public static void checkRawData(RawData data, String expected)
    {
        final String dataString = data.toBinString();
        Trace(String.format("Data:     %32s%nExpected: %32s", dataString, expected));

        Assert.assertTrue(
            String.format("Values do not match. %s (data) != %s (expected)", dataString, expected),
            expected.equals(dataString)
        );
    }

    public static void checkRawData(RawData data, int expected)
    {
        checkRawData(data, toBinString(expected, data.getBitSize()));
    }

    public static void checkRawData(RawData data, long expected)
    {
        checkRawData(data, TestUtils.toBinString(expected, data.getBitSize()));
    }

    public static String toBinString(int value, int bitSize)
    {
        final String binstr =  Integer.toBinaryString(value);

        if (binstr.length() > bitSize)
            return binstr.substring(binstr.length()-bitSize, binstr.length());

        final int count = bitSize - binstr.length();
        return (count > 0) ? String.format("%0" + count + "d", 0) + binstr: binstr;
    }
    
    public static String toBinString(long value, int bitSize)
    {
        final String binstr =  Long.toBinaryString(value);

        if (binstr.length() > bitSize)
            return binstr.substring(binstr.length()-bitSize, binstr.length());

        final int count = bitSize - binstr.length();
        return (count > 0) ? String.format("%0" + count + "d", 0) + binstr: binstr;
    }
}
