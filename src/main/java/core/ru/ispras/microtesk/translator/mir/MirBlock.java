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

import ru.ispras.fortress.util.Pair;

import java.util.Collections;
import java.util.List;

import static ru.ispras.microtesk.translator.mir.Instruction.*;
import static ru.ispras.microtesk.translator.mir.MirContext.LocalInfo;

public final class MirBlock {
  public final BasicBlock bb;
  public final MirContext ctx;

  public MirBlock(final MirContext ctx, final BasicBlock bb) {
    this.ctx = ctx;
    this.bb = bb;
  }

  public Local getLocal(final int index) {
    return new Local(index, ctx.locals.get(index));
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

  public Assignment assign(final Local lhs, final Operand rhs) {
    return assign(lhs, UnOpcode.Use.make(rhs));
  }

  public Assignment assign(final Local lhs, final Rvalue rhs) {
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
    final Local lo2 = newLocal(src.getType());
    final Local hi2 = newLocal(src.getType());
    append(new Zext(lo2, lo));
    append(new Zext(hi2, hi));
    append(new Extract(ret, src, lo2, hi2));
    return ret;
  }

  public Pair<MirBlock, MirBlock> branch(final Operand cond) {
    final MirBlock taken = ctx.newBlock();
    final MirBlock other = ctx.newBlock();

    append(new Branch(cond, taken.bb, other.bb));

    return new Pair<MirBlock, MirBlock>(taken, other);
  }

  public Branch jump(final MirBlock block) {
    return jump(block.bb);
  }

  public Branch jump(final BasicBlock bb) {
    return append(new Branch(bb));
  }

  public Disclose disclose(final Local lhs, final Operand src, int index) {
    return append(new Disclose(lhs, src, Collections.singletonList(Constant.valueOf(32, index))));
  }

  public Call thiscall(final String method, final List<Operand> args, final Local ret) {
    return call(getNamedLocal(".self"), method, args, ret);
  }

  public Call call(
      final Operand callee, final String method, final List<Operand> args, final Local ret) {
    return append(new Call(callee, method, args, ret));
  }

  public <T extends Instruction> T append(final T insn) {
    bb.insns.add(insn);
    return insn;
  }
}
