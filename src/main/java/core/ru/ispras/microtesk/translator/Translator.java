/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

import org.antlr.runtime.CharStream;
import org.antlr.runtime.TokenSource;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.translator.antlrex.Preprocessor;
import ru.ispras.microtesk.translator.antlrex.TokenSourceStack;
import ru.ispras.microtesk.translator.antlrex.log.LogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogStoreConsole;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * The {@link Translator} is a base class for all translators. It implements all common
 * facilities shared by translators.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 * @param <Ir> Class describing the internal representation (IR) constructed by the translator.
 */
public abstract class Translator<Ir> {
  private final Set<String> fileExtFilter;
  private final List<TranslatorHandler<Ir>> handlers;
  private final SymbolTable symbols;

  private String outDir;
  private TranslatorContext context;
  private Set<String> revisions;
  private LogStore log;

  private final Preprocessor preprocessor;
  private TokenSourceStack source;

  public Translator(final Set<String> fileExtFilter) {
    InvariantChecks.checkNotNull(fileExtFilter);

    this.fileExtFilter = fileExtFilter;
    this.handlers = new ArrayList<>();
    this.symbols = new SymbolTable();

    this.outDir = PackageInfo.DEFAULT_OUTDIR;
    this.context = null;
    this.revisions = null;
    this.log = LogStoreConsole.INSTANCE;

    this.preprocessor = new Preprocessor(this);
    this.source = null;
  }

  public final void addHandler(final TranslatorHandler<Ir> handler) {
    InvariantChecks.checkNotNull(handler);
    handlers.add(handler);
  }

  protected final void processIr(final Ir ir) {
    InvariantChecks.checkNotNull(ir);
    for (final TranslatorHandler<Ir> handler : handlers) {
      handler.processIr(ir);
    }

    if (null != context) {
      context.addIr(ir);
    }
  }

  protected final SymbolTable getSymbols() {
    return symbols;
  }

  public final String getOutDir() {
    return outDir;
  }

  public final void setOutDir(final String outDir) {
    InvariantChecks.checkNotNull(outDir);
    this.outDir = outDir;
  }

  public final TranslatorContext getContext() {
    return context;
  }

  public final void setContext(final TranslatorContext context) {
    InvariantChecks.checkNotNull(context);
    this.context = context;
  }

  protected final Set<String> getRevisions() {
    return revisions;
  }

  public final void setRevisions(final Set<String> revisions) {
    InvariantChecks.checkNotNull(revisions);
    InvariantChecks.checkTrue(null == this.revisions);

    this.revisions = Collections.unmodifiableSet(revisions);
    this.preprocessor.defineAll(revisions);
  }

  public final LogStore getLog() {
    return log;
  }

  public final void setLog(final LogStore log) {
    InvariantChecks.checkNotNull(log);
    this.log = log;
  }

  protected final Preprocessor getPreprocessor() {
    return preprocessor;
  }

  public final void addPath(final String path) {
    preprocessor.addPath(path);
  }

  public final void startLexer(final CharStream stream) {
    source.push(newLexer(stream));
  }

  protected final TokenSource startLexer(final List<String> filenames) {
    ListIterator<String> iterator = filenames.listIterator(filenames.size());

    // Create a stack of lexers.
    source = new TokenSourceStack();

    // Process the files in reverse order (emulate inclusion).
    while (iterator.hasPrevious()) {
      preprocessor.includeTokensFromFile(iterator.previous());
    }

    return source;
  }

  public final boolean translate(
      final Options options,
      final TranslatorContext context,
      final Set<String> revisions,
      final String... fileNames) {
    if (null != options) {
      if (options.hasValue(Option.INCLUDE)) {
        addPath(options.getValueAsString(Option.INCLUDE));
      }
      setOutDir(options.getValueAsString(Option.OUTPUT_DIR));
    }

    if (null != context) {
      setContext(context);
    }

    setRevisions(revisions);

    for (final String fileName : fileNames) {
      final String fileDir = FileUtils.getFileDir(fileName);
      if (null != fileDir) {
        addPath(fileDir);
      }
    }

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
      return start(options, filteredFileNames);
    }

    return true;
  }

  protected static String getModelName(final Options options, final String fileName) {
    final String fileBasedModelName = FileUtils.getShortFileNameNoExt(fileName);

    if (null == options) {
      return fileBasedModelName;
    }

    final String modelName = options.getValueAsString(Option.MODEL_NAME);
    return !modelName.isEmpty() ? modelName : fileBasedModelName;
  }

  protected static String getRevisionId(final Options options) {
    return null != options ? options.getValueAsString(Option.REV_ID) : "";
  }

  protected abstract TokenSource newLexer(CharStream stream);

  protected abstract boolean start(final Options options, final List<String> fileNames);
}
