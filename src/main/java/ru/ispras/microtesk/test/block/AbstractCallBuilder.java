/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * CallBuilder.java, May 6, 2013 5:22:10 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.block;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.instruction.AddressingModeImm;

public final class AbstractCallBuilder
{
    private final String name;
    private final Map<String, Argument> arguments;
    private final Map<String, Object> attributes;
    private Situation situation;
    
    private final ArgumentBuilder.Setter argumentSetter = new ArgumentBuilder.Setter()
    {
        @Override
        public void setArgument(String name, Argument argument)
        {
            assert !arguments.containsKey(name);
            arguments.put(name, argument);
        }
    }; 
    
    private final SituationBuilder.Setter situationSetter = new SituationBuilder.Setter()
    {
        @Override
        public void setSituation(Situation asituation)
        {
            assert null == situation;
            situation = asituation;
        }
    }; 

    protected AbstractCallBuilder(String name)
    {
        this.name       = name;
        this.arguments  = new HashMap<String, Argument>();
        this.attributes = new HashMap<String, Object>();
        this.situation  = null;
    }

    public void setAttribute(String name, Object value)
    {
        assert !attributes.containsKey(name);
        attributes.put(name, value);
    }
   
    public ArgumentBuilder setArgumentUsingBuilder(String name, String modeName)
    {
        return new ArgumentBuilder(argumentSetter, name, modeName);
    }

    public Argument setArgumentImmediate(String name, int value)
    {
        final Argument.ModeArg valueArg =
            new Argument.ModeArg(AddressingModeImm.PARAM_NAME, value);

        final Argument argument = 
            new Argument(
               name,
               AddressingModeImm.NAME,
               Collections.singletonMap(valueArg.name, valueArg)
               );

        argumentSetter.setArgument(name, argument);
        return argument;
    }

    public Argument setArgumentImmediateRandom(String name)
    {
        final Argument.ModeArg valueArg =
            new Argument.ModeArg(AddressingModeImm.PARAM_NAME);

        final Argument argument =
            new Argument(
                name,
                AddressingModeImm.NAME,
                Collections.singletonMap(valueArg.name, valueArg)
                );

        argumentSetter.setArgument(name, argument);
        return argument;
    }

    public SituationBuilder setTestSituation(String name)
    {
        return new SituationBuilder(situationSetter, name);
    }

    public AbstractCall build()
    {
        return new AbstractCall(name, arguments, attributes, situation);
    }
}
