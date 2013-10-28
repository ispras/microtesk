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
import java.util.Iterator;
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

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        final LocationConcat other = (LocationConcat) obj;

        if (!getType().equals(other.getType()))
            return false;

        if (locations.size() != other.locations.size())
            return false;

        final Iterator<LocationAtom> thisIterator = locations.iterator();
        final Iterator<LocationAtom> otherIterator = other.getLocations().iterator();

        while(thisIterator.hasNext() && otherIterator.hasNext() )
        {
            if (!thisIterator.next().equals(otherIterator.next()))
                return false;
        }

        return true;
    }
}
