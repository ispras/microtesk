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
  public static final Class<?> ACTION_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction.class;

  public static final Class<?> TRANSITION_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition.class;

  private final String context;
  private final ST st;
  private final STGroup group;
  private final ST stReg;

  private List<String> currentMarks = null;

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

    st.add("imps", ACTION_CLASS.getName());
    st.add("imps", TRANSITION_CLASS.getName());
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
      final String stop,
      final List<Stmt> stmts) {
    InvariantChecks.checkNotNull(start);
    InvariantChecks.checkNotNull(stop);
    InvariantChecks.checkNotNull(stmts);

    st.add("members", "");

    buildAction(start, true);
    buildAction(stop, true);

    final String current = start;
    buildTransition(current, stop, null);
  }

  private void buildAction(final String id, final String... args) {
    buildAction(id, false, args);
  }

  private void buildAction(final String id, boolean isPre, final String... args) {
    InvariantChecks.checkNotNull(id);

    final ST stAction = group.getInstanceOf("action");
    stAction.add("id", id);
    stAction.add("name", context + "." + id);

    for (final String arg : args) {
      stAction.add("args", arg);
    }

    if (null != currentMarks) {
      for (final String mark : currentMarks) {
        stAction.add("marks", mark);
      }
      currentMarks = null;
    }

    st.add("members", stAction);

    if (isPre) {
      stReg.add("pres", id);
    } else {
      stReg.add("acts", id);
    }
  }
  
  private void buildTransition(final String source, final String target, final String guard) {
    InvariantChecks.checkNotNull(source);
    InvariantChecks.checkNotNull(target);

    final ST stTrans = group.getInstanceOf("transition");

    stTrans.add("source", source);
    stTrans.add("target", target);

    if (null != guard) {
      stTrans.add("guard", guard);
    }

    stReg.add("trans", stTrans);
  }
}
