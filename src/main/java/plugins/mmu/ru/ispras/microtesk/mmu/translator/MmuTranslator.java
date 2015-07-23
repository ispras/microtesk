/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator;

import java.io.FileReader;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenRewriteStream;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.mmu.translator.generation.Generator;
import ru.ispras.microtesk.mmu.translator.grammar.MmuLexer;
import ru.ispras.microtesk.mmu.translator.grammar.MmuParser;
import ru.ispras.microtesk.mmu.translator.grammar.MmuTreeWalker;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.builder.MmuSpecBuilder;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.antlrex.Preprocessor;
import ru.ispras.microtesk.translator.antlrex.TokenSourceStack;
import ru.ispras.microtesk.translator.antlrex.log.LogStore;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;
import ru.ispras.microtesk.utils.FileUtils;

public final class MmuTranslator extends Translator<Ir> {
  private static final Set<String> FILTER = Collections.singleton(".mmu");

  private static MmuSubsystem spec = null;

  public static MmuSubsystem getSpecification() {
    return spec;
  }

  public static void setSpecification(final MmuSubsystem mmu) {
    InvariantChecks.checkNotNull(mmu);
    spec = mmu;
  }

  private final MmuSpecBuilder specBuilder;

  public MmuTranslator() {
    super(FILTER);

    specBuilder = new MmuSpecBuilder();

    addHandler(specBuilder);
    addHandler(new Generator());
  }

  private final Preprocessor pp = new Preprocessor(this);

  private TokenSourceStack source;

  @Override
  public void addPath(final String path) {
    InvariantChecks.checkNotNull(path);
    pp.addPath(path);
  }

  @Override
  public void startLexer(final CharStream stream) {
    InvariantChecks.checkNotNull(stream);
    source.push(new MmuLexer(stream, pp));
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

  @Override
  public void start(final List<String> fileNames) {
    InvariantChecks.checkNotNull(fileNames);

    final String fileName = fileNames.get(0);
    final String modelName = FileUtils.getShortFileNameNoExt(fileName);

    Logger.message("Translating: " + fileName);
    Logger.message("Model name: " + modelName);
    Logger.message("");

    final LogStore LOG = getLog();
    final SymbolTable symbols = new SymbolTable();
    final Ir ir = new Ir();

    try {
      final ANTLRReaderStream input = new ANTLRReaderStream(new FileReader(fileName));
      input.name = fileName;

      final TokenSource source = startLexer(fileNames);

      final CommonTokenStream tokens = new TokenRewriteStream();
      tokens.setTokenSource(source);

      final MmuParser parser = new MmuParser(tokens);
      parser.assignLog(LOG);
      parser.assignSymbols(symbols);
      parser.commonParser.assignLog(LOG);
      parser.commonParser.assignSymbols(symbols);

      final MmuParser.startRule_return r = parser.startRule();
      final CommonTree t = (CommonTree) r.getTree();

      Logger.debug("AST: " + t.toStringTree());

      if (!parser.isCorrect()) {
        Logger.error("TRANSLATION WAS INTERRUPTED DUE TO SYNTACTIC ERRORS.");
        return;
      }

      final CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
      nodes.setTokenStream(tokens);

      final MmuTreeWalker walker = new MmuTreeWalker(nodes);
      walker.assignLog(LOG);
      walker.assignSymbols(symbols);
      walker.assignIR(ir);

      walker.startRule();

      if (!walker.isCorrect()) {
        Logger.error("TRANSLATION WAS INTERRUPTED DUE TO SEMANTIC ERRORS.");
        return;
      }

      processIr(ir);

      System.out.println(specBuilder.getSpecification());
      setSpecification(specBuilder.getSpecification());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
