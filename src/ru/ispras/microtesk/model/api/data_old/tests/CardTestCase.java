/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * $Id: CardTestCase.java, Oct 17, 2012 6:54:57 PM Andrei Tatarnikov Exp $
 */

package ru.ispras.microtesk.model.api.data_old.tests;

import org.junit.Test;

import ru.ispras.microtesk.model.api.data_old.Card;


public class CardTestCase
{
    @Test
    public void test()
    {
        System.out.println(new Card(10, 16).intValue());
        System.out.println(new Card(10, 16).toBinString());
        // TODO
        //fail("Not yet implemented");
    }
}
