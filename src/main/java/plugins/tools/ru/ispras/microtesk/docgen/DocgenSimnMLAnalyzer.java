/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.docgen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RuleReturnScope;
import org.antlr.runtime.TokenRewriteStream;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import ru.ispras.microtesk.translator.antlrex.IncludeFileFinder;
import ru.ispras.microtesk.translator.antlrex.TokenSourceStack;
import ru.ispras.microtesk.translator.antlrex.TokenSourceIncluder;
import ru.ispras.microtesk.translator.antlrex.log.ILogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogEntry;
import ru.ispras.microtesk.translator.antlrex.symbols.ReservedKeywords;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.grammar.SimnMLLexer;
import ru.ispras.microtesk.translator.simnml.grammar.SimnMLParser;
import ru.ispras.microtesk.translator.simnml.grammar.SimnMLTreeWalker;
import ru.ispras.microtesk.translator.simnml.ir.IR;
import ru.ispras.microtesk.translator.simnml.ir.IrWalker;
import ru.ispras.microtesk.translator.simnml.ir.IrWalker.Direction;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveSyntesizer;

public final class DocgenSimnMLAnalyzer implements TokenSourceIncluder {

  private final ILogStore LOG = new ILogStore() {
    @Override
    public void append(LogEntry entry) {
      System.err.println(entry);
    }
  };

  public DocgenSimnMLAnalyzer() {}

  private String getModelName(String fileName) {
    final String shortFileName = getShortFileName(fileName);
    final int dotPos = shortFileName.lastIndexOf('.');

    if (-1 == dotPos) {
      return shortFileName.toLowerCase();
    }

    return shortFileName.substring(0, dotPos).toLowerCase();
  }

  private String getShortFileName(String fileName) {
    return new File(fileName).getName();
  }

  // /////////////////////////////////////////////////////////////////////////
  // Include file finder
  // /////////////////////////////////////////////////////////////////////////

  private IncludeFileFinder finder = new IncludeFileFinder();

  public void addPath(final String path) {
    finder.addPaths(path);
  }

  // /////////////////////////////////////////////////////////////////////////
  // Lexer
  // /////////////////////////////////////////////////////////////////////////

  private TokenSourceStack source;

  public TokenSource startLexer(final List<String> filenames) {
    ListIterator<String> iterator = filenames.listIterator(filenames.size());

    // Create a stack of lexers.
    source = new TokenSourceStack();

    // Process the files in reverse order (emulate inclusion).
    while (iterator.hasPrevious()) {
      includeTokensFrom(iterator.previous());
    }

    return source;
  }

  @Override
  public void includeTokensFrom(String filename) {
    final ANTLRFileStream stream = finder.openFile(filename);

    System.out.println("Included: " + filename);

    if (null == stream) {
      System.err.println("INCLUDE FILE '" + filename + "' HAS NOT BEEN FOUND.");
      return;
    }

    source.push(new SimnMLLexer(stream, this));
  }

  // /////////////////////////////////////////////////////////////////////////
  // Parser
  // /////////////////////////////////////////////////////////////////////////

  public IR startParserAndWalker(TokenSource source) throws RecognitionException {
    final SymbolTable symbols = new SymbolTable();

    symbols.defineReserved(ESymbolKind.KEYWORD, ReservedKeywords.JAVA);
    symbols.defineReserved(ESymbolKind.KEYWORD, ReservedKeywords.RUBY);

    final CommonTokenStream tokens = new TokenRewriteStream();
    tokens.setTokenSource(source);

    final SimnMLParser parser = new SimnMLParser(tokens);
    parser.assignLog(LOG);
    parser.assignSymbols(symbols);
    parser.commonParser.assignLog(LOG);
    parser.commonParser.assignSymbols(symbols);
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

  // /////////////////////////////////////////////////////////////////////////
  // Translator
  // /////////////////////////////////////////////////////////////////////////

  public void start(final List<String> filenames) throws RecognitionException {
    if (filenames.isEmpty()) {
      System.err.println("FILES ARE NOT SPECIFIED.");
      return;
    }

    final String fileName = filenames.get(filenames.size() - 1);
    final String modelName = getModelName(fileName);

    System.out.println("Translating: " + fileName);
    System.out.println("Model name: " + modelName);

    final TokenSource source = startLexer(filenames);
    final IR ir = startParserAndWalker(source);

    final PrimitiveSyntesizer primitiveSyntesizer =
        new PrimitiveSyntesizer(ir.getOps().values(), getShortFileName(fileName), LOG);

    if (!primitiveSyntesizer.syntesize()) {
      System.err.println("FAILED TO SYNTHESIZE INFORMATION ON DESCRIBED OPERATIONS. "
          + "TRANSLATION WAS INTERRUPTED.");
      return;
    }
    ir.setRoots(primitiveSyntesizer.getRoots());

    // TO PLATON >> TODO: YOUR CODE GOES HERE
    IrWalker walker = new IrWalker(ir, Direction.LINEAR);
    DocBook xml = null;
    try {
      xml = new DocBook(modelName);
    } catch (FormatterException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    XmlDocumenter documenter = null;

    try {
      documenter = new XmlDocumenter(xml, modelName);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    walker.traverse(documenter);
    XmlWriter writer = null;

    try {
      writer = new XmlWriter(new FileWriter(new File("documentation.xml")));
      writer.write(xml);
      writer.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws RecognitionException {
    final DocgenSimnMLAnalyzer analyzer = new DocgenSimnMLAnalyzer();
    try {
      analyzer.start(Arrays.asList(args));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
