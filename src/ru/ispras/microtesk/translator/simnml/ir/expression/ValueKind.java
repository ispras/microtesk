/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ValueKind.java, Oct 11, 2013 9:02:22 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

/**
 * Specifies the kind of a value stored in an expression terminal or produced as a result of an operation.
 * 
 * @author Andrei Tatarnikov
 */

public enum ValueKind
{
    /** MicroTESK Model API value. */
    MODEL,
    
    /** Native Java value. */
    NATIVE
}