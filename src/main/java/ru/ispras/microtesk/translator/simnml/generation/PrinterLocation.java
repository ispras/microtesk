/*
 * Copyright (c) 2013 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * PrinterLocation.java, Aug 9, 2013 3:32:22 PM Andrei Tatarnikov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.translator.simnml.generation;

import ru.ispras.microtesk.translator.simnml.ir.location.Location;
import ru.ispras.microtesk.translator.simnml.ir.location.LocationAtom;
import ru.ispras.microtesk.translator.simnml.ir.location.LocationConcat;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;

public final class PrinterLocation
{
    private static final String   ACCESS_FORMAT = ".access(%s)";
    private static final String BITFIELD_FORMAT = ".bitField(%s, %s)";
    private static final String   CONCAT_FORMAT = "Location.concat(%s)";

    private PrinterLocation() {}

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
            final String indexText = new PrinterExpr(location.getIndex()).toString();
            sb.append(String.format(ACCESS_FORMAT, indexText));
        }

        if (null != location.getBitfield())
        {
            final LocationAtom.Bitfield bitfield = location.getBitfield();
            sb.append(String.format(BITFIELD_FORMAT, new PrinterExpr(bitfield.getFrom()), new PrinterExpr(bitfield.getTo())));
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
