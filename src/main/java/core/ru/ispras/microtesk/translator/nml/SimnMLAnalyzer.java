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

package ru.ispras.microtesk.translator.nml;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RuleReturnScope;
import org.antlr.runtime.TokenRewriteStream;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.antlrex.IncludeFileFinder;
import ru.ispras.microtesk.translator.antlrex.TokenSourceStack;
import ru.ispras.microtesk.translator.antlrex.TokenSourceIncluder;
import ru.ispras.microtesk.translator.antlrex.log.LogStore;
import ru.ispras.microtesk.translator.antlrex.symbols.ReservedKeywords;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;
import ru.ispras.microtesk.translator.nml.generation.Generator;
import ru.ispras.microtesk.translator.nml.grammar.SimnMLLexer;
import ru.ispras.microtesk.translator.nml.grammar.SimnMLParser;
import ru.ispras.microtesk.translator.nml.grammar.SimnMLTreeWalker;
import ru.ispras.microtesk.translator.nml.ir.IR;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveSyntesizer;
import ru.ispras.microtesk.utils.FileUtils;

public final class SimnMLAnalyzer extends Translator<IR> implements TokenSourceIncluder {
  private static final Set<String> FILTER = Collections.singleton(".nml");

  public SimnMLAnalyzer() {
    super(FILTER);
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

  private TokenSource startLexer(final List<String> filenames) {
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

    Logger.message("Included: " + filename);

    if (null == stream) {
      Logger.error("INCLUDE FILE '" + filename + "' HAS NOT BEEN FOUND.");
      return;
    }

    source.push(new SimnMLLexer(stream, this));
  }

  // /////////////////////////////////////////////////////////////////////////
  // Parser
  // /////////////////////////////////////////////////////////////////////////

  private IR startParserAndWalker(TokenSource source) {// throws RecognitionException {
    final LogStore log = getLog();
    final SymbolTable symbols = new SymbolTable();

    symbols.defineReserved(ESymbolKind.KEYWORD, ReservedKeywords.JAVA);
    symbols.defineReserved(ESymbolKind.KEYWORD, ReservedKeywords.RUBY);

    final CommonTokenStream tokens = new TokenRewriteStream();
    tokens.setTokenSource(source);

    final SimnMLParser parser = new SimnMLParser(tokens);
    parser.assignLog(log);
    parser.assignSymbols(symbols);
    parser.commonParser.assignLog(log);
    parser.commonParser.assignSymbols(symbols);
    parser.setTreeAdaptor(new CommonTreeAdaptor());

    try {
      final RuleReturnScope result = parser.startRule();
      final CommonTree tree = (CommonTree) result.getTree();

      // Disabled: needed for debug purposes only. TODO: command-line switch for debug outputs.
      // print(tree);

      final CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
      nodes.setTokenStream(tokens);

      final IR ir = new IR();
      final SimnMLTreeWalker walker = new SimnMLTreeWalker(nodes);

      walker.assignLog(log);
      walker.assignSymbols(symbols);
      walker.assignIR(ir);

      walker.startRule();
      return ir;
    } catch (RecognitionException re) {
      return null;
    }
  }

  // /////////////////////////////////////////////////////////////////////////
  // Generator
  // /////////////////////////////////////////////////////////////////////////

  private void startGenerator(String modelName, String fileName, IR ir) {
    final Generator generator = new Generator(getOutDir() + "/src/java", modelName, fileName, ir);
    generator.generate();
  }

  // /////////////////////////////////////////////////////////////////////////
  // Translator
  // /////////////////////////////////////////////////////////////////////////

  @Override
  protected void start(final List<String> filenames) {
    if (filenames.isEmpty()) {
      Logger.error("FILES ARE NOT SPECIFIED.");
      return;
    }

    final String fileName = filenames.get(filenames.size() - 1);
    final String modelName = FileUtils.getShortFileNameNoExt(fileName);

    Logger.message("Translating: " + fileName);
    Logger.message("Model name: " + modelName);

    final TokenSource source = startLexer(filenames);
    final IR ir = startParserAndWalker(source);

    processIr(ir);

    final ru.ispras.microtesk.translator.nml.coverage.Analyzer analyzer =
        new ru.ispras.microtesk.translator.nml.coverage.Analyzer(ir, modelName);
    analyzer.generateOutput(this.getOutDir());

    final PrimitiveSyntesizer primitiveSyntesizer = new PrimitiveSyntesizer(
        ir.getOps().values(), FileUtils.getShortFileName(fileName), getLog());

    if (!primitiveSyntesizer.syntesize()) {
      Logger.error(FAILED_TO_SYNTH_PRIMITIVES);
      return;
    }
    ir.setRoots(primitiveSyntesizer.getRoots());

    startGenerator(modelName, FileUtils.getShortFileName(fileName), ir);
  }

  // /////////////////////////////////////////////////////////////////////////
  // Debug
  // /////////////////////////////////////////////////////////////////////////
  /*
   * private static void print(final CommonTree tree) { print(tree, 0); }
   * 
   * private static void print(Object obj, int indent) { if(obj == null) { return; }
   * 
   * CommonTree ast = (CommonTree)obj; StringBuffer sb = new StringBuffer(indent);
   * 
   * for(int i = 0; i < indent; i++) { sb.append("   "); }
   * 
   * System.out.println(sb.toString() + ast.getText());
   * 
   * for(int i = 0; i < ast.getChildCount(); i++) { print(ast.getChild(i), indent + 1); } }
   */
  /*
   * private static final String FAILED_TO_SYNTH_INSTRUCTIONS =
   * "FAILED TO SYNTHESIZE INSTRUCTIONS. " + "TRANSLATION WAS INTERRUPTED.";
   */

  private static final String FAILED_TO_SYNTH_PRIMITIVES =
    "FAILED TO SYNTHESIZE INFORMATION ON DESCRIBED OPERATIONS. " + "TRANSLATION WAS INTERRUPTED.";
}
