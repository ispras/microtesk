/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ArmRegisterXInitializerGenerator.java, May 23, 2013 7:31:40 PM Andrei
 * Tatarnikov
 */

package ru.ispras.microtesk.test.data;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.test.block.Argument;

public class ArmRegisterXInitializerGenerator implements IInitializerGenerator 
{
    private final IModel model;

    public ArmRegisterXInitializerGenerator(IModel model)
    {
        assert null != model;
        this.model = model;
    }

    @Override
    public boolean isCompatible(Argument dest)
    {
        final String REX = "^REGISTER[\\d]{1,2}$";
        final Matcher matcher = Pattern.compile(REX).matcher(dest.getModeName());
        return matcher.matches();
    }

    @Override
    public List<ConcreteCall> createInitializingCode(Argument dest, Data data) throws ConfigurationException
    {
        System.out.println("initializing " + dest.getModeName());
        
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }
}
