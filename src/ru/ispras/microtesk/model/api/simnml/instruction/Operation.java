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

        public Info(Class<?> opClass, String name)
        {
            this.opClass = opClass;
            this.name = name;
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
        public boolean isSupported(IOperation op)
        {
            for (IInfo i : childs)
                if (i.isSupported(op))
                    return true;

            return false;
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
