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

package ru.ispras.microtesk.translator.mmu.ir;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.data.DataType;

public final class Attribute {
  private final String id;
  private final List<Stmt> stmts;
  private final DataType type;

  public Attribute(String id, DataType type, List<Stmt> stmts) {
    checkNotNull(id);
    checkNotNull(stmts);

    this.id = id;
    this.type = type;
    this.stmts = Collections.unmodifiableList(stmts);
  }

  public String getId() {
    return id;
  }

  public DataType getType() {
    return type;
  }

  public List<Stmt> getStatements() {
    return stmts;
  }

  @Override
  public String toString() {
    return String.format("attribute %s = %s", id, stmts);
  }
}
