package ru.ispras.microtesk.translator.mmu;

/**
 * Symbols used in MMU.
 * 
 * @author Taya Sergeeva
 */

public enum ESymbolKind
{
    BUFFER,
    
    SET,
    
    LINE,
    
    INDEX,
    
    MATCH,
	
    ADDRESS,
    
	/** Reserved keywords */
    KEYWORD, 

    /** Constant number or static numeric expression */
    BUFFER_CONST,

    /** Constant string */
    BUFFER_STRING, 

    /** Attribute of a mode or an operation (e.g. syntax, format, image). */
    LENGTH, ATTRIBUTE, CONST, LINE_CONST, ADDRESS_CONST, MATCH_CONST, MATCH_STRING, MATCH_ADDRESS, SET_CONST, POLICY_STRING, POLICY_CONST, INDEX_CONST, INDEX_ADDRESS, ASSOCIATIVITY_CONST, SETS_CONST, DATA_CONST, TAG_CONST
}
