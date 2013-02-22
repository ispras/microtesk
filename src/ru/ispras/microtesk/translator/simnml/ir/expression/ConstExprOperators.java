/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ConstExprOperators.java, Oct 22, 2012 1:48:08 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import java.util.HashMap;

/**
 * The IConstBinaryOperator interface is common interface for binary operations 
 * with constant expressions.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

interface IConstBinaryOperator
{
    public Object calculate(Object a1, Object a2);    
}

/**
 * The IConstUnaryOperator interface is common interface for unary operations 
 * with constant expressions.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a> 
 */

interface IConstUnaryOperator
{
    public Object calculate(Object a1);    
}

/**
 * The ConstExprOperators class stores operations that are applied to
 * constant expression.
 * 
 * Supported data types:
 * 
 * int.class, long.class, double.class, boolean.class
 *
 * Supported operations:
 *
 * Binary:
 *
 *  OR              '||'
 *  AND             '&&'
 *  VERT_BAR        '|'
 *  UP_ARROW        '^' 
 *  AMPER           '&'
 *  EQ              '=='
 *  NEQ             '!='
 *  LEQ             '<='
 *  GEQ             '>='
 *  LEFT_BROCKET    '<' 
 *  RIGHT_BROCKET   '>'
 *  LEFT_SHIFT      '<<' 
 *  RIGHT_SHIFT     '>>' 
 *  ROTATE_LEFT     '<<<'
 *  ROTATE_RIGHT    '>>>'
 *  PLUS            '+'
 *  MINUS           '-'
 *  MUL             '*'
 *  DIV             '/'
 *  REM             '%'
 *  DOUBLE_STAR     '**'
 *
 * Unary:
 *
 *  UNARY_PLUS      "UNARY_PLUS"
 *  UNARY_MINUS     "UNARY_MINUS"
 *  TILDE           "~"
 *  NOT             "!"
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public class ConstExprOperators
{
    private static final HashMap<String, IConstBinaryOperator>  binOpTable = new HashMap<String, IConstBinaryOperator>();
    private static final HashMap<String, IConstUnaryOperator>    unOpTable = new HashMap<String, IConstUnaryOperator>();
    private static final HashMap<Class<?>, Class<?>>             typeTable = new HashMap<Class<?>, Class<?>>();

    /**
     * Returns an object that implements a binary operation for the specified operator
     * and parameter data types.
     * 
     * @param opText Textual representation of the operation.
     * @param type1 Left parameter type. 
     * @param type2 Right parameter type.
     * @return Binary operation.
     */
    
    public static IConstBinaryOperator getBinaryOperator(String opText, Class<?> type1, Class<?> type2) 
    {
        return binOpTable.get(opText + type1 + type2);
    }
    
    /**
     * Returns an object that implements an unary operation for the specified operator
     * and parameter data types.
     * 
     * @param opText Textual representation of the operation.
     * @param type Parameter type.
     * @return Unary operation.
     */
    
    public static IConstUnaryOperator getUnaryOperator(String opText, Class<?> type)
    {
        return unOpTable.get(opText + type);
    }

    private static void addOperator(String opText, Class<?> type1, Class<?> type2, IConstBinaryOperator op)
    {
        IConstBinaryOperator prev = binOpTable.put(opText + type1 + type2, op);
        assert null == prev : "The operation has already been defined.";
    }

    private static void addOperator(String opText, Class<?> type, IConstUnaryOperator op)
    {
        IConstUnaryOperator prev = unOpTable.put(opText + type, op);
        assert null == prev : "The operation has already been defined.";
    }
    
    /**
     * Returns a primitive type that corresponds to the specified type (e.g. int.class for Integer.class).
     * 
     * @param Type information. 
     * @return Type information for the corresponding primitive type. 
     */

    public static Class<?> getPrimitive(Class<?> c)
    {
        Class<?> result = typeTable.get(c);
        assert null != result : "No primitive type for the specified type in the table.";
        return result;
    }
    
    static
    {
        initTypeTable();
        initOperatorTable();
    }
    
    private static void initTypeTable()
    {
        typeTable.put(Integer.class, int.class);
        typeTable.put(Long.class,    long.class);
        typeTable.put(Double.class,  double.class);
        typeTable.put(Boolean.class, boolean.class);

        typeTable.put(int.class,     int.class);
        typeTable.put(long.class,    long.class);
        typeTable.put(double.class,  double.class);
        typeTable.put(boolean.class, boolean.class);        
    }
   
    private static void initOperatorTable()
    {
        // BINARY OPERATORS:
        addOR();             // OR            '||'
        addAND();            // AND           '&&' 
        addVERT_BAR();       // VERT_BAR      '|'
        addUP_ARROW();       // UP_ARROW      '^'
        addAMPER();          // AMPER         '&'
        addEQ();             // EQ            '=='
        addNEQ();            // NEQ           '!=' 
        addLEQ();            // LEQ           '<='
        addGEQ();            // GEQ           '>='
        addLEFT_BROCKET();   // LEFT_BROCKET  '<'
        addRIGHT_BROCKET();  // RIGHT_BROCKET '>'
        addLEFT_SHIFT();     // LEFT_SHIFT    '<<'
        addRIGHT_SHIFT();    // RIGHT_SHIFT   '>>'
        addROTATE_LEFT();    // ROTATE_LEFT   '<<<'
        addROTATE_RIGHT();   // ROTATE_RIGHT  '>>>'
        addPLUS();           // PLUS          '+'
        addMINUS();          // MINUS         '-'
        addMUL();            // MUL           '*'
        addDIV();            // DIV           '/'
        addDOUBLE_STAR();    // DOUBLE_STAR   '**'
        
        // UNARY OPERATORS:
        addUNARY_PLUS();     // UNARY_PLUS    "UNARY_PLUS"
        addUNARY_MINUS();    // UNARY_MINUS   "UNARY_MINUS"
        addTILDE();          // TILDE         "~"
        addNOT();            // NOT           "!"
    }    
    
    // OR ('||') 
    private static void addOR()
    {
        addOperator("||", boolean.class, boolean.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2)
                { return (Boolean)a1 || (Boolean)a2; }
        });
    }

    // AND ('&&')
    private static void addAND()
    {
        addOperator("&&", boolean.class, boolean.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Boolean)a1 && (Boolean)a2; }
        });
    }
    
    // VERT_BAR ('|')
    private static void addVERT_BAR()
    {
        addOperator("|", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 | (Integer)a2; }
        });

        addOperator("|", long.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 | (Long)a2; }
        });

        addOperator("|", boolean.class, boolean.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Boolean)a1 | (Boolean)a2; }
        });

        addOperator("|", int.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 | (Long)a2; }
        });

        addOperator("|", long.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 | (Integer)a2; }
        });
    }
    
    // UP_ARROW ('^')
    private static void addUP_ARROW()
    {
        addOperator("^", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 ^ (Integer)a2; }
        });

        addOperator("^", long.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 ^ (Long)a2; }
        });

        addOperator("^", boolean.class, boolean.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Boolean)a1 ^ (Boolean)a2; }
        });

        addOperator("^", int.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 ^ (Long)a2; }
        });

        addOperator("^", long.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 ^ (Integer)a2; }
        });
    }
    
    // AMPER ('&')
    private static void addAMPER()
    {       
        addOperator("&", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 & (Integer)a2; }
        });

        addOperator("&", long.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 & (Long)a2; }
        });

        addOperator("&", boolean.class, boolean.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Boolean)a1 & (Boolean)a2; }
        });

        addOperator("&", int.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 & (Long)a2; }
        });

        addOperator("&", long.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 & (Integer)a2; }
        });
    }
    
    // EQ ('==')
    private static void addEQ()
    {        
        addOperator("==", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 == (Integer)a2; }
        });

        addOperator("==", long.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 == (Long)a2; }
        });
        
        addOperator("==", double.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 == (Double)a2; }
        });

        addOperator("==", boolean.class, boolean.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Boolean)a1 == (Boolean)a2; }
        });
    }
    
    // NEQ ('!=')    
    private static void addNEQ()
    {        
        addOperator("!=", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 != (Integer)a2; }
        });

        addOperator("!=", long.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 != (Long)a2; }
        });

        addOperator("!=", double.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 != (Double)a2; }
        });

        addOperator("!=", boolean.class, boolean.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Boolean)a1 != (Boolean)a2; }
        });
    }
    
    // LEQ ('<=')
    private static void addLEQ()
    {        
        addOperator("<=", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 <= (Integer)a2; }
        });

        addOperator("<=", long.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 <= (Long)a2; }
        });
        
        addOperator("<=", double.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 <= (Double)a2; }
        });

        addOperator("<=", int.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 <= (Long)a2; }
        });
        
        addOperator("<=", long.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 <= (Integer)a2; }
        });
        
        addOperator("<=", int.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 <= (Double)a2; }
        });
        
        addOperator("<=", double.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 <= (Integer)a2; }
        });
        
        addOperator("<=", long.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 <= (Double)a2; }
        });
        
        addOperator("<=", double.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 <= (Long)a2; }
        });
    }
        
    // GEQ ('>=')
    private static void addGEQ()
    {
        addOperator(">=", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 >= (Integer)a2; }
        });

        addOperator(">=", long.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 >= (Long)a2; }
        });
        
        addOperator(">=", double.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 >= (Double)a2; }
        });

        addOperator(">=", int.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 >= (Long)a2; }
        });
        
        addOperator(">=", long.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 >= (Integer)a2; }
        });
        
        addOperator(">=", int.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 >= (Double)a2; }
        });
        
        addOperator(">=", double.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 >= (Integer)a2; }
        });
        
        addOperator(">=", long.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 >= (Double)a2; }
        });
        
        addOperator(">=", double.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 >= (Long)a2; }
        });
    }
    
    // LEFT_BROCKET ('<')
    private static void addLEFT_BROCKET()
    {        
        addOperator("<", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 < (Integer)a2; }
        });

        addOperator("<", long.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 < (Long)a2; }
        });
        
        addOperator("<", double.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 < (Double)a2; }
        });

        addOperator("<", int.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 < (Long)a2; }
        });
        
        addOperator("<", long.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 < (Integer)a2; }
        });
        
        addOperator("<", int.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 < (Double)a2; }
        });
        
        addOperator("<", double.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 < (Integer)a2; }
        });
        
        addOperator("<", long.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 < (Double)a2; }
        });
        
        addOperator("<", double.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 < (Long)a2; }
        });
    }
    
    // RIGHT_BROCKET ('>')
    private static void addRIGHT_BROCKET()
    {
        addOperator(">", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 > (Integer)a2; }
        });

        addOperator(">", long.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 > (Long)a2; }
        });
        
        addOperator(">", double.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 > (Double)a2; }
        });

        addOperator(">", int.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 > (Long)a2; }
        });
        
        addOperator(">", long.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 > (Integer)a2; }
        });
        
        addOperator(">", int.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 > (Double)a2; }
        });
        
        addOperator(">", double.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 > (Integer)a2; }
        });
        
        addOperator(">", long.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 > (Double)a2; }
        });
        
        addOperator(">", double.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 > (Long)a2; }
        });
    }

    // LEFT_SHIFT ('<<')
    private static void addLEFT_SHIFT()
    {        
        addOperator("<<", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 << (Integer)a2; }
        });

        addOperator("<<", long.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 << (Long)a2; }
        });

        addOperator("<<", int.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 << (Long)a2; }
        });
        
        addOperator("<<", long.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 << (Integer)a2; }
        });
    }

    // RIGHT_SHIFT ('>>')
    private static void addRIGHT_SHIFT()
    {
        addOperator(">>", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 >> (Integer)a2; }
        });

        addOperator(">>", long.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 >> (Long)a2; }
        });

        addOperator(">>", int.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 >> (Long)a2; }
        });
        
        addOperator(">>", long.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 >> (Integer)a2; }
        });
    }
    
    // ROTATE_LEFT ('<<<')
    private static void addROTATE_LEFT()
    {
        addOperator("<<<", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2)
                { return Integer.rotateLeft((Integer)a1, (Integer)a2); }
        });

        addOperator("<<<", long.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2)
                { return Long.rotateLeft((Long)a1, (Integer)a2); }
        });
    }
    
    // ROTATE_RIGHT ('>>>')
    private static void addROTATE_RIGHT()
    {
        addOperator(">>>", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2)
                { return Integer.rotateRight((Integer)a1, (Integer)a2); }
        });

        addOperator(">>>", long.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2)
                { return Long.rotateRight((Long)a1, (Integer)a2); }
        });        
    }

    // PLUS ('+')
    private static void addPLUS()
    {
        addOperator("+", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 + (Integer)a2; }
        });

        addOperator("+", long.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 + (Long)a2; }
        });
        
        addOperator("+", double.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 + (Double)a2; }
        });

        addOperator("+", int.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 + (Long)a2; }
        });
        
        addOperator("+", long.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 + (Integer)a2; }
        });
        
        addOperator("+", int.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 + (Double)a2; }
        });
        
        addOperator("+", double.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 + (Integer)a2; }
        });
        
        addOperator("+", long.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 + (Double)a2; }
        });
        
        addOperator("+", double.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2)
                { return (Double)a1 + (Long)a2; }
        });        
    }

    // MINUS ('-')
    private static void addMINUS()
    {
        addOperator("-", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 - (Integer)a2; }
        });

        addOperator("-", long.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 - (Long)a2; }
        });

        addOperator("-", double.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 - (Double)a2; }
        });

        addOperator("-", int.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 - (Long)a2; }
        });

        addOperator("-", long.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 - (Integer)a2; }
        });

        addOperator("-", int.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 - (Double)a2; }
        });

        addOperator("-", double.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 - (Integer)a2; }
        });

        addOperator("-", long.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 - (Double)a2; }
        });

        addOperator("-", double.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2)
                { return (Double)a1 - (Long)a2; }
        });
    }
    
    // MUL ('*')
    private static void addMUL()
    {
        addOperator("*", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 * (Integer)a2; }
        });

        addOperator("*", long.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 * (Long)a2; }
        });

        addOperator("*", double.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 * (Double)a2; }
        });

        addOperator("*", int.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 * (Long)a2; }
        });

        addOperator("*", long.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 * (Integer)a2; }
        });

        addOperator("*", int.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 * (Double)a2; }
        });

        addOperator("*", double.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 * (Integer)a2; }
        });

        addOperator("*", long.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 * (Double)a2; }
        });

        addOperator("*", double.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2)
                { return (Double)a1 * (Long)a2; }
        });
    }

    // DIV ('/')
    private static void addDIV()
    {
        addOperator("/", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 / (Integer)a2; }
        });

        addOperator("/", long.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 / (Long)a2; }
        });

        addOperator("/", double.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 / (Double)a2; }
        });

        addOperator("/", int.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 / (Long)a2; }
        });

        addOperator("/", long.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 / (Integer)a2; }
        });

        addOperator("/", int.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Integer)a1 / (Double)a2; }
        });

        addOperator("/", double.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Double)a1 / (Integer)a2; }
        });

        addOperator("/", long.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (Long)a1 / (Double)a2; }
        });

        addOperator("/", double.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2)
                { return (Double)a1 / (Long)a2; }
        });
    }

    // DOUBLE_STAR ('**')
    private static void addDOUBLE_STAR()
    {
        addOperator("**", int.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (int) Math.pow((Integer)a1, (Integer)a2); }
        });

        addOperator("**", long.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (long) Math.pow((Long)a1, (Long)a2); }
        });

        addOperator("**", double.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return Math.pow((Double)a1, (Double)a2); }
        });

        addOperator("**", int.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (long) Math.pow((Integer)a1, (Long)a2); }
        });

        addOperator("**", long.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return (long) Math.pow((Long)a1, (Integer)a2); }
        });

        addOperator("**", int.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return Math.pow((Integer)a1, (Double)a2); }
        });

        addOperator("**", double.class, int.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return Math.pow((Double)a1, (Integer)a2); }
        });
        
        addOperator("**", long.class, double.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2) 
                { return Math.pow((Long)a1, (Double)a2); }
        });
        
        addOperator("**", double.class, long.class, new IConstBinaryOperator() {
            @Override
            public Object calculate(Object a1, Object a2)
                { return Math.pow((Double)a1, (Long)a2); }
        });  
    }    
    
    // UNARY_PLUS ("UNARY_PLUS")
    private static void addUNARY_PLUS()
    {
        addOperator("UNARY_PLUS", int.class, new IConstUnaryOperator() {
            @Override
            public Object calculate(Object a) { return +(Integer)a; }
        });

        addOperator("UNARY_PLUS", long.class, new IConstUnaryOperator() {
            @Override
            public Object calculate(Object a) { return +(Long)a; }
        });

        addOperator("UNARY_PLUS", double.class, new IConstUnaryOperator() {
            @Override
            public Object calculate(Object a) { return +(Double)a; }
        });
    }
    
    // UNARY_MINUS ("UNARY_MINUS")
    private static void addUNARY_MINUS()
    {
        addOperator("UNARY_MINUS", int.class, new IConstUnaryOperator() {
            @Override
            public Object calculate(Object a) { return -(Integer)a; }
        });

        addOperator("UNARY_MINUS", long.class, new IConstUnaryOperator() {
            @Override
            public Object calculate(Object a) { return -(Long)a; }
        });

        addOperator("UNARY_MINUS", double.class, new IConstUnaryOperator() {
            @Override
            public Object calculate(Object a) { return -(Double)a; }
        });
    }

    // TILDE ("~")
    private static void addTILDE()
    {
        addOperator("~", int.class, new IConstUnaryOperator() {
            @Override
            public Object calculate(Object a) { return ~(Integer)a; }
        });

        addOperator("~", long.class, new IConstUnaryOperator() {
            @Override
            public Object calculate(Object a) { return ~(Long)a; }
        });
    }

    // NOT ("!")
    private static void addNOT()
    {
        addOperator("!", boolean.class, new IConstUnaryOperator() {
            @Override
            public Object calculate(Object a) { return !(Boolean)a; }
        });
    }
}


