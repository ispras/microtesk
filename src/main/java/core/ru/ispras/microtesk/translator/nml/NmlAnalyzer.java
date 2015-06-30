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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RuleReturnScope;
import org.antlr.runtime.TokenRewriteStream;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.antlrex.IncludeFileFinder;
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
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveSyntesizer;
import ru.ispras.microtesk.utils.FileUtils;

public final class NmlAnalyzer extends Translator<Ir> implements Preprocessor {
  private static final Set<String> FILTER = Collections.singleton(".nml");

  public NmlAnalyzer() {
    super(FILTER);
  }

  //------------------------------------------------------------------------------------------------
  // Lexer and Preprocessor
  //------------------------------------------------------------------------------------------------

  private static enum IfDefScope {
    IF_TRUE,
    IF_FALSE,
    ELSE_TRUE,
    ELSE_FALSE
  }

  private TokenSourceStack source;

  private final IncludeFileFinder finder = new IncludeFileFinder();
  private final HashMap<String, String> defines = new LinkedHashMap<>();
  private final Stack<IfDefScope> ifdefs = new Stack<>();

  public void addPath(final String path) {
    finder.addPaths(path);
  }

  @Override
  public boolean isDefined(final String key) {
    return defines.containsKey(key.trim());
  }

  @Override
  public boolean underIfElse() {
    return !ifdefs.empty();
  }

  @Override
  public boolean isHidden() {
    if (underIfElse()) {
      IfDefScope scope = ifdefs.peek();
      return scope == IfDefScope.IF_FALSE || scope == IfDefScope.ELSE_FALSE;
    }

    return false;
  }

  @Override
  public void onDefine(final String key, final String val) {
    final int index = val.indexOf("//");
    final String value = index == -1 ? val : val.substring(0, index);

    defines.put(key.trim(), value.trim());
  }

  @Override
  public void onUndef(final String key) {
    defines.remove(key.trim());
  }

  @Override
  public void onIfdef(final String key) {
    if (isHidden() || !isDefined(key)) {
      ifdefs.push(IfDefScope.IF_FALSE);
    } else {
      ifdefs.push(IfDefScope.IF_TRUE);
    }
  }

  @Override
  public void onIfndef(final String key) {
    if (isHidden() || isDefined(key)) {
      ifdefs.push(IfDefScope.IF_FALSE);
    } else {
      ifdefs.push(IfDefScope.IF_TRUE);
    }
  }

  @Override
  public void onElse() {
    InvariantChecks.checkTrue(underIfElse());

    final IfDefScope scope = ifdefs.pop();
    InvariantChecks.checkTrue(scope == IfDefScope.IF_TRUE || scope == IfDefScope.IF_FALSE);

    if (isHidden() || scope == IfDefScope.IF_TRUE) {
      ifdefs.push(IfDefScope.ELSE_FALSE);
    } else {
      ifdefs.push(IfDefScope.ELSE_TRUE);
    }
  }

  @Override
  public void onEndif() {
    InvariantChecks.checkTrue(underIfElse());
    ifdefs.pop();
  }

  @Override
  public String expand(final String key) {
    return defines.get(key.trim());
  }

  private TokenSource startLexer(final List<String> filenames) {
    ListIterator<String> iterator = filenames.listIterator(filenames.size());

    // Create a stack of lexers.
    source = new TokenSourceStack();

    // Process the files in reverse order (emulate inclusion).
    while (iterator.hasPrevious()) {
      includeTokensFromFile(iterator.previous());
    }

    return source;
  }

  @Override
  public void includeTokensFromFile(final String filename) {
    final ANTLRFileStream stream = finder.openFile(filename);

    Logger.message("Included: " + filename);

    if (null == stream) {
      Logger.error("INCLUDE FILE '" + filename + "' HAS NOT BEEN FOUND.");
      return;
    }

    source.push(new NmlLexer(stream, this));
  }

  @Override
  public void includeTokensFromString(final String substitution) {
    if (substitution != null && !substitution.isEmpty()) {
      final ANTLRStringStream stream = new ANTLRStringStream(substitution);
      source.push(new NmlLexer(stream, this));
    }
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
