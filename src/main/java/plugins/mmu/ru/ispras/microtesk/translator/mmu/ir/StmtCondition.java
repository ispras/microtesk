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

public final class StmtCondition extends Stmt {
  public static final class Block {
    private final Node condition;
    private final List<Stmt> statements;

    private Block(Node condition, List<Stmt> statements) {
      checkNotNull(statements);
      this.condition = condition;
      this.statements = Collections.unmodifiableList(statements);
    }

    public static Block newIfBlock(Node condition, List<Stmt> statements) {
      checkNotNull(condition);
      return new Block(condition, statements);
    }

    public static Block newElseBlock(List<Stmt> statements) {
      return new Block(null, statements);
    }

    public Node getCondition() {
      return condition;
    }

    public boolean isElseBlock() {
      return null == condition;
    }

    public List<Stmt> getStatements() {
      return statements;
    }
  }

  private final List<Block> blocks;

  StmtCondition(List<Block> blocks) {
    super(Kind.COND);

    checkNotEmpty(blocks);
    this.blocks = Collections.unmodifiableList(blocks);
  }

  public List<Block> getBlocks() {
    return blocks;
  }
}
