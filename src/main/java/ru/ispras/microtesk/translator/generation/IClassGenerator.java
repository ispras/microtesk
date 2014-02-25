/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IClassGenerator.java, Jul 10, 2012 10:44:34 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.generation;

import java.io.IOException;

/**
 * The IClassGenerator interface is a base interface to be implemented by all
 * class file generators. 
 * 
 * @author Andrei Tatarnikov
 */

public interface IClassGenerator
{
    /**
     * Runs generation of a class file.
     * 
     * @throws Exception Raised if the generator failed to generate the needed file
     * due to an I/O problem.
     */

    public void generate() throws IOException;
}
