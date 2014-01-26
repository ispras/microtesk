/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LocationFactory.java, Aug 7, 2013 12:48:09 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.errors.SymbolTypeMismatch;
import ru.ispras.microtesk.translator.antlrex.errors.UndeclaredSymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedPrimitive;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.expression.ExprUtils;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class LocationFactory extends WalkerFactoryBase
{
    private static final String OUT_OF_BOUNDS =
        "The bitfield expression tries to access bit %d which is beyond location bounds (%d bits).";

    private static final String FAILED_TO_CALCULATE_SIZE =
        "Unable to calculate bitfield size. The given bitfield expressions cannot be reduced to constant value.";

    private List<LocationAtom> log; 

    public void setLog(List<LocationAtom> locations)
    {
        log = locations;
    }

    public List<LocationAtom> getLog()
    {
        return log;
    }

    public void resetLog()
    {
        log = null;
    }
    
    private void addToLog(LocationAtom location)
    {
        if (null != log)
            log.add(location);
    }

    public LocationFactory(WalkerContext context)
    {
        super(context);
        resetLog();
    }

    public LocationAtom location(Where where, String name) throws SemanticException
    {
        final ISymbol<ESymbolKind> symbol = findSymbol(where, name);
        final ESymbolKind kind = symbol.getKind();

        if ((ESymbolKind.MEMORY != kind) && (ESymbolKind.ARGUMENT != kind))
            raiseError(where, new SymbolTypeMismatch<ESymbolKind>(name, kind, Arrays.asList(ESymbolKind.MEMORY, ESymbolKind.ARGUMENT)));

        final LocationCreator creator = (ESymbolKind.MEMORY == kind) ?
            new MemoryBasedLocationCreator(this, where, name, null) :
            new ArgumentBasedLocationCreator(this, where, name);

        final LocationAtom result = creator.create();

        addToLog(result);
        return result;
    }

    public LocationAtom location(Where where, String name, Expr index) throws SemanticException
    {
        assert null != index;

        final ISymbol<ESymbolKind> symbol = findSymbol(where, name);
        final ESymbolKind kind = symbol.getKind();

        if (ESymbolKind.MEMORY != kind)
            raiseError(where, new SymbolTypeMismatch<ESymbolKind>(name, kind, ESymbolKind.MEMORY));

        final LocationCreator creator = new MemoryBasedLocationCreator(this, where, name, index);
        final LocationAtom result = creator.create();

        addToLog(result);
        return result;
    }

    public LocationAtom bitfield(Where where, LocationAtom location, Expr pos) throws SemanticException
    {
        assert null != location;
        assert null != pos;

        if (pos.getValueInfo().isConstant())
            checkBitfieldBounds(where, ExprUtils.integerValue(pos), location.getType().getBitSize());

        final Type bitfieldType = new Type(location.getType().getTypeId(), ExprUtils.createConstant(1));
        return LocationAtom.createBitfield(location, pos, pos, bitfieldType);
    }

    public LocationAtom bitfield(Where where, LocationAtom location, Expr from, Expr to) throws SemanticException
    {
        assert null != location;
        assert null != from;
        assert null != to;

        if (from.getValueInfo().isConstant() != to.getValueInfo().isConstant())
            raiseError(where, FAILED_TO_CALCULATE_SIZE);

        if (from.getValueInfo().isConstant())
        {
            final int fromPos = ExprUtils.integerValue(from);
            final int toPos = ExprUtils.integerValue(to);
            final int locationSize = location.getType().getBitSize();

            checkBitfieldBounds(where, fromPos, locationSize);
            checkBitfieldBounds(where, toPos, locationSize);

            final int  bitfieldSize = Math.abs(toPos - fromPos) + 1;
            final Type bitfieldType = new Type(location.getType().getTypeId(), ExprUtils.createConstant(bitfieldSize));

            return LocationAtom.createBitfield(location, from, to, bitfieldType);
        }

        final ExprUtils.ReducedExpr reducedFrom = ExprUtils.reduce(from);
        final ExprUtils.ReducedExpr reducedTo   = ExprUtils.reduce(to);

        if (null == reducedFrom || null == reducedTo)
            raiseError(where, FAILED_TO_CALCULATE_SIZE);

        assert null != reducedFrom.polynomial; // Cannot be reduced to constant at this point
        assert null != reducedTo.polynomial;   // Cannot be reduced to constant at this point

        if (reducedFrom.polynomial.isEquivalent(reducedTo.polynomial))
        {
            final int  bitfieldSize = Math.abs(reducedTo.constant - reducedFrom.constant) + 1;
            final Type bitfieldType = new Type(location.getType().getTypeId(), ExprUtils.createConstant(bitfieldSize));

            return LocationAtom.createBitfield(location, from, to, bitfieldType);
        }

        raiseError(where, FAILED_TO_CALCULATE_SIZE);
        return null;
    }

    private void checkBitfieldBounds(Where w, int position, int size) throws SemanticException
    {
        if (!(0 <= position && position < size))
            raiseError(w, String.format(OUT_OF_BOUNDS, position, size));
    }

    public LocationConcat concat(Where w, LocationAtom left, Location right)
    {
        assert null != left;
        assert null != right;

        final int   leftSize = left.getType().getBitSize();
        final int  rightSize = right.getType().getBitSize();
        final int concatSize = leftSize + rightSize; 

        final Type concatType = new Type(
            left.getType().getTypeId(), ExprUtils.createConstant(concatSize));

        if (right instanceof LocationAtom)
            return new LocationConcat(concatType, Arrays.asList((LocationAtom) right, left));

        final List<LocationAtom> concatenated = new ArrayList<LocationAtom>(((LocationConcat) right).getLocations());
        concatenated.add(left);

        return new LocationConcat(concatType, concatenated);
    }

    private ISymbol<ESymbolKind> findSymbol(Where where, String name) throws SemanticException
    {
        final ISymbol<ESymbolKind> symbol = getSymbols().resolve(name);

        if (null == symbol)
            raiseError(where, new UndeclaredSymbol(name));

        return symbol;
    }
}

interface LocationCreator
{
    public LocationAtom create() throws SemanticException;
}

final class MemoryBasedLocationCreator extends WalkerFactoryBase implements LocationCreator
{
    private final Where where;
    private final String name;
    private final Expr  index;

    public MemoryBasedLocationCreator(WalkerContext context, Where where, String name, Expr index)
    {
        super(context);

        this.where = where;
        this.name  = name;
        this.index = index;
    }

    @Override
    public LocationAtom create() throws SemanticException
    {
        final MemoryExpr memory = findMemory();
        return LocationAtom.createMemoryBased(name, memory, index);
    }

    private MemoryExpr findMemory() throws SemanticException
    {
        if (!getIR().getMemory().containsKey(name))
            raiseError(where, new UndefinedPrimitive(name, ESymbolKind.MEMORY));

        return getIR().getMemory().get(name);
    }
}

final class ArgumentBasedLocationCreator extends WalkerFactoryBase implements LocationCreator
{
    private static final String UNEXPECTED_PRIMITIVE =
        "The %s argument refers to a %s primitive that cannot be used as a location.";

    private final Where where;
    private final String name;

    public ArgumentBasedLocationCreator(WalkerContext context, Where where, String name)
    {
        super(context);

        this.where = where;
        this.name  = name;
    }

    @Override
    public LocationAtom create() throws SemanticException
    {
        final Primitive primitive = findArgument();

        if ((Primitive.Kind.MODE != primitive.getKind()) && (Primitive.Kind.IMM != primitive.getKind()))
            raiseError(where, String.format(UNEXPECTED_PRIMITIVE, name, primitive.getKind()));            

        return LocationAtom.createPrimitiveBased(name, primitive);
    }

    private Primitive findArgument() throws SemanticException
    {
        if (!getThisArgs().containsKey(name))
            raiseError(where, new UndefinedPrimitive(name, ESymbolKind.ARGUMENT));

        return getThisArgs().get(name);
    }
}
