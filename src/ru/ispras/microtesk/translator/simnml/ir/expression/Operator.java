/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Operator.java, Aug 14, 2013 12:33:41 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.translator.simnml.ir.value.Operation;
import ru.ispras.microtesk.translator.simnml.ir.value.ValueInfo;

public enum Operator
{
    OR       ("||",     Operation.OR),
    AND      ("&&",     Operation.AND),

    BIT_OR   ("|",      Operation.BIT_OR),
    BIT_XOR  ("^",      Operation.BIT_XOR),

    BIT_AND  ("&",      Operation.BIT_AND),

    EQ       ("==",     Operation.EQ),
    NOT_EQ   ("!=",     Operation.NOT_EQ),

    LEQ      ("<=",     Operation.LEQ),
    GEQ      (">=",     Operation.GEQ),
    LESS     ("<",      Operation.LESS),
    GREATER  (">",      Operation.GREATER),

    L_SHIFT  ("<<",     Operation.L_SHIFT),
    R_SHIFT  (">>",     Operation.R_SHIFT),

    L_ROTATE ("<<<",    Operation.L_ROTATE), 
    R_ROTATE (">>>",    Operation.R_ROTATE),

    PLUS     ("+",      Operation.PLUS),
    MINUS    ("-",      Operation.MINUS),

    MUL      ("*",      Operation.MUL),
    DIV      ("/",      Operation.DIV),

    MOD      ("%",      Operation.MOD),

    POW      ("**",     Operation.POW),

    UPLUS    ("UPLUS",  Operation.UPLUS),
    UMINUS   ("UMINUS", Operation.UMINUS),
    BIT_NOT  ("~",      Operation.BIT_NOT),
    NOT      ("!",      Operation.NOT)
    ;

    private static final Map<String, Operator> operators;
    static
    {
        final Operator[] ops = Operator.values();
        operators =  new HashMap<String, Operator>(ops.length);

        for (Operator o : ops)
            operators.put(o.text(), o);
    }

    public static Operator forText(String text)
    {
        return operators.get(text);
    }

    private final String         text;
    private final Operation operation;

    private Operator(String text, Operation operation)
    {
        assert null != text;
        assert null != operation;

        this.text      = text;
        this.operation = operation;
    }

    public String text()
    {
        return text;
    }

    public int priority()
    {
        return operation.priority();
    }

    public int operands()
    {
        return operation.operands();
    }

    ValueInfo calculate(ValueInfo cast, List<ValueInfo> values)
    {
        return operation.calculate(cast, values);
    }

    boolean isSupportedFor(ValueInfo value)
    {
        return operation.isSupportedFor(value);
    }
}
