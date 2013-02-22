/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * $Id: ELogEntryKind.java, Jul 30, 2012 3:38:25 PM Andrei Tatarnikov Exp $
 */

package ru.ispras.microtesk.translator.antlrex.log;

/**
 * The ELogEntryKind enumeration describes three levels of an event or an exception
 * (usually a record is added to the log due to a runtime exception) that can occur
 * during translation of an ADL specification.
 *
 * @author Andrei Tatarnikov
 */

public enum ELogEntryKind
{
    /**
     * Signifies a severe translation error. Usually it means that some the
     * design specification was incorrect, which cause translation to fail.
     * In other words, the tool was unable to produce any meaningful output.
     */    
	ERROR,
	
	/**
	 * Signifies a minor translation error. This usually means a small issue in
	 * the design specification which can potentially cause incorrect results.
	 */  
	WARNING,
	
	/**
	 * Signifies an informational message that highlights some issue in the ADL
	 * specification (or in the tool) that requires user attention, but is not
	 * necessarily an error.  
	 */
	MESSAGE
}