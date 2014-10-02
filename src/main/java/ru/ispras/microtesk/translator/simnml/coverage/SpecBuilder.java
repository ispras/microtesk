package ru.ispras.microtesk.translator.simnml.coverage;

import java.util.Collections;

public final class SpecBuilder
{
    public InstanceSpec build()
    {
        return new InstanceSpec(null, Collections.<String, ArgumentSpec>emptyMap());
    }
}
