/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LocationExprFactoryClass.java, Jan 22, 2013 5:58:35 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.antlrex.errors.SymbolTypeMismatch;
import ru.ispras.microtesk.translator.antlrex.errors.UndeclaredSymbol;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedPrimitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;
import ru.ispras.microtesk.translator.simnml.ir.shared.MemoryExpr;

public final class LocationExprFactoryClass
{
    private LocationExprFactoryClass() {}

    public static LocationExprFactory createFactory(WalkerContext context)
    {
        final LocationExprFactory result = new LocationExprFactoryImpl(context);
        return result;
        //return new LocationExprFactoryDebug(result, symbols);
    }
}

final class LocationExprFactoryImpl extends WalkerFactoryBase implements LocationExprFactory
{
    private List<LocationInfo> log; 

    public LocationExprFactoryImpl(WalkerContext context)
    {
        super(context);
        resetLog();
    }
    
    public void setLog(List<LocationInfo> locations)
    {
        log = locations;
    }
    
    public List<LocationInfo> getLog()
    {
        return log;
    }
    
    public void resetLog()
    {
        log = null;
    }

    @Override
    public LocationExpr location(
        Where where, String name, Map<String, Primitive> globalArgTypes) throws SemanticException
    {
        final ISymbol<ESymbolKind> symbol = findSymbol(where, name);
        final ESymbolKind kind = symbol.getKind();

        if ((ESymbolKind.MEMORY != kind) && (ESymbolKind.ARGUMENT != kind))
            raiseError(where, new SymbolTypeMismatch<ESymbolKind>(name, kind, Arrays.asList(ESymbolKind.MEMORY, ESymbolKind.ARGUMENT)));
        
        final LocationExpr result;

        if (ESymbolKind.MEMORY == kind)
        {
            final MemoryBasedLocationCreator creator = new MemoryBasedLocationCreator(where, name, null);
            result = creator.create();
        }
        else
        {
            final ArgumentBasedLocationCreator creator = new ArgumentBasedLocationCreator(where, name, globalArgTypes);
            result = creator.create();
        }
        
        if (null != log)
            log.add(new LocationInfo(name, kind, null));
        
        return result;
    }

    @Override
    public LocationExpr location(
        Where where, String name, Expr index) throws SemanticException
    {
        assert null != index;

        final ISymbol<ESymbolKind> symbol = findSymbol(where, name);
        final ESymbolKind kind = symbol.getKind();

        if (ESymbolKind.MEMORY != kind)
            raiseError(where, new SymbolTypeMismatch<ESymbolKind>(name, kind, ESymbolKind.MEMORY));

        final MemoryBasedLocationCreator creator = new MemoryBasedLocationCreator(where, name, index);
        final LocationExpr result = creator.create();

        if (null != log)
            log.add(new LocationInfo(name, kind, index));

        return result;
    }

    @Override
    public LocationExpr bitfield(
        Where w, LocationExpr loc, Expr start, Expr end)
    {
        assert null != loc;
        assert null != start;
        assert null != end;

        final int startPos = ((Number)start.getValue()).intValue();
        final int endPos = ((Number)end.getValue()).intValue();

        final int bitfieldSize = endPos - startPos + 1;
        final int locationSize = ((Number)loc.getType().getBitSize().getValue()).intValue();

        //assert startPos <= endPos; // TODO: restriction of the current implementation
        assert (startPos + bitfieldSize) <= locationSize; // TODO: raise a semantic error here!
        assert (endPos + bitfieldSize) <= locationSize; // TODO: raise a semantic error here!

        final String bitfieldExprText =
            String.format("%s.bitField(%s, %s)", loc.getText(), start.getText(), end.getText());
        
        final TypeExpr bitfieldExprType = new TypeExpr(
            loc.getType().getTypeId(),
            ExprClass.createConstant(bitfieldSize, Integer.toString(bitfieldSize))
        );

        return new LocationExpr(bitfieldExprText, bitfieldExprType);
    }

    @Override
    public LocationExpr concat(
        Where w, LocationExpr loc1, LocationExpr loc2)
    {
        assert null != loc1;
        assert null != loc2;

        final int locSize1 = ((Number)loc1.getType().getBitSize().getValue()).intValue();
        final int locSize2 = ((Number)loc2.getType().getBitSize().getValue()).intValue();
        final int concatSize = locSize1 + locSize2; 

        final String concatExprText =
            String.format("%s.concat(%s)", loc1.getText(), loc2.getText());
        
        final TypeExpr concatExprType = new TypeExpr(
            loc1.getType().getTypeId(),
            ExprClass.createConstant(concatSize, Integer.toString(concatSize))
        );

        return new LocationExpr(concatExprText, concatExprType);
    }

    private ISymbol<ESymbolKind> findSymbol(Where where, String name) throws SemanticException
    {
        final ISymbol<ESymbolKind> symbol = getSymbols().resolve(name);

        if (null == symbol)
            raiseError(where, new UndeclaredSymbol(name));

        return symbol;
    }

    private final class MemoryBasedLocationCreator
    {
        private final String LOCATION_ACCESS_FRMT = "%s.access(%s)";

        private final Where  where;
        private final String  name;
        private final Expr   index;

        public MemoryBasedLocationCreator(Where w, String name, Expr index)
        {
            this.where = w;
            this.name  = name;
            this.index = index;
        }

        public LocationExpr create() throws SemanticException
        {
            final MemoryExpr memory = findMemory();
            return new LocationExpr(getLocationText(), memory.getType());
        }

        private MemoryExpr findMemory() throws SemanticException
        {
            if (!getIR().getMemory().containsKey(name))
                raiseError(where, new UndefinedPrimitive(name, ESymbolKind.MEMORY));

            return getIR().getMemory().get(name);
        }

        private String getLocationText() throws SemanticException
        {
            if (null != index)
                checkIndexType(index);

            final String indexText = (null == index) ? "" : index.getText(); 
            return String.format(LOCATION_ACCESS_FRMT, name, indexText);
        }

        private void checkIndexType(final Expr index) throws SemanticException
        {
            final boolean isJavaExpr = 
                (index.getKind() == EExprKind.JAVA) || (index.getKind() == EExprKind.JAVA_STATIC); 

            if (!isJavaExpr)
                raiseError(where, new InvalidIndexExpression(index));

            assert null != index.getJavaType();

            final boolean isIntegerType = 
                index.getJavaType().equals(int.class) || index.getJavaType().equals(Integer.class); 

            if (!isIntegerType)
                raiseError(where, new InvalidIndexExpression(index));
        }

        private final class InvalidIndexExpression implements ISemanticError
        {
            private static final String FORMAT =
                "The %s expression cannot be used as an index. It should be a java-compatible expression " +
                "that can evaluated to an integer value.";

            private final Expr expr;

            public InvalidIndexExpression(Expr expr)
            {                
                this.expr = expr;
            }

            @Override
            public String getMessage()
            {
                return String.format(FORMAT, expr.getText(), expr.getKind().name());
            }
        }
    }

    private final class ArgumentBasedLocationCreator
    {
        private final Where where;
        private final String name;
        private final Map<String, Primitive> argTypes;

        public ArgumentBasedLocationCreator(
            Where w, String name, Map<String, Primitive> argTypes)
        {
            assert (null != argTypes) : "No information about arguments is provided.";

            this.where = w;
            this.name = name;
            this.argTypes = argTypes;
        }

        public LocationExpr create() throws SemanticException
        {
            final Primitive argType = findArgumentType();
            switch (argType.getKind())
            {
            case IMM:
                return createImmValueBasedLocation(argType);

            case MODE:
                return createModeBasedLocation(argType);

            default:
                reportUnexpectedType(argType);
                break;
            }

            assert false : "Should not reach here!";
            return null;
        }

        private Primitive findArgumentType() throws SemanticException
        {
            if (!argTypes.containsKey(name))
                raiseError(where, new UndefinedPrimitive(name, ESymbolKind.ARGUMENT));

            return argTypes.get(name);
        }

        public LocationExpr createImmValueBasedLocation(Primitive argType)
        {
            return new LocationExpr(name, argType.getReturnType());
        }

        public LocationExpr createModeBasedLocation(Primitive argType) throws SemanticException
        {
            return new LocationExpr(getLocationText(), argType.getReturnType());
        }

        private String getLocationText() throws SemanticException
        {
            return String.format("%s.access()", name);
        }

        private void reportUnexpectedType(final Primitive argType) throws SemanticException
        {
            raiseError(where, new ISemanticError()
            {
                private static final String FORMAT =
                    "The %s argument refers to a %s primitive that cannot be used as a location.";

                @Override
                public String getMessage()
                {
                    return String.format(FORMAT, name, argType.getKind());
                }
            });
        }
    }
}

final class LocationExprFactoryDebug implements LocationExprFactory
{
    private final LocationExprFactory factory;
    private final SymbolTable<ESymbolKind> symbols;

    public LocationExprFactoryDebug(
        LocationExprFactory factory,
        SymbolTable<ESymbolKind> symbols
        )
    {
        this.factory = factory;
        this.symbols = symbols;
    }
    
    public void setLog(List<LocationInfo> locations)
    {
        factory.setLog(locations);
    }
    
    public List<LocationInfo> getLog()
    {
        return factory.getLog();
    }
    
    public void resetLog()
    {
        factory.resetLog();
    }

    private void trace(String method, String text, LocationExpr result, String details)
    {
        final String FORMAT =
            "### TRACE (LocationExprFactory, %s): %s -> %s %s %s\n";

        System.out.printf(
            FORMAT,
            method,
            text,
            result.getText(),
            getTypeDescription(result.getType()),
            details
        );
    }
    
    private String getTypeDescription(TypeExpr type)
    {
        return String.format(
            "Type=[%s(%s), %s]",
            type.getTypeId().name(),
            type.getBitSize().getText(),
            type.getRefName()
            );
    }

    @Override
    public LocationExpr location(
        Where w, String name, Map<String, Primitive> globalArgTypes) throws SemanticException
    {
        final LocationExpr result =
            factory.location(w, name, globalArgTypes);

        final ESymbolKind kind =
            symbols.resolve(name).getKind();

        final String symbolInfo;
        if (kind == ESymbolKind.ARGUMENT)
        {
            final Primitive argType = globalArgTypes.get(name);

            symbolInfo = String.format(
                "%s(%s %s)",
                kind.name(),
                argType.getKind().name(),
                argType.getName()
                );
        }
        else
        {
            symbolInfo = kind.name();
        }

        trace(
            "location",
            name,
            result,
            String.format("Symbol=%s", symbolInfo)
            );

        return result;
    }

    @Override
    public LocationExpr location(
        Where w, String name, Expr index) throws SemanticException
    {
        final LocationExpr result = factory.location(w, name, index);

        final ESymbolKind kind = symbols.resolve(name).getKind();

        trace(
            "location",
            String.format("%s[%s]", name, index.getText()),
            result,
            String.format("Symbol=%s", kind.name())
            );

        return result;
    }

    @Override
    public LocationExpr bitfield(
        Where w, LocationExpr loc, Expr start, Expr end) throws SemanticException
    {
        final LocationExpr result = factory.bitfield(w, loc, start, end);

        trace(
            "bitfield",
            "",
            result,
            ""
            );

        return result;
    }

    @Override
    public LocationExpr concat(
        Where w, LocationExpr loc1, LocationExpr loc2) throws SemanticException
    {
        final LocationExpr result = factory.concat(w, loc1, loc2);

        trace(
            "concat",
            "",
            result,
            ""
            );

        return result;
    }
}
