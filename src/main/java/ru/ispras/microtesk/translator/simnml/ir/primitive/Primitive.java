/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Primitive.java, Jul 9, 2013 11:32:13 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public class Primitive
{
    public static enum Kind
    {
        /** Addressing mode. */
        MODE,
        /** Operation. */
        OP,
        /** Immediate value. */
        IMM
    }

    public static final class Holder
    {
        private Primitive value;

        public Holder()
           { this.value = null; }

        public Holder(Primitive value)
           { assert value != null; this.value = value; } 

        public void setValue(Primitive value)
            { assert null == this.value : "Aready assigned."; this.value = value; }

        public Primitive getValue()
            { return value; }
    }

    /**
     * The Reference class describes references to the current primitive made
     * from another primitive (parent). There may be several reference since
     * a primitive (AND rule) can have several parameters of the same type.  
     */
    public static final class Reference
    {
        private final Primitive     source;
        private final Set<String> refNames;

        /**
         * Constructs a reference made from the source (parent)
         * primitive to the current primitive.
         */
        private Reference(Primitive source)
        {
            this.source   = source;
            this.refNames = new LinkedHashSet<String>();
        }

        /**
         * Registers a reference from the parent primitive to
         * the current primitive. 
         */
        private void addReference(String referenceName)
            { refNames.add(referenceName); }

        /** 
         * Returns the name of the primitive that makes a reference
         * to the current primitive.
         */
        public String getName()
            { return source.getName(); }

        /**
         * Returns the primitive the refers to the current primitive.
         */
        public Primitive getSource()
            { return source; }

        /**
         * Returns the number of references made from the parent
         * primitive to the current primitive.  
         */
        public int getReferenceCount()
            { return refNames.size(); }

        /**
         * Returns names of the references (parameter names) made from
         * the parent primitive to the current primitive.  
         */
        public Iterable<String> getReferenceNames()
            { return refNames; }
    }

    private final String                    name;
    private final Kind                      kind;
    private final boolean               isOrRule;
    private final Type                returnType;
    private final Set<String>          attrNames;
    private final Map<String, Reference> parents;

    Primitive(String name, Kind kind, boolean isOrRule, Type returnType, Set<String> attrNames)
    {
        this.name       = name;
        this.kind       = kind;
        this.isOrRule   = isOrRule;
        this.returnType = returnType;
        this.attrNames  = attrNames;
        this.parents    = new HashMap<String, Reference>();
    }

    Primitive(Primitive source)
    {
        this.name       = source.name;
        this.kind       = source.kind;
        this.isOrRule   = source.isOrRule;
        this.returnType = source.returnType;
        this.attrNames  = source.attrNames;
        this.parents    = source.parents;
    }

    /**
     * Registers a reference made from the parent primitive
     * to the current primitive.
     *  
     * @param parent Parent primitive.
     * @param referenceName The name of the reference (parameter) made from the 
     * parent primitive to the current primitive. 
     */
    protected void addParentReference(Primitive parent, String referenceName)
    {
        final Reference reference;
        if (parents.containsKey(parent.getName()))
        {
            reference = parents.get(parent.getName());
        }
        else
        {
            reference = new Reference(parent);
            parents.put(parent.getName(), reference);
        }
        reference.addReference(referenceName);
    }

    public final String getName()
    {
        if (null != name)
            return name;

        if (Kind.IMM == kind)
            return returnType.getJavaText();

        assert false : "Primitive name is not defined.";
        return null;
    }

    public final Kind getKind()
    {
        return kind;
    }

    public final boolean isOrRule()
    {
        return isOrRule;
    }

    public final Type getReturnType()
    {
        return returnType;
    }

    public final Set<String> getAttrNames()
    {
        return attrNames;
    }

    /**
     * Checks whether the current primitive is a root primitive.
     * A primitive is a root primitive if it does not have parents. 
     * 
     * @return true if it is a root primitive or false otherwise.
     */

    public final boolean isRoot()
    {
        return 0 == getParentCount();
    }

    /** 
     * Returns the collection of primitives (parents) that make 
     * references to the current primitive (have parameters of
     * the corresponding type).  
     */

    public final Iterable<Reference> getParents()
    {
        return parents.values(); 
    }

    /** 
     * Returns the number of primitives (parents) that make references
     * to the current primitive (have parameters of the corresponding type). 
     */

    public final int getParentCount()
    {
        return parents.size();
    }

    /** 
     * Returns the total number of references made to the current primitive
     * from parent primitives (the total number of parameters of all parent
     * primitives which have the corresponding type).   
     */

    public final int getParentReferenceCount()
    {
        int count = 0;
        for (Reference ref : getParents())
            count += ref.getReferenceCount();
        return count;
    }
}
