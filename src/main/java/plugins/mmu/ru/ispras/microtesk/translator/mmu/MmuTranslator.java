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

package ru.ispras.microtesk.translator.mmu;

import java.io.FileReader;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.antlrex.log.LogStore;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;
import ru.ispras.microtesk.translator.mmu.grammar.MmuLexer;
import ru.ispras.microtesk.translator.mmu.grammar.MmuParser;
import ru.ispras.microtesk.translator.mmu.grammar.MmuTreeWalker;
import ru.ispras.microtesk.translator.mmu.ir.Ir;
import ru.ispras.microtesk.translator.mmu.spec.builder.MmuSpecBuilder;
import ru.ispras.microtesk.utils.FileUtils;

public class MmuTranslator extends Translator<Ir> {

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Number of cmd arguments is " + args.length);
      return;
    }

    final String fileName = args[0]; 
    final MmuTranslator translator = new MmuTranslator();
    translator.addHandler(new MmuSpecBuilder());

    translator.start(fileName);
  }

  @Override
  public void start(String ... fileNames) {
    final String fileName = fileNames[0];
    final String modelName = FileUtils.getShortFileNameNoExt(fileName);

    System.out.println("Translating: " + fileName);
    System.out.println("Model name: " + modelName);
    System.out.println();

    final LogStore LOG = getLog();
    final SymbolTable symbols = new SymbolTable();
    final Ir ir = new Ir();

    try {
      final ANTLRReaderStream input = new ANTLRReaderStream(new FileReader(fileName));
      input.name = fileName;

      final MmuLexer lexer = new MmuLexer(input);
      final CommonTokenStream tokens = new CommonTokenStream(lexer);

      final MmuParser parser = new MmuParser(tokens);
      parser.assignLog(LOG);
      parser.assignSymbols(symbols);
      parser.commonParser.assignLog(LOG);
      parser.commonParser.assignSymbols(symbols);

      final MmuParser.startRule_return r = parser.startRule();
      final CommonTree t = (CommonTree) r.getTree();

      System.out.println("AST: " + t.toStringTree());

      if (!parser.isCorrect()) {
        System.err.println("TRANSLATION WAS INTERRUPTED DUE TO SYNTACTIC ERRORS.");
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
        System.err.println("TRANSLATION WAS INTERRUPTED DUE TO SEMANTIC ERRORS.");
        return;
      }

      processIr(ir);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
