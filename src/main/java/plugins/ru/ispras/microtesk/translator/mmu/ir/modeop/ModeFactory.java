package ru.ispras.microtesk.translator.mmu.ir.modeop;

import java.util.ArrayList;
import java.util.Map;

import ru.ispras.microtesk.translator.antlrex.IErrorReporter;

public class ModeFactory
{
    public final Map<String, Mode> modes;
    public final IErrorReporter reporter;
    
    public ModeFactory(Map<String, Mode> modes, IErrorReporter reporter)
    {
        this.modes    = modes;
        this.reporter = reporter;
    }

    public Mode createModeOr(ArrayList<String> names)
    {
        /*
        
        for (String modeName : names)
        {
            if (!modes.containsKey(modeName))
                ;
        }*/
        
        return new Mode(true, names);
    }
}
