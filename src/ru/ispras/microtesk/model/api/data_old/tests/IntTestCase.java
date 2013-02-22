package ru.ispras.microtesk.model.api.data_old.tests;

import ru.ispras.microtesk.model.api.data_old.Int;

import org.junit.Test;

import static ru.ispras.microtesk.model.api.rawdata.tests.TestUtils.*;

public class IntTestCase
{   
   /* 
    private void checkDataFormat(int size, int value)
    {
        Int i = new Int(size);
        i.assign(value);
       
        // System.out.println(i.toBinString());
        // System.out.println(intToBinString(value, size));

        assertTrue(
            String.format("Wrong data format: %x", value),
            i.toBinString().equals(intToBinString(value, size))
        );
    }
    
    @Test
    public void testDataFormat()
    {
        for (int i = 1; i <= 128; ++i)
        {
            checkDataFormat(i, 0);
            checkDataFormat(i, -1);
            checkDataFormat(i, Integer.MAX_VALUE);
            checkDataFormat(i, Integer.MIN_VALUE);

            checkDataFormat(i, 8);
            checkDataFormat(i, 16);
            checkDataFormat(i, 32);
            checkDataFormat(i, 64);

            checkDataFormat(i, 0xFFFF);
        }
    }
*/

    @Test
    public void test()
    {
        System.out.println(new Int(0xFFFFFFFFFFFFFFFFL, 64).intValue());
        System.out.println(new Int(-1, 32).intValue());
        System.out.println(new Int(-1, 32).toBinString());
        System.out.println(new Int(234, 32).intValue());
        
        
        
       /* Int i = new Int(64);
        i.assign(77);

        System.out.println(i.getBuffer().toBinString());
        
        BigInteger bi = new BigInteger("77");
        
        System.out.println(bi.toString(2));
        System.out.println(bi.shiftRight(2).toString(2));
        
        BitVector bv = new BitVector(bi.bitLength());
        byte[] ba =  bi.toByteArray();
        
        for (int j = 0; j < ba.length; j++)
        {
            bv.setByte(j, (char)ba[j]);
        }
        
        System.out.println(bv.toBinString());
        
        
        BigInteger bi2 = new BigInteger("1111111", 2);
        
        System.out.println(bi2.toString(2));
        System.out.println(bi2.bitCount());
        System.out.println(bi2.bitLength());
        
        System.out.println(bi2.shiftRight(2).toString(2));
        System.out.println(bi2.shiftRight(2).bitCount());
        System.out.println(bi2.shiftRight(2).bitLength());
        
        //fail("Not yet implemented");
         * 
         */

        /*
        BigInteger bi1 = new BigInteger("-3");
        System.out.println(bi1.toString(2));
        
        BigInteger bi2 = new BigInteger("5");
        System.out.println(bi2.toString(2));
        
        BigInteger bi3 = bi1.add(bi2);        
        System.out.println(bi3.toString(2));
        
        Int i1 = new Int(32);
        i1.assign(-3);
        System.out.println(i1.toBinString());
        
        Int i2 = new Int(32);
        i2.assign(5);
        System.out.println(i2.toBinString());
        
        i1.add(i2);
        System.out.println(i1.toBinString());*/
        
        //BigInteger.valueOf(val)
        
    }
    
    /*public void print(Data d)
    {
        System.out.println("Bit size: " + d.getLocation().getBitSize());
        System.out.println("Byte size: " + d.getLocation().getByteSize());
        System.out.println(d.toBinString());
    }*/
    
    @Test
    public void testLocations()
    {
        
        
        Int i = new Int(0xF0F, 16);
                
        System.out.println(i.toBinString());
        System.out.println(toBinString(0xF0f, 16));
        
       /* print(new Data(i, 3, 6));
        print(new Data(i, 2, 8));
        print(new Data(i, 0, 12));
        print(new Data(i, 9, 6));*/
        
        
        
    }

}
