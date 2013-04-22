/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * SharedSTBuilder.java, Dec 6, 2012 4:22:54 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.generation.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.memory.EMemoryKind;
import ru.ispras.microtesk.model.api.memory.Label;
import ru.ispras.microtesk.model.api.memory.MemoryBase;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.model.api.simnml.SimnMLProcessorModel;

import ru.ispras.microtesk.translator.generation.ITemplateBuilder;
import ru.ispras.microtesk.translator.simnml.ir.IR;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetExpr;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.simnml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

public class SharedSTBuilder implements ITemplateBuilder
{
    public final String specFileName;
    public final String modelName;
    public final IR ir;

    public SharedSTBuilder(IR ir, String specFileName, String modelName)
    {
        this.specFileName = specFileName;
        this.modelName = modelName;
        this.ir = ir;
    }

    private void insertEmptyLine(ST t)
    {
        t.add("members", "");
    }

    private void buildHeader(ST t)
    {
        t.add("file", specFileName);
        t.add("pack", String.format(SHARED_PACKAGE_FORMAT, modelName));

        if (!ir.getTypes().isEmpty() && !ir.getMemory().isEmpty())
        {
            t.add("imps", ETypeID.class.getName());
            t.add("imps", Type.class.getName());
        }

        if (!ir.getMemory().isEmpty())
        {
            t.add("imps", EMemoryKind.class.getName());
        }

        t.add("imps", Label.class.getName());
        t.add("imps", MemoryBase.class.getName());
    }

    private void buildLets(STGroup group, ST t)
    {
        if (!ir.getLets().isEmpty())
            insertEmptyLine(t);

        for (Map.Entry<String, LetExpr> let : ir.getLets().entrySet())
        {
            final ST tLet = group.getInstanceOf("let");

            tLet.add("name", let.getKey());
            tLet.add("type", let.getValue().getJavaType().getSimpleName());
            tLet.add("value", let.getValue().getText());

            t.add("members", tLet);
        }
    }

    private void buildTypes(STGroup group, ST t)
    {
        if (!ir.getTypes().isEmpty())
            insertEmptyLine(t);

        for (Map.Entry<String, TypeExpr> type : ir.getTypes().entrySet())
        {
            if (null == type.getValue().getRefName())
            {
                final ST tType = group.getInstanceOf("type");

                tType.add("name", type.getKey());
                tType.add("typeid", type.getValue().getTypeId());
                tType.add("size", type.getValue().getBitSize().getText());

                t.add("members", tType);
            }
            else
            {
                final ST tType = group.getInstanceOf("type_alias");

                tType.add("name", type.getKey());
                tType.add("alias", type.getValue().getRefName());

                t.add("members", tType);                
            }
        }
    }

    private void buildMemory(STGroup group, ST t)
    {
        if (!ir.getMemory().isEmpty())
            insertEmptyLine(t);

        final List<String> registers = new ArrayList<String>();
        final List<String> memory = new ArrayList<String>();
        final List<String> variables = new ArrayList<String>();

        for (Map.Entry<String, MemoryExpr> mem : ir.getMemory().entrySet())
        {
            buildMemoryLine(group, t, mem.getKey(), mem.getValue());

            switch (mem.getValue().getKind())
            {
            case REG:
                registers.add(mem.getKey());
                break;

            case MEM:
                memory.add(mem.getKey());
                break;

            case VAR:
                variables.add(mem.getKey());
                break;

            default:
                assert false : "Unknown kind!";
                break;
            }
        }

        insertEmptyLine(t);

        buildMemoryLineArray(group, t, SimnMLProcessorModel.SHARED_REGISTERS, registers);
        buildMemoryLineArray(group, t, SimnMLProcessorModel.SHARED_MEMORY, memory);
        buildMemoryLineArray(group, t, SimnMLProcessorModel.SHARED_VARIABLES, variables);

        buildLabels(group, t);
    }
    
    private void buildMemoryLine(STGroup group, ST t, String name, MemoryExpr memory)
    {
        final ST tMemory = group.getInstanceOf("memory");

        tMemory.add("name", name);
        tMemory.add("kind", memory.getKind());

        final TypeExpr typeExpr = memory.getType();
        if (null != typeExpr.getRefName())
        {
            tMemory.add("type", typeExpr.getRefName());
        }
        else
        {
            final ST tNewType = group.getInstanceOf("new_type");

            tNewType.add("typeid", typeExpr.getTypeId());
            tNewType.add("size", typeExpr.getBitSize().getText());

            tMemory.add("type", tNewType);
        }

        tMemory.add("size", memory.getSize().getText());

        t.add("members", tMemory);
    }

    private void buildMemoryLineArray(STGroup group, ST t, String name, List<String> items)
    {
        final ST tArray = group.getInstanceOf("memory_array");

        tArray.add("type", MemoryBase.class.getSimpleName() + "[]");
        tArray.add("name", name);
        tArray.add("items", items);

        t.add("members", tArray);
    }

    private void buildLabels(STGroup group, ST t)
    {
        final ST tLabels = group.getInstanceOf("memory_array");

        tLabels.add("type", Label.class.getSimpleName() + "[]");
        tLabels.add("name", SimnMLProcessorModel.SHARED_LABELS);

        for (LetLabel label : ir.getLabels().values())
        {
            final ST tNewLabel = group.getInstanceOf("new_label");

            tNewLabel.add("name", label.getName());
            tNewLabel.add("memory", label.getMemoryName());
            tNewLabel.add("index", label.getIndex());

            tLabels.add("items", tNewLabel);
        }

        t.add("members", tLabels);
    }

    @Override
    public ST build(STGroup group)
    {
        final ST t = group.getInstanceOf("shared");

        buildHeader(t);
        buildLets(group, t);
        buildTypes(group, t);
        buildMemory(group, t);

        return t;
    }
}
