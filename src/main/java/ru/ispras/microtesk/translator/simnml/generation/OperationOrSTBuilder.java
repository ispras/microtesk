/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * OperationOrSTBuilder.java, Dec 7, 2012 2:46:37 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.generation;

import java.util.ArrayList;
import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.instruction.Operation;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveOR;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

final class OperationOrSTBuilder implements ITemplateBuilder
{
    private final String specFileName;
    private final String modelName;
    private final PrimitiveOR op;

    public OperationOrSTBuilder(
        String specFileName,
        String modelName,
        PrimitiveOR op
        )
    {
        assert op.getKind() == Primitive.Kind.OP;

        this.specFileName = specFileName;
        this.modelName    = modelName;
        this.op           = op;
    }

    @Override
    public ST build(STGroup group)
    {
        final ST t = group.getInstanceOf("op");

        t.add("name", op.getName());
        t.add("file", specFileName);
        t.add("pack", String.format(OP_PACKAGE_FORMAT, modelName));

        t.add("imps", Operation.class.getName());
        t.add("base", Operation.class.getSimpleName());

        final List<String> opNames = new ArrayList<String>(op.getORs().size());
        for (Primitive p : op.getORs())
            opNames.add(p.getName());

        t.add("ops", opNames);
        return t;
    }
}
