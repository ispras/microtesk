/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * $Id: LogEntry.java, Jul 30, 2012 4:32:48 PM Andrei Tatarnikov Exp $
 */

package ru.ispras.microtesk.translator.antlrex.log;

/**
 * The LogEntry class stores information about a translation issue registered
 * in the log.
 * 
 * @author Andrei Tatarnikov
 */

public class LogEntry
{	
	private ELogEntryKind     kind;
	private ESenderKind     sender;

	private String          source;
	private int               line;
	private int           position;	
	private String         message;

    private static final String FORMAT = "%s %d:%d %s (%s): \"%s\"";

	/**
	 * Creates a LogEntry object.
	 * 
	 * @param kind The severity level of the issue.
	 * @param sender The subsystem that detected the issue.
	 * @param source A source file (in ADL) that caused translation issues. 
	 * @param line The number of the problematic line in the ADL file.
	 * @param position The position in the problematic line in the ADL file. 
	 * @param message The text message containing a description of the issue.
	 */

	public LogEntry(
			ELogEntryKind kind,
			ESenderKind   sender,
			String        source,
			int           line,
			int           position,
			String        message
			)
	{
		this.kind     = kind;
		this.sender   = sender;
		this.source   = source;
		this.line     = line;
		this.position = position;
		this.message  = message;
	}

	/**
	 * Return the textual representation of the entry.
	 */

	@Override
	public String toString()
	{
	    return String.format(
	        FORMAT,
	        getSource(),
	        getLine(),
	        getPosition(),
	        getKind().toString(),
	        getSender().toString(),
	        getMessage()
            );     
	}

	/**
	 * Returns an identifier that signifies the severity level of the issue. 
	 * 
	 * @return The severity level of the issue.
	 */

	public ELogEntryKind getKind()
	{
		return kind;
	}

	/**
	 * Returns an identifier of the subsystem that detected an issue. 
	 * 
	 * @return Identifier of the subsystem that detected an issue.
	 */

	public ESenderKind getSender()
	{
		return sender;
	}

	/**
	 * Returns the name of the source file that caused a translation issue.
	 * 
	 * @return Source file name.
	 */

	public String getSource()
	{
		return source;
	}

	/**
	 * Returns the number of the problematic line in the source file.
	 * 
	 * @return The line number.
	 */

	public int getLine()
	{
		return line;
	}

	/**
	 * Returns the position in the problematic line at which the issue was detected.  
	 * 
	 * @return The position of the text in the problematic line that caused the issue.
	 */

	public int getPosition()
	{
		return position;
	}

	/**
	 * Returns the issue description.
	 * 
	 * @return The issue description.  
	 */

	public String getMessage()
	{
		return message;
	}	
}
