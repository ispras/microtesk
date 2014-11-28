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

package ru.ispras.microtesk.translator.simnml.antlrex;

import java.util.Map;
import ru.ispras.microtesk.translator.antlrex.IErrorReporter;
import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;
import ru.ispras.microtesk.translator.simnml.ir.IR;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;

public class WalkerFactoryBase implements WalkerContext {
  private final WalkerContext context;

  public WalkerFactoryBase(WalkerContext context) {
    if (null == context) {
      throw new NullPointerException();
    }
    this.context = context;
  }

  @Override
  public IErrorReporter getReporter() {
    return context.getReporter();
  }

  @Override
  public SymbolTable getSymbols() {
    return context.getSymbols();
  }

  @Override
  public IR getIR() {
    return context.getIR();
  }

  @Override
  public Map<String, Primitive> getThisArgs() {
    return context.getThisArgs();
  }

  @Override
  public Primitive.Holder getThis() {
    return context.getThis();
  }

  protected final void raiseError(final Where where, final String what) throws SemanticException {
    raiseError(where, new ISemanticError() {
      @Override
      public String getMessage() {
        return what;
      }
    });
  }

  protected final void raiseError(Where where, ISemanticError what) throws SemanticException {
    getReporter().raiseError(where, what);
  }
}
