/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

import org.antlr.runtime.IntStream;
import org.antlr.runtime.RecognitionException;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.antlrex.Where;

public final class SemanticException extends RecognitionException {
  private static final long serialVersionUID = 209516770104977723L;

  private final ISemanticError error;

  protected SemanticException(IntStream input, ISemanticError error) {
    super(input);
    this.error = error;
  }

  protected SemanticException(Where location, ISemanticError error) {
    super();
    this.error = error;

    this.line = location.getLine();
    this.charPositionInLine = location.getPosition();
    this.approximateLineInfo = true;
  }

  @Override
  public String getMessage() {
    return error.getMessage();
  }
}
