/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * WalkerContext.java, Jul 22, 2013 7:49:17 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.antlrex;

import java.util.Map;

import ru.ispras.microtesk.translator.antlrex.IErrorReporter;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.ir.IR;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;

public interface WalkerContext
{
    public IErrorReporter getReporter();

    public SymbolTable<ESymbolKind> getSymbols();
    public IR getIR();

    public Map<String, Primitive> getThisArgs();
    public Primitive.Holder getThis();
}
