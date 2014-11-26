package ru.ispras.microtesk.translator.mmu.ir.modeop;

import java.util.ArrayList;

public class Op
{
    private final boolean isOrRule;
    private final ArrayList<String> ors; 

    public Op(boolean isOrRule, ArrayList<String> ors)
    {
        this.isOrRule = isOrRule;
        this.ors = ors;
    }

    public boolean isOrRule()
    {
        return isOrRule;
    }
    
    public String[] getOrNames()
    {
        return ors.toArray(new String[] {});
    }
}
