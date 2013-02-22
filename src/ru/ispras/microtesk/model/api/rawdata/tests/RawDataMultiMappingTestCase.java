/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * RawDataMultiMappingTestCase.java, Nov 13, 2012 4:35:54 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.rawdata.tests;

import org.junit.Test;

import ru.ispras.microtesk.model.api.rawdata.RawData;
import ru.ispras.microtesk.model.api.rawdata.RawDataMultiMapping;

import static ru.ispras.microtesk.model.api.rawdata.tests.TestUtils.*;

public class RawDataMultiMappingTestCase
{
    @Test
    public void test1()
    {
        final RawData rd1 = RawData.valueOf("11110000");
        final RawData rd2 = RawData.valueOf("00110011");
        final RawData rd3 = RawData.valueOf("11000000");

        final RawData rd = new RawDataMultiMapping(new RawData[] {rd1, rd2, rd3 } );

        checkRawData(
            rd,
            "11000000" + "00110011" + "11110000"
            );

        rd.assign(
            RawData.valueOf("11000011" + "11011011" + "10100001")
            );

        checkRawData(
            rd,
            "11000011" + "11011011" + "10100001"
            );

        checkRawData(
            rd1,
            "10100001"
            );

        checkRawData(
            rd2,
            "11011011"
            );

        checkRawData(
            rd3,
            "11000011"
            );
    }
    
    @Test
    public void test2()
    {
        final RawData rd1 = RawData.valueOf("0110");
        final RawData rd2 = RawData.valueOf("1001");

        final RawData rd = new RawDataMultiMapping(new RawData[] {rd1, rd2 } );

        checkRawData(
            rd,
            "1001" + "0110"
            );

        rd.assign(
            RawData.valueOf("1111" + "0000")
            );

        checkRawData(
            rd,
            "1111" + "0000"
            );

        checkRawData(
            rd1,
            "0000"
            );

        checkRawData(
            rd2,
            "1111"
            );
    }
    
    @Test
    public void test21()
    {
        final RawData rd1 = RawData.valueOf("011");
        final RawData rd2 = RawData.valueOf("1001");

        final RawData rd = new RawDataMultiMapping(new RawData[] {rd1, rd2 } );

        checkRawData(
            rd,
            "1001" + "011"
            );

        final RawData rdX = RawData.valueOf("1111" + "000");
        checkRawData(rdX, "1111" + "000");
        
        rd.assign(
                rdX
            );

        checkRawData(
            rd,
            "1111" + "000"
            );

        checkRawData(
            rd1,
            "000"
            );

        checkRawData(
            rd2,
            "1111"
            );
    }
    
    @Test
    public void test3()
    {
        final RawData rd1 = RawData.valueOf("0010000110");
        final RawData rd2 = RawData.valueOf("00001001");
        
        final RawData rd = new RawDataMultiMapping(new RawData[] {rd1, rd2 } );
        System.out.println(rd.toBinString());
        
        checkRawData(
            rd,
            "00001001" + "0010000110"
            );
        
        /*
        rd.assign(
            RawData.valueOf("01010101" + "1100110011")
            );
        
        checkRawData(
            rd,
            "01010101" + "1100110011"
            );
        */
        
        final RawData rdX = RawData.valueOf("11110011" + "1111110011");
        
        rd.assign(rdX);
            
        checkRawData(
            rd,
            "11110011" + "1111110011"
            );
    }
    
    @Test
    public void test4()
    {
        final RawData rd1 = RawData.valueOf("1100000000");
        final RawData rd2 = RawData.valueOf("00");
        final RawData rd3 = RawData.valueOf("111");
        final RawData rd4 = RawData.valueOf("1100011100");
        
        final RawData rd = new RawDataMultiMapping(new RawData[] {rd1, rd2, rd3, rd4 } );
        System.out.println(rd.toBinString());
        
        checkRawData(
            rd,
            "1100011100" + "111" + "00" + "1100000000"
            );
    }
    
    @Test
    public void test5()
    {
        final RawData rd1 = RawData.valueOf("1");
        final RawData rd2 = RawData.valueOf("0");
        final RawData rd3 = RawData.valueOf("0");
        final RawData rd4 = RawData.valueOf("1");
        final RawData rd5 = RawData.valueOf("1");
        
        final RawData rd = new RawDataMultiMapping(new RawData[] {rd1, rd2, rd3, rd4, rd5 } );
        System.out.println(rd.toBinString());
        
        checkRawData(
            rd,
            "11001"
            );
        
       rd.assign(
            RawData.valueOf("00110")
            );
        
        checkRawData(
            rd,
            "00110"
            );
        
        checkRawData(rd1, "0");
        checkRawData(rd2, "1");
        checkRawData(rd3, "1");
        checkRawData(rd4, "0");
        checkRawData(rd5, "0");

    }
    
    @Test
    public void test6()
    {
        final RawData rd1 = RawData.valueOf("111100001");
        final RawData rd2 = RawData.valueOf("0000111100");
        final RawData rd3 = RawData.valueOf("00110110101");
        final RawData rd4 = RawData.valueOf("11110000");
        final RawData rd5 = RawData.valueOf("111");
        
        final RawData rd = new RawDataMultiMapping(new RawData[] {rd1, rd2, rd3, rd4, rd5 } );
        System.out.println(rd.toBinString());
        
        checkRawData(
            rd,
            "111" + "11110000" + "00110110101" + "0000111100" + "111100001"
            );
    }

}
