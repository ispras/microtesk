/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * AddressingMode.java, Nov 27, 2012 2:48:19 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.simnml.instruction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.type.Type;

/**
 * The AddressingMode abstract class is the base class for all classes that
 * simulate behavior specified by "mode" Sim-nML statements. The class
 * provides definitions of classes and static methods to be used by its
 * descendants (ones that are to implement the IAddressingMode interface). 
 * 
 * @author Andrei Tatarnikov
 */

public abstract class AddressingMode implements IAddressingMode
{
    /**
     * The ParamDecl class is aimed to specify declarations
     * addressing mode parameters. 
     * 
     * @author Andrei Tatarnikov
     */

    public static final class ParamDecl
    {
        public final String name;
        public final   Type type;

        public ParamDecl(String name, Type type)
        {
            this.name = name;
            this.type = type;
        }        
    }
    
    /**
     * Create a table of addressing mode parameter declarations.
     * 
     * @param decls An array of addressing mode parameter declarations.
     * @return The table of addressing mode parameter declarations.
     */

    protected static Map<String, Type> createDeclarations(ParamDecl[] decls)
    {        
        final Map<String, Type> result = new LinkedHashMap<String, Type>();

        for (ParamDecl d : decls)
            result.put(d.name, d.type);

        return Collections.unmodifiableMap(result);
    }
    
    /**
     * Extracts the specified argument from the table of arguments and
     * wraps it into a location object. 
     * 
     * @param name The name of the argument.
     * @param decls A table of parameter declarations.
     * @param args A table of parameters.
     * @return The location that stores the specified addressing mode argument. 
     */

    protected static Location getArgument(String name, Map<String, Type> decls, Map<String, Data> args)
    {
        final Data data = args.get(name);

        assert decls.get(name).equals(data.getType()) :
            String.format("The %s parameter does not exist.", name);

        return new Location(data);
    }
    
    /**
     * The AddressingMode.Info class is an implementation of the IInfo interface
     * that provides logic for storing information about a single addressing mode.
     * The class is to be used by generated classes that implement behavior of
     * particular addressing modes.
     * 
     * @author Andrei Tatarnikov
     */

    protected static final class Info implements IInfo
    {
        private final Class<?>          modeClass;
        private final String            name;
        private final IFactory          factory;
        private final Map<String, Type> decls;
        
        private Collection<MetaAddressingMode> metaData;

        public Info(Class<?> modeClass, String name, IFactory factory, Map<String, Type> decls)
        {
            this.modeClass = modeClass;
            this.name      = name;
            this.factory   = factory;
            this.decls     = decls;
            this.metaData  = null;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public Map<String, IAddressingModeBuilderEx> createBuilders()
        {
            final IAddressingModeBuilderEx builder =
                new AddressingModeBuilder(name, factory, decls);

            return Collections.singletonMap(name, builder);
        }

        @Override
        public Collection<MetaAddressingMode> getMetaData()
        {
            if (null == metaData)
                metaData = createMetaData(name, decls.keySet());
            
            return metaData;
        }
        
        private static Collection<MetaAddressingMode> createMetaData(String name, Collection<String> argumentNames)
        {
            final MetaAddressingMode result =
                new MetaAddressingMode(name, argumentNames);

            return Collections.singletonList(result);
        }

        @Override
        public boolean isSupported(IAddressingMode mode)
        {
            return modeClass.equals(mode.getClass());
        }
    }
    
    /**
     * The InfoOrRule class is an implementation of the IInfo interface
     * that provides logic for storing information about a group of addressing
     * modes united by an OR-rule. The class is to be used by generated classes
     * that specify a set of addressing modes described by OR rules.
     * 
     * @author Andrei Tatarnikov
     */

    protected static final class InfoOrRule implements IInfo
    {
        private final String  name;
        private final IInfo[] childs;

        public InfoOrRule(String name, IInfo ... childs)
        {
            this.name   = name;
            this.childs = childs;
        }

        @Override
        public String getName() 
        { 
            return name;
        }

        @Override
        public Map<String, IAddressingModeBuilderEx> createBuilders()
        {
            final Map<String, IAddressingModeBuilderEx> result =
                new HashMap<String, IAddressingModeBuilderEx>();

            for (IInfo i : childs)
                result.putAll(i.createBuilders());

            return Collections.unmodifiableMap(result);
        }

        @Override
        public Collection<MetaAddressingMode> getMetaData()
        {
            final ArrayList<MetaAddressingMode> result = 
                 new ArrayList<MetaAddressingMode>();

            for (IInfo i : childs)
                result.addAll(i.getMetaData());

            return Collections.unmodifiableCollection(result);
        }

        @Override
        public boolean isSupported(IAddressingMode mode)
        {
            for (IInfo i : childs)
                if (i.isSupported(mode))
                    return true;

            return false;
        }        
    }

    /**
     * Basic generic implementation of the onBeforeLoad method.
     */

    @Override
    public void onBeforeLoad()
    {
        System.out.println(getClass().getSimpleName() + ": onBeforeLoad");
    }

    /**
     * Basic generic implementation of the onBeforeStore method.
     */

    @Override
    public void onBeforeStore()
    {
        System.out.println(getClass().getSimpleName() + ": onBeforeStore");        
    }

    /**
     * Default implementation of the syntax attribute. Provided to allow using 
     * addressing modes that have no explicitly specified syntax attribute. This
     * method does not do any useful work and should never be called. It is needed
     * only to let inherited classes compile.
     */

    @Override
    public String syntax()
    {
        // This code should never be called!
        assert false : "Default implementation. Should never be called!";
        return null;
    }
    
    /**
     * Default implementation of the image attribute. Provided to allow using 
     * addressing modes that have no explicitly specified image attribute. This
     * method does not do any useful work and should never be called. It is needed
     * only to let inherited classes compile.
     */

    @Override
    public String image()
    {
        // This code should never be called!
        assert false : "Default implementation. Should never be called!";
        return null;
    }

    /**
     * Default implementation of the action attribute. Provided to allow using 
     * addressing modes that have no explicitly specified action attribute. This
     * method does not do any useful work and should never be called. It is needed
     * only to let inherited classes compile.
     */

    public void action()
    {
        // This code should never be called!
        assert false : "Default implementation. Should never be called!";
    }

    /** 
     * Default implementation of the access method. The method is overridden
     * in concrete addressing mode class if the return expression was specified
     * for this addressing mode. 
     */

    @Override
    public Location access()
    {
        // This code should never be called!
        assert false : "Default implementation. Should never be called!";
        return null;
    }
}
