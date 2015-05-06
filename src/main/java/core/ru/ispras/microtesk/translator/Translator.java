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

package ru.ispras.microtesk.translator;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.antlrex.log.LogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogStoreConsole;
import ru.ispras.microtesk.translator.generation.PackageInfo;

public abstract class Translator<Ir> {

  public static Translator<?> load(String className) {
    InvariantChecks.checkNotNull(className);

    final ClassLoader loader = Translator.class.getClassLoader();
    try {
      final Class<?> translatorClass = loader.loadClass(className);
      return (Translator<?>) translatorClass.newInstance();
    } catch(ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      return null;
    }
  }

  private String outDir;
  private LogStore log;
  private final List<TranslatorHandler<Ir>> handlers;

  public Translator() {
    this.outDir = PackageInfo.DEFAULT_OUTDIR;
    this.log = LogStoreConsole.INSTANCE;
    this.handlers = new ArrayList<>();
  }

  public final String getOutDir() {
    return outDir;
  }

  public final void setOutDir(String outDir) {
    checkNotNull(outDir);
    this.outDir = outDir;
  }

  public final LogStore getLog() {
    return log;
  }

  public final void setLog(final LogStore log) {
    checkNotNull(log);
    this.log = log;
  }
  
  public final void addHandler(final TranslatorHandler<Ir> handler) {
    checkNotNull(handler);
    handlers.add(handler);
  }

  protected final void processIr(final Ir ir) {
    checkNotNull(ir);
    for (final TranslatorHandler<Ir> handler: handlers) {
      handler.processIr(ir);
    }
  }

  public abstract void addPath(String path);
  public abstract void start(String... fileNames);

  public static void main(String[] args) {
    System.out.println(load("ru.ispras.microtesk.translator.nml.SimnMLAnalyzer"));
    System.out.println(load("ru.ispras.microtesk.translator.mmu.MmuTranslator"));
  }
}
