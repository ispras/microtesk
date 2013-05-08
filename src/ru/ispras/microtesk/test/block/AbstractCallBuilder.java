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

import ru.ispras.microtesk.model.api.instruction.IInstruction;
import ru.ispras.microtesk.model.api.simnml.instruction.AddressingModeImm;

public final class AbstractCallBuilder
{
    private final Map<String, Argument> arguments;
    private final Map<String, Object> attributes;

    protected AbstractCallBuilder(IInstruction instruction)
    {
        this.arguments  = new HashMap<String, Argument>();
        this.attributes = new HashMap<String, Object>();
    }

    public void setAttribute(String name, Object value)
    {
        assert !attributes.containsKey(name);
        attributes.put(name, value);
    }

    protected void setArgument(String name, Argument argument)
    {
        assert !arguments.containsKey(name);
        arguments.put(name, argument);
    }

    public ArgumentBuilder setArgumentUsingBuilder(String name, String modeName)
    {
        return new ArgumentBuilder(this,  name, modeName);
    }

    public void setArgumentImmediate(String name, int value)
    {
        final Argument.ModeArg valueArg =
            new Argument.ModeArg(AddressingModeImm.PARAM_NAME, value);

        final Argument argument = 
            new Argument(
               name,
               AddressingModeImm.NAME,
               Collections.singletonMap(valueArg.name, valueArg)
               );

        setArgument(name, argument);
    }

    public AbstractCall build()
    {
        return new AbstractCall(arguments, attributes);
    }
}
