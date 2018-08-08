package ru.ispras.microtesk.translator.mir;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MirBlock {
  private final MirContext ctx;
  private final BasicBlock bb;

  public MirBlock(final MirContext ctx, final BasicBlock bb) {
    this.ctx = ctx;
    this.bb = bb;
  }

  public Local newLocal(final DataType type) {
    ctx.locals.add(type);
    return new Local(ctx.locals.size());
  }

  public Local getNamedLocal(final String name) {
    for (final LocalInfo info : ctx.localInfo.values()) {
      if (name.equals(info.name)) {
        return new Local(info.id);
      }
    }
    return null;
  }

  public Assignment assign(final Lvalue lhs, final Rvalue rhs) {
    return append(new Assignment(lhs, rhs));
  }

  public Local assignLocal(final Operand op) {
    if (op instanceof Local) {
      return (Local) op;
    }
    return assignLocal(Opcode.Use.make(op));
  }

  public Local assignLocal(final Rvalue rhs) {
    final Local lhs = newLocal(null); // FIXME
    assign(lhs, rhs);
    return lhs;
  }

  public Local extract(final Operand src, final Operand lo, final Operand hi) {
    final Local ret = newLocal(null);
    append(new Extract(ret, src, lo, hi));
    return ret;
  }

  public <T extends Instruction> T append(final T insn) {
    bb.insns.add(insn);
    return insn;
  }
}

final class MirContext {
  public final List<BasicBlock> blocks = new ArrayList<>();
  public final BasicBlock landingPad = new BasicBlock();

  public final List<DataType> locals = new ArrayList();
  public final Map<Integer, LocalInfo> localInfo = new HashMap<>();
}

final class LocalInfo {
  public final int id;
  public final String name;

  public LocalInfo(final int id, final String name) {
    this.id = id;
    this.name = name;
  }
}
