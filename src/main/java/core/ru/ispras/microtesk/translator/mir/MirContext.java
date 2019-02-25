package ru.ispras.microtesk.translator.mir;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MirBlock {
  public final BasicBlock bb;
  public final MirContext ctx;

  public MirBlock(final MirContext ctx, final BasicBlock bb) {
    this.ctx = ctx;
    this.bb = bb;
  }

  public Local newLocal(final DataType type) {
    return newLocal(new IntTy(64)); // FIXME
  }

  public Local newLocal(final MirTy type) {
    ctx.locals.add(type);
    return new Local(ctx.locals.size(), type);
  }

  public Local getNamedLocal(final String name) {
    for (final LocalInfo info : ctx.localInfo.values()) {
      if (name.equals(info.name)) {
        return new Local(info.id, ctx.locals.get(info.id - 1));
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
    return assignLocal(BvOpcode.Add.make(op, new Constant(op.getType().getSize(), 0)));
  }

  public Local assignLocal(final Rvalue rhs) {
    final Local lhs = newLocal(rhs.getType());
    assign(lhs, rhs);
    return lhs;
  }

  public Local extract(final Operand src, int size, final Operand lo, final Operand hi) {
    final Local ret = newLocal(new IntTy(size));
    append(new Extract(ret, src, lo, hi));
    return ret;
  }

  public Pair<MirBlock, MirBlock> branch(final Operand cond) {
    final MirBlock taken = ctx.newBlock();
    final MirBlock other = ctx.newBlock();

    append(new Branch(cond, taken.bb, other.bb));

    return new Pair<MirBlock, MirBlock>(taken, other);
  }

  public void jump(final MirBlock block) {
    append(new Terminator(block.bb));
  }

  public <T extends Instruction> T append(final T insn) {
    bb.insns.add(insn);
    return insn;
  }
}

public final class MirContext {
  public final List<BasicBlock> blocks = new ArrayList<>();
  public final BasicBlock landingPad = new BasicBlock();

  public final List<MirTy> locals = new ArrayList();
  public final Map<Integer, LocalInfo> localInfo = new HashMap<>();

  public MirBlock newBlock() {
    final MirBlock block = new MirBlock(this, new BasicBlock());
    blocks.add(block.bb);

    return block;
  }
}

final class LocalInfo {
  public final int id;
  public final String name;

  public LocalInfo(final int id, final String name) {
    this.id = id;
    this.name = name;
  }
}
