/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BlockBuilder.java, Apr 30, 2013 3:37:41 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import ru.ispras.microtesk.test.sequence.Generator;
import ru.ispras.microtesk.test.sequence.GeneratorBuilder;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.iterator.IIterator;
import ru.ispras.microtesk.test.sequence.iterator.SingleValueIterator;

public final class BlockBuilder
{
    private List<Block> nestedBlocks;
    private Map<String, Object> attributes;

    private String compositorName;
    private String combinatorName;

    public BlockBuilder()
    {
        this.nestedBlocks = new ArrayList<Block>();
        this.attributes   = new HashMap<String, Object>();

        this.compositorName = null;
        this.combinatorName = null;
    }

    public void setCompositor(String name)
    {
        assert null == compositorName;
        compositorName = name;
    }

    public void setCombinator(String name)
    {
        assert null == combinatorName;
        combinatorName = name;
    }

    public void setAttribute(String name, Object value)
    {
        assert !attributes.containsKey(name);
        attributes.put(name, value);
    }

    public void addBlock(Block block)
    {
        assert null != block;
        nestedBlocks.add(block);
    }

    public void addCall(AbstractCall call)
    {
        assert null != call;

        final Sequence<AbstractCall> sequence = new Sequence<AbstractCall>();
        sequence.add(call);

        final IIterator<Sequence<AbstractCall>> iterator =
           new SingleValueIterator<Sequence<AbstractCall>>(sequence);

        nestedBlocks.add(new Block(iterator));
    }

    public Block build()
    {
        final GeneratorBuilder<AbstractCall> generatorBuilder =
            new GeneratorBuilder<AbstractCall>();

        if (null != combinatorName)
            generatorBuilder.setCombinator(combinatorName);

        if (null != compositorName)
            generatorBuilder.setCompositor(compositorName);

        for (Block block : nestedBlocks)
            generatorBuilder.addIterator(block.getIterator());

        final Generator<AbstractCall> generator =
            generatorBuilder.getGenerator();

        return new Block(generator, attributes);
    }
}
