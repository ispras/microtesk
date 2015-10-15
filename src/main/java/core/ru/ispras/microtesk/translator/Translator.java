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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.antlr.runtime.CharStream;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.translator.antlrex.log.LogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogStoreConsole;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.utils.FileUtils;

public abstract class Translator<Ir> {
  private final Set<String> fileExtFilter;
  private String outDir;
  private TranslatorContext context;

  private LogStore log;
  private final List<TranslatorHandler<Ir>> handlers;

  public Translator(final Set<String> fileExtFilter) {
    InvariantChecks.checkNotNull(fileExtFilter);

    this.fileExtFilter = fileExtFilter;
    this.outDir = PackageInfo.DEFAULT_OUTDIR;
    this.context = null;
    this.log = LogStoreConsole.INSTANCE;
    this.handlers = new ArrayList<>();
  }

  public final String getOutDir() {
    return outDir;
  }

  public final void setOutDir(final String outDir) {
    checkNotNull(outDir);
    this.outDir = outDir;
  }

  public final TranslatorContext getContext() {
    return context;
  }

  public final void setContext(final TranslatorContext context) {
    checkNotNull(context);
    this.context = context;
  }

  public abstract void addPath(String path);
 
  public abstract void startLexer(final CharStream stream);

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

  public final boolean start(final String... fileNames) {
    final List<String> filteredFileNames = new ArrayList<>();

    for (final String fileName : fileNames) {
      final String fileExt = FileUtils.getFileExtension(fileName).toLowerCase();
      if (fileExtFilter.contains(fileExt)) {
        if (!new File(fileName).exists()) {
          Logger.error("FILE DOES NOT EXISTS: " + fileName);
          return false;
        }

        filteredFileNames.add(fileName);
      }
    }

    if (!filteredFileNames.isEmpty()) {
      start(filteredFileNames);
    }

    return true;
  }

  protected abstract void start(final List<String> fileNames);

  protected final void processIr(final Ir ir) {
    checkNotNull(ir);
    for (final TranslatorHandler<Ir> handler : handlers) {
      handler.processIr(ir);
    }

    if (null != context) {
      context.addIr(ir);
    }
  }
}
