/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LocationPrinter.java, Aug 9, 2013 3:32:22 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.generation.utils;

import ru.ispras.microtesk.translator.simnml.ir.expression.Location;
import ru.ispras.microtesk.translator.simnml.ir.expression.LocationAtom;
import ru.ispras.microtesk.translator.simnml.ir.expression.LocationConcat;

public final class LocationPrinter
{
    private static final String   ACCESS_FORMAT = "%s.access(%s)";
    private static final String BITFIELD_FORMAT = ".bitField(%s, %s)";
    private static final String   CONCAT_FORMAT = "Location.concat(%s)";

    private LocationPrinter() {}

    public static String toString(Location location)
    {
        if (location instanceof LocationConcat)
           return toString((LocationConcat) location);
        return toString((LocationAtom) location);
    }

    private static String toString(LocationAtom location)
    {
        final StringBuilder sb = new StringBuilder();

        final String indexText = (null != location.getIndex()) ? location.getIndex().getText() : "";
        sb.append(String.format(ACCESS_FORMAT, location.getName(), indexText));

        if (null != location.getBitfield())
        {
            final LocationAtom.Bitfield bitfield = location.getBitfield();
            sb.append(String.format(BITFIELD_FORMAT, bitfield.getFrom().getText(), bitfield.getTo().getText()));
        }

        return sb.toString();
    }

    private static String toString(LocationConcat location)
    {
        final StringBuilder sb = new StringBuilder();

        for (LocationAtom la : location.getLocations())
        {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(toString(la));
        }

        return String.format(CONCAT_FORMAT, sb.toString());
    }
}
