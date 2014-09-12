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

import ru.ispras.microtesk.model.api.memory.Label;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.state.Resetter;
import ru.ispras.microtesk.model.api.state.Status;
import ru.ispras.microtesk.model.api.type.Type;

import static ru.ispras.microtesk.model.api.memory.EMemoryKind.*;

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

    public static final Type  index = Type.CARD(6);
    public static final Type nibble = Type.CARD(4);
    public static final Type byte_t = Type.CARD(8);

    /*
    mem M[MSIZE, byte]
    reg R[REGS, byte]
    reg PC[1, byte]
    */

    public static final Memory  M = new Memory(MEM, "M",  byte_t, MSIZE); 
    public static final Memory  R = new Memory(REG, "R",  byte_t, REGS);
    public static final Memory PC = new Memory(REG, "PC", byte_t, 1);

    /*
    var SRC1[1, byte], SRC2[1, byte], DEST[1, byte]
    */

    public static final Memory SRC1 = new Memory(VAR, "SRC1", byte_t, 1);
    public static final Memory SRC2 = new Memory(VAR, "SRC2", byte_t, 1);
    public static final Memory DEST = new Memory(VAR, "DEST", byte_t, 1);

    /*
    MetaData Source (collections for memory and registers).  
    */

    public static final Memory[] __REGISTERS = new Memory[] { R, PC };
    public static final Memory[]    __MEMORY = new Memory[] { M };
    public static final Memory[] __VARIABLES = new Memory[] {};
    public static final Label[]     __LABELS = new Label[] {};

    public static final Status   __CTRL_TRANSFER = new Status("__CTRL_TRANSFER", 0);
    public static final Status[]      __STATUSES = {__CTRL_TRANSFER};

    public static final Resetter __RESETTER = new Resetter(__VARIABLES, __STATUSES);
}
