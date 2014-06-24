/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Operation.java, Nov 27, 2012 4:19:03 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.simnml.instruction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaArgument;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;

/**
 * The Operation abstract class is the base class for all classes that
 * simulate behavior specified by "op" Sim-nML statements. The class
 * provides definitions of classes to be used by its descendants
 * (generated classes that are to implement the IOperation interface). 
 * 
 * @author Andrei Tatarnikov
 */

public abstract class Operation implements IOperation
{
    /**
     * The ParamDecl class is aimed to specify declarations
     * operation parameters. 
     * 
     * @author Andrei Tatarnikov
     */

    public static final class ParamDecl
    {
        private final MetaArgument metaData;

        public ParamDecl(String name, IOperation.IInfo info)
        {
            this.metaData = new MetaArgument(
                name, operationsToTypeNameList(info.getMetaData()));
        }

        public ParamDecl(String name, IAddressingMode.IInfo info)
        {
            this.metaData = new MetaArgument(
                name, modesToTypeNameList(info.getMetaData()));
        }

        public MetaArgument getMetaData()
        {
            return metaData;
        }

        private static List<String> modesToTypeNameList(Collection<MetaAddressingMode> modes)
        {
            final List<String> result =
                new ArrayList<String>(modes.size());

            for (MetaAddressingMode mode : modes)
                result.add(mode.getName());

            return result;
        }

        private static List<String> operationsToTypeNameList(Collection<MetaOperation> ops)
        {
            final List<String> result =
                new ArrayList<String>(ops.size());

            for (MetaOperation op : ops)
                result.add(op.getName());

            return result;
        }
    }

    /**
     * The Info class is an implementation of the IInfo interface.
     * It is designed to store information about a single operation. 
     * The class is to be used by generated classes that implement
     * behavior of particular operations.
     * 
     * @author Andrei Tatarnikov
     */

    public static final class Info implements IInfo
    {
        private final Class<?> opClass;
        private final String   name;
        private final Collection<MetaOperation> metaData;

        public Info(Class<?> opClass, String name, ParamDecl[] params)
        {
            this.opClass  = opClass;
            this.name     = name;
            this.metaData = createMetaData(name, params);
        }

        private static Collection<MetaOperation> createMetaData(String name, ParamDecl[] params)
        {
            final List<MetaArgument> args =
                new ArrayList<MetaArgument>();

            for (ParamDecl p : params)
                args.add(p.getMetaData());

            return Collections.singletonList(
                new MetaOperation(name, args));
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public boolean isSupported(IOperation op)
        {
            return opClass.equals(op.getClass());
        }

        @Override
        public Collection<MetaOperation> getMetaData()
        {
            return metaData;
        }        
    }

    /**
     * The InfoOrRule class is an implementation of the IInfo interface
     * that provides logic for storing information about a group of operations
     * united by an OR-rule. The class is to be used by generated classes
     * that specify a set of operations united by an OR rule.
     * 
     * @author Andrei Tatarnikov
     */

    public static final class InfoOrRule implements IInfo
    {
        private final String  name;
        private final IInfo[] childs;
        private final Collection<MetaOperation> metaData;

        public InfoOrRule(String name, IInfo ... childs)
        {
            this.name   = name;
            this.childs = childs;
            this.metaData = createMetaData(name, childs);
        }

        private static Collection<MetaOperation> createMetaData(String name, IInfo[] childs)
        {
            final List<MetaOperation> result = new ArrayList<MetaOperation>();  

            for (IInfo i : childs)
                result.addAll(i.getMetaData());

            return Collections.unmodifiableCollection(result);
        }

        @Override
        public String getName() 
        { 
            return name;
        }

        @Override
        public boolean isSupported(IOperation op)
        {
            for (IInfo i : childs)
                if (i.isSupported(op))
                    return true;

            return false;
        }

        @Override
        public Collection<MetaOperation> getMetaData()
        {
            return metaData;
        } 
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
}
