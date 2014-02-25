/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * $Id: ILogStore.java, Jul 30, 2012 6:34:29 PM Andrei Tatarnikov Exp $
 */

package ru.ispras.microtesk.translator.antlrex.log;

/**
 * A base interface for a log store class (the class that will store and process information
 * about events occurred during translation of an ADL model).
 * 
 * @author Andrei Tatarnikov
 */

public interface ILogStore
{
	/**
	 * Appends a record to the log store.
	 * 
	 * @param entry A log entry object. Stores detailed information bout the event.
	 */

	public void append(LogEntry entry);
}
