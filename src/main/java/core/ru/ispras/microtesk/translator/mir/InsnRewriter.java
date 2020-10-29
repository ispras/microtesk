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

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import static ru.ispras.microtesk.translator.mir.Instruction.*;
import static ru.ispras.microtesk.translator.mir.GlobalNumbering.Ite;
import static ru.ispras.microtesk.translator.mir.GlobalNumbering.Phi;
import static ru.ispras.microtesk.translator.mir.GlobalNumbering.SsaStore;

public class InsnRewriter extends InsnVisitor {
  private final Frame frame;
  private final Map<BasicBlock, BasicBlock> bbMap = new java.util.HashMap<>();

  private SortedSet<Integer> retained;
  private MirBlock block;
  private int nskip;

  public static void rewrite(final MirContext ctx, final Map<String, BigInteger> presets) {
    final EvalContext eval = EvalContext.eval(ctx, presets);
    final InsnRewriter worker = new InsnRewriter(eval.getFrame());

    worker.doRewrite(ctx, getAliveSet(ctx, worker.frame));
    worker.doRewrite(ctx, getReachedSet(ctx));
  }

  private static SortedSet<Integer> getAliveSet(final MirContext mir, final Frame frame) {
    final SortedSet<Integer> set = new java.util.TreeSet<>();
    for (int i = 0; i < mir.getSignature().params.size() + 1; ++i) {
      set.add(i);
    }
    int index = 0;
    for (final Operand value : frame.locals) {
      if (value.equals(VoidTy.VALUE)) {
        set.add(index);
      }
      ++index;
    }
    return set;
  }

  private static SortedSet<Integer> getReachedSet(final MirContext mir) {
    final SortedSet<Integer> set = new java.util.TreeSet<>();
    for (int i = 0; i < mir.getSignature().params.size() + 1; ++i) {
      set.add(i);
    }
    final OperandVisitor<Void> visitor = new OperandVisitor<Void>() {
      @Override
      public Void visitLocal(final Local opnd) {
        set.add(opnd.id);
        return null;
      }

      @Override
      public Void visitOperand(final Operand opnd) {
        return null;
      }
    };
    final OperandWalker<Void> walker = new OperandWalker<>(visitor);
    final List<BasicBlock> worklist = EvalContext.breadthFirst(mir);
    for (final BasicBlock bb : worklist) {
      for (final Instruction insn : bb.insns) {
        insn.accept(walker);
      }
    }
    return set;
  }

  private void doRewrite(final MirContext ctx, final SortedSet<Integer> retained) {
    final List<BasicBlock> worklist = EvalContext.breadthFirst(ctx);
    ctx.blocks.clear();
    ctx.blocks.addAll(worklist);

    this.retained = retained;

    bbMap.clear();
    final List<BasicBlock> blocks = new java.util.ArrayList<>();
    final int nblocks = ctx.blocks.size();
    for (int i = 0; i < nblocks; ++i) {
      final BasicBlock origin = ctx.blocks.get(i);
      final BasicBlock bb = new BasicBlock();
      blocks.add(bb);

      bbMap.put(bb, origin);
      bbMap.put(origin, bb);
    }
    for (int i = 0; i < nblocks; ++i) {
      this.block = new MirBlock(ctx, blocks.get(i));
      this.nskip = 0;
      for (final Instruction insn : ctx.blocks.get(i).insns) {
        insn.accept(this);
      }
    }
    ctx.blocks.clear();
    ctx.blocks.addAll(blocks);

    retainIndices(ctx.locals, retained);
    retainIndices(this.frame.locals, retained);
  }

  private static <T> void retainIndices(final List<T> list, final SortedSet<Integer> indices) {
    int i = 0;
    for (final int index : indices) {
      list.set(i++, list.get(index));
    }
    list.subList(indices.size(), list.size()).clear();
  }

  private static List<MirTy> localsOf(final MirContext ctx) {
    return Lists.tailOf(ctx.locals, ctx.getSignature().params.size() + 1);
  }

  private InsnRewriter(final Frame frame) {
    this.frame = frame;
  }

  private Operand rewrite(final Operand source) {
    for (final Local opnd = cast(source, Local.class); opnd != null; ) {
      final int index = indexOf(opnd);
      return (isAlive(index))
          ? rebase(opnd)
          : offsetDirect(frame.locals.get(index));
    }
    for (final Static opnd = cast(source, Static.class); opnd != null; ) {
      final Operand value = frame.get(opnd.name, opnd.version);
      return (value.equals(VoidTy.VALUE)) ? opnd : rewrite(value);
    }
    for (final Index opnd = cast(source, Index.class); opnd != null; ) {
      return new Index((Lvalue) rewriteLvalue(opnd.base), rewrite(opnd.index));
    }
    for (final Field opnd = cast(source, Field.class); opnd != null; ) {
      return new Field((Lvalue) rewriteLvalue(opnd.base), opnd.name);
    }
    for (final Closure opnd = cast(source, Closure.class); opnd != null; ) {
      return new Closure(opnd.callee, rewriteAll(opnd.upvalues));
    }
    for (final Ite opnd = cast(source, Ite.class); opnd != null; ) {
      final Operand guard = rewrite(opnd.guard);
      final Operand taken = rewrite(opnd.taken);
      final Operand other = rewrite(opnd.other);
      if (guard instanceof Constant) {
        final BigInteger value = ((Constant) guard).getValue();
        if (value.equals(BigInteger.ZERO)) {
          return other;
        } else {
          return taken;
        }
      } else {
        return new Ite(guard, taken, other);
      }
    }
    return source;
  }

  private Lvalue rewriteLvalue(final Lvalue opnd) {
    if (opnd instanceof Static) {
      return opnd;
    }
    return (Lvalue) rewrite(opnd);
  }

  private static <T> T cast(final Object o, final Class<T> cls) {
    if (cls.isInstance(o)) {
      return cls.cast(o);
    }
    return null;
  }

  private List<Operand> rewriteAll(final List<Operand> opnds) {
    final List<Operand> ret = new java.util.ArrayList<>();
    for (final Operand opnd : opnds) {
      ret.add(rewrite(opnd));
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  private <T extends Operand> T rebase(final T source) {
    if (source instanceof Local) {
      return (T) new Local(offsetIndex(indexOf(source)), source.getType());
    }
    return source;
  }

  private Operand offsetDirect(final Operand source) {
    for (final Local local = cast(source, Local.class); local != null; ) {
      return new Local(offsetIndex(local.id), local.getType());
    }
    return source;
  }

  private int offsetIndex(final int index) {
    return retained.headSet(index).size();
  }

  private int indexOf(final Operand opnd) {
    final Local local = (Local) opnd;
    final BasicBlock bb = getBlockOrigin(block.bb);

    return local.id + bb.getOrigin(block.bb.insns.size() + nskip);
  }

  private BasicBlock getBlockOrigin(final BasicBlock bb) {
    return bbMap.get(bb);
  }

  private BasicBlock getBlockImage(final BasicBlock bb) {
    return bbMap.get(bb);
  }

  private boolean isAlive(final Operand opnd) {
    for (final Local local = cast(opnd, Local.class); local != null; ) {
      return isAlive(indexOf(local));
    }
    return true;
  }

  private boolean isAlive(final int index) {
    return retained.contains(index);
  }

  @Override
  public void visit(final Conditional insn) {
    if (isAlive(insn.lhs)) {
      final Operand guard = rewrite(insn.guard);
      final Operand taken = rewrite(insn.taken);
      final Operand other = rewrite(insn.other);

      block.append(new Conditional(rebase(insn.lhs), guard, taken, other));
    } else {
      ++nskip;
    }
  }

  @Override
  public void visit(final Assignment insn) {
    if (isAlive(insn.lhs)) {
      final Operand op1 = rewrite(insn.op1);
      final Operand op2 = rewrite(insn.op2);
      block.assign(rebase(insn.lhs), insn.opc.make(op1, op2));
    } else {
      ++nskip;
    }
  }

  @Override
  public void visit(final Concat insn) {
    block.append(new Concat(rebase(insn.lhs), rewriteAll(insn.rhs)));
  }

  @Override
  public void visit(final Extract insn) {
    block.append(new Extract(
        rebase(insn.lhs), rewrite(insn.rhs), rewrite(insn.lo), rewrite(insn.hi)));
  }

  public void visit(final Sext insn) {
    if (isAlive(insn.lhs)) {
      block.append(new Sext(rebase(insn.lhs), rewrite(insn.rhs)));
    } else {
      ++nskip;
    }
  }

  public void visit(final Zext insn) {
    if (isAlive(insn.lhs)) {
      block.append(new Zext(rebase(insn.lhs), rewrite(insn.rhs)));
    } else {
      ++nskip;
    }
  }

  public void visit(final Branch insn) {
    if (insn.successors.size() == 1) {
      block.append(new Branch(getBlockImage(insn.other)));
    } else {
      final Operand guard = rewrite(insn.guard);
      if (guard instanceof Constant) {
        final int variant = ((Constant) guard).getValue().abs().intValue();
        final BasicBlock target =
            (insn.target.containsKey(variant)) ? insn.target.get(variant) : insn.other;
        block.append(new Branch(getBlockImage(target)));
      } else {
        block.append(new Branch(
            guard, getBlockImage(insn.target.get(1)), getBlockImage(insn.other)));
      }
    }
  }

  public void visit(final Return insn) {
    block.append(new Return(rewrite(insn.value)));
  }

  public void visit(final Instruction.Exception insn) {
    block.append(insn); // TODO
  }

  public void visit(final Call insn) {
    block.call(rewrite(insn.callee), insn.method, rewriteAll(insn.args), (Local) rebase(insn.ret));
  }

  public void visit(final Invoke insn) {
    block.append(insn); // TODO
  }

  public void visit(final Load insn) {
    if (isAlive(insn.target)) {
      block.append(new Load(rewriteLvalue(insn.source), (Local) rebase(insn.target)));
    } else {
      ++nskip;
    }
  }

  public void visit(final Store insn) {
    block.append(new Store(rewriteLvalue(insn.target), rewrite(insn.source))); // FIXME
  }

  public void visit(final SsaStore insn) {
    final Store origin = insn.origin;
    final Store update = new Store(rewriteLvalue(origin.target), rewrite(origin.source));
    block.append(new GlobalNumbering.SsaStore(insn.target, update));
  }

  public void visit(final Phi insn) {
    if (insn.value != null) {
      final Operand value = rewrite(insn.value);
      if (value instanceof Ite) {
        final Phi phi = new Phi(insn.target, insn.values);
        phi.value = (Ite) value;
        block.append(phi);
      } else {
        block.append(new Store(insn.target, value));
      }
    }
  }

  public void visit(final Disclose insn) {
    if (isAlive(insn.target)) {
      block.append(new Disclose((Local) rebase(insn.target), rewrite(insn.source), insn.indices));
    } else {
      ++nskip;
    }
  }
}
