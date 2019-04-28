package ru.ispras.microtesk.translator.mir;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MirText {
  private final MirContext context;
  private final List<String> lines = new java.util.ArrayList<>();

  public MirText(final MirContext ctx) {
    this.context = ctx;
    collect(lines, ctx);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final String newline = System.lineSeparator();

    for (final String line : lines) {
      sb.append(line);
      sb.append(newline);
    }
    return sb.toString();
  }

  private static void collect(final List<String> lines, final MirContext ctx) {
    lines.add(String.format("%s %s", ctx.name, ctx.getSignature().getName()));

    final Map<BasicBlock, String> labels = new java.util.IdentityHashMap<>();

    int i = 0;
    for (final BasicBlock bb : ctx.blocks) {
      labels.put(bb, String.format("bb%d", i++));
    }
    final InsnText visitor = new InsnText(labels, lines);
    for (final BasicBlock bb : ctx.blocks) {
      lines.add(String.format("%s:", labels.get(bb)));

      int j = 0;
      for (final Instruction insn : bb.insns) {
        visitor.origin = bb.getOrigin(j++);
        insn.accept(visitor);
      }
    }
  }

  private static final class InsnText extends InsnVisitor {
    public int origin = 0;
    private final Map<BasicBlock, String> labels;
    private final List<String> lines;

    InsnText(final Map<BasicBlock, String> labels, final List<String> lines) {
      this.labels = labels;
      this.lines = lines;
    }

    @Override
    public void visit(final Assignment insn) {
      lines.add(String.format("%s = %s %s %s %s",
        stringOf(insn.lhs),
        insn.opc.typeOf(insn.op1, insn.op2).getName(),
        insn.opc,
        insn.op1.getType().getName(),
        stringOf(insn.opc, insn.op1, insn.op2)));
    }


    private String stringOf(final BinOpcode opc, final Operand op1, final Operand op2) {
      if (opc instanceof UnOpcode) {
        return stringOf(op1);
      }
      return String.format("%s, %s", stringOf(op1), stringOf(op2));
    }

    private String stringOf(final Operand opnd) {
      for (final Local lval = cast(opnd, Local.class); lval != null;) {
        return String.format("%%%d", this.origin + lval.id);
      }
      for (final Field lval = cast(opnd, Field.class); lval != null;) {
        return String.format("%s.%s", stringOf(lval.base), lval.name);
      }
      for (final Index lval = cast(opnd, Index.class); lval != null;) {
        return String.format("%s[%s]", stringOf(lval.base), stringOf(lval.index));
      }
      for (final Closure lval = cast(opnd, Closure.class); lval != null; ) {
        return String.format("{%s} %s", concatOperands(lval.upvalues), lval.callee);
      }
      for (final Static lval = cast(opnd, Static.class); lval != null; ) {
        return (lval.version == 0) ? lval.name : String.format("%s!%d", lval.name, lval.version);
      }
      for (final GlobalNumbering.Ite ite = cast(opnd, GlobalNumbering.Ite.class); ite != null; ) {
        return String.format("ite %s %s %s",
          stringOf(ite.guard), stringOf(ite.taken), stringOf(ite.other)); 
      }
      return String.format("%s", opnd);
    }

    private static <T> T cast(final Object o, final Class<T> cls) {
      if (cls.isInstance(o)) {
        return cls.cast(o);
      }
      return null;
    }

    @Override
    public void visit(final Concat insn) {
      final String args = concatOperands(insn.rhs);
      lines.add(String.format("%s = Concat %s %s",
        stringOf(insn.lhs), insn.lhs.getType().getName(), args));
    }

    @Override
    public void visit(final Extract insn) {
      lines.add(String.format("%s = Extract %s of %s %s <%s, %s>",
        stringOf(insn.lhs),
        insn.lhs.getType().getName(),
        insn.rhs.getType().getName(),
        stringOf(insn.rhs),
        stringOf(insn.hi),
        stringOf(insn.lo)));
    }

    @Override
    public void visit(final Sext insn) {
      lines.add(String.format("%s = Sext %s %s to %s",
        stringOf(insn.lhs),
        insn.rhs.getType().getName(),
        stringOf(insn.rhs),
        insn.lhs.getType().getName()));
    }

    @Override
    public void visit(final Zext insn) {
      lines.add(String.format("%s = Zext %s %s to %s",
        stringOf(insn.lhs),
        insn.rhs.getType().getName(),
        stringOf(insn.rhs),
        insn.lhs.getType().getName()));
    }

    @Override
    public void visit(final Branch insn) {
      if (insn.successors.size() == 1) {
        lines.add(String.format("br label %%%s", getLabel(0, insn)));
      } else {
        lines.add(String.format("br i1 %s, label %%%s, label %%%s",
          stringOf(insn.guard), getLabel(0, insn), getLabel(1, insn)));
      }
    }

    private String getLabel(int n, final Terminator t) {
      return labels.get(t.successors.get(n));
    }

    @Override
    public void visit(final Return insn) {
      if (insn.value == null) {
        lines.add("ret void");
      } else {
        lines.add(String.format("ret %s %s", insn.value.getType().getName(), stringOf(insn.value)));
      }
    }

    @Override
    public void visit(final Exception insn) {
    }

    @Override
    public void visit(final Call insn) {
      final String args = concatOperands(insn.args);
      if (insn.ret == null) {
        lines.add(String.format("call void %s %s (%s)",
          insn.method, stringOf(insn.callee), args));
      } else {
        lines.add(String.format("%s = call %s %s %s (%s)",
          stringOf(insn.ret),
          insn.ret.getType().getName(),
          insn.method,
          stringOf(insn.callee),
          args));
      }
    }

    @Override
    public void visit(final Invoke insn) {
    }

    @Override
    public void visit(final Load insn) {
      lines.add(String.format("%s = load %s, %s %s",
        stringOf(insn.target),
        insn.target.getType().getName(),
        insn.source.getContainerType().getName(),
        stringOf(insn.source)));
    }

    @Override
    public void visit(final Store insn) {

      lines.add(String.format("store %s %s, %s %s",
        insn.source.getType().getName(),
        stringOf(insn.source),
        insn.target.getContainerType().getName(),
        stringOf(insn.target)));
    }

    @Override
    public void visit(final Disclose insn) {
      lines.add(String.format("%s = Disclose %s of %s %s %s",
        stringOf(insn.target),
        insn.target.getType().getName(),
        insn.source.getType().getName(),
        stringOf(insn.source),
        concatOperands(insn.indices)));
    }

    @Override
    public void visit(final GlobalNumbering.Phi insn) {
      lines.add(String.format("%s = phi %s",
        stringOf(insn.target),
        (insn.value != null) ? stringOf(insn.value) : concatOperands(insn.values)));
    }

    @Override
    public void visit(final GlobalNumbering.SsaStore insn) {
      visit(insn.origin);
      final String value = lines.remove(lines.size() - 1);
      lines.add(String.format("%s = %s", stringOf(insn.target), value));
    }

    private String concatOperands(final Collection<? extends Operand> operands) {
      final StringBuilder sb = new StringBuilder();
      final Iterator<? extends Operand> it = operands.iterator();
      if (it.hasNext()) {
        Operand op = it.next();
        sb.append(String.format("%s %s", op.getType().getName(), stringOf(op)));
        while (it.hasNext()) {
          op = it.next();
          sb.append(", ").append(String.format("%s %s", op.getType().getName(), stringOf(op)));
        }
      }
      return sb.toString();
    }
  }
}
