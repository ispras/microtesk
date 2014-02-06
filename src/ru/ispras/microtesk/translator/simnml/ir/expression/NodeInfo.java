/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * NodeInfo.java, Jan 27, 2014 2:09:43 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.expression.Node;
import ru.ispras.microtesk.translator.simnml.ir.location.Location;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfo;

/*

Design notes:

  Kind (kind of the node, determines the set of maintained attributes).
  Source (Location, NamedConstant, Constant, Operator (including conditions), depending on Kind).
  ValueInfo (current, resulting, top-level, final value).

  Coercions (coercion (explicit cast) can be applied zero or more times to all element kinds):
    ValueInfoBeforeCoercion, array of ValueInfo: first is initial value before first coercion,
    last is value before final coercion. Value after the final coercion is ValueInfo.

  # For operators (goes to the 'source' object):
  #   CastValueInfo (operands are cast to a common type (implicit cast), if their types are different).
  
  Question?
  
  Mapping of MicroTESK data types to SMT-LIB data types? 

*/

public final class NodeInfo
{
    public static enum Kind
    {
        LOCATION    (Location.class,       Node.Kind.VARIABLE),
        NAMED_CONST (LetConstant.class,    Node.Kind.VALUE),
        CONST       (SourceConstant.class, Node.Kind.VALUE),
        OPERATOR    (SourceOperator.class, Node.Kind.EXPR);

        private final Class<?>  sourceClass;
        private final Node.Kind    nodeKind;

        private Kind(Class<?> sourceClass, Node.Kind nodeKind)
        { 
            this.sourceClass = sourceClass;
            this.nodeKind    = nodeKind;
        }

        boolean isCompatibleSource(Object source)
        {
            return this.sourceClass.isAssignableFrom(source.getClass());
        }

        boolean isCompatibleNode(Node.Kind nodeKind)
        {
            return this.nodeKind == nodeKind;
        }
    }

    static NodeInfo newLocation(Location source)
    {
        checkNotNull(source);

        return new NodeInfo(
            NodeInfo.Kind.LOCATION, source, ValueInfo.createModel(source.getType()));
    }

    static NodeInfo newNamedConst(LetConstant source)
    {
        checkNotNull(source);

        return new NodeInfo(
            NodeInfo.Kind.NAMED_CONST, source, source.getExpr().getValueInfo());
    }

    static NodeInfo newConst(SourceConstant source)
    {
        checkNotNull(source);

        return new NodeInfo(
            NodeInfo.Kind.CONST, source, ValueInfo.createNative(source.getValue()));
    }

    static NodeInfo newOperator(SourceOperator source)
    {
        checkNotNull(source);

        return new NodeInfo(
            NodeInfo.Kind.OPERATOR, source, source.getResultValueInfo());
    }

    private final Kind            kind;
    private final Object          source;
    private final ValueInfo       valueInfo;
    private final List<ValueInfo> coercionChain;

    private NodeInfo(
        Kind            kind,
        Object          source,
        ValueInfo       valueInfo,
        List<ValueInfo> coercionChain
        )
    {
        if (!kind.isCompatibleSource(source))
            throw new IllegalArgumentException(
                String.format("%s is not proper source for %s.", source.getClass().getSimpleName(), kind));

        this.kind          = kind;
        this.source        = source;
        this.valueInfo     = valueInfo;
        this.coercionChain = Collections.unmodifiableList(coercionChain);
    }

    private NodeInfo(Kind kind, Object source, ValueInfo valueInfo)
    {
        this(kind, source, valueInfo, Collections.<ValueInfo>emptyList());
    }

    public NodeInfo coerceTo(ValueInfo valueInfo)
    {
        checkNotNull(valueInfo);

        if (getValueInfo().equals(valueInfo))
            return this;

        final List<ValueInfo> coercions = new ArrayList<ValueInfo>(getCoercionChain());
        coercions.add(getValueInfo());

        return new NodeInfo(
            getKind(),
            getSource(),
            valueInfo,
            coercions
            );
    }

    public Kind getKind()
    {
        return kind;
    }

    public Object getSource()
    {
        return source;
    }

    public ValueInfo getValueInfo()
    {
        return valueInfo;
    }

    public boolean isCoersionApplied()
    {
        return !coercionChain.isEmpty();  
    }

    public List<ValueInfo> getCoercionChain()
    {
        return coercionChain;
    }

    private static void checkNotNull(Object o)
    {
        if (null == o)
            throw new NullPointerException();
    }
}
