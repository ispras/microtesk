package ru.ispras.microtesk.translator.simnml.coverage;

import ru.ispras.fortress.expression.Node;

import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;

public final class ArgumentSpec
{
    public final InstanceSpec   instance;
    public final Node           immediate;

    private ArgumentSpec(InstanceSpec instance, Node immediate)
    {
        this.instance = instance;
        this.immediate = null;
    }

    public static ArgumentSpec createInstanceArgument(InstanceSpec instance)
    {
        return new ArgumentSpec(instance, null);
    }

    public static ArgumentSpec createImmediateArgument(Node immediate)
    {
        return new ArgumentSpec(null, immediate);
    }

    public boolean isMode()
    {
        return !isImmediate()
            && instance.getOrigin().getKind() == Primitive.Kind.MODE;
    }

    public boolean isOp()
    {
        return !isImmediate()
            && instance.getOrigin().getKind() == Primitive.Kind.OP;
    }

    public boolean isImmediate()
    {
        return immediate != null;
    }
}
