/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * EExprKind.java, Jan 22, 2013 12:15:04 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

/**
 * The EExprKind enumeration specifies how a specific expression is represented
 * (calculated and stored). There are three options:
 * 
 * 1. Java expression (Java types and operators).
 * 2. Static Java expression (Java expression calculated at translation time). 
 * 3. Model expression (storage classes and operations are provided by model API). 
 * 
 * @author Andrei Tatarnikov
 */

public enum EExprKind
{
    /** 
     * Java expression are calculated with the help of features provided by Java.   
     * Arguments and the expression result are stored as Java types (int, long,
     * boolean, etc) and operations are performed using Java operators (+, -, &&,
     * ||, >>, etc.).
     */

    JAVA,

    /**
     * Same as JAVA_EXPR, but the result of such expressions is statically calculated
     * (during translation). There is a restrictions on expression arguments:
     * their values should be know at translation time. So they can be numerical
     * literals, constants (LET-construction) or other static expressions.     
     */

    JAVA_STATIC,

    /**
     * Model expressions are calculated using features provided by the Model API library.
     * Data is stored in locations of a particular type. Operations are performed by model
     * classes.
     */

    MODEL
}
