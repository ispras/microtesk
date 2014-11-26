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

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.List;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;

public final class StatementCondition extends Statement {
  public static final class Block {
    private final Expr condition;
    private final List<Statement> statements;

    private Block(Expr condition, List<Statement> statements) {
      if (null == statements) {
        throw new NullPointerException();
      }

      this.condition = condition;
      this.statements = statements;
    }

    public static Block newIfBlock(Expr condition, List<Statement> statements) {
      if (null == condition) {
        throw new NullPointerException();
      }

      return new Block(condition, statements);
    }

    public static Block newElseBlock(List<Statement> statements) {
      return new Block(null, statements);
    }

    public Expr getCondition() {
      return condition;
    }

    public boolean isElseBlock() {
      return null == condition;
    }

    public List<Statement> getStatements() {
      return statements;
    }
  }

  private final List<Block> blocks;

  StatementCondition(List<Block> blocks) {
    super(Kind.COND);

    if (null == blocks) {
      throw new NullPointerException();
    }

    if (blocks.isEmpty()) {
      throw new IllegalArgumentException();
    }

    this.blocks = blocks;
  }

  public int getBlockCount() {
    return blocks.size();
  }

  public Block getBlock(int index) {
    if (!((0 <= index) && (index < getBlockCount()))) {
      throw new IndexOutOfBoundsException();
    }

    return blocks.get(index);
  }
}
