/*
 * Copyright (c) 2012 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ProcessorModel.java, Dec 3, 2012 10:10:15 AM Andrei Tatarnikov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.model.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.memory.Label;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.metadata.*;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.IOperation;
import ru.ispras.microtesk.model.api.instruction.IOperationBuilder;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.model.api.state.ModelStateObserver;
import ru.ispras.microtesk.model.api.state.Resetter;
import ru.ispras.microtesk.model.api.state.Status;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.UnsupportedTypeException;

/**
 * The ProcessorModel class is base class for all families of microprocessor
 * model classes. It implements all methods of the interfaces that provide
 * services to external users. The responsibility to initialize member data
 * (to create corresponding objects) is delegated to descendant classes. 
 * 
 * @author Andrei Tatarnikov
 */

public abstract class ProcessorModel implements IModel, ICallFactory
{
    public static final String SHARED_REGISTERS = "__REGISTERS";
    public static final String SHARED_MEMORY    = "__MEMORY";
    public static final String SHARED_VARIABLES = "__VARIABLES";
    public static final String SHARED_LABELS    = "__LABELS";
    public static final String SHARED_STATUSES  = "__STATUSES";
    public static final String SHARED_RESETTER  = "__RESETTER";

    private final String name;

    private final AddressingModeStore modes;
    private final OperationStore ops;

    private final IModelStateObserver observer;
    private final Resetter resetter;
    private final MetaModel metaModel;

    public ProcessorModel(
        String name,
        IAddressingMode.IInfo[] modes,
        IOperation.IInfo[] ops,
        Memory[] registers,
        Memory[] memory,
        Label[] labels,
        Status[] statuses,
        Resetter resetter
        )
    {
        this.name = name;

        this.modes = new AddressingModeStore(modes);
        this.ops = new OperationStore(ops); 

        this.observer = 
            new ModelStateObserver(registers, memory, labels, statuses);

        this.resetter = resetter;

        this.metaModel = new MetaModel(
            this.modes.getMetaData(),
            this.ops.getMetaData(),
            new MemoryStore(registers).getMetaData(),
            new MemoryStore(memory).getMetaData()
            );
    }

    public final String getName()
    {
        return name;
    }

    // IModel
    @Override
    public final MetaModel getMetaData()
    {
        return metaModel;
    }

    // IModel
    @Override
    public final IModelStateObserver getStateObserver()
    {
        return observer;
    }

    // IModel
    @Override
    public final ICallFactory getCallFactory()
    {
        return this;
    }

    // ICallFactory
    @Override
    public final IAddressingModeBuilder newMode(String name) throws ConfigurationException
    {
        final String ERROR_FORMAT = "The %s addressing mode is not defined.";

        if (null == name)
            throw new NullPointerException();

        final IAddressingMode.IInfo modeInfo = modes.getModeInfo(name); 

        if (null == modeInfo)
           throw new UnsupportedTypeException(String.format(ERROR_FORMAT, name));

        final Map<String, IAddressingModeBuilder> builders = modeInfo.createBuilders();

        final IAddressingModeBuilder result = builders.get(name);

        if (null == result)
            throw new  UnsupportedTypeException(String.format(ERROR_FORMAT, name));

        return result;
    }

    // ICallFactory
    @Override
    public final IOperationBuilder newOp(String name, String contextName) throws ConfigurationException
    {
        final String ERROR_FORMAT = "The %s operation is not defined.";

        if (null == name)
            throw new NullPointerException();

        final IOperation.IInfo opInfo = ops.getOpInfo(name); 

        if (null == opInfo)
           throw new UnsupportedTypeException(String.format(ERROR_FORMAT, name));

        Map<String, IOperationBuilder> builders = null;
        
        builders = opInfo.createBuildersForShortcut(contextName);
        if (null == builders)
            builders = opInfo.createBuilders();

        final IOperationBuilder result = builders.get(name);

        if (null == result)
            throw new  UnsupportedTypeException(String.format(ERROR_FORMAT, name));

        return result;
    }

    // ICallFactory
    @Override
    public InstructionCall newCall(IOperation op)
    {
        if (null == op)
            throw new NullPointerException();

        return new InstructionCall(resetter, op);
    }

    private static final class MemoryStore
    {
        private final Collection<MetaLocationStore> metaData;

        public MemoryStore(Memory[] memory)
        {
            this.metaData =
                new ArrayList<MetaLocationStore>(memory.length);

            for(Memory m : memory)
                this.metaData.add(m.getMetaData());
        }

        public Collection<MetaLocationStore> getMetaData()
        {
            return metaData;
        }
    }

    private static final class AddressingModeStore
    {
        private final Map<String, IAddressingMode.IInfo> items;
        private final Collection<MetaAddressingMode>  metaData;

        public AddressingModeStore(IAddressingMode.IInfo[] modes)
        {
            this.items = new HashMap<String, IAddressingMode.IInfo>(modes.length);
            this.metaData = new ArrayList<MetaAddressingMode>(modes.length);

            for(IAddressingMode.IInfo i : modes)
            {
                items.put(i.getName(), i);
                this.metaData.addAll(i.getMetaData());
            }
        }

        public IAddressingMode.IInfo getModeInfo(String name)
        {
            return items.get(name);
        }

        public Collection<MetaAddressingMode> getMetaData()
        {
            return metaData;
        }
    }

    private static final class OperationStore
    {
        private final Map<String, IOperation.IInfo> items;
        private final Collection<MetaOperation> metaData;

        public OperationStore(IOperation.IInfo[] ops)
        {
            this.items = new HashMap<String, IOperation.IInfo>(ops.length);
            this.metaData = new ArrayList<MetaOperation>(ops.length);

            for(IOperation.IInfo i : ops)
            {
                items.put(i.getName(), i);
                this.metaData.addAll(i.getMetaData());
            }
        }

        public IOperation.IInfo getOpInfo(String name)
        {
            return items.get(name);
        }

        public Collection<MetaOperation> getMetaData()
        {
            return metaData;
        }
    }
}
