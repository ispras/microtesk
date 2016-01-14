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

import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.antlrex.errors.UnrecognizedStructure;
import ru.ispras.microtesk.translator.antlrex.log.LogEntry;
import ru.ispras.microtesk.translator.antlrex.log.LogStore;
import ru.ispras.microtesk.translator.antlrex.log.SenderKind;
import ru.ispras.microtesk.translator.antlrex.symbols.Where;

/**
 * The ParserEx class is an extension of the ANTLR library class Parser that
 * provides means of error reporting based on MicroTESK library classes
 * facilitating logging.
 * 
 * @author Andrei Tatarnikov
 */

public class ParserEx extends Parser implements ErrorReporter {
  private LogStore log = null;
  private int errorCount = 0;
  private String tempErrorMessage = "";

  public ParserEx(final TokenStream input, final RecognizerSharedState state) {
    super(input, state);
  }

  public void assignLog(LogStore log) {
    this.log = log;
  }

  @Override
  public final void reportError(RecognitionException re) {
    InvariantChecks.checkNotNull(log);

    if (re instanceof SemanticException) {
      reportError((SemanticException) re);
      return;
    }

    tempErrorMessage = "";
    super.reportError(re);

    final LogEntry logEntry = new LogEntry(
      LogEntry.Kind.ERROR,
      SenderKind.PARSER,
      getSourceName(),
      re.line,
      re.charPositionInLine,
      tempErrorMessage
      );

    log.append(logEntry);
    ++errorCount;
  }

  public final void reportError(SemanticException se) {
    InvariantChecks.checkNotNull(log);

    final LogEntry logEntry = new LogEntry(
      LogEntry.Kind.ERROR,
      SenderKind.SYNTACTIC,
      getSourceName(),
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
  public void raiseError(Where where, ISemanticError error) throws SemanticException {
    throw new SemanticException(where, error);
  }

  @Override
  public void raiseError(Where where, String what) throws SemanticException {
    throw new SemanticException(where, new SemanticError(what));
  }

  protected final Where where(Token node) {
    return new Where(getSourceName(), node.getLine(), node.getCharPositionInLine());
  }

  protected final void checkNotNull(Token t, Object obj) throws SemanticException {
    if (null == obj) {
      raiseError(where(t), new UnrecognizedStructure());
    }
  }

  protected final void checkNotNull(Token t, Object obj, String text) throws SemanticException {
    if (null == obj) {
      raiseError(where(t), new UnrecognizedStructure(text));
    }
  }
}
