/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: TestProgramTemplate.java,v 1.17 2009/08/06 11:04:09 kamkin Exp $
 */

package com.unitesk.testfusion.core.engine;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Operand;
import com.unitesk.testfusion.core.model.Program;
import com.unitesk.testfusion.core.model.PseudoInstruction;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class TestProgramTemplate
{
    public static final int LINE_SIZE = 80;
    
    /** Enables/disables self-checking test generation. */
    protected boolean selfCheck;

    /**
     * Checks if self-checking test generation is enabled.
     * 
     * @return <code>true</code> if self-checking test generation is enabled;
     *         <code>false</code> otherwise.
     */
    public boolean isSelfCheck()
    {
        return selfCheck;
    }
    
    /**
     * Enables/disables self-checking test generation.
     * 
     * @param <code>selfCheck</code> the self-checking status.
     */
    public void setSelfCheck(boolean selfCheck)
    {
        this.selfCheck = selfCheck;
    }
    
    /**
     * Returns the header of the test program.
     * 
     * @return the header of the test program.
     */
    public abstract String getHeader();
    
    /**
     * Returns the footer of the test program.
     * 
     * @return the footer of the test program.
     */
    public abstract String getFooter();

    /**
     * Returns the test program prefix.
     * 
     * @param  <code>context</code> the context of generation.
     * 
     * @return the test program prefix.
     */
    public abstract Program getTestPrefix(GeneratorContext context);

    /**
     * Returns the test program suffix.
     * 
     * @param  <code>context</code> the context of generation.
     * 
     * @return the test program suffix.
     */
    public abstract Program getTestSuffix(GeneratorContext context);

    /**
     * Returns the test situation prefix. Test situation prefix is inserted
     * before preparation program.
     * 
     * @param  <code>context</code> the context of generation.
     * 
     * @return the test sitution prefix.
     */
    public abstract Program getTestSituationPrefix(GeneratorContext context, boolean first, boolean last);

    /**
     * Returns the test action prefix. Test situation prefix is inserted
     * after preparation program.
     * 
     * @param  <code>context</code> the context of generation.
     * 
     * @return the test action prefix.
     */
    public abstract Program getTestActionPrefix(GeneratorContext context, boolean first, boolean last);
    
    /**
     * Returns the test action suffix. Test action suffix is inserted after
     * test action.
     * 
     * @param  <code>context</code> the context of generation.
     * 
     * @return the test action suffix.
     */
    public abstract Program getTestActionSuffix(GeneratorContext context, boolean first, boolean last);
    
    /**
     * Returns a token, which is a beginning of single line comment.
     * 
     * @return a token, which is a beginning of single line comment.
     */
    public abstract String singleLineComment();

    /**
     * Checks if multiline comments are supported.
     * 
     * @return <code>true</code> if multiline comments are supported;
     *         <code>false</code> otherwise.
     */
    public abstract boolean supportMultilineComments();
    
    /**
     * Returns a token, which is the opening bracket of multiline comment.
     * 
     * @return a token, which is the opening bracket of multiline comment.
     */
    public abstract String getOpeningBracket();

    /**
     * Returns a token, which is the closing bracket of multiline comment.
     * 
     * @return a token, which is the closing bracket of multiline comment.
     */
    public abstract String getClosingBracket();
    
    /**
     * Returns a token, which is a character used in full-line separator
     * comments.
     * 
     * @return a token, which is a character used in full-line separator
     *         comments.
     */
    public abstract String getStartString();
    
    /**
     * Creates a single-line comment.
     * 
     * @param  <code>comment</code> the text of comment.
     * 
     * @return a single-line comment.
     */
    public PseudoInstruction createComment(String comment)
    {
        return new PseudoInstruction(singleLineComment() + " " + comment);
    }

    /**
     * Creates a decorated single-line comment.
     * 
     * @param  <code>comment</code> the text of comment.
     * 
     * @return a decorated single-line comment.
     */
    public PseudoInstruction createDecoratedComment(String comment)
    {
        int i, k, n;

        StringBuffer buffer = new StringBuffer();
        
        String leftBracket  = supportMultilineComments() ? getOpeningBracket()  : singleLineComment();
        String rightBracket = supportMultilineComments() ? getClosingBracket() : singleLineComment();
        String startString  = supportMultilineComments() ? getStartString()  : singleLineComment();
        
        int offset = leftBracket.length() + rightBracket.length() + 2;
        
        if(comment.length() + offset > LINE_SIZE)
        {
            return new PseudoInstruction(leftBracket + " " + comment + " " + rightBracket);
        }
        
        n = comment.length() != 0 ? LINE_SIZE - (comment.length() + offset) : LINE_SIZE - offset + 2;
        
        n /= startString.length();
        
        buffer.append(leftBracket);
        
        for(i = 0, k = n / 2; i < k; i++)
            { buffer.append(startString); }
        
        if(comment.length() != 0)
            { buffer.append(" " + comment + " "); }
        
        for(; i < n; i++)
            { buffer.append(startString); }
        
        buffer.append(rightBracket);
        
        return new PseudoInstruction(buffer.toString());
    }

    public PseudoInstruction createDecoratedComment()
    {
        return createDecoratedComment("");
    }
    
    /**
     * Returns the test situation label.
     * 
     * @return the test situation label.
     */
    public String getTestSituationLabel(int n)
    {
        return "test_situation_" + n;
    }

    /**
     * Returns the test situation label.
     * 
     * @return the test situation label.
     */
    public String getTestActionLabel(int n)
    {
        return "test_action_" + n;
    }
    
    /**
     * Returns the label for success execution of self-checking test program.
     * 
     * @return the label for success execution of self-checking test program.
     */
    public String getSuccessLabel()
    {
        return "success";
    }
    
    /**
     * Returns the label for erroneous execution of self-checking test program.
     * 
     * @return the label for erroneous execution of self-checking test program.
     */
    public String getErrorLabel()
    {
        return "error";
    }
    
    /**
     * Returns the label for forward jumps.
     * 
     * @return the label for forward jumps.
     */
    public String getForwardLabel(int situation)
    {
        return "forward_jump_" + situation;
    }
    
    /**
     * Returns the label for backward jumps.
     * 
     * @return the label for backward jumps.
     */
    public String getBackwardLabel(int situation)
    {
        return "backward_jump_" + situation;
    }

    /**
     * Returns the test oracle that checks the state of the given operand.
     * 
     * @param  <code>operand</code> the register operand to be checked.
     * 
     * @return the test oracle that checks the state of the given operand.
     */
    public Program getTestOracle(Operand operand)
    {
        return new Program();
    }
    
    /**
     * Returns the test oracle that checks the state of the microprocessor.
     * 
     * @param  <code>register</code> the register to be checked.
     * 
     * @return the test oracle that checks the state of the microprocessor.
     */
    public Program getTestOracle(Program action)
    {
        int i, j, k, size1, size2;
        
        Program program = new Program();
        
        size1 = action.countInstruction();
        for(i = 0; i < size1; i++)
        {
            Instruction instruction = action.getInstruction(i);
            
            // Store global position of the instruction.
            k = instruction.getPosition();
            
            // Set position of the instruction in the test action.
            instruction.setPosition(i);
            
            size2 = instruction.countOperand();
            for(j = 0; j < size2; j++)
            {
                Operand operand = instruction.getOperand(j);
                
                if(!operand.isRegister() || !operand.isOutput())
                    { continue; }
                
                program.append(getTestOracle(operand));
            }
            
            // Restore global position of the instruction.
            instruction.setPosition(k);
        }
        
        return program;
    }
}
