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

package ru.ispras.microtesk.translator.antlrex;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.io.File;

import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

import ru.ispras.microtesk.translator.antlrex.log.ELogEntryKind;
import ru.ispras.microtesk.translator.antlrex.log.ESenderKind;
import ru.ispras.microtesk.translator.antlrex.log.ILogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogEntry;

public class ParserEx extends Parser implements IErrorReporter {
  private ILogStore log = null;
  private int errorCount = 0;

  private final String sourceName;
  private String tempErrorMessage = "";

  public ParserEx(TokenStream input, RecognizerSharedState state) {
    super(input, state);
    sourceName = new File(getSourceName()).getName();
  }

  public void assignLog(ILogStore log) {
    this.log = log;
  }

  @Override
  public final void reportError(RecognitionException re) {
    checkNotNull(log);
    if (re instanceof SemanticException) {
      throw new IllegalArgumentException();
    }

    tempErrorMessage = "";
    super.reportError(re);

    final LogEntry logEntry = new LogEntry(
      ELogEntryKind.ERROR,
      ESenderKind.PARSER,
      sourceName,
      re.line,
      re.charPositionInLine,
      tempErrorMessage
      );

    log.append(logEntry);
    ++errorCount;
  }

  public final void reportError(SemanticException se) {
    checkNotNull(log);

    final LogEntry logEntry = new LogEntry(
      ELogEntryKind.ERROR,
      ESenderKind.SYNTACTIC,
      sourceName,
      se.line,
      se.charPositionInLine,
      se.getMessage()
      );

    log.append(logEntry);
    ++errorCount;
  }

  @Override
  public final void emitErrorMessage(String errorMessage) {
    tempErrorMessage = errorMessage;
  }

  public final int getErrorCount() {
    return errorCount;
  }

  public final void resetErrorCount() {
    errorCount = 0;
  }

  public final boolean isCorrect() {
    return getErrorCount() == 0;
  }

  @Override
  public void raiseError(ISemanticError error) throws SemanticException {
    throw new SemanticException(input, error);
  }

  @Override
  public void raiseError(Where where, ISemanticError error) throws SemanticException {
    throw new SemanticException(where, error);
  }

  protected final Where where(Token node) {
    return new Where(sourceName, node.getLine(), node.getCharPositionInLine());
  }
}
