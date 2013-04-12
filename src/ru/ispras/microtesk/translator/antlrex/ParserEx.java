/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ParserEx.java, Oct 26, 2012 12:42:35 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.antlrex;

import java.io.File;

import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

import ru.ispras.microtesk.translator.antlrex.log.ELogEntryKind;
import ru.ispras.microtesk.translator.antlrex.log.ESenderKind;
import ru.ispras.microtesk.translator.antlrex.log.ILogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogEntry;

public class ParserEx extends Parser implements IErrorReporter
{
    private ILogStore log  = null;
    private int errorCount = 0;
    
    private final String sourceName;
    private String tempErrorMessage = "";
 
    public ParserEx(TokenStream input, RecognizerSharedState state)
    {
        super(input, state);
        sourceName =  new File(getSourceName()).getName(); 
    }
    
    public void assignLog(ILogStore log)
    {
        this.log = log;
    }
    
    @Override
    public final void reportError(RecognitionException re)
    {
        assert (null != log);       
        assert !(re instanceof SemanticException);

        tempErrorMessage = "";
        super.reportError(re);

        LogEntry logEntry = new LogEntry(ELogEntryKind.ERROR,
            ESenderKind.PARSER,
            sourceName,
            re.line,
            re.charPositionInLine,
            tempErrorMessage
            );

        log.append(logEntry);
        ++errorCount;
    }
    
    public final void reportError(SemanticException se)
    {
        assert (null != log);

        LogEntry logEntry = new LogEntry(ELogEntryKind.ERROR,
            ESenderKind.SYNTACTIC,
            sourceName,
            se.line,
            se.charPositionInLine,
            se.getMessage()
            );
        

        log.append(logEntry);
        ++errorCount;
    }
    
    @Override
    public final void emitErrorMessage(String errorMessage)
    {
        tempErrorMessage = errorMessage;
    }
    
    public final int getErrorCount()
    {
        return errorCount;
    }
    
    public final void resetErrorCount()
    {
        errorCount = 0;
    }
    
    public final boolean isCorrect()
    {
        return getErrorCount() == 0; 
    }

    @Override
    public void raiseError(ISemanticError error) throws SemanticException
    {
        throw new SemanticException(input, error); 
    }

    @Override
    public void raiseError(Where where, ISemanticError error) throws SemanticException
    {
        throw new SemanticException(where, error);
    }
    
    protected final Where where(Token node)
    {
        return new Where(sourceName, node.getLine(), node.getCharPositionInLine());
    }
}
