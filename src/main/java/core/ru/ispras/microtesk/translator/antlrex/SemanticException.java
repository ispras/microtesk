/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

import org.antlr.runtime.RecognitionException;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.antlrex.Where;

/**
 * The {@code SemanticException} exception is thrown by the translator (lexer, parser, tree walker
 * or other its parts) if it detects a semantic error in the translated specification.
 * <p>All semantic errors found in the specification must be reported as
 * {@code SemanticException}. In situations when an error is caused by issues in code (coding
 * mistakes, invariant violations, limitations) an unchecked exception inherited from
 * {@link RuntimeException} must be thrown. 
 * <p>The {@code SemanticException} exception is inherited from the {@link RecognitionException}
 * ANTLR exception to allow handling them in the same way.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class SemanticException extends RecognitionException {
  private static final long serialVersionUID = 209516770104977723L;

  private final Where where;
  private final ISemanticError error;

  SemanticException(Where location, ISemanticError error) {
    super();

    this.where = location; 
    this.error = error;

    this.line = location.getLine();
    this.charPositionInLine = location.getPosition();
    this.approximateLineInfo = true;
  }

  @Override
  public String getMessage() {
    return error.getMessage();
  }

  public ISemanticError getError() {
    return error;
  }

  public Where getWhere() {
    return where;
  }
}
