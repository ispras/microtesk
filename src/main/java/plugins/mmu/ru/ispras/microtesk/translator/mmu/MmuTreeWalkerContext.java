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

package ru.ispras.microtesk.translator.mmu;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.translator.mmu.ir.Var;

final class MmuTreeWalkerContext {
  public static enum Kind {
    GLOBAL,
    BUFFER,
    MEMORY
  }

  public static final MmuTreeWalkerContext GLOBAL = 
      new MmuTreeWalkerContext(Kind.GLOBAL, "");

  private final Kind kind;
  private final String id;
  private final Map<String, Var> variables;

  MmuTreeWalkerContext(Kind kind, String id) {
    checkNotNull(kind);
    checkNotNull(id);

    this.kind = kind;
    this.id = id;
    this.variables = new HashMap<>();
  }

  public Kind getKind() {
    return kind;
  }

  public String getId() {
    return id;
  }

  public void defineVariable(Var variable) {
    checkNotNull(variable);
    variables.put(variable.getId(), variable);
  }

  public void defineVariableAs(String variableId, Var variable) {
    checkNotNull(variableId);
    checkNotNull(variable);
    variables.put(variableId, variable);
  }

  public Var getVariable(String variableId) {
    return variables.get(variableId);
  }
}
