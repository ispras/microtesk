/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * StatementFunctionCall.java, Jul 25, 2013 11:29:32 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.List;

public final class StatementFormat extends Statement
{
    private final String                   format;
    private final List<Format.Marker>     markers;
    private final List<Format.Argument> arguments;

    StatementFormat(String format, List<Format.Marker> markers, List<Format.Argument> arguments)
    {
        super(Kind.FORMAT);

        this.format    = format;
        this.markers   = markers;
        this.arguments = arguments;
    }

    public String getFormat()
    {
        return format;
    }

    public List<Format.Marker> getMarkers()
    {
        return markers;
    }

    public List<Format.Argument> getArguments()
    {
        return arguments;
    }
}
