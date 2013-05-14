/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * SituationBuilder.java, May 14, 2013 4:19:03 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.block;

public final class SituationBuilder
{
    interface Setter
    {
        void setSituation(Situation situation);
    }

    private final Setter setter;
    private final String name;

    public SituationBuilder(Setter setter, String name)
    {
        assert null != setter;
        
        this.setter = setter;
        this.name = name;
    }

    public Situation build()
    {
        final Situation situation = new Situation(name);
        setter.setSituation(situation);
        return situation;
    }
}
