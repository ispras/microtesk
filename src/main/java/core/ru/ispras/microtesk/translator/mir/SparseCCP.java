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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static ru.ispras.microtesk.translator.mir.Instruction.*;
import static ru.ispras.microtesk.translator.mir.GlobalNumbering.DepthFirstPath;
import static ru.ispras.microtesk.translator.mir.GlobalNumbering.Ite;
import static ru.ispras.microtesk.translator.mir.GlobalNumbering.Phi;
import static ru.ispras.microtesk.translator.mir.GlobalNumbering.SsaStore;

public class SparseCCP extends InsnVisitor {
  private final Map<Integer, ImmBranch> mapping = new java.util.HashMap<>();
  private final Map<Integer, DefPoint> locals = new java.util.HashMap<>();
  private final Map<Static, DefPoint> globals = new java.util.HashMap<>();
  private final Map<Operand, Phi> phiJoins = new java.util.HashMap<>();
  private final Frame frame;

  static class ImmBranch {
    final CmpOpcode opc;
    final Local lvalue;
    final Operand rvalue;

    ImmBranch(CmpOpcode opc, Local lvalue, Operand rvalue) {
      this.opc = opc;
      this.lvalue = lvalue;
      this.rvalue = rvalue;
    }

    public boolean isEquality() {
      return opc.equals(CmpOpcode.Eq);
    }

    public boolean substitutes(final Operand opnd) {
      return opnd instanceof Local && lvalue.id == ((Local) opnd).id;
    }
  }

  static class DefPoint {
    final int index;
    final BasicBlock bb;
    final List<Lvalue> deps;

    DefPoint(final int index, final BasicBlock bb) {
      this.index = index;
      this.bb = bb;
      this.deps = Collections.emptyList(); // TODO
    }
  }

  SparseCCP(Frame frame) {
    this.frame = frame;
  }

  @Override
  public void visit(final Assignment insn) {
    if (insn.opc.equals(CmpOpcode.Eq) || insn.opc.equals(CmpOpcode.Ne)) {
      final int condId = insn.lhs.id;
      final CmpOpcode opc = (CmpOpcode) insn.opc;

      final Local lvalue;
      final Operand rvalue;
      if (orderLessThan(insn.op1, insn.op2)) {
        lvalue = (Local) insn.op2;
        rvalue = insn.op1;
      } else {
        lvalue = (Local) insn.op1;
        rvalue = insn.op2;
      }
      mapping.put(condId, new ImmBranch(opc, lvalue, rvalue));
    }
  }

  /* Imm < Local < Static
   * Static: X!n < X!m <==> n < m
   * if unordered, substitute lhs for rhs
   */
  private boolean orderLessThan(final Operand lhs, final Operand rhs) {
    if (lhs instanceof Static) {
      return ((Static) lhs).isSame(rhs) && versionOf(lhs) < versionOf(rhs);
    }
    // lhs : (Constant | Local)
    // rhs : (Constant | Local | Static)
    return lhs instanceof Constant || rhs instanceof Static;
    // lhs : Local
    // rhs : (Constant | Local)
  }

  private static int versionOf(final Operand opnd) {
    return ((Static) opnd).version;
  }

  @Override
  public void visit(final Branch insn) {
    final ImmBranch br = branchOnImm(insn.guard);
    if (br != null && br.rvalue instanceof Constant) {
      final BasicBlock join = searchJoin(insn);
      final BasicBlock entry =
          (br.isEquality()) ? insn.target.get(1) : insn.other;
      final List<BasicBlock> body = EvalContext.breadthFirst(entry, join);
      body.remove(join);

      final Rewriter rw = new Rewriter(br.lvalue, br.rvalue);
      for (final BasicBlock bb : body) {
        rw.block = bb;
        rw.index = 0;
        for (final Instruction insn2 : bb.insns) {
          insn2.accept(rw);
          ++rw.index;
        }
      }
    }
  }

  @Override
  public void visit(final Phi insn) {
    final Operand ref = getAssignedValue(insn.target);
    if (ref instanceof Local) {
      phiJoins.put(ref, insn);
    } else if (ref instanceof Ite) {
      phiJoins.put(((Ite) ref).guard, insn);
    }
    phiJoins.put(insn.target, insn);
    insn.value = inlineIte(insn.value);
  }

  private Ite inlineIte(final Ite origin) {
    final ImmBranch br = branchOnImm(origin.guard);
    if (br != null && phiJoins.containsKey(br.lvalue)) {
      final Ite guardIte = phiJoins.get(br.lvalue).value;
      if (guardIte.taken.equals(br.rvalue)) {
        return rewriteIte(origin, guardIte.guard, !br.isEquality());
      } else if (guardIte.other.equals(br.rvalue)) {
        return rewriteIte(origin, guardIte.guard, br.isEquality());
      }
    }
    return origin;
  }

  private Ite rewriteIte(final Ite ite, final Operand guard, final boolean swap) {
    final Operand taken = (swap) ? ite.other : ite.taken;
    final Operand other = (swap) ? ite.taken : ite.other;
    return new Ite(guard, propagate(taken, guard, true), propagate(other, guard, false));
  }

  private Operand propagate(final Operand opnd, final Operand guard, final boolean value) {
    final Operand e = getAssignedValue(opnd);
    if (phiJoins.containsKey(e)) {
      return propagate(inlineIte(phiJoins.get(e).value), guard, value);
    }
    if (e instanceof Ite) {
      final Ite ite = (Ite) e;
      if (ite.guard.equals(guard)) {
        final Operand child = (value) ? ite.taken : ite.other;
        return propagate(child, guard, value);
      } else {
        return new Ite(
            ite.guard,
            propagate(ite.taken, guard, value),
            propagate(ite.other, guard, value));
      }
    }
    return e;
  }

  private Operand getAssignedValue(Operand opnd) {
    if (opnd instanceof Local) {
      var local = (Local) opnd;
      return selectNonVoid(frame.locals.get(local.id), local);
    } else if (opnd instanceof Static) {
      var mem = (Static) opnd;
      return selectNonVoid(frame.get(mem.name, mem.version), mem);
    } else {
      return opnd;
    }
  }

  private static Operand selectNonVoid(Operand value, Operand def) {
    return (value != VoidTy.VALUE) ? value : def;
  }

  static class Replacer extends OperandVisitor<Operand> {
    private final Operand lvalue;
    private final Operand rvalue;

    public Replacer(final Operand lvalue, final Operand rvalue) {
      this.lvalue = lvalue;
      this.rvalue = rvalue;
    }

    @Override
    public Operand visitOperand(final Operand opnd) {
      return opnd;
    }

    @Override
    public Operand visitStatic(final Static opnd) {
      if (opnd.equals(lvalue)) {
        return rvalue;
      }
      return opnd;
    }

    @Override
    public Operand visitLocal(final Local opnd) {
      if (lvalue instanceof Local && ((Local) lvalue).id == opnd.id) {
        return rvalue;
      }
      return opnd;
    }

    @Override
    public Operand visitField(final Field opnd, final Operand base) {
      return new Field((Lvalue) base, opnd.name);
    }

    @Override
    public Operand visitIndex(final Index opnd, final Operand base, final Operand index) {
      return new Index((Lvalue) base, index);
    }

    @Override
    public Operand visitClosure(final Closure opnd, final List<Operand> upvalues) {
      return new Closure(opnd.callee, upvalues);
    }

    @Override
    public Operand visitIte(
        final Ite opnd, final Operand guard, final Operand taken, final Operand other) {
      return new Ite(guard, taken, other);
    }
  }

  static class Rewriter extends DirectRewriter {
    int index;
    BasicBlock block;

    Rewriter(final Operand lvalue, final Operand rvalue) {
      super(new Replacer(lvalue, rvalue));
    }

    @Override
    public void notifyRewrite(final Instruction source, final Instruction result) {
      block.insns.set(index, result);
    }
  }

  private static BasicBlock searchJoin(final Branch br) {
    if (br.successors.size() > 1) {
      final List<BasicBlock> pathTaken = depthFirst(br.target.get(1));
      final List<BasicBlock> pathOther = depthFirst(br.other);

      for (final BasicBlock bb : pathTaken) {
        if (pathOther.contains(bb)) {
          return bb;
        }
      }
    }
    return null;
  }

  private static List<BasicBlock> depthFirst(final BasicBlock entry) {
    return DepthFirstPath.get(entry, new DepthFirstPath.Sibling<BasicBlock>() {
      @Override
      public Collection<BasicBlock> get(final BasicBlock bb) {
        return EvalContext.targetsOf(bb);
      }
    });
  }

  private ImmBranch branchOnImm(final Operand opnd) {
    return (opnd instanceof Local) ? mapping.get(((Local) opnd).id) : null;
  }

  abstract static class DirectRewriter extends InsnVisitor {
    private final OperandVisitor<Operand> visitor;

    DirectRewriter(final OperandVisitor<Operand> visitor) {
      this.visitor = visitor;
    }

    public abstract void notifyRewrite(Instruction source, Instruction result);

    public final Operand dispatch(final Operand opnd) {
      if (opnd instanceof Field) {
        final Field field = (Field) opnd;
        return visitor.visitField(field, dispatch(field.base));
      }
      if (opnd instanceof Index) {
        final Index index = (Index) opnd;
        return visitor.visitIndex(index, dispatch(index.base), dispatch(index.index));
      }
      if (opnd instanceof Closure) {
        final Closure c = (Closure) opnd;
        return visitor.visitClosure(c, dispatchAll(c.upvalues));
      }
      if (opnd instanceof Local) {
        return visitor.visitLocal((Local) opnd);
      }
      if (opnd instanceof Constant) {
        return visitor.visitConst((Constant) opnd);
      }
      if (opnd instanceof Static) {
        return visitor.visitStatic((Static) opnd);
      }
      if (opnd instanceof Ite) {
        final Ite ite = (Ite) opnd;
        return visitor.visitIte(
          ite, dispatch(ite.guard), dispatch(ite.taken), dispatch(ite.other));
      }
      return visitor.visitOperand(opnd);
    }

    public final List<Operand> dispatchAll(final List<Operand> operands) {
      final List<Operand> values = new java.util.ArrayList<>(operands.size());
      for (final Operand opnd : operands) {
        values.add(dispatch(opnd));
      }
      return values;
    }

    private Local visitLhs(final Local opnd) {
      return (Local) visitor.visitLhs(opnd);
    }

    private Lvalue visitLvalue(final Lvalue opnd) {
      return (Lvalue) visitor.visitLvalue(opnd);
    }

    public void visit(final Assignment insn) {
      final Operand op1 = dispatch(insn.op1);
      final Operand op2 = dispatch(insn.op2);
      final var lhs = visitLhs(insn.lhs);
      notifyRewrite(insn, new Assignment(lhs, insn.opc.make(op1, op2)));
    }

    public void visit(final Concat insn) {
      final List<Operand> args = dispatchAll(insn.rhs);
      final var lhs = visitLhs(insn.lhs);
      notifyRewrite(insn, new Concat(lhs, args));
    }

    public void visit(final Extract insn) {
      final Operand rhs = dispatch(insn.rhs);
      final Operand lo = dispatch(insn.lo);
      final Operand hi = dispatch(insn.hi);
      final var lhs = visitLhs(insn.lhs);
      notifyRewrite(insn, new Extract(lhs, rhs, lo, hi));
    }

    public void visit(final Sext insn) {
      final Operand rhs = dispatch(insn.rhs);
      final var lhs = visitLhs(insn.lhs);
      notifyRewrite(insn, new Sext(lhs, rhs));
    }

    public void visit(final Zext insn) {
      final Operand rhs = dispatch(insn.rhs);
      final var lhs = visitLhs(insn.lhs);
      notifyRewrite(insn, new Zext(lhs, rhs));
    }

    public void visit(final Branch insn) {
      if (insn.successors.size() > 1) {
        final Operand guard = dispatch(insn.guard);
        notifyRewrite(insn, new Branch(guard, insn.target.get(1), insn.other));
      }
    }

    public void visit(final Return insn) {
      if (insn.value != null) {
        final Operand value = dispatch(insn.value);
        notifyRewrite(insn, new Return(value));
      }
    }

    public void visit(final Instruction.Exception insn) { /* TODO */ }

    public void visit(final Call insn) {
      notifyRewrite(insn, rewriteCall(insn));
    }

    public void visit(final Invoke insn) {
      notifyRewrite(insn, new Invoke(rewriteCall(insn.call)));
    }

    private Call rewriteCall(final Call insn) {
      final List<Operand> args = dispatchAll(insn.args);
      final Operand callee = dispatch(insn.callee);
      final var lhs = (insn.ret != null) ? visitLhs(insn.ret) : null;
      return new Call(callee, insn.method, args, (Local) lhs);
    }

    public void visit(final Load insn) {
      final Lvalue src = (Lvalue) dispatch(insn.source);
      final Local lhs = visitLhs(insn.target);
      notifyRewrite(insn, new Load(src, lhs));
    }

    public void visit(final Store insn) {
      final Operand value = dispatch(insn.source);
      final Lvalue lhs = (Lvalue) dispatch(insn.target);
      notifyRewrite(insn, new Store(lhs, value));
    }

    public void visit(final Disclose insn) {
      final Operand source = dispatch(insn.source);
      final Local lhs = visitLhs(insn.target);
      notifyRewrite(insn, new Disclose(lhs, source, insn.indices));
    }

    public void visit(final Conditional insn) {
      final Operand guard = dispatch(insn.guard);
      final Operand taken = dispatch(insn.taken);
      final Operand other = dispatch(insn.other);
      final Local lhs = visitLhs(insn.lhs);

      notifyRewrite(insn, new Conditional(lhs, guard, taken, other));
    }

    public void visit(final Phi insn) {
      if (insn.value != null) {
        final Operand value = dispatch(insn.value);
        final Static lhs = (Static) visitLvalue(insn.target);
        final Phi phi = new Phi(lhs, Collections.<Static>emptyList());
        phi.value = (Ite) value;
        notifyRewrite(insn, phi);
      }
    }

    public void visit(final SsaStore store) {
      final Store insn = store.origin;
      final Operand value = dispatch(insn.source);
      final var src = visitLvalue(insn.target);
      final var lhs = (Static) visitLvalue(store.target);
      notifyRewrite(insn, new SsaStore(lhs, new Store(src, value)));
    }
  }
}
