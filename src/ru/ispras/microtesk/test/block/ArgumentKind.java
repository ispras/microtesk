/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ArgumentKind.java, May 14, 2013 2:55:41 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.block;

public enum ArgumentKind
{
    /** Concrete value that does not participate in test data generation (default).*/
    DEFAULT,

    /** Unknown value to be generated by the test data generation engine.*/
    FREE,

    /** Used as input value for test data generation.*/
    CLOSED
}
