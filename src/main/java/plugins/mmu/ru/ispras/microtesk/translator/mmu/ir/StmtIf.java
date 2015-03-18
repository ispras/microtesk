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
import static ru.ispras.fortress.util.InvariantChecks.checkNotEmpty;

import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.Pair;

public final class StmtIf extends Stmt {
  private final List<Pair<Node, List<Stmt>>> ifBlocks;
  private final List<Stmt> elseBlock;

  public StmtIf(List<Pair<Node, List<Stmt>>> ifBlocks, List<Stmt> elseBlock) {
    super(Kind.IF);

    checkNotEmpty(ifBlocks);
    checkNotNull(elseBlock);

    this.ifBlocks = Collections.unmodifiableList(ifBlocks);
    this.elseBlock = Collections.unmodifiableList(elseBlock);
  }

  public List<Pair<Node, List<Stmt>>> getIfBlocks() {
    return ifBlocks;
  }

  public List<Stmt> getElseBlock() {
    return elseBlock;
  }

  @Override
  public String toString() {
    return String.format("StmtIf [if=%s, else=%s]", ifBlocks, elseBlock);
  }
}
