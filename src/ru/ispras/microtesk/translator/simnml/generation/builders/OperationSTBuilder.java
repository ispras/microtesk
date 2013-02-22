/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * OperationSTBuilder.java, Dec 7, 2012 3:27:59 PM Andrei Tatarnikov
 */ 

package ru.ispras.microtesk.translator.simnml.generation.builders;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.simnml.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.simnml.instruction.IOperation;
import ru.ispras.microtesk.model.api.simnml.instruction.Operation;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;
import ru.ispras.microtesk.translator.simnml.ir.modeop.Attribute;
import ru.ispras.microtesk.translator.simnml.ir.modeop.EArgumentKind;
import ru.ispras.microtesk.translator.simnml.ir.modeop.Op;
import ru.ispras.microtesk.translator.simnml.ir.modeop.Argument;
import ru.ispras.microtesk.translator.simnml.ir.modeop.Statement;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

public class OperationSTBuilder implements ITemplateBuilder
{
    private final String specFileName;
    private final String modelName;
    private final Op op;

    private boolean modesImported = false;
    private boolean   opsImported = false;
    private boolean  immsImported = false;

    private void importModeDependencies(ST t)
    {
        if (!modesImported)
        {
            t.add("imps", IAddressingMode.class.getName());
            t.add("imps", String.format(MODE_CLASS_FORMAT, modelName, "*"));
            modesImported = true;
        }
    }

    private void importOpDependencies(ST t)
    {
        if (!opsImported)
        {
            t.add("imps", IOperation.class.getName());
            opsImported = true;
        }
    }

    private void importImmDependencies(ST t)
    {
        if (!immsImported)
        {
            t.add("imps", Location.class.getName());
            //t.add("imps", String.format("%s.*", Type.class.getPackage().getName()));
            immsImported = true;
        }
    }

    public OperationSTBuilder(
        String specFileName,
        String modelName,
        Op op
        )
    {
        this.specFileName = specFileName;
        this.modelName = modelName;
        this.op = op;
    }

    private void buildHeader(ST t)
    {
        t.add("name", op.getName());
        t.add("file", specFileName);
        t.add("pack", String.format(OP_PACKAGE_FORMAT, modelName));

        t.add("imps", Operation.class.getName());
        t.add("imps", String.format("%s.*", Type.class.getPackage().getName()));
        t.add("imps", String.format("%s.*", Data.class.getPackage().getName()));
        t.add("simps", String.format(SHARED_CLASS_FORMAT, modelName));

        t.add("base", Operation.class.getSimpleName());
    }
    
    private void buildArguments(STGroup group, ST t)
    {
        for (Argument arg : op.getArgs().values())
        {
            t.add("arg_names", arg.getName());

            if (EArgumentKind.MODE == arg.getKind())
            {
                importModeDependencies(t);
                t.add("arg_types", IAddressingMode.class.getSimpleName());

                final ST argCheckST = group.getInstanceOf("op_arg_check_opmode");

                argCheckST.add("arg_name", arg.getName());
                argCheckST.add("arg_type", arg.getTypeText());

                t.add("arg_checks", argCheckST);
            }
            else if (EArgumentKind.OP == arg.getKind())
            {
                importOpDependencies(t);
                t.add("arg_types", IOperation.class.getSimpleName());

                final ST argCheckST = group.getInstanceOf("op_arg_check_opmode");

                argCheckST.add("arg_name", arg.getName());
                argCheckST.add("arg_type", arg.getTypeText());

                t.add("arg_checks", argCheckST);
            }
            else // if EArgumentKind.TYPE == oa.getKind()
            {
                importImmDependencies(t);
                t.add("arg_types", Location.class.getSimpleName());

                final ST argCheckST = group.getInstanceOf("op_arg_check_imm");

                argCheckST.add("arg_name", arg.getName());
                argCheckST.add("arg_type", arg.getTypeText());

                t.add("arg_checks", argCheckST);
            }
        }
    }

    private void buildAttributes(STGroup group, ST t)
    {
        for (Attribute attr : op.getAttrs().values())
        {
            final ST attrST = group.getInstanceOf("op_attribute");

            attrST.add("name", attr.getName());
            attrST.add("rettype", attr.getRetTypeName());

            for (Statement stmt: attr.getStatements())
                attrST.add("stmts", stmt.getText());

            t.add("attrs", attrST);
        }
    }

    @Override
    public ST build(STGroup group)
    {        
        final ST t = group.getInstanceOf("op");

        buildHeader(t);
        buildArguments(group, t);
        buildAttributes(group, t);

        return t;
    }
}
