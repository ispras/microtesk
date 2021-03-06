/*
    Copyright 2019 ISP RAS (http://www.ispras.ru)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package ru.ispras.microtesk.translator.mir;

import ru.ispras.castle.util.Logger;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ru.ispras.microtesk.translator.mir.BvOpcode.bitVectorOf;
import static ru.ispras.microtesk.translator.mir.BvOpcode.constantOf;
import static ru.ispras.microtesk.translator.mir.GlobalNumbering.Phi;
import static ru.ispras.microtesk.translator.mir.GlobalNumbering.SsaStore;
import static ru.ispras.microtesk.translator.mir.Instruction.*;

public final class EvalContext extends InsnVisitor {
  private static final List<CmpOpcode> EQOPC = Arrays.asList(CmpOpcode.Eq, CmpOpcode.Ne);

  private final Map<String, List<Operand>> globals = new java.util.HashMap<>();
  private final Map<Operand, Set<Operand>> inequal = new java.util.HashMap<>();
  private final Frame frame;

  private int origin = 0;

  public static EvalContext eval(final MirContext mir, final Map<String, BigInteger> presets) {
    final var context = new EvalContext();
    for (final var entry : presets.entrySet()) {
      // FIXME 128-bit default value may break
      context.frame.set(entry.getKey(), 1, Constant.valueOf(128, entry.getValue()));
    }
    return context.evalInternal(mir);
  }

  public static EvalContext propagatePhi(MirContext mir, Map<String, BigInteger> presets) {
    // TODO avoid N-times calling topological order
    final EvalContext ctx = eval(mir, presets);
    final var propagate = new ScalarPhiPropagation(ctx.getFrame());
    for (BasicBlock bb : topologicalOrder(mir)) {
      for (Instruction insn : bb.insns) {
        insn.accept(propagate);
      }
    }
    return ctx;
  }

  private static final class ScalarPhiPropagation extends InsnVisitor {
    final Frame frame;

    ScalarPhiPropagation(Frame frame) {
      this.frame = frame;
    }

    @Override
    public void visit(final Phi insn) {
      if (!Types.isArray(insn.target) && insn.value != null) {
        final Operand ref = getStoredValue(insn.target);
        if (ref instanceof Local) {
          resetLocal((Local) ref, insn.value);
        } else if (ref == insn.target) {
          store(insn.target, insn.value);
        }
      }
    }

    private void resetLocal(Local local, Operand value) {
      frame.locals.set(local.id, value);
    }

    private void store(Static mem, Operand val) {
      if (val instanceof Static) {
        val = getStoredValue((Static) val);
      }
      frame.set(mem.name, mem.version, val);
    }

    private Operand getStoredValue(final Static mem) {
      final Operand value = frame.get(mem.name, mem.version);
      return (value.equals(VoidTy.VALUE)) ? mem : value;
    }
  }

  EvalContext() {
    this.frame = new Frame(this.globals);
  }

  private EvalContext evalInternal(final MirContext mir) {
    eval(mir.locals.size(), topologicalOrder(mir));
    return this;
  }

  Frame getFrame() {
    return frame;
  }

  Frame eval(final int nlocals, final Collection<BasicBlock> blocks) {
    final int n = nlocals - frame.locals.size();
    if (n > 0) {
      frame.locals.addAll(Collections.nCopies(n, VoidTy.VALUE));
    }
    for (final BasicBlock bb : blocks) {
      int i = 0;
      for (final Instruction insn : bb.insns) {
        Logger.debug("EVAL: %s", MirText.stringOf(insn));
        this.origin = bb.getOrigin(i++);
        insn.accept(this);
      }
    }
    return frame;
  }

  @Override
  public void visit(final Assignment insn) {
    if (insn.opc.equals(UnOpcode.Use)) {
      setLocal(indexOf(insn.lhs), getValueRec(insn.op1));
    }
    final Operand op1 = getValueRec(insn.op1);
    final Operand op2 = getValueRec(insn.op2);
    if (insn.opc instanceof ConstEvaluated && op1 instanceof Constant && op2 instanceof Constant) {
      final ConstEvaluated eval = (ConstEvaluated) insn.opc;
      final Constant value = eval.evalConst((Constant) op1, (Constant) op2);
      setLocal(indexOf(insn.lhs), value);
    } else if (EQOPC.contains(insn.opc) && op1.equals(op2)) {
      setLocal(indexOf(insn.lhs), newBoolean(insn.opc.equals(CmpOpcode.Eq)));
    } else if (EQOPC.contains(insn.opc) && getInequal(op1).contains(op2)) {
      setLocal(indexOf(insn.lhs), newBoolean(insn.opc.equals(CmpOpcode.Ne)));
    } else if (getModified(insn.opc, op1, op2) != null) {
      setInequal(rebaseLocal(insn.lhs), getModified(insn.opc, op1, op2));
    }
  }

  private Local rebaseLocal(final Local lval) {
    return new Local(indexOf(lval), lval.getType());
  }

  private Operand getModified(final BinOpcode opc, final Operand op1, final Operand op2) {
    if (opc.equals(BvOpcode.Add)) {
      if (isNonZero(op1)) {
        return op2;
      } else if (isNonZero(op2)) {
        return op1;
      }
    }
    return null;
  }

  private static boolean isNonZero(final Operand opnd) {
    if (opnd instanceof Constant) {
      return ((Constant) opnd).getValue().signum() != 0;
    }
    return false;
  }

  private void setInequal(final Operand lhs, final Operand rhs) {
    getCreateInequal(lhs).add(rhs);
    getCreateInequal(rhs).add(lhs);
  }

  private Set<Operand> getInequal(final Operand lval) {
    return inequal.getOrDefault(lval, Collections.emptySet());
  }

  private Set<Operand> getCreateInequal(final Operand lval) {
    return inequal.computeIfAbsent(lval, k -> new java.util.HashSet<>());
  }

  private static Constant newBoolean(final boolean value) {
    return Constant.bitOf(value ? 1 : 0);
  }

  @Override
  public void visit(final Disclose insn) {
    int i = 0;
    Operand opnd = getValueRec(insn.source);
    while (i < insn.indices.size() && opnd instanceof Closure) {
      final BigInteger index = insn.indices.get(i++).getValue();
      opnd = getValueDirect(((Closure) opnd).upvalues.get(index.intValue()));
    }
    if (i >= insn.indices.size()) {
      setLocal(indexOf(insn.target), opnd);
    }
  }

  @Override
  public void visit(final Load insn) {
    final Static mem = cast(insn.source, Static.class);
    if (mem != null && mem.version > 0) {
      final Operand stored = frame.get(mem.name, mem.version);
      if (stored instanceof Constant) {
        final int size = insn.target.getType().getSize();
        final var value =
            constantOf(bitVectorOf((Constant) stored).resize(size, false));
        setLocal(indexOf(insn.target), value);
      } else if (!stored.equals(VoidTy.VALUE)) {
        setLocal(indexOf(insn.target), stored);
      } else {
        frame.set(mem.name, mem.version, rebaseLocal(insn.target));
      }
    }
  }

  @Override
  public void visit(final Conditional insn) {
    final Operand guard = getValueRec(insn.guard);
    if (guard instanceof Constant) {
      final Operand value = isNonZero(guard) ? insn.taken : insn.other;
      setLocal(indexOf(insn.lhs), getValueRec(value));
    } else {
      final Operand taken = getValueRec(insn.taken);
      final Operand other = getValueRec(insn.other);
      if (taken.equals(other)) {
        setLocal(indexOf(insn.lhs), taken);
      }
    }
  }

  @Override
  public void visit(final SsaStore insn) {
    final Static mem = insn.target;
    if (insn.origin.target instanceof Static) {
      frame.set(mem.name, mem.version, getValueRec(insn.origin.source));
    } else {
      frame.set(mem.name, mem.version, VoidTy.VALUE);
    }
  }

  @Override
  public void visit(final Sext insn) {
    if (insn.rhs instanceof Constant) {
      final int size = insn.lhs.getType().getSize();
      final Constant value =
        constantOf(bitVectorOf((Constant) insn.rhs).resize(size, true));
      setLocal(indexOf(insn.lhs), value);
    }
  }

  @Override
  public void visit(final Zext insn) {
    if (insn.rhs instanceof Constant) {
      final int size = insn.lhs.getType().getSize();
      final Constant value =
        constantOf(bitVectorOf((Constant) insn.rhs).resize(size, false));
      setLocal(indexOf(insn.lhs), value);
    }
  }

  private Operand getValueRec(final Operand opnd) {
    if (opnd instanceof Closure) {
      final Closure closure = (Closure) opnd;
      final List<Operand> upvalues = new java.util.ArrayList<>(closure.upvalues.size());
      for (final Operand upval : closure.upvalues) {
        upvalues.add(getValueRec(upval));
      }
      return new Closure(closure.callee, upvalues);
    } else if (opnd instanceof Local) {
      final int index = indexOf((Local) opnd);
      final Operand value = getLocal(index);
      if (value.equals(VoidTy.VALUE)) {
        return new Local(index, opnd.getType());
      }
      return value;
    } else if (opnd instanceof Static) {
      final Static mem = (Static) opnd;
      final Operand value = frame.get(mem.name, mem.version);
      return (value.equals(VoidTy.VALUE)) ? mem : value;
    }
    return opnd;
  }

  private Operand getValueDirect(final Operand opnd) {
    if (opnd instanceof Local) {
      final Local local = (Local) opnd;
      final Operand value = getLocal(local.id);
      return (value.equals(VoidTy.VALUE)) ? local : value;
    }
    return opnd;
  }

  private int indexOf(final Local opnd) {
    return ((Local) opnd).id + this.origin;
  }

  Operand getLocal(final int index) {
    return frame.locals.get(index);
  }

  private void setLocal(final int index, final Operand value) {
    frame.locals.set(index, value);
  }

  public static List<BasicBlock> topologicalOrder(final MirContext mir) {
    final Map<BasicBlock, Set<BasicBlock>> inedges = new java.util.HashMap<>();
    for (final BasicBlock bb : mir.blocks) {
      for (final BasicBlock succ : targetsOf(bb)) {
        Set<BasicBlock> pred = inedges.get(succ);
        if (pred == null) {
          pred = new java.util.LinkedHashSet<>();
          inedges.put(succ, pred);
        }
        pred.add(bb);
      }
    }
    final List<BasicBlock> sorted = new java.util.ArrayList<>();
    final List<BasicBlock> queued = new java.util.ArrayList<>();
    queued.add(mir.blocks.get(0));
    while (!queued.isEmpty()) {
      final BasicBlock bb = queued.remove(queued.size() - 1);
      for (final BasicBlock succ : targetsOf(bb)) {
        final Set<BasicBlock> pred = inedges.get(succ);
        pred.remove(bb);
        if (pred.isEmpty()) {
          queued.add(succ);
        }
      }
      sorted.add(bb);
    }
    return sorted;
  }

  public static List<BasicBlock> breadthFirst(final MirContext mir) {
    return breadthFirst(mir.blocks.get(0), null);
  }

  public static List<BasicBlock> breadthFirst(final BasicBlock entry, final BasicBlock endpoint) {
    final List<BasicBlock> blocks = new java.util.ArrayList<>();
    blocks.add(entry);

    for (int i = 0; i < blocks.size(); ++i) {
      final BasicBlock source = blocks.get(i);
      if (!source.equals(endpoint)) {
        for (final BasicBlock bb : targetsOf(source)) {
          if (!blocks.contains(bb)) {
            blocks.add(bb);
          }
        }
      }
    }
    return blocks;
  }

  public static List<BasicBlock> targetsOf(final BasicBlock bb) {
    final Instruction insn = Lists.lastOf(bb.insns);
    if (insn instanceof Branch) {
      return ((Branch) insn).successors;
    }
    return Collections.emptyList();
  }

  private static <T> T cast(final Object o, final Class<T> cls) {
    if (cls.isInstance(o)) {
      return cls.cast(o);
    }
    return null;
  }
}
