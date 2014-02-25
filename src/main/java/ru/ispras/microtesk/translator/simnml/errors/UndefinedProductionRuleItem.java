/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * UndefinedModeInOrRule.java, Dec 25, 2012 12:52:58 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;

public class UndefinedProductionRuleItem implements ISemanticError
{
    private static final String FORMAT =
        "The '%s' item of the '%s' %s-rule is not defined or is not a %s definition.";

    private final String  itemName;
    private final String  ruleName;
    private final boolean isOrRule;
    private final ESymbolKind expectedKind;

    public UndefinedProductionRuleItem(
        String itemName,
        String ruleName,
        boolean isOrRule,
        ESymbolKind expectedKind
        )
    {
        this.itemName     = itemName;
        this.ruleName     = ruleName;
        this.isOrRule     = isOrRule;
        this.expectedKind = expectedKind;
    }

    @Override
    public String getMessage()
    {
        return String.format(FORMAT,
            itemName,
            ruleName,
            isOrRule ? "OR" : "AND",
            expectedKind.name()
            );
    }        
}
