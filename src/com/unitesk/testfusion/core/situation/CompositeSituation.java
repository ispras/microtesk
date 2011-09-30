/* 
 * Copyright (c) 2007-2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: CompositeSituation.java,v 1.4 2009/08/19 16:50:49 kamkin Exp $
 */

package com.unitesk.testfusion.core.situation;

import java.util.ArrayList;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Processor;
import com.unitesk.testfusion.core.model.Program;

/**
 * Abstract class that represents composite situation.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class CompositeSituation extends Situation
{
    /** Flags that shows if the situation has value or not. */
    protected boolean hasValue;
    
    /** Index of the current situation. */
    protected int currentSituation;
    
    /** Registered situations. */
    protected ArrayList<Situation> situations = new ArrayList<Situation>();

    /** Default constructor. */
    protected CompositeSituation() {}
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to composite situation object.
     */
    protected CompositeSituation(CompositeSituation r)
    {
        super(r);
        
        hasValue = r.hasValue;
        currentSituation = r.currentSituation;
        
        situations.clear();
        for(Situation situation : r.situations)
            { situations.add(situation.clone()); }
    }

    /**
     * Returns the number of elements in the composite situation.
     * 
     * @return the number of elements in the composite situation.
     */
    public int countSituation()
    {
        return situations.size();
    }
    
    /**
     * Registers the given situation in the composite situation.
     *  
     * @param <code>situation</code> the situation.
     */
    public void registerSituation(Situation situation)
    {
        situations.add(situation);
    }
    
    /** Changes the current situation. */
    public abstract void nextSituation();
    
    @Override
    public void setInstruction(Instruction instruction)
    {
        super.setInstruction(instruction);
        
        for(Situation situation : situations)
            { situation.setInstruction(instruction); }
    }
    
    @Override
    public void init(Processor processor, GeneratorContext context)
    {
        Situation situation = situations.get(currentSituation);

        situation.init(processor, context);
    }
    
    @Override
    public void useRegisters(Processor processor, GeneratorContext context)
    {
        Situation situation = situations.get(currentSituation);

        situation.useRegisters(processor, context);
    }
    
    @Override
    public boolean construct(Processor processor, GeneratorContext context)
    {
        Situation situation = situations.get(currentSituation);

        return situation.construct(processor, context);
    }

    @Override
    public Program prepare(Processor processor, GeneratorContext context, int layer)
    {
        Situation situation = situations.get(currentSituation);
        
        return situation.prepare(processor, context, layer);
    }
    
    @Override
    public boolean isConsistent()
    {
        Situation situation = situations.get(currentSituation);
        
        return situation.isConsistent();
    }

    @Override
    public String getDescription()
    {
        Situation situation = situations.get(currentSituation);
        
        return situation.getDescription();
    }
    
    @Override
    public boolean hasValue()
    {
        return hasValue;
    }

    @Override
    public void init()
    {
        hasValue = false;
        currentSituation = 0;
        
        for(Situation situation : situations)
        {
            situation.init();

            hasValue |= situation.hasValue();
        }
    }

    @Override
    public void next()
    {
        Situation situation = situations.get(currentSituation);
        
        if(!hasValue)
            { return; }
        
        if(situation.hasValue())
        {
            situation.next();
            
            if(situation.hasValue())
                { return; }
        }
        
        nextSituation();
    }

    @Override
    public void stop()
    {
        hasValue = false;
    }
    
    @Override
    public String toString()
    {
        Situation situation = situations.get(currentSituation);
        
        return situation.toString(); 
    }
    
    @Override
    public abstract CompositeSituation clone();
}
