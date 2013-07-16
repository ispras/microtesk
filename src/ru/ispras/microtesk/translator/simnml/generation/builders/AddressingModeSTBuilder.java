/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * AddressingModeSTBuilder.java, Dec 7, 2012 3:26:13 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.generation.builders;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import java.util.Map;

import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.simnml.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.simnml.instruction.AddressingMode;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.modeop.Argument;
import ru.ispras.microtesk.translator.simnml.ir.modeop.Attribute;
import ru.ispras.microtesk.translator.simnml.ir.modeop.Statement;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

public class AddressingModeSTBuilder implements ITemplateBuilder
{
    private final String specFileName;
    private final String    modelName;
    private final PrimitiveAND   mode;

    public AddressingModeSTBuilder(
        String specFileName,
        String modelName,
        PrimitiveAND mode
        )
    {
        assert mode.getKind() == Primitive.Kind.MODE;

        this.specFileName = specFileName;
        this.modelName = modelName;
        this.mode = mode;
    }

    private void buildHeader(STGroup group, ST t)
    {
        t.add("name", mode.getName());
        t.add("file", specFileName);
        t.add("pack", String.format(MODE_PACKAGE_FORMAT, modelName));

        t.add("imps", Map.class.getName());
        t.add("imps", String.format("%s.*", Type.class.getPackage().getName()));
        t.add("imps", String.format("%s.*", Data.class.getPackage().getName()));
        t.add("imps", Location.class.getName());

        t.add("imps", IAddressingMode.class.getName());
        t.add("imps", AddressingMode.class.getName());

        t.add("simps", String.format(SHARED_CLASS_FORMAT, modelName));
        t.add("base",  AddressingMode.class.getSimpleName());
    }

    private void buildArguments(STGroup group, ST t)
    {
        for(Argument arg : mode.getArgs().values())
        {
            final ST paramDecl = group.getInstanceOf("new_mode_param");

            paramDecl.add("name", arg.getName());
            paramDecl.add("type", arg.getTypeText());

            t.add("param_decls", paramDecl);

            t.add("param_names", arg.getName());
        }
    }

    private void buildAttributes(STGroup group, ST t)
    {
        for (Attribute attr : mode.getAttrs().values())
        {
            final ST attrST = group.getInstanceOf("mode_attribute");

            attrST.add("name",    attr.getName());
            attrST.add("rettype", attr.getRetTypeName());

            for (Statement stmt: attr.getStatements())
                attrST.add("stmts", stmt.getText());

            t.add("attrs", attrST);
        }
    }

    private void buildReturnExpession(ST t)
    {
        final Expr returnExpr = mode.getReturnExpr();

        if (null == returnExpr)
        {
            t.add("ret", false);
            return;
        }

        if (null != returnExpr.getLocation())
        {
            t.add("ret", returnExpr.getLocation().getText());
        }
        else
        {
            t.add("ret", String.format("new Location(%s)", returnExpr.getText()));
        }
    }

    @Override
    public ST build(STGroup group)
    {
        final ST t = group.getInstanceOf("mode");

        buildHeader(group, t);
        buildArguments(group, t);
        buildAttributes(group, t);
        buildReturnExpession(t);

        return t;
    }
}
