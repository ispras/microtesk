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

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Map;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import ru.ispras.microtesk.translator.antlrex.log.LogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogStoreConsole;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;
import ru.ispras.microtesk.translator.mmu.grammar.MmuLexer;
import ru.ispras.microtesk.translator.mmu.grammar.MmuParser;
import ru.ispras.microtesk.translator.mmu.grammar.MmuTreeWalker;
import ru.ispras.microtesk.translator.mmu.ir.Ir;

public class MmuTranslator {
  public static String getModelName(String fileName) {
    final String shortFileName = getShortFileName(fileName);
    final int dotPos = shortFileName.lastIndexOf('.');

    if (-1 == dotPos) {
      return shortFileName.toLowerCase();
    }

    return shortFileName.substring(0, dotPos).toLowerCase();
  }

  public static String getShortFileName(String fileName) {
    return new File(fileName).getName();
  }
  
  private static final Map<String, MmuTranslatorHandler> handlers = new LinkedHashMap<>();

  public static void addHandler(MmuTranslatorHandler handler) {
    checkNotNull(handler);
    handlers.put(handler.getId(), handler);
  }

  static {
    addHandler(new MmuTranslatorHandler() {
      @Override
      public String getId() {
        return "default";
      }

      @Override
      public void processIr(Ir ir) {
        System.out.println(ir);
      }
    });
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Number of cmd arguments is " + args.length);
      return;
    }

    final LogStore LOG = LogStoreConsole.INSTANCE;

    final String fileName = args[0];
    final String modelName = getModelName(fileName);

    System.out.println("Translating: " + fileName);
    System.out.println("Model name: " + modelName);
    System.out.println();

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

      for (MmuTranslatorHandler handler : handlers.values()) {
        handler.processIr(ir);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
