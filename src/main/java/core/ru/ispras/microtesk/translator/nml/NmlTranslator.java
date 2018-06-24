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

import org.antlr.runtime.CharStream;
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
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.antlrex.ReservedKeywords;
import ru.ispras.microtesk.translator.antlrex.symbols.Symbol;
import ru.ispras.microtesk.translator.antlrex.symbols.Where;
import ru.ispras.microtesk.translator.nml.codegen.Generator;
import ru.ispras.microtesk.translator.nml.codegen.decoder.DecoderGenerator;
import ru.ispras.microtesk.translator.nml.codegen.metadata.MetaDataGenerator;
import ru.ispras.microtesk.translator.nml.codegen.whyml.WhymlGenerator;
import ru.ispras.microtesk.translator.nml.coverage.Analyzer;
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
import ru.ispras.microtesk.translator.nml.ir.shared.LetConstant;
import ru.ispras.microtesk.tools.microft.IrInspector;
import ru.ispras.microtesk.utils.FileUtils;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;

public final class NmlTranslator extends Translator<Ir> {
  private static final Set<String> FILTER = Collections.singleton(".nml");

  public NmlTranslator() {
    super(FILTER);

    getSymbols().defineReserved(NmlSymbolKind.KEYWORD, ReservedKeywords.JAVA);
    getSymbols().defineReserved(NmlSymbolKind.KEYWORD, ReservedKeywords.RUBY);

    defineSymbolForInternalVariable(LetConstant.FLOAT_EXCEPTION_FLAGS);
    defineSymbolForInternalVariable(LetConstant.FLOAT_ROUNDING_MODE);

    // Detects parent-child connections between primitives
    addHandler(new ReferenceDetector());
    // Adds the list of root operations to IR
    addHandler(new RootDetector());

    addHandler(new ImageAnalyzer());
    addHandler(new ArgumentModeDetector());
    addHandler(new BranchDetector());
    addHandler(new MemoryAccessDetector());
    addHandler(new Analyzer(this));
    addHandler(new PrimitiveSyntesizer(this));
    addHandler(new ExceptionDetector());

    // Generate Java code of the ISA model
    addHandler(new MetaDataGenerator(this));
    addHandler(new DecoderGenerator(this));
    addHandler(new Generator(this));

    // Generate WhyML code for the ISA
    addHandler(new WhymlGenerator(this));

    addHandler(new IrInspector(this));
  }

  private void defineSymbolForInternalVariable(final LetConstant constant) {
    final String name = constant.getName();
    getSymbols().define(Symbol.newSymbol(
        name, NmlSymbolKind.LET_CONST, new Where("", 0, 0), getSymbols().peek(), false));
  }

  @Override
  protected TokenSource newLexer(final CharStream stream) {
    return new NmlLexer(stream, getPreprocessor(), getSymbols());
  }

  @Override
  protected boolean start(final Options options, final List<String> filenames) {
    InvariantChecks.checkNotNull(filenames);

    if (filenames.isEmpty()) {
      Logger.error("FILES ARE NOT SPECIFIED.");
      return false;
    }

    final String fileName = filenames.get(filenames.size() - 1);
    final String modelName = FileUtils.getShortFileNameNoExt(fileName);
    final String revisionId = null != options ? options.getValueAsString(Option.REVID) : "";

    Logger.message("Translating: %s", fileName);
    Logger.message("Model name: %s", modelName);

    if (null != revisionId && !revisionId.isEmpty()) {
      Logger.message("Revision: %s", revisionId);
    }

    final TokenSource source = startLexer(filenames);
    final Ir ir = startParserAndWalker(modelName, revisionId, source);
    if (null == ir) {
      return false;
    }

    processIr(ir);
    return true;
  }

  private Ir startParserAndWalker(
      final String modelName,
      final String revisionId,
      final TokenSource source) {
    final CommonTokenStream tokens = new TokenRewriteStream();
    tokens.setTokenSource(source);

    final NmlParser parser = new NmlParser(tokens);
    final Deque<Boolean> revisionApplicable = new ArrayDeque<>();

    parser.assignLog(getLog());
    parser.assignSymbols(getSymbols());
    parser.assignRevisions(getRevisions(), revisionApplicable);

    parser.commonParser.assignLog(getLog());
    parser.commonParser.assignSymbols(getSymbols());
    parser.commonParser.assignRevisions(getRevisions(), revisionApplicable);

    parser.setTreeAdaptor(new CommonTreeAdaptor());

    try {
      final RuleReturnScope result = parser.startRule();
      final CommonTree tree = (CommonTree) result.getTree();

      if (Logger.isDebug()) {
        Logger.debug("AST: " + tree.toStringTree());
      }

      if (!parser.isCorrect()) {
        Logger.error("TRANSLATION WAS INTERRUPTED DUE TO SYNTACTIC ERRORS.");
        return null;
      }

      final CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
      nodes.setTokenStream(tokens);

      final Ir ir = new Ir(modelName, revisionId);
      final NmlTreeWalker walker = new NmlTreeWalker(nodes);

      ir.add(LetConstant.FLOAT_EXCEPTION_FLAGS.getName(), LetConstant.FLOAT_EXCEPTION_FLAGS);
      ir.add(LetConstant.FLOAT_ROUNDING_MODE.getName(), LetConstant.FLOAT_ROUNDING_MODE);

      walker.assignLog(getLog());
      walker.assignSymbols(getSymbols());
      walker.assignIR(ir);

      walker.startRule();
      if (!walker.isSuccessful()) {
        Logger.error("TRANSLATION WAS INTERRUPTED DUE TO SEMANTIC ERRORS.");
        return null;
      }

      return ir;
    } catch (final RecognitionException re) {
      Logger.error(re.getMessage());
      return null;
    }
  }
}
