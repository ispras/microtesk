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

import org.antlr.runtime.CharStream;
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
import ru.ispras.microtesk.translator.antlrex.Preprocessor;
import ru.ispras.microtesk.translator.antlrex.TokenSourceStack;
import ru.ispras.microtesk.translator.antlrex.log.LogStore;
import ru.ispras.microtesk.translator.antlrex.symbols.ReservedKeywords;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;
import ru.ispras.microtesk.translator.nml.coverage.Analyzer;
import ru.ispras.microtesk.translator.nml.generation.Generator;
import ru.ispras.microtesk.translator.nml.grammar.NmlLexer;
import ru.ispras.microtesk.translator.nml.grammar.NmlParser;
import ru.ispras.microtesk.translator.nml.grammar.NmlTreeWalker;
import ru.ispras.microtesk.translator.nml.ir.BranchDetector;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveSyntesizer;
import ru.ispras.microtesk.utils.FileUtils;

public final class NmlTranslator extends Translator<Ir> {
  private static final Set<String> FILTER = Collections.singleton(".nml");

  public NmlTranslator() {
    super(FILTER);
  }

  //------------------------------------------------------------------------------------------------
  // Lexer and Preprocessor
  //------------------------------------------------------------------------------------------------

  private TokenSourceStack source;

  private final Preprocessor pp = new Preprocessor() {
    @Override
    public void includeTokensFromFile(final String filename) {
      final CharStream stream = this.tokenStreamFromFile(filename);
      if (null == stream) {
        Logger.error("INCLUDE FILE '" + filename + "' HAS NOT BEEN FOUND.");
        return;
      }
  
      Logger.message("Included: " + filename);
      source.push(new NmlLexer(stream, pp));
    }

    @Override
    public void includeTokensFromString(final String substitution) {
      final CharStream stream = this.tokenStreamFromString(substitution);
      if (stream != null) {
        source.push(new NmlLexer(stream, this));
      }
    }
  };

  @Override
  public void addPath(final String path) {
    pp.addPath(path);
  }

  private TokenSource startLexer(final List<String> filenames) {
    ListIterator<String> iterator = filenames.listIterator(filenames.size());

    // Create a stack of lexers.
    source = new TokenSourceStack();

    // Process the files in reverse order (emulate inclusion).
    while (iterator.hasPrevious()) {
      pp.includeTokensFromFile(iterator.previous());
    }

    return source;
  }

  //------------------------------------------------------------------------------------------------
  // Parser
  //------------------------------------------------------------------------------------------------

  private Ir startParserAndWalker(final TokenSource source) {// throws RecognitionException {
    final LogStore log = getLog();
    final SymbolTable symbols = new SymbolTable();

    symbols.defineReserved(NmlSymbolKind.KEYWORD, ReservedKeywords.JAVA);
    symbols.defineReserved(NmlSymbolKind.KEYWORD, ReservedKeywords.RUBY);

    final CommonTokenStream tokens = new TokenRewriteStream();
    tokens.setTokenSource(source);

    final NmlParser parser = new NmlParser(tokens);
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

      final Ir ir = new Ir();
      final NmlTreeWalker walker = new NmlTreeWalker(nodes);

      walker.assignLog(log);
      walker.assignSymbols(symbols);
      walker.assignIR(ir);

      walker.startRule();
      return ir;
    } catch (final RecognitionException re) {
      return null;
    }
  }

  //------------------------------------------------------------------------------------------------
  // Generator
  //------------------------------------------------------------------------------------------------

  private void startGenerator(final String modelName, final String fileName, final Ir ir) {
    final Generator generator = new Generator(getOutDir() + "/src/java", modelName, fileName, ir);
    generator.generate();
  }

  //------------------------------------------------------------------------------------------------
  // Translator
  //------------------------------------------------------------------------------------------------

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
    final Ir ir = startParserAndWalker(source);

    processIr(ir);

    final Analyzer coverageAnalyzer = new Analyzer(ir, modelName);
    coverageAnalyzer.generateOutput(this.getOutDir());

    final PrimitiveSyntesizer primitiveSyntesizer = new PrimitiveSyntesizer(
        ir.getOps().values(), FileUtils.getShortFileName(fileName), getLog());

    final BranchDetector branchDetector = new BranchDetector(ir);
    branchDetector.start();

    if (!primitiveSyntesizer.syntesize()) {
      Logger.error(FAILED_TO_SYNTH_PRIMITIVES);
      return;
    }
    ir.setRoots(primitiveSyntesizer.getRoots());

    startGenerator(modelName, FileUtils.getShortFileName(fileName), ir);
  }

  private static final String FAILED_TO_SYNTH_PRIMITIVES =
      "FAILED TO SYNTHESIZE INFORMATION ON DESCRIBED OPERATIONS. TRANSLATION WAS INTERRUPTED.";
}
