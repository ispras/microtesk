package ru.ispras.microtesk.translator.mir;

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

  public Local newLocal(final int size) {
    return newLocal(new IntTy(size));
  }

  public Local newLocal(final MirTy type) {
    ctx.locals.add(type);
    return new Local(ctx.locals.size() - 1, type);
  }

  public Local getNamedLocal(final String name) {
    for (final LocalInfo info : ctx.localInfo.values()) {
      if (name.equals(info.name)) {
        return new Local(info.id, ctx.locals.get(info.id));
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
    return assignLocal(UnOpcode.Use.make(op));
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
    append(new Branch(block.bb));
  }

  public <T extends Instruction> T append(final T insn) {
    bb.insns.add(insn);
    return insn;
  }
}

public final class MirContext {
  public final String name;
  public final List<BasicBlock> blocks = new ArrayList<>();
  public final BasicBlock landingPad = new BasicBlock();

  public final List<MirTy> locals = new ArrayList<>();
  public final Map<Integer, LocalInfo> localInfo = new HashMap<>();

  public MirContext(final String name, final FuncTy signature) {
    this.name = name;

    locals.add(signature);
    locals.addAll(signature.params);

    localInfo.put(0, new LocalInfo(0, ".self"));
  }

  public MirBlock newBlock() {
    final MirBlock block = new MirBlock(this, new BasicBlock());
    blocks.add(block.bb);

    return block;
  }

  public FuncTy getSignature() {
    return (FuncTy) locals.get(0);
  }

  public void renameParameter(final int index, final String name) {
    if (index >= 0 && index < getSignature().params.size()) {
      localInfo.put(index + 1, new LocalInfo(index + 1, name));
    } else {
      throw new IndexOutOfBoundsException();
    }
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
