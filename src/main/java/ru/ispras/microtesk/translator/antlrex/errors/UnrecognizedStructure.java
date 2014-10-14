/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.antlrex.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;

public final class UnrecognizedStructure implements ISemanticError {
  private static final String MESSAGE =
      "Failed to recognize the grammar structure. It will be ignored";

  private final String what;

  public UnrecognizedStructure(String what) {
    this.what = what;
  }

  public UnrecognizedStructure() {
    this.what = null;
  }

  @Override
  public String getMessage() {
    if (null == what) {
      return MESSAGE + ".";
    }

    return String.format("%s: '%s'.", MESSAGE, what);
  }
}
