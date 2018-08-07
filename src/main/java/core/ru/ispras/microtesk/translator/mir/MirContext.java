package ru.ispras.microtesk.translator.mir;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MirContext {
  private final BasicBlock bb;
  private final List<BasicBlock> blocks;
  private final Locals locals;

  public static MirContext newMir() {
    return new MirContext(new BasicBlock(), new ArrayList<BasicBlock>(), new Locals());
  }

  public MirContext fork(final BasicBlock bb) {
    return new MirContext(bb, this.blocks, this.locals);
  }

  private MirContext(
      final BasicBlock bb,
      final List<BasicBlock> blocks,
      final Locals locals) {
    this.bb = bb;
    this.blocks = blocks;
    this.locals = locals;

    blocks.add(bb);
  }

  public Local newLocal(final DataType type) {
    locals.type.add(type);
    return new Local(locals.type.size());
  }

  public Local getNamedLocal(final String name) {
    for (final LocalInfo info : locals.info.values()) {
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

  private static class Locals {
    public final List<DataType> type = new ArrayList();
    public final Map<Integer, LocalInfo> info = new HashMap<>();
  }

  private static class LocalInfo {
    public final int id;
    public final String name;

    public LocalInfo(final int id, final String name) {
      this.id = id;
      this.name = name;
    }
  }
}
