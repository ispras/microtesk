/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LocationConcat.java, Aug 7, 2013 12:35:38 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import java.util.Collections;
import java.util.List;

import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class LocationConcat implements Location
{
    private final Type type;
    private final List<LocationAtom> locations;

    LocationConcat(Type type, List<LocationAtom> locations)
    {
        assert null != type;
        assert null != locations;

        this.type = type;
        this.locations = Collections.unmodifiableList(locations);
    }

    @Override
    public Type getType()
    {
        return type;
    }

    public List<LocationAtom> getLocations()
    {
        return locations;
    }
}
