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

import ru.ispras.microtesk.translator.simnml.ir.location.Location;
import ru.ispras.microtesk.translator.simnml.ir.location.LocationAtom;
import ru.ispras.microtesk.translator.simnml.ir.location.LocationConcat;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;

public final class LocationPrinter
{
    private static final String   ACCESS_FORMAT = ".access(%s)";
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

        sb.append(location.getName());

        if (!needsAccessCall(location))
        {
            final String indexText = (null != location.getIndex()) ? ExprPrinter.toString(location.getIndex()) : "";
            sb.append(String.format(ACCESS_FORMAT, indexText));
        }

        if (null != location.getBitfield())
        {
            final LocationAtom.Bitfield bitfield = location.getBitfield();
            sb.append(String.format(BITFIELD_FORMAT, ExprPrinter.toString(bitfield.getFrom()), ExprPrinter.toString(bitfield.getTo())));
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
    
    private static boolean needsAccessCall(LocationAtom location)
    {
        if (!(location.getSource() instanceof LocationAtom.PrimitiveSource))
            return false;

        final Primitive primitive = 
            ((LocationAtom.PrimitiveSource) location.getSource()).getPrimitive(); 

        if (Primitive.Kind.IMM == primitive.getKind())
            return true;

        return false;
    }
}
