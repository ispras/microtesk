/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LocationAtom.java, Aug 7, 2013 1:02:11 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

public final class LocationAtom implements Location
{
    public static interface Source
    {
        public ESymbolKind getSymbolKind();
        public TypeExpr getType();
    }

    public static final class MemorySource implements Source
    {
        private final MemoryExpr memory;

        private MemorySource(MemoryExpr memory)
        {
            assert null != memory;
            this.memory = memory;
        }

        @Override
        public ESymbolKind getSymbolKind()
        {
            return ESymbolKind.MEMORY;
        }

        @Override
        public TypeExpr getType()
        {
            return memory.getType();
        }

        public MemoryExpr getMemory()
        {
            return memory;
        }
    }

    public static final class PrimitiveSource implements Source
    {
        private final Primitive primitive;

        private PrimitiveSource(Primitive primitive)
        {
            assert null != primitive;
            this.primitive = primitive;
        }

        @Override
        public ESymbolKind getSymbolKind()
        {
            return ESymbolKind.ARGUMENT;
        }

        @Override
        public TypeExpr getType()
        {
            return primitive.getReturnType();
        }

        public Primitive getPrimitive()
        {
            return primitive;
        }
    }

    public static final class Bitfield
    {
        private final Expr     from;
        private final Expr       to;
        private final TypeExpr type;

        private Bitfield(Expr from, Expr to, TypeExpr type)
        {
            assert null != from;
            assert null != to;
            assert null != type;

            this.from = from;
            this.to   = to;
            this.type = type;
        }

        public Expr getFrom()
        {
            return from;
        }

        public Expr getTo()
        {
            return to;
        }

        public TypeExpr getType()
        {
            return type;
        }
    }

    private final String       name;
    private final Source     source;
    private final Expr        index;
    private final Bitfield bitfield;

    private LocationAtom(String name, Source source, Expr index, Bitfield bitfield)
    {
        assert null != name;
        assert null != source;

        this.name     = name;
        this.source   = source;
        this.index    = index;
        this.bitfield = bitfield;
    }

    static LocationAtom createMemoryBased(String name, MemoryExpr memory, Expr index)
    {
        return new LocationAtom(name, new MemorySource(memory), index, null);
    }

    static LocationAtom createPrimitiveBased(String name, Primitive primitive)
    {
        return new LocationAtom(name, new PrimitiveSource(primitive), null, null);
    }

    static LocationAtom createBitfield(LocationAtom location, Expr from, Expr to, TypeExpr type)
    {
        return new LocationAtom(location.getName(), location.getSource(), location.getIndex(), new Bitfield(from, to, type));
    }

    public String getName()
    {
        return name;
    }

    public Source getSource()
    {
        return source;
    }

    @Override
    public TypeExpr getType()
    {
        return source.getType();
    }

    public Expr getIndex()
    {
        return index;
    }

    public Bitfield getBitfield()
    {
        return bitfield;
    }
}
