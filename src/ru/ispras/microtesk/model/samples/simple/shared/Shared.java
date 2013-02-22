/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Shared.java, Nov 7, 2012 5:15:09 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.samples.simple.shared;

import ru.ispras.microtesk.model.api.memory.MemoryBase;
import ru.ispras.microtesk.model.api.type.Type;

import static ru.ispras.microtesk.model.api.memory.EMemoryKind.*;
import static ru.ispras.microtesk.model.api.type.ETypeID.*;

public final class Shared
{
    private Shared() {}
    
    //////////////////////////////////////////////////////////////////////////
    
    /*
    let MSIZE = 2 ** 6
    let REGS = 16
    */

    public static final int MSIZE = 64; // 2 ** 6
    public static final int  REGS = 16;

    /*
    type index  = card(6)
    type nibble = card(4)
    type byte   = int(8)
    */
    
    public static final Type  index = new Type(CARD, 6);
    public static final Type nibble = new Type(CARD, 4);
    public static final Type byte_t = new Type(CARD, 8);

    /*
    mem M[MSIZE, byte]
    reg R[REGS, byte]
    reg PC[1, byte]
    */

    public static final MemoryBase  M = new MemoryBase(MEM, "M",  byte_t, MSIZE); 
    public static final MemoryBase  R = new MemoryBase(REG, "R",  byte_t, REGS);
    public static final MemoryBase PC = new MemoryBase(REG, "PC", byte_t, 1);

    /*
    var SRC1[1, byte], SRC2[1, byte], DEST[1, byte]
    */

    public static final MemoryBase SRC1 = new MemoryBase(VAR, "SRC1", byte_t, 1);
    public static final MemoryBase SRC2 = new MemoryBase(VAR, "SRC2", byte_t, 1);
    public static final MemoryBase DEST = new MemoryBase(VAR, "DEST", byte_t, 1);
    
    /*
    MetaData Source (collections for memory and registers).  
    */
    
    public static final MemoryBase[] __REGISTERS = new MemoryBase[] { R, PC };
    public static final MemoryBase[] __MEMORY    = new MemoryBase[] { M };
}
