package ru.ispras.microtesk.translator.simnml.coverage;

import ru.ispras.fortress.expression.*;
import ru.ispras.fortress.data.Data;

final class Expression
{
    public final static Node TRUE = new NodeValue(Data.newBoolean(true));
    public final static Node FALSE = new NodeValue(Data.newBoolean(false));

    public static NodeOperation EQ(Node lhs, Node rhs)
    {
        return new NodeOperation(StandardOperation.EQ, lhs, rhs);
    }

    public static NodeOperation EXTRACT(NodeValue bot, NodeValue top, Node bv)
    {
        return new NodeOperation(StandardOperation.BVEXTRACT, top, bot, bv);
    }

    public static NodeOperation EXTRACT(int bot, NodeValue top, Node bv)
    {
        return EXTRACT(NodeValue.newInteger(bot), top, bv);
    }

    public static NodeOperation EXTRACT(NodeValue bot, int top, Node bv)
    {
        return EXTRACT(bot, NodeValue.newInteger(top), bv);
    }

    public static NodeOperation CONCAT(Node ... args)
    {
        return new NodeOperation(StandardOperation.BVCONCAT, args);
    }

    public static NodeOperation AND(Node ... args)
    {
        return new NodeOperation(StandardOperation.AND, args);
    }

    public static NodeOperation OR(Node ... args)
    {
        return new NodeOperation(StandardOperation.OR, args);
    }

    public static NodeOperation STORE(Node array, Node key, Node value)
    {
        return new NodeOperation(StandardOperation.STORE, array, key, value);
    }

    public static NodeOperation SELECT(Node array, Node key)
    {
        return new NodeOperation(StandardOperation.SELECT, array, key);
    }
}
