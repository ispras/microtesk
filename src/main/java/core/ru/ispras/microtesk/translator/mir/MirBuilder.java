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
import java.util.Collections;
import java.util.List;

import static ru.ispras.microtesk.translator.mir.Instruction.Call;
import static ru.ispras.microtesk.translator.mir.Instruction.Load;
import static ru.ispras.microtesk.translator.mir.Instruction.Return;
import static ru.ispras.microtesk.translator.mir.Instruction.Store;

public class MirBuilder {
  public static final FuncTy VOID_TO_VOID_TYPE =
      new FuncTy(VoidTy.VALUE, Collections.<MirTy>emptyList());
  private static final Local SELF = new Local(0, VOID_TO_VOID_TYPE);

  private final MirContext mir;
  private final MirBlock block;

  private final List<MirTy> typeList = new java.util.ArrayList<>();
  private final List<Operand> operands = new java.util.ArrayList<>();
  private final List<Instruction> body = new java.util.ArrayList<>();

  public MirBuilder() {
    this("");
  }

  public MirBuilder(final String name) {
    this.mir = new MirContext(name, VOID_TO_VOID_TYPE);
    this.block = mir.newBlock();
  }

  private MirBuilder(final String name, final MirBuilder that) {
    this(name);
    this.mir.locals.addAll(that.mir.locals);
    this.typeList.addAll(that.typeList);
    this.operands.addAll(that.operands);
    this.body.addAll(that.body);
  }

  public MirBuilder copyAs(final String name) {
    return new MirBuilder(name, this);
  }

  public String getName() {
    return mir.name;
  }

  public MirContext build() {
    return build(getName());
  }

  public MirContext build(final String name) {
    final MirContext ctx =
        new MirContext(name, new FuncTy(VoidTy.VALUE, typeList));
    ctx.locals.addAll(Lists.tailOf(mir.locals, mir.getSignature().params.size() + 1));

    final BasicBlock bb = ctx.newBlock().bb;
    bb.insns.addAll(block.bb.insns);
    bb.insns.addAll(body);
    bb.insns.add(new Return(null));

    return ctx;
  }

  public int addParameter(final int size) {
    return addParameter(new IntTy(size));
  }

  public int addParameter(final MirTy type) {
    typeList.add(type);
    return typeList.size();
  }

  public void refParameter(final int index) {
    operands.add(new Local(index, typeList.get(index - 1)));
  }

  public void addValue(final int size, final BigInteger value) {
    operands.add(Constant.valueOf(size, value));
  }

  public void makeClosure(final String name, final int nargs) {
    final List<Operand> upvalues = Lists.removeLastN(operands, nargs);
    operands.add(new Closure(name, upvalues));
  }

  public void makeCall(final String method, final int nargs) {
    final List<Operand> args = Lists.removeLastN(operands, nargs);
    final Operand callee = Lists.removeLast(operands);

    body.add(new Call(callee, method, args, null));
  }

  public void makeThisCall(final String method, final int nargs) {
    final List<Operand> args = Lists.removeLastN(operands, nargs);
    body.add(new Call(SELF, method, args, null));
  }

  public void refMemory(final int nbits, final String name) {
    final MirTy type = new IntTy(nbits);
    final Static mem = new Static(name, type);
    // WARN changing signature after ref will break MIR
    final Local base = block.newLocal(type);
    final Local tmp = new Local(base.id + typeList.size(), type);

    body.add(new Load(mem, tmp));
    body.add(new Store(mem, tmp));
  }
}
