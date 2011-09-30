/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BracketTemplateIterator.java,v 1.10 2009/08/06 10:27:36 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator;

import com.unitesk.testfusion.core.iterator.BracketExpressionIterator;
import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Program;
import com.unitesk.testfusion.core.situation.BracketSituation;
import com.unitesk.testfusion.core.situation.Situation;

/**
 * Class <code>BracketTemplateIterator</code> considers templates as bracket
 * expressions. It iterates all valid bracket expressions of the bounded depth
 * with the given number of brackets.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class BracketTemplateIterator extends TemplateIterator
{
    /** Instruction corresponding to the opening bracket. */
    protected Instruction openingBracket;
    
    /** Instruction corresponding to the closing bracket. */
    protected Instruction closingBracket;
    
    /** Bracket body. */
    protected Program bracketBody;
    
    /** Number of brackets. */
    protected int number;
    
    /** Iterator of bracket expressions. */
    protected BracketExpressionIterator iterator;
    
    /**
     * Constructor.
     * 
     * @param <code>number</code> the number of brackets.
     * 
     * @param <code>minDepth</code> the minimum depth of bracket expressions.
     * 
     * @param <code>maxDepth</code> the maximum depth of bracket expressions.
     */
    public BracketTemplateIterator(int number, int minDepth, int maxDepth)
    {
        this.number = number;
        this.iterator = new BracketExpressionIterator(number, minDepth, maxDepth);
    }
    
    /**
     * Constructor. Depth of bracket expressions is not fixed.
     * 
     * @param <code>number</code> the number of brackets.
     */
    public BracketTemplateIterator(int number)
    {
        this(number, 0, number - 1);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to an object of bracket template
     *        iterator.
     */
    protected BracketTemplateIterator(BracketTemplateIterator r)
    {
        this.openingBracket = r.openingBracket;
        this.closingBracket = r.closingBracket;
        this.bracketBody = r.bracketBody;
     
        this.number = r.number;
        this.iterator = r.iterator.clone();
    }
    
    /**
     * Sets the opening bracket.
     * 
     * @param <code>openingBracket</code> the instruction to be used as opening
     *        bracket.
     */
    public void registerOpeningBracket(Instruction openingBracket)
    {
        super.registerInstruction(openingBracket);
        
        this.openingBracket = openingBracket; 
    }
    
    /**
     * Sets the opening bracket.
     * 
     * @param <code>openingBracket</code> the instruction to be used as opening
     *        bracket.
     *        
     * @param <code>situation</code> the test situation of the instruction.
     */
    public void registerOpeningBracket(Instruction openingBracket, Situation situation)
    {
        super.registerInstruction(openingBracket, situation);
        
        this.openingBracket = openingBracket;
    }
    
    /**
     * Sets the closing bracket.
     * 
     * @param <code>closingBracket</code> the instruction to be used as closing
     *        bracket.
     */
    public void registerClosingBracket(Instruction closingBracket)
    {
        super.registerInstruction(closingBracket);
        
        this.closingBracket = closingBracket;
    }

    /**
     * Sets the closing bracket.
     * 
     * @param <code>closingBracket</code> the instruction to be used as closing
     *        bracket.
     *         
     * @param <code>situation</code> the test situation of the instruction.
     */
    public void registerClosingBracket(Instruction closingBracket, Situation situation)
    {
        super.registerInstruction(closingBracket, situation);
        
        this.closingBracket = closingBracket;
    }
    
    /**
     * Registers the bracket body.
     * 
     * @param <code>bracketBody</code> the sequence of instructions to be used
     *        as bracket body.
     */
    public void registerBracketBody(Program bracketBody)
    {
        this.bracketBody = bracketBody;
    }
    
    /** Initializes the iterator. */
    @Override
    public void init()
    {
        iterator.init();
    }

    /**
     * Checks if the iterator is not exhausted (template is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean hasValue()
    {
        return iterator.hasValue();
    }

    /**
     * Sets the bracket number in the test situation of the instruction.
     * 
     * @param <code>instruction</code> the instruction.
     * 
     * @param <code>number</code> the bracket number.
     */
    protected void setBracketNumber(Instruction instruction, int number)
    {
        Situation situation = instruction.getSituation();
        
        if(situation instanceof BracketSituation)
        {
            BracketSituation bracketSituation = (BracketSituation)situation;
            bracketSituation.setBracketNumber(number);
        }
    }
    
    /**
     * Returns the current template.
     * 
     * @return the current template.
     */
    @Override
    public Program value()
    {
        int n = 0;
        int top = 0;
        int stack[] = new int[number];
        
        Program template = new Program();
        boolean[] bracketExpression = iterator.value();
        
        for(int i = 0; i < bracketExpression.length; i++)
        {
            if(bracketExpression[i] == BracketExpressionIterator.OPENING_BRACKET)
            {
                Instruction instruction = openingBracket.clone();

                setBracketNumber(instruction, n);
                template.append(instruction);
                
                stack[top++] = n++;
                
                if(bracketExpression[i + 1] == BracketExpressionIterator.CLOSING_BRACKET)
                    { template.append(bracketBody.clone()); }
            }
            else
            {
                Instruction instruction = closingBracket.clone();

                setBracketNumber(instruction, stack[--top]);
                template.append(instruction);
            }
        }
        
        return template;
    }

    /** Randomizes test template within one iteration. */
    @Override
    public void randomize()
    {
        // Do nothing
    }
    
    /** Makes iteration. */
    @Override
    public void next()
    {
        iterator.next();
    }
    
    /** Stops the iterator. */
    public void stop()
    {
        iterator.stop();
    }

    public BracketTemplateIterator clone()
    {
        return new BracketTemplateIterator(this);
    }
}
