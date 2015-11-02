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

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class Callable {
  private final String name;
  private final List<Variable> params;
  private final Map<String, Variable> locals;
  private final List<Stmt> body;
  private final Variable output;

  public Callable(
      final String name,
      final List<Variable> params,
      final Map<String, Variable> locals,
      final List<Stmt> body,
      final Variable output) {
    this.name = name;
    this.params = params;
    this.locals = locals;
    this.body = body;
    this.output = output;
  }

  public String getName() {
    return name;
  }

  public Map<String, Variable> getLocals() {
    return Collections.unmodifiableMap(locals);
  }

  public List<Variable> getParameters() {
    return Collections.unmodifiableList(params);
  }

  public Variable getParameter(final int i) {
    return params.get(i);
  }

  public Variable getParameter(final String name) {
    for (final Variable p : params) {
      if (p.getName().equals(name)) {
        return p;
      }
    }
    return null;
  }

  public Variable getOutput() {
    return output;
  }

  public List<Stmt> getBody() {
    return body;
  }
}
