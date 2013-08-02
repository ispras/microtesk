/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * SimnMLTranslator.java, Oct 19, 2012 6:13:32 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml;

import java.io.File;
import java.io.FileReader;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import ru.ispras.microtesk.translator.antlrex.log.ILogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogEntry;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;

import ru.ispras.microtesk.translator.keywords.JavaKeyword;
import ru.ispras.microtesk.translator.keywords.RubyKeyword;
import ru.ispras.microtesk.translator.simnml.generation.Generator;
import ru.ispras.microtesk.translator.simnml.grammar.SimnMLLexer;
import ru.ispras.microtesk.translator.simnml.grammar.SimnMLParser;
import ru.ispras.microtesk.translator.simnml.grammar.SimnMLTreeWalker;
import ru.ispras.microtesk.translator.simnml.ir.IR;
import ru.ispras.microtesk.translator.simnml.ir.primitive.InstructionBuilder;

public class SimnMLTranslator
{
    private static final ILogStore LOG = new ILogStore()
    {
        @Override
        public void append(LogEntry entry)
        {
            System.err.println(entry);
        }
    };

    public static String getModelName(String fileName)
    {
        final String shortFileName = getShortFileName(fileName);
        final int dotPos = shortFileName.lastIndexOf('.');

        if (-1 == dotPos)
            return shortFileName.toLowerCase();

        return shortFileName.substring(0, dotPos).toLowerCase();
    }

    public static String getShortFileName(String fileName)
    {
        return new File(fileName).getName();
    }

    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.err.println("Number of cmd arguments is " + args.length);
            return;
        }

        final String fileName = args[0];
        final String modelName = getModelName(fileName);

        System.out.println("Translating: " + fileName);
        System.out.println("Model name: " + modelName);

        final SymbolTable<ESymbolKind> symbols = new SymbolTable<ESymbolKind>();

        symbols.defineReserved(ESymbolKind.KEYWORD, JavaKeyword.STRINGS);
        symbols.defineReserved(ESymbolKind.KEYWORD, RubyKeyword.STRINGS);
        
        final IR ir = new IR();

        try
        {
            final ANTLRReaderStream input = new ANTLRReaderStream(new FileReader(fileName));
            input.name = fileName;

            final SimnMLLexer lexer = new SimnMLLexer(input);
            final CommonTokenStream tokens = new CommonTokenStream(lexer);

            final SimnMLParser parser = new SimnMLParser(tokens);
            parser.assignLog(LOG);
            parser.assignSymbols(symbols);

            final SimnMLParser.startRule_return r = parser.startRule();
            final CommonTree t = (CommonTree) r.getTree();

            System.out.println("AST: " + t.toStringTree());

            if (!parser.isCorrect())
            {
                System.err.println("TRANSLATION WAS INTERRUPTED DUE TO SYNTACTIC ERRORS.");
                return;
            }

            final CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
            nodes.setTokenStream(tokens);

            final SimnMLTreeWalker walker = new SimnMLTreeWalker(nodes);
            walker.assignLog(LOG);
            walker.assignSymbols(symbols);
            walker.assignIR(ir);

            walker.startRule();

            if (!walker.isCorrect())
            {
                System.err.println("TRANSLATION WAS INTERRUPTED DUE TO SEMANTIC ERRORS.");
                return;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        final InstructionBuilder instructionBuilder = new InstructionBuilder(ir.getOps(), getShortFileName(fileName), LOG);
        if (!instructionBuilder.buildInstructions())
        {
            System.err.println("FAILED TO SYNTESIZE INSTRUCTIONS. TRANSLATION WAS INTERRUPTED.");
            return;
        }
        ir.putInstructions(instructionBuilder.getInstructions());

        final Generator generator = new Generator(modelName, getShortFileName(fileName), ir);
        generator.generate();
    }
}
