/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Block.java, May 6, 2013 1:32:37 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.block;

import ru.ispras.microtesk.test.core.Sequence;
import ru.ispras.microtesk.test.core.iterator.IIterator;

public interface Block
{
    public IIterator<Sequence<Call>> getIterator();
}
