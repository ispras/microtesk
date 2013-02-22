/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * $Id: TreeParserEx.java, Jul 27, 2012 12:19:46 PM Andrei Tatarnikov Exp $
 */

package ru.ispras.microtesk.translator.antlrex;

import java.io.File;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.runtime.tree.TreeParser;

import ru.ispras.microtesk.translator.antlrex.exception.SemanticException;
import ru.ispras.microtesk.translator.antlrex.log.ELogEntryKind;
import ru.ispras.microtesk.translator.antlrex.log.ESenderKind;
import ru.ispras.microtesk.translator.antlrex.log.ILogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogEntry;

/**
 * The TreeParserEx class is an extension of the standard ANTLR TreeParser class.
 * It provides advanced error-handling facilities by overriding standard
 * error-handling methods. This allows collecting full information about errors
 * in a special log store.
 * 
 * To enable the feature in your implementation, inherit specify TreeParserEx
 * as a base class for you tree parser class (in a grammar file or in your code) 
 * add the following code to the top of your tree parser grammar file:
 * 
 * <pre>
 * @rulecatch{
 * catch(SemanticException se) {
 *     reportError(se);
 *     recover(input,se);
 * }
 * catch (RecognitionException re) { // Default behavior
 *     reportError(re);
 *     recover(input,re);
 * }
 * }</pre>
 * 
 * This will enable handling custom error messages thrown by semantic actions.
 * 
 * @author Andrei Tatarnikov
 */

public class TreeParserEx extends TreeParser implements IErrorReporter
{
	private ILogStore log  = null;
	private int errorCount = 0;       
	
	// Stores standard ANTLR exception messages thrown by generated 
	// TreeParser methods (rules). See the emitErrorMessage method.

	private String tempErrorMessage = "";	
	
	/**
	 * Creates a TreeParserEx object.
	 * 
	 * @param input A stream of AST nodes.
	 * @param state A recognizer state that is used in error recovery and can be shared among recognizers.
	 */
	
	public TreeParserEx(TreeNodeStream input, RecognizerSharedState state)
	{
		super(input, state);
	}
	
	/**
	 * Creates a TreeParserEx object
	 * 
	 * @param input A stream of AST nodes.
	 */

	public TreeParserEx(TreeNodeStream input)
	{
		super(input);
	}
	
	/**
	 * Assigns a log store to the tree parser. 
	 * 
	 * @param log A log store object.
	 */

	public final void assignLog(ILogStore log)
	{
		this.log = log;
	}
	
	/**
	 * An overridden error handling function. Packs information information
	 * about and exception into a log entry object and posts it to the log store. 
	 * Aimed to handle standard ANTLR recognition exception thrown by automatically
	 * generated parser code.  
	 * 
	 * @param re A standard ANTLR exception. 
	 */

	@Override
	public final void reportError(RecognitionException re)
	{
		assert (null != log);		
		assert !(re instanceof SemanticException);

		tempErrorMessage = "";
		super.reportError(re);

        final LogEntry logEntry = new LogEntry(ELogEntryKind.ERROR,
            ESenderKind.TREEWALKER,
            new File(getSourceName()).getName(),
            re.line,
            re.charPositionInLine,
            tempErrorMessage
            );

		log.append(logEntry);
		++errorCount;
	}
	
	/**
	 * Provides convenient handling for extended exceptions thrown by semantic actions.
	 * Post the collected information to the log store.
	 * 
	 * @param se A custom exception thrown by code located in semantic actions.  
	 */
	
	public final void reportError(SemanticException se)
	{
	    assert (null != log);

        final LogEntry logEntry = new LogEntry(ELogEntryKind.ERROR,
            ESenderKind.SEMANTIC,
            new File(getSourceName()).getName(),
            se.line,
            se.charPositionInLine,
            se.getMessage()
            );

	    log.append(logEntry);
	    ++errorCount;
	}
	
	/**
	 * An overridden method of the BaseRecognizer class. Allows collecting text
	 * printed by the reportError method of the BaseRecognizer class. It is needed
	 * to pick up messages of standard RecognitionException exceptions.  
	 * 
	 * @param errorMessage Error message text.
	 */

	@Override
	public final void emitErrorMessage(String errorMessage)
	{
		tempErrorMessage = errorMessage;
	}

	/**
	 * Returns the number of errors reported during parsing. 
	 * 
	 * @return Number of errors.
	 */

	public final int getErrorCount()
	{
	    return errorCount;
	}
	
	/**
	 * Resets (sets to zero) he counter of parsing errors.
	 */
	
	public final void resetErrorCount()
	{
	    errorCount = 0;
	}
	
	/**
	 * Checks if parsing was successful (no errors occurred).
	 * 
	 * @return <b>true</b> if no parsing errors were detected and <b>false</b> if there were errors. 
	 */
	
	public final boolean isCorrect()
	{
	    return getErrorCount() == 0; 
	}

    @Override
    public final void raiseError(ISemanticError error) throws SemanticException
    {   
        throw new SemanticException(input, error);        
    }

    @Override
    public final void raiseError(Where where, ISemanticError error) throws SemanticException
    {
        throw new SemanticException(where, error);        
    }

    protected final Where where(CommonTree node)
    {
        return new Where(getSourceName(), node.getLine(), node.getCharPositionInLine());
    }
}
