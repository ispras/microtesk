/*
 * Copyright 2012-2016 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.translator.antlrex.ReservedKeywords;
import ru.ispras.microtesk.translator.nml.coverage.Analyzer;
import ru.ispras.microtesk.translator.nml.generation.Generator;
import ru.ispras.microtesk.translator.nml.generation.metadata.MetaDataGenerator;
import ru.ispras.microtesk.translator.nml.grammar.NmlLexer;
import ru.ispras.microtesk.translator.nml.grammar.NmlParser;
import ru.ispras.microtesk.translator.nml.grammar.NmlTreeWalker;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.analysis.ArgumentModeDetector;
import ru.ispras.microtesk.translator.nml.ir.analysis.BranchDetector;
import ru.ispras.microtesk.translator.nml.ir.analysis.ExceptionDetector;
import ru.ispras.microtesk.translator.nml.ir.analysis.ImageAnalyzer;
import ru.ispras.microtesk.translator.nml.ir.analysis.MemoryAccessDetector;
import ru.ispras.microtesk.translator.nml.ir.analysis.PrimitiveSyntesizer;
import ru.ispras.microtesk.translator.nml.ir.analysis.ReferenceDetector;
import ru.ispras.microtesk.translator.nml.ir.analysis.RootDetector;
import ru.ispras.microtesk.utils.FileUtils;

public final class NmlTranslator extends Translator<Ir> {
  private static final Set<String> FILTER = Collections.singleton(".nml");

  public NmlTranslator() {
    super(FILTER);

    getSymbols().defineReserved(NmlSymbolKind.KEYWORD, ReservedKeywords.JAVA);
    getSymbols().defineReserved(NmlSymbolKind.KEYWORD, ReservedKeywords.RUBY);

    // Detects parent-child connections between primitives
    addHandler(new ReferenceDetector());
    // Adds the list of root operations to IR 
    addHandler(new RootDetector());

    addHandler(new ArgumentModeDetector());
    addHandler(new BranchDetector());
    addHandler(new MemoryAccessDetector());
    addHandler(new Analyzer(this));
    addHandler(new PrimitiveSyntesizer(this));
    addHandler(new ExceptionDetector());
    //addHandler(new ImageAnalyzer());

    // Generate Java code of the ISA model
    addHandler(new MetaDataGenerator(this));
    addHandler(new Generator(this));
  }

  @Override
  protected TokenSource newLexer(final CharStream stream) {
    return new NmlLexer(stream, getPreprocessor(), getSymbols());
  }

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
    final Ir ir = startParserAndWalker(modelName, source);

    processIr(ir);
  }

  private Ir startParserAndWalker(final String modelName, final TokenSource source) {
    final CommonTokenStream tokens = new TokenRewriteStream();
    tokens.setTokenSource(source);

    final NmlParser parser = new NmlParser(tokens);
    parser.assignLog(getLog());
    parser.assignSymbols(getSymbols());
    parser.commonParser.assignLog(getLog());
    parser.commonParser.assignSymbols(getSymbols());
    parser.setTreeAdaptor(new CommonTreeAdaptor());

    try {
      final RuleReturnScope result = parser.startRule();
      final CommonTree tree = (CommonTree) result.getTree();

      // Disabled: needed for debug purposes only. TODO: command-line switch for debug outputs.
      // print(tree);

      final CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
      nodes.setTokenStream(tokens);

      final Ir ir = new Ir(modelName);
      final NmlTreeWalker walker = new NmlTreeWalker(nodes);

      walker.assignLog(getLog());
      walker.assignSymbols(getSymbols());
      walker.assignIR(ir);

      walker.startRule();
      return ir;
    } catch (final RecognitionException re) {
      return null;
    }
  }
}
