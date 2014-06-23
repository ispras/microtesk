/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Situation.java, May 15, 2013 2:36:11 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.situation;

import ru.ispras.microtesk.model.api.metadata.MetaSituation;

public abstract class Situation implements ISituation
{
    private final IInfo info;

    public Situation(IInfo info)
    {
        assert null != info; 
        this.info = info;
    }
    
    protected final IInfo getInfo()
    {
        return info;
    }

    public static final class Info implements IInfo
    {
        private final String name;
        private final IFactory factory;
        private final MetaSituation metaData;

        public Info(String name, IFactory factory)
        {
            assert null != factory;

            this.name     = name;
            this.factory  = factory; 
            this.metaData = new MetaSituation(name);
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public MetaSituation getMetaData()
        {
            return metaData;
        }

        @Override
        public ISituation createSituation()
        {
            return factory.create();
        }
    }
}
