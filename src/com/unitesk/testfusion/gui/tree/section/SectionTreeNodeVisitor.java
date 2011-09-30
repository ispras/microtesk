/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SectionTreeNodeVisitor.java,v 1.1 2008/08/18 14:09:59 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.section;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface SectionTreeNodeVisitor
{
    public void onProcessor(SectionTree tree, ProcessorNode section);
    public void onGroup(SectionTree tree, GroupNode group);
    public void onInstruction(SectionTree tree, InstructionNode instruction);
    public void onSituation(SectionTree tree, SituationNode situation);
}
