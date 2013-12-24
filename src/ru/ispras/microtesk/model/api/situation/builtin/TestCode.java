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

import ru.ispras.formula.solver.Environment;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;

/**
 * Private code. Presents here only for testing purposes. Probably, it will be removed.   
 * 
 * @author Andrei Tatarnikov
 */

class TestCode
{
    private static void initializeSolverEngine()
    {
        if (Environment.isUnix())
        {
            Environment.setSolverPath("tools/z3/unix/z3");
        }
        else if(Environment.isWindows())
        {
            Environment.setSolverPath("tools/z3/windows/z3.exe");
        }
        else if(Environment.isOSX())
        {
            Environment.setSolverPath("tools/z3/osx/z3");
        }
        else
        {
            assert false : 
                String.format(
                    "Please set up paths for the external engine. Platform: %s",
                    System.getProperty("os.name")
                    );
        }
    }

    public static void main(String[] arg)
    {
        initializeSolverEngine();

        try
        {
            testRandomSituation();
            testAddOverflowSituation();
            testAddNormalSituation();
        }
        catch (ConfigurationException e)
        {
            e.printStackTrace();
        }
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

    private static void testAddOverflowSituation() throws ConfigurationException
    {
        System.out.println("Add Overflow Situation");

        final AddOverflowSituation situation = new AddOverflowSituation();
         printResult(situation.solve());
    }

    private static void testAddNormalSituation() throws ConfigurationException
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
