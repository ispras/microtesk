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

package ru.ispras.microtesk.mmu.translator.ir;

import ru.ispras.fortress.expression.Node;

import java.util.List;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.fortress.util.InvariantChecks.checkTrue;

public final class StmtCall extends Stmt {
  private final Callable callee;
  private final List<Node> arguments;

  public StmtCall(final Callable callee, final List<Node> arguments) {
    super(Kind.CALL);
    checkNotNull(callee);
    checkNotNull(arguments);
    checkTrue(callee.getParameters().size() == arguments.size());

    this.callee = callee;
    this.arguments = arguments;
  }

  public Callable getCallee() {
    return callee;
  }

  public List<Node> getArguments() {
    return arguments;
  }

  @Override
  public String toString() {
    return String.format("stmt call %s%s", callee.getName(), arguments);
  }
}
