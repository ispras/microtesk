package ru.ispras.microtesk.translator.simnml.coverage;

import java.util.Map;

import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;

public final class InstanceSpec
{
    final PrimitiveAND              origin;
    final Map<String, ArgumentSpec> arguments;

    InstanceSpec(PrimitiveAND origin, Map<String, ArgumentSpec> arguments)
    {
        this.origin = origin;
        this.arguments = arguments;
    }

    public PrimitiveAND getOrigin()
    {
        return origin;
    }

    public Map<String, ArgumentSpec> getArguments()
    {
        return arguments;
    }
}
