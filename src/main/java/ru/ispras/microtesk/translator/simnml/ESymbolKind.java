/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ESymbolKind.java, Dec 11, 2012 4:49:38 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml;

/**
 * Symbols used in Sim-nML translators.
 * 
 * @author Andrei Tatarnikov
 */

public enum ESymbolKind
{
    /** Reserved keywords */
    KEYWORD,

    /** Constant number or static numeric expression */
    LET_CONST,
    
    /** Constant label that associates some ID with a location (reg, mem or var item)*/
    LET_LABEL,

    /** Constant string */
    LET_STRING,

    /** Type declaration */
    TYPE,

    /** Memory storage (reg, mem, var) */
    MEMORY,

    /** Addressing mode */
    MODE,

    /** Operation */
    OP,

    /** Argument of a mode or an operation. */
    ARGUMENT,

    /** Attribute of a mode or an operation (e.g. syntax, format, image). */
    ATTRIBUTE
}
