/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Situation.java, Mar 21, 2014 10:25:23 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

public final class Situation
{
    /**
     * Unique ID, corresponds to Java class name.
     * Format: "<INSTRUCTION NAME>_<Situation name>" (for shared situations: _<Situation name>).
     */
    private final String fullName;

    /**
     * If it is linked to all instructions or to one specific instruction.
     */
    private final boolean isShared;

    public Situation(String fullName, boolean isShared)
    {
        this.fullName = fullName;
        this.isShared = isShared;
    }

    public String getFullName()
    {
        return fullName;
    }

    public boolean isShared()
    {
        return isShared;
    }
}
