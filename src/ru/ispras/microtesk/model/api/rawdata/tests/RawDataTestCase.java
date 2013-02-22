/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * RawDataTestCase.java, Oct 17, 2012 2:36:56 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.rawdata.tests;

import java.math.BigInteger;

import ru.ispras.microtesk.model.api.rawdata.RawData;
import ru.ispras.microtesk.model.api.rawdata.RawDataMapping;
import ru.ispras.microtesk.model.api.rawdata.RawDataStore;

import org.junit.Assert;
import org.junit.Test;

import static ru.ispras.microtesk.model.api.rawdata.tests.TestUtils.*;

public class RawDataTestCase
{
    private static final String SEPARATOR =
        "/" + "******************************************************************" + 
        "%n* %-64s*%n" +
        "*******************************************************************" + "/";
    
    @Test
    public void creationTests()
    {
        Trace(String.format(SEPARATOR, "Creation Tests"));
        
        // NOT ALLOWED (assertion)
        /*checkRawData( 
            new Int(0, 0).getRawData(),
            0
        );*/

        checkRawData(
            RawData.valueOf(1, 1),
            1
        );

        checkRawData(
            RawData.valueOf(-1, 32),
            -1
        );

        checkRawData(
            RawData.valueOf(0, 4),
            0
        );

        checkRawData(
            RawData.valueOf(0xFFFFL, 64),
            0xFFFFL
        );
        
        checkRawData(
            RawData.valueOf(0xFFFF0000L, 64),
            0xFFFF0000L
        );
        
        checkRawData(
            RawData.valueOf(0xFFFF0000FFFF0000L, 65),
            0xFFFF0000FFFF0000L
        );
        
        checkRawData(
            RawData.valueOf(0, 33),
            0
        );
        
        checkRawData(
            RawData.valueOf(Integer.valueOf("1110111", 2), 7),
            "1110111"
        );
        
        checkRawData(
            RawData.valueOf(Integer.valueOf("1100101010011", 2), 13),
            "1100101010011"
        );
    }
    
    ////////////////////////////////////////////////////////////////////////////////////
    // COPY CONSTRUCTOR, ASSIGN AND RESET TESTS 
    ////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void copyingTests()
    {
        // Some representative test data (with odd length).
        final String SAMPLE_35BIT = "101"+"10101010"+"10101010"+"10101010"+"10101010";
        
        Trace(String.format(SEPARATOR, "Copying Tests"));
        
        //////////////////////////////////////////////////////////
        // Test case 0: creates an empty data array and assigns data to it.
        
        final RawData rd01 = new RawDataStore(35);

        checkRawData(
            rd01,
            0 
        );
        
        final RawData rd02 = RawData.valueOf(Long.valueOf(SAMPLE_35BIT, 2), 35);
        
        checkRawData(
            rd02,
            SAMPLE_35BIT 
        );
        
        rd01.assign(rd02);
        
        checkRawData(
            rd01,
            SAMPLE_35BIT 
        );
        
        checkRawData(
            rd02,
            SAMPLE_35BIT 
        );
        
        // We call reset to make sure we deal with a copy of the data.
        rd02.reset();
        
        checkRawData(
            rd02,
            0 
        );
        
        checkRawData(
            rd01,
            SAMPLE_35BIT 
        );
        
        // In this test case, we call reset to make sure we deal with a copy of the data.
        rd01.reset();

        checkRawData(
            rd01,
            0 
        );
        
        //////////////////////////////////////////////////////////
        // Test case 1: the assign and reset methods 
        
        final RawData rd11 = RawData.valueOf(Long.valueOf(SAMPLE_35BIT, 2), 35);
        final RawData rd12 = RawData.valueOf(Long.valueOf("11111111111111111", 2), 35);

        rd11.assign(rd12);
        
        checkRawData(
            rd12,
            "00000000000000000011111111111111111" 
        );
        
        // We call reset to make sure we deal with a copy of the data.
        rd12.reset();
        
        checkRawData(
            rd11,
            "00000000000000000011111111111111111" 
        );
            
        checkRawData(
            rd12,
            0 
        );

        // In this test case, we call reset to make sure we deal with a copy of the data.
        rd11.reset();

        checkRawData(
            rd11,
            0 
        );

        //////////////////////////////////////////////////////////
        // Test case 2: the copy constructor

        final RawData rd21 = RawData.valueOf(Long.valueOf(SAMPLE_35BIT, 2), 35);
        final RawData rd22 = new RawDataStore(rd21);
        
        checkRawData(
            rd21,
            SAMPLE_35BIT 
        );
        
        checkRawData(
            rd22,
            SAMPLE_35BIT 
        );
        
        // We call reset to make sure we deal with a copy of the data.
        rd21.reset();
        
        checkRawData(
            rd21,
            0 
        );
            
        checkRawData(
            rd22,
            SAMPLE_35BIT 
        );
        
        // We call reset to make sure we deal with a copy of the data.
        rd22.reset();
        
        checkRawData(
            rd21,
            0 
        );
            
        checkRawData(
            rd22,
            0 
        );
        
        //////////////////////////////////////////////////////////
        // Test case 3: assignment with truncation
        
        // NOTE: NO IMPLICIT CONVERSIONS ARE ALLOWED.
        // TO MAKE OPERATIONS IWTH DIFFERENT TYPES, WE NEED TO COERCE THEN EXPLICITLY.
        
        /*
        final RawData rd31 = new RawDataStore(27);

        checkRawData(
            rd31,
            0 
        );
        
        final RawData rd32 = new Int(Long.valueOf(SAMPLE_35BIT, 2), 35).getRawData();
        
        checkRawData(
            rd32,
            SAMPLE_35BIT 
        );
        
        rd31.assign(rd32);
        
        checkRawData(
            rd31,
            "010"+"10101010"+"10101010"+"10101010" 
        );
        
        checkRawData(
            rd32,
            SAMPLE_35BIT 
        );
        */
    }
    
    ////////////////////////////////////////////////////////////////////////////////////
    // READING MAPPING TESTS 
    ////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void mappingReadingTests()
    {
        // Some representative test data (with odd length).
        final String SAMPLE_35BIT_BIN_STR = "101"+"10101010"+"10101010"+"10101010"+"10101010";
        
        Trace(String.format(SEPARATOR, "Mapping Reading Tests"));
        
        Trace(RawData.valueOf(0xFF, 32).toBinString());
        Trace(RawData.valueOf(0xFF00FF00, 32).toBinString());
        
        checkRawData(
           new RawDataMapping(RawData.valueOf(-1, 32), 0, 32),
           -1
        );

        checkRawData(
            new RawDataMapping(RawData.valueOf(0xFF00FF00, 32), 0, 32),
            0xFF00FF00
        );

        // NOT ALLOWED (ASSERTION)
        /*checkRawData(
            new RawDataMapping(RawData.valueOf(-1, 32), 0, 0),
            ""   
        );*/
        
        ////////////////////////////////////////////////////////////
        // Test for multiple of 8 data arrays (no incomplete bytes).

        checkRawData(
            new RawDataMapping(RawData.valueOf(0xFF00FF00, 32), 0, 8),
            0x00
        );

        checkRawData(
            new RawDataMapping(RawData.valueOf(0xFF00FF00, 32), 8, 8),
            0xFF
        );
        
        checkRawData(
            new RawDataMapping(RawData.valueOf(0xFF00FF00, 32), 16, 8),
            0x00
        );
        
        checkRawData(
            new RawDataMapping(RawData.valueOf(0xFF00FF00, 32), 24, 8),
            0xFF
        );
        
        
        checkRawData(
            new RawDataMapping(RawData.valueOf(0xFF00FF00, 32), 4, 8),
            0xF0
        );
        
        checkRawData(
            new RawDataMapping(RawData.valueOf(0xFF00FF00, 32), 20, 8),
            0xF0
        );
       
        checkRawData(
            new RawDataMapping(RawData.valueOf(0xFF00FF00, 32), 4, 16),
            0x0FF0
        );
        
        checkRawData(
            new RawDataMapping(RawData.valueOf(0xFF00FF00, 32), 12, 16),
            0xF00F
        );
        
        ////////////////////////////////////////////////////////////
        // Test for data arrays with an incomplete high byte (size is not multiple of 8)

        checkRawData(
            new RawDataMapping(RawData.valueOf(-1L, 35), 0, 35),
            -1L
        );

        checkRawData(
            new RawDataMapping(RawData.valueOf(Long.valueOf("11" + SAMPLE_35BIT_BIN_STR, 2), 35), 0, 35),
            SAMPLE_35BIT_BIN_STR 
        );

        checkRawData(
            new RawDataMapping(RawData.valueOf(Long.valueOf(SAMPLE_35BIT_BIN_STR, 2), 35), 27, 8),
            "101"+"10101" 
        );

        checkRawData(
            new RawDataMapping(RawData.valueOf(Long.valueOf(SAMPLE_35BIT_BIN_STR, 2), 35), 24, 11),
            "101"+"10101010" 
        );

        checkRawData(
            new RawDataMapping(RawData.valueOf(Long.valueOf(SAMPLE_35BIT_BIN_STR, 2), 35), 23, 11),
            "01"+"101010101" 
        );

        checkRawData(
            new RawDataMapping(RawData.valueOf(Long.valueOf("11" + SAMPLE_35BIT_BIN_STR, 2), 35), 1, 5),
            "10101" 
        );

        checkRawData(
            new RawDataMapping(RawData.valueOf(Long.valueOf("11" + SAMPLE_35BIT_BIN_STR, 2), 35), 2, 15),
            "0"+"10101010"+"101010" 
        );
        
        checkRawData(
            new RawDataMapping(RawData.valueOf(Long.valueOf("11" + SAMPLE_35BIT_BIN_STR, 2), 35), 8, 4),
            "1010" 
        );
        
        checkRawData(
            new RawDataMapping(RawData.valueOf(0xFF00FF00, 29), 5, 23),
            "11110000000011111111000"
        );
        
        checkRawData(
            new RawDataMapping(RawData.valueOf(Long.valueOf("11" + SAMPLE_35BIT_BIN_STR, 2), 35), 25, 10),
            "101"+"1010101" 
        );
        
        checkRawData(
            new RawDataMapping(RawData.valueOf(0x00FFFFFF, 32), 22, 10),
            "0000000011" 
        );        
    }
    
    ////////////////////////////////////////////////////////////////////////////////////
    // WRITING MAPPING TESTS
    ////////////////////////////////////////////////////////////////////////////////////
    
    @Test
    public void mappingWritingTests()
    {
        // Some representative test data (with odd length).
        final String SAMPLE_35BIT_BIN_STR = "101"+"10101010"+"10101010"+"10101010"+"10101010";
        
        Trace(String.format(SEPARATOR, "Mapping Writing Tests"));
        
        //////////////////////////////////////////////////////////
        // Test Case 1: Mapping size equals source size.
        
        final RawData rd01 = RawData.valueOf(Long.valueOf(SAMPLE_35BIT_BIN_STR, 2), 35);
        
        checkRawData(
            rd01,
            SAMPLE_35BIT_BIN_STR 
        );
        
        final RawData rd02 = RawData.valueOf(0xF0F00F, 35);
        
        checkRawData(
            rd02,
            0xF0F00F 
        );
        
        final RawData rdm01 = new RawDataMapping(rd01);
        
        checkRawData(
            rdm01,
            SAMPLE_35BIT_BIN_STR 
        );
        
        rdm01.assign(rd02);
        
        checkRawData(
            rd01,
            0xF0F00F 
        );
          
        checkRawData(
            rd02,
            0xF0F00F 
        );
        
        checkRawData(
            rdm01,
            0xF0F00F 
        );
        
        rd02.reset();

        checkRawData(
            rd01,
            0xF0F00F 
        );

        checkRawData(
            rd02,
            0
        );

        checkRawData(
            rdm01,
            0xF0F00F 
        );
        
        rdm01.reset();
        
        checkRawData(
            rd01,
            0 
        );

        checkRawData(
            rd02,
            0
        );

        checkRawData(
            rdm01,
            0 
        );

        //////////////////////////////////////////////////////////
        // Test Case 2: Mapped region is located in the middle of the source data
        // array (the offset and the mapping length is multiple of 8).
        
        final RawData rd11 = RawData.valueOf(Long.valueOf(SAMPLE_35BIT_BIN_STR, 2), 35);
        
        checkRawData(
            rd11,
            SAMPLE_35BIT_BIN_STR 
        );
        
        final RawData rd12 = RawData.valueOf(0xFF, 8);
        
        checkRawData(
            rd12,
            0xFF 
        );
        
        final RawData rdm11 = new RawDataMapping(rd11, 8, 8);
        
        checkRawData(
            rdm11,
            "10101010" 
        );
        
        rdm11.assign(rd12);
        
        checkRawData(
            rdm11,
            0xFF 
        );
        
        checkRawData(
           rd11,
           "10110101010101010101111111110101010" 
        );

        //////////////////////////////////////////////////////////
        // Test Case 3: Mapped region is located in the middle of the source data
        // array (the offset and the mapping length is multiple of 8).
        
        final RawData rd31 = RawData.valueOf(Long.valueOf(SAMPLE_35BIT_BIN_STR, 2), 35);
        
        checkRawData(
            rd31,
            SAMPLE_35BIT_BIN_STR 
        );
        
        final RawData rd32 = RawData.valueOf(0xFFFF, 11);
        
        checkRawData(
            rd32,
            0xFFFF 
        );
        
        final RawData rdm31 = new RawDataMapping(rd31, 3, 11);
        
        checkRawData(
            rdm31,
            "10101010101" 
        );
        
        rdm31.assign(rd32);

        checkRawData(
            rd31,
            "10110101010101010101011111111111010" 
        );
                
        checkRawData(
            rd32,
            "11111111111" 
        );

        checkRawData(
            rdm31,
            0xFFFF 
        );
        
        rdm31.reset();
        
        checkRawData(
            rd31,
            "10110101010101010101000000000000010"
        );
                   
        checkRawData(
            rd32,
            "11111111111" 
        );

        checkRawData(
            rdm31,
            0 
        );

        
        //////////////////////////////////////////////////////////
        // Test Case 4: Mapped region is located in the middle of the source data
        // array (the offset is multiple of 8 and the mapping length is not multiple of 8).

        final RawData rd41 = RawData.valueOf(Long.valueOf(SAMPLE_35BIT_BIN_STR, 2), 35);

        checkRawData(
            rd41,
            SAMPLE_35BIT_BIN_STR 
        );

        final RawData rd42 = RawData.valueOf(0xFF, 5);

        checkRawData(
            rd42,
            0xFF 
        );

        final RawData rdm41 = new RawDataMapping(rd41, 8, 5);

        checkRawData(
            rdm41,
            "01010" 
        );

        rdm41.assign(rd42);

        checkRawData(
            rd41,
            "10110101010101010101011111110101010" 
        );

        checkRawData(
            rd42,
            "11111" 
        );
        
        checkRawData(
            rdm41,
            "11111" 
        );
        
        /*
        rdm31.reset();
        
        checkRawData(
            rd31,
            "10110101010101010101000000000000010"
        );
                   
        checkRawData(
            rd32,
            "11111111111" 
        );

        checkRawData(
            rdm31,
            0 
        );*/
        
        //////////////////////////////////////////////////////////
        // Test Case 5: Mapped region is located in the middle of the source data
        // array (the offset is multiple of 8 and the mapping length is not multiple of 8).

        final RawData rd51 = RawData.valueOf(Long.valueOf(SAMPLE_35BIT_BIN_STR, 2), 35);

        checkRawData(
            rd51,
            SAMPLE_35BIT_BIN_STR 
        );

        final RawData rd52 = RawData.valueOf(0, 5);

        checkRawData(
            rd52,
            0 
        );

        final RawData rdm51 = new RawDataMapping(rd51, 8, 5);

        checkRawData(
            rdm51,
            "01010"
        );

        rdm51.assign(rd52);

        checkRawData(
            rd51,
            "10110101010101010101010000010101010" 
        );

        checkRawData(
            rd52,
            0 
        );
        
        checkRawData(
            rdm51,
            0 
        );
        
        //////////////////////////////////////////////////////////
        // Test Case 6: Mapped region is located in the middle of the source data
        // array (the offset is multiple of 8 and the mapping length is not multiple of 8).

        final RawData rd61 = RawData.valueOf(0, 35);

        checkRawData(
            rd61,
            0 
        );

        final RawData rd62 = RawData.valueOf(0xFF, 5);

        checkRawData(
            rd62,
            0xFF 
        );

        final RawData rdm61 = new RawDataMapping(rd61, 1, 5);

        checkRawData(
            rdm61,
            "00000"
        );

        rdm61.assign(rd62);

        checkRawData(
            rd61,
            "00000000000000000000000000000111110" 
        );

        checkRawData(
            rd62,
            0xFF 
        );
        
        checkRawData(
            rdm61,
            0xFF 
        );
    }

    @Test
    public void multiLayerMappingTests()
    {
        // TODO IMPLEMENT
        
    }

    @Test
    public void valueOfTests()
    {
        Assert.assertTrue(
            RawData.valueOf("0101010011100110000").toBinString().equals("0101010011100110000"));
        
        Assert.assertTrue(
            RawData.valueOf("0101010011100110000").toBinString().equals("0101010011100110000"));
        
        Assert.assertTrue(
            RawData.valueOf("11111", 8).toBinString().equals("00011111"));
                
        Assert.assertTrue(
            RawData.valueOf("111011", 8).toBinString().equals("00111011"));

        Assert.assertTrue(
            RawData.valueOf("11111111111011", 8).toBinString().equals("11111011"));
    }

    @Test
    public void toByteArrayTests()
    {
        // TODO: MORE COMPLEX TESTS!!!
        
        final RawData rd = RawData.valueOf("01011100011010101");
        BigInteger bi = new BigInteger(rd.toByteArray());

        System.out.println(bi.toString(2));
        
    }
}
