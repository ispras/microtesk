/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * SimnMLAnalyzer.java, Oct 19, 2012 6:13:32 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml;

import java.io.*;
import java.util.*;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import ru.ispras.microtesk.translator.antlrex.IncludeFileFinder;
import ru.ispras.microtesk.translator.antlrex.TokenSourceStack;
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

public class SimnMLAnalyzer
{
    private final ILogStore LOG = new ILogStore()
    {
        @Override
        public void append(LogEntry entry)
        {
            System.err.println(entry);
        }
    };

    private String getModelName(String fileName)
    {
        final String shortFileName = getShortFileName(fileName);
        final int dotPos = shortFileName.lastIndexOf('.');

        if (-1 == dotPos)
            return shortFileName.toLowerCase();

        return shortFileName.substring(0, dotPos).toLowerCase();
    }

    private String getShortFileName(String fileName)
    {
        return new File(fileName).getName();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Include file finder
    ///////////////////////////////////////////////////////////////////////////

    private IncludeFileFinder finder = new IncludeFileFinder();

    public void addPath(final String path)
    {
        finder.addPaths(path);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Lexer
    ///////////////////////////////////////////////////////////////////////////
    
    private TokenSourceStack source;

    public TokenSource startLexer(final List<String> filenames)
    {
        ListIterator<String> iterator = filenames.listIterator(filenames.size());

        // Create a stack of lexers.
        source = new TokenSourceStack();
        
        // Process the files in reverse order (emulate inclusion).
        while(iterator.hasPrevious())
            { lexInclude(iterator.previous()); }
            
        return source;
    }
    
    public void startSublexer(CharStream chars)
    {
        source.push(new SimnMLLexer(chars, this));
    }
    
    public void lexInclude(final String filename)
    {
        final ANTLRFileStream stream = finder.openFile(filename);

        System.out.println(filename);

        if (null == stream)
        { 
        	System.err.println("INCLUDE FILE '" + filename + "' HAS NOT BEEN FOUND.");
        	return;
        }

        if (0 == stream.size())
        { 
        	System.err.println("INCLUDE FILE '" + filename + "' IS EMPTY.");
        	return;
        }

        startSublexer(stream);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Parser
    ///////////////////////////////////////////////////////////////////////////

    public IR startParserAndWalker(TokenSource source) throws RecognitionException
    {
        final SymbolTable<ESymbolKind> symbols = new SymbolTable<ESymbolKind>();
        symbols.defineReserved(ESymbolKind.KEYWORD, JavaKeyword.STRINGS);
        symbols.defineReserved(ESymbolKind.KEYWORD, RubyKeyword.STRINGS);

        final CommonTokenStream tokens = new TokenRewriteStream();
        tokens.setTokenSource(source);

        final SimnMLParser parser = new SimnMLParser(tokens);        
        parser.assignLog(LOG);
        parser.assignSymbols(symbols);
        parser.setTreeAdaptor(new CommonTreeAdaptor());

        final RuleReturnScope result = parser.startRule();
        final CommonTree tree = (CommonTree) result.getTree();

        // Disabled: needed for debug purposes only. TODO: command-line switch for debug outputs. 
        // print(tree);

        final CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
        nodes.setTokenStream(tokens);
        
        final IR ir = new IR();
        final SimnMLTreeWalker walker = new SimnMLTreeWalker(nodes);

        walker.assignLog(LOG);
        walker.assignSymbols(symbols);
        walker.assignIR(ir);

        walker.startRule();
        return ir;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Generator
    ///////////////////////////////////////////////////////////////////////////
    public void startGenerator(String modelName, String fileName, IR ir)
    {
        final Generator generator = new Generator(modelName, fileName, ir);
        generator.generate();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Translator
    ///////////////////////////////////////////////////////////////////////////
    
    public void start(final List<String> filenames) throws RecognitionException
    {
        if(filenames.isEmpty())
        {
            System.err.println("FILES ARE NOT SPECIFIED.");
            return;
        }

        final String fileName  = filenames.get(filenames.size() - 1);
        final String modelName = getModelName(fileName);

        System.out.println("Translating: " + fileName);
        System.out.println("Model name: " + modelName);

        final TokenSource source = startLexer(filenames);
        final IR ir = startParserAndWalker(source);

        final InstructionBuilder instructionBuilder = new InstructionBuilder(ir.getOps(), getShortFileName(fileName), LOG);
        if (!instructionBuilder.buildInstructions())
        {
            System.err.println("FAILED TO SYNTHESIZE INSTRUCTIONS. TRANSLATION WAS INTERRUPTED.");
            return;
        }
        ir.putInstructions(instructionBuilder.getInstructions());

        startGenerator(modelName, getShortFileName(fileName), ir);
    }

    public void start(final String[] filenames) throws RecognitionException
    {
        start(Arrays.asList(filenames));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Debug
    ///////////////////////////////////////////////////////////////////////////
/*
    private static void print(final CommonTree tree)
    {
        print(tree, 0);
    }

    private static void print(Object obj, int indent)
    {
        if(obj == null)
            { return; }

        CommonTree ast = (CommonTree)obj;
        StringBuffer sb = new StringBuffer(indent);

        for(int i = 0; i < indent; i++)
            { sb.append("   "); }

        System.out.println(sb.toString() + ast.getText());

        for(int i = 0; i < ast.getChildCount(); i++)
            { print(ast.getChild(i), indent + 1); }
    }
*/
}
