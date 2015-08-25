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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Stmt;

final class ControlFlowBuilder {
  private final String context;
  private final ST st;
  private final STGroup group;
  private final ST stReg;

  protected ControlFlowBuilder(
      final String context,
      final ST st,
      final STGroup group,
      final ST stReg) {
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(st);
    InvariantChecks.checkNotNull(group);
    InvariantChecks.checkNotNull(stReg);

    this.context = context;
    this.st = st;
    this.group = group;
    this.stReg = stReg;
  }

  public void build(
      final String start,
      final String end,
      final String startRead,
      final List<Stmt> stmtsRead,
      final String startWrite,
      final List<Stmt> stmtsWrite) {
    InvariantChecks.checkNotNull(start);
    InvariantChecks.checkNotNull(end);
    InvariantChecks.checkNotNull(startRead);
    InvariantChecks.checkNotNull(startWrite);
    InvariantChecks.checkNotNull(stmtsRead);
    InvariantChecks.checkNotNull(stmtsWrite);
  }

  public void build(
      final String start,
      final String end,
      final List<Stmt> stmts) {
    InvariantChecks.checkNotNull(start);
    InvariantChecks.checkNotNull(end);
    InvariantChecks.checkNotNull(stmts);
  }
}
