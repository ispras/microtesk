/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * JavaKeyword.java, Oct 22, 2012 1:49:01 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.keywords;

public final class JavaKeyword
{
	private JavaKeyword() { }
	
    public static final String[] STRINGS =
    {
        // Reserved language keywords.
        "abstract", "continue", "for",        "new",       "switch", 
        "assert",   "default",  "goto",       "package",   "synchronized",
        "boolean",  "do",       "if",         "private",   "this",
        "break",    "double",   "implements", "protected", "throw",
        "byte",     "else",     "import",     "public",    "throws",
        "case",     "enum",     "instanceof", "return",    "transient", 
        "catch",    "extends",  "int",        "short",     "try",
        "char",     "final",    "interface",  "static",    "void",
        "class",    "finally",  "long",       "strictfp",  "volatile",
        "const",    "float",    "native",     "super",     "while"
        
        // Class names from the "java.lang" package.
        // TODO: Add names that can cause conflicts with generated constructs.
    };
}
