/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * $Id: IErrorReporter.java, Sep 21, 2012 4:35:05 PM Andrei Tatarnikov Exp $
 */

package ru.ispras.microtesk.translator.antlrex;

public interface IErrorReporter
{
    public void raiseError(ISemanticError error) throws SemanticException;
    public void raiseError(Where where, ISemanticError error) throws SemanticException;    
}
