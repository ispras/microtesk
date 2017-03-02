/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.primitive;

import ru.ispras.fortress.util.InvariantChecks;

public final class StatementFunctionCall extends Statement {
  private final String name;
  private final Object[] args;

  public StatementFunctionCall(final String name, final Object ... args) {
    super(Kind.FUNCALL);
    InvariantChecks.checkNotNull(name);

    this.name = name;
    this.args = args;
  }

  public String getName() {
    return name;
  }

  public int getArgumentCount() {
    return args.length;
  }

  public Object getArgument(final int index) {
    return args[index];
  }
}
