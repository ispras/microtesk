/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * InstructionBase.java, Nov 28, 2012 12:20:03 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.simnml.instruction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.config.UndeclaredException;
import ru.ispras.microtesk.model.api.exception.config.UninitializedException;

import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaInstruction;
import ru.ispras.microtesk.model.api.metadata.MetaSituation;
import ru.ispras.microtesk.model.api.metadata.MetaArgument;
import ru.ispras.microtesk.model.api.situation.ISituation;

import ru.ispras.microtesk.model.api.instruction.IInstructionEx;
import ru.ispras.microtesk.model.api.instruction.IArgumentBuilder;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBuilderEx;

/**
 * The InstructionBase abstract class is a base class for all instructions modeled
 * based on a Sim-nML specification. It provides class definitions and method
 * implementations for its descendants (ones that are to implement the IInstruction
 * interface). In particular, it contains common implementation of instruction meta
 * data provider and a base class for instruction call builders. 
 * 
 * @author Andrei Tatarnikov
 */

public abstract class InstructionBase implements IInstructionEx
{
    /**
     * The ParamDecl class specifies a parameter declaration for an instruction.
     * 
     * @author Andrei Tatarnikov
     */

    public static final class ParamDecl
    {
        public final String                name;
        public final IAddressingMode.IInfo info;

        public ParamDecl(String name, IAddressingMode.IInfo info)
        {
            this.name = name;
            this.info = info;
        }        
    }

    private final String name;
    private final Map<String, ISituation.IInfo> situations;
    private final MetaInstruction metaData;

    /**
     * Creates basic logic for some specified instruction based on its 
     * name and the list of its parameters. 
     *  
     * @param name Instruction name.
     * @param params List of instruction parameter declarations.
     */

    public InstructionBase(String name, ParamDecl[] params, ISituation.IInfo[] situationInfos)
    {
        if (null == name)
            throw new NullPointerException("name");

        if (null == params)
            throw new NullPointerException("params");

        if (null == situationInfos)
            throw new NullPointerException("situationInfos");

        this.name       = name;
        this.situations = params.length > 0 ? createSituations(situationInfos) : Collections.<String, ISituation.IInfo>emptyMap();
        this.metaData   = createMetaData(name, params, situations.values());
    }

    private static Map<String, ISituation.IInfo> createSituations(ISituation.IInfo[] situationInfos)
    {
        final Map<String, ISituation.IInfo> result =
            new HashMap<String, ISituation.IInfo>();

        for (ISituation.IInfo si : situationInfos)
            result.put(si.getName(), si);

        return result;
    }

    private static MetaInstruction createMetaData(
         String name,
         ParamDecl[] params,
         Collection<ISituation.IInfo> situations
         )
    {
        final Collection<MetaArgument> metaParams = new ArrayList<MetaArgument>();

        for (ParamDecl p : params)
        {
            final List<String> typeNames = 
                new ArrayList<String>(p.info.getMetaData().size());

            for (MetaAddressingMode am : p.info.getMetaData())
                typeNames.add(am.getName());

            metaParams.add(new MetaArgument(p.name, typeNames));
        }

        final List<MetaSituation> metaSituations = new ArrayList<MetaSituation>();
        for (ISituation.IInfo situation : situations)
            metaSituations.add(situation.getMetaData());

        return new MetaInstruction(name, metaParams, metaSituations);
    }

    @Override
    public final String getName()
    {
        return name;
    }

    @Override
    public final MetaInstruction getMetaData()
    {
        return metaData;
    }
    
    public ISituation createSituation(String name) throws ConfigurationException
    {
        final String ERROR_FORMAT = 
            "The %s situation cannot be assosiated with the %s instruction.";

        if (!situations.containsKey(name))
            throw new UndeclaredException(String.format(ERROR_FORMAT, name, getName()));

        return situations.get(name).createSituation();
    }

    /**
     * The CallBuilderBase abstract class is the base implementation for all
     * instruction call builders. It provides logic for initializing and
     * accessing instruction parameters based in the list of their declaration
     * passed by a constructor of a descendant class.  
     * 
     * @author Andrei Tatarnikov
     */

    protected static abstract class CallBuilderBase implements IInstructionCallBuilderEx
    {
        private final Map<String, IArgumentBuilderEx> argBuilders;

        /**
         * Creates basic logic for building instruction arguments based on the
         * list of parameter declarations.
         *  
         * @param params Parameter declarations.
         */

        public CallBuilderBase(ParamDecl[] params)
        {
            this.argBuilders = createArgumentBuilders(params);
        }

        private static Map<String, IArgumentBuilderEx> createArgumentBuilders(ParamDecl[] params)
        {
            final Map<String, IArgumentBuilderEx> result = new HashMap<String, IArgumentBuilderEx>();
            
            for (ParamDecl p : params)
                result.put(p.name, new ArgumentBuilder(p.info.createBuilders()));
            
            return Collections.unmodifiableMap(result);
        } 

        @Override
        public final IArgumentBuilder getArgumentBuilder(String name) throws ConfigurationException
        {
            checkUndeclaredArgument(name);
            return argBuilders.get(name);
        }

        /**
         * Gets an addressing mode describing the specified instruction argument.
         * The method finds a corresponding argument builder and requests a
         * product from it. If the argument builder did not gather the information
         * needed to build the product, it generates a configuration error.
         * 
         * @param name Name of the instruction argument.
         * @return The addressing mode object providing access to the specified instruction argument.
         * @throws ConfigurationException Exception that informs of an error that occurs on
         * attempt to build an addressing mode object due to incorrect configuration.
         */ 

        protected final IAddressingMode getArgument(String name) throws ConfigurationException
        {
            checkUndeclaredArgument(name);
            checkUninitializedArgument(name);
            
            return argBuilders.get(name).getProduct(); 
        }

        private void checkUndeclaredArgument(String name) throws UndeclaredException
        {
            final String ERROR_FORMAT = 
                "The %s argument is not declared for the current instruction.";

            if (!argBuilders.containsKey(name))
                throw new UndeclaredException(String.format(ERROR_FORMAT, name));
        }

        private void checkUninitializedArgument(String name) throws ConfigurationException
        {
            final String ERROR_FORMAT = 
                 "The %s argument is not initialized for the current instruction call.";

            if (null == argBuilders.get(name).getProduct())
                throw new UninitializedException(String.format(ERROR_FORMAT, name));                 
        }
    }
}
