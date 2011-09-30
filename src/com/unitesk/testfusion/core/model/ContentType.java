/* 
 * Copyright (c) 2007-2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: ContentType.java,v 1.10 2009/11/09 14:53:15 kamkin Exp $
 */

package com.unitesk.testfusion.core.model;

import com.unitesk.testfusion.core.type.*;

/**
 * Content type of instruction operand.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class ContentType
{
    /** Integer type (1-32-bit integer values). */
    public static final ContentType INTEGER(int width)
    {
        return IntegerType.TYPE(width);
    }

    /** Boolean type (1-bit integer values). */
    public static final ContentType BOOLEAN = BooleanType.TYPE;
    
    /** Byte type (8-bit integer values). */
    public static final ContentType BYTE = ByteType.TYPE;
    
    /** Half-word type (16-bit integer values). */
    public static final ContentType HALF_WORD = HalfWordType.TYPE;
    
    /** Word type (32-bit integer values). */
    public static final ContentType WORD = WordType.TYPE;
    
    /** Pair-word type (pair of 32-bit integer values). */
    public static final ContentType PAIR_WORD = PairWordType.TYPE;
    
    /** Double-word type (64-bit integer values). */
    public static final ContentType DOUBLE_WORD = DoubleWordType.TYPE;

    /** Single type (32-bit floating-point values). */
    public static final ContentType SINGLE = SingleType.TYPE;
    
    /** Pair-single type (pair of 32-bit floating-point values). */
    public static final ContentType PAIR_SINGLE = PairSingleType.TYPE;
    
    /** Double type (64-bit floating-point values). */
    public static final ContentType DOUBLE = DoubleType.TYPE;
    
    /** Address offset. */
    public static final ContentType OFFSET = OffsetType.TYPE;
    
    /** Virtual address of data. */
    public static final ContentType DATA_ADDRESS = DataAddressType.TYPE;
    
    /** Virtual address of instruction. */
    public static final ContentType INSTRUCTION_ADDRESS = InstructionAddressType.TYPE;
    
    /** Physical address. */
    public static final ContentType PHYSICAL_ADDRESS = PhysicalAddressType.TYPE;
    
    /** Name of the content type. */
    protected String name;
    
    /** Base content type. */
    protected ContentType base;
    
    /**
     * Flag that reflects permission of bidirectional type casting between
     * this type and the base content type.
     */
    protected boolean synonym;

    /**
     * Constructor.
     * 
     * @param <code>name</code> the content type name.
     * @param <code>base</code> the base content type.
     * @param <code>synonym</code> the permission of bidirectional type
     *        casting.
     */
    protected ContentType(String name, ContentType base, boolean synonym)
    {
        this.name = name;
        this.base = base;
        this.synonym = synonym;
    }
    
    /**
     * Constructor. Bidirectional type casting between this type and the base
     * content type is not permitted.
     * 
     * @param <code>name</code> the content type name.
     * @param <code>base</code> the base content type.
     */
    protected ContentType(String name, ContentType base)
    {
        this(name, base, false);
    }
    
    /**
     * Constructor of the top-level content type.
     * 
     * @param <code>name</code> the content type name.
     */
    protected ContentType(String name)
    {
        this(name, null);
    }

    /**
     * Returns the content type name.
     * 
     * @return the content type name.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Checks if the value is compatible with the content type.
     *
     * @return <code>true</code> if the value is compatible with the content
     *         type; <code>false</code> otherwise.
     */
    public abstract boolean checkType(long value);
    
    /**
     * Returns a string representation of the value.
     * 
     * @return a string representation of the value.
     */
    public String getDescription(long value)
    {
        return "0x" + Long.toHexString(value);
    }

    /** Returns width of the type. */
    public abstract int getWidth();
    
    /**
     * Checks if this type can be converted to the given content type.
     *
     * @param  <code>type</code> the content type.
     * 
     * @return <code>true</code> if this type can be converted to the given
     *         content type.
     */
    public final boolean isCompatibleTo(ContentType type)
    {
        if(equals(type))
            { return true; }

        return base != null && base.isCompatibleTo(type)
            || type.synonym && isCompatibleTo(type.base);
    }
    
    /**
     * Compares the content types.
     * 
     * @param  <code>o</code> the content type to be compared.
     * 
     * @return <code>true</code> if content types are equal; <code>false</code>
     *         otherwise.
     */
    public boolean equals(Object o)
    {
        if(!(o instanceof ContentType))
            { return false; }
        
        ContentType r = (ContentType)o;
        
        return name.equals(r.name);
    }
    
    /**
     * Returns a hash code value for the object.
     */
    public int hashCode()
    {
        return name.hashCode();
    }
    
    /**
     * Returns a string representation of the content type.
     * 
     * @return a string representation of the content type.
     */
    public String toString()
    {
        return getName();
    }
}
