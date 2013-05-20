/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TestCode.java, May 20, 2013 1:41:43 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.situation.builtin;

import java.util.Map;
import ru.ispras.microtesk.model.api.data.Data;

/**
 * Private code. Presents here only for testing purposes. Probably, it will be removed.   
 * 
 * @author Andrei Tatarnikov
 */

class TestCode
{
    public static void main(String[] arg)
    {
        testRandomSituation();
        testAddOverflowSituation();
        testAddNormalSituation();
    }    

    private static void testRandomSituation()
    {
        System.out.println("Random Situation");

        final RandomSituation situation = new RandomSituation();

        situation.setOutput("a");
        situation.setOutput("b");
        situation.setOutput("c");
        situation.setOutput("d");

        printResult(situation.solve());
    }

    private static void testAddOverflowSituation()
    {
        System.out.println("Add Overflow Situation");
        
        final AddOverflowSituation situation = new AddOverflowSituation();
        printResult(situation.solve());
    }

    private static void testAddNormalSituation()
    {
        System.out.println("Add Normal Situation");
        
        final AddNormalSituation situation = new AddNormalSituation();
        printResult(situation.solve());
    }

    private static void printResult(Map<String, Data> result)
    {
        for (Map.Entry<String, Data> entry : result.entrySet())
        {
            System.out.println(entry.getKey() + " = " +
                entry.getValue().getRawData().toBinString());
        }
    }
}
