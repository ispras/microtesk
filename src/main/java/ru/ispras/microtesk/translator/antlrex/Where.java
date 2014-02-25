/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * $Id: Where.java, Sep 21, 2012 4:34:44 PM Andrei Tatarnikov Exp $
 */

package ru.ispras.microtesk.translator.antlrex;

public final class Where
{
    private String unit;
    private int    line;
    private int    position;

    public Where(String unit, int line, int position)
    {
        this.unit = unit;
        this.line = line;
        this.position = position;
    }

    public String getUnit()
    {
        return unit;
    }

    public int getLine()
    {
        return line;
    }

    public int getPosition()
    {
        return position;
    }
    
    @Override
    public String toString()
    {
        return String.format("%s %d:%d", unit, line, position);
    }

}
