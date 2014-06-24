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

import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.simnml.instruction.AddressingModeImm;
import ru.ispras.microtesk.model.api.simnml.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.simnml.instruction.IOperation;
import ru.ispras.microtesk.model.api.simnml.instruction.Operation;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

public class OperationSTBuilder extends PrimitiveBaseSTBuilder
{
    private final String specFileName;
    private final String modelName;
    private final PrimitiveAND op;

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
        PrimitiveAND op
        )
    {
        assert op.getKind() == Primitive.Kind.OP;

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
        t.add("imps", String.format("%s.*", Location.class.getPackage().getName()));
        t.add("simps", String.format(SHARED_CLASS_FORMAT, modelName));

        t.add("base", Operation.class.getSimpleName());
    }
    
    private void buildArguments(STGroup group, ST t)
    {
        boolean isImmModeImported = false;
        
        for (Map.Entry<String, Primitive> e : op.getArguments().entrySet())
        {
            final String    argName = e.getKey();
            final Primitive argType = e.getValue();

            t.add("arg_names", argName);

            if (argType.getKind() ==  Primitive.Kind.IMM)
            {
                t.add("arg_tnames", String.format("%s.INFO(%s)",
                    AddressingModeImm.class.getSimpleName(), argType.getName()));

                if (!isImmModeImported)
                {
                    t.add("imps", AddressingModeImm.class.getName());
                    isImmModeImported = true;
                }
            }
            else
            {
                t.add("arg_tnames", String.format("%s.INFO", argType.getName()));
            }

            final ST argCheckST;
            if (Primitive.Kind.MODE == argType.getKind())
            {
                importModeDependencies(t);
                t.add("arg_types", IAddressingMode.class.getSimpleName());

                argCheckST = group.getInstanceOf("op_arg_check_opmode");
            }
            else if (Primitive.Kind.OP == argType.getKind())
            {
                importOpDependencies(t);
                t.add("arg_types", IOperation.class.getSimpleName());

                argCheckST = group.getInstanceOf("op_arg_check_opmode");
            }
            else // if Primitive.Kind.IMM == oa.getKind()
            {
                importImmDependencies(t);
                t.add("arg_types", Location.class.getSimpleName());

                argCheckST = group.getInstanceOf("op_arg_check_imm");
            }
            
            argCheckST.add("arg_name", argName);
            argCheckST.add("arg_type", argType.getName());

            t.add("arg_checks", argCheckST);
        }
    }

    private void buildAttributes(STGroup group, ST t)
    {
        for (Attribute attr : op.getAttributes().values())
        {
            final ST attrST = group.getInstanceOf("op_attribute");

            attrST.add("name", attr.getName());
            attrST.add("rettype", getRetTypeName(attr.getKind()));

            if (Attribute.Kind.ACTION == attr.getKind())
            {
                for (Statement stmt: attr.getStatements())
                    addStatement(attrST, stmt, false);
            }
            else if (Attribute.Kind.EXPRESSION == attr.getKind())
            {
                assert 1 == attr.getStatements().size() : "Expression attributes must always include a single statement.";
                
                final Statement stmt = (attr.getStatements().size() > 0) ?
                    attr.getStatements().get(0) : null;

                addStatement(attrST, stmt, true);
            }
            else
            {
                assert false : "Unknown attribute kind: " + attr.getKind();
            }

            attrST.add("override", isStandardAttribute(attr.getName()));
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
