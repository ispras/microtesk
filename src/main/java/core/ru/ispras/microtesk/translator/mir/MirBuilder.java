package ru.ispras.microtesk.translator.mir;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

public class MirBuilder {
  private final static FuncTy VOID_TO_VOID_TYPE =
      new FuncTy(VoidTy.VALUE, Collections.<MirTy>emptyList());

  private final List<Instruction> body = new java.util.ArrayList<>();
  private final List<Operand> operands = new java.util.ArrayList<>();

  public MirContext build(final String name) {
    final MirContext mir = new MirContext(name, VOID_TO_VOID_TYPE);
    final MirBlock block = mir.newBlock();
    block.bb.insns.addAll(body);
    block.bb.insns.add(new Return(null));

    return mir;
  }

  public void addValue(final int size, final BigInteger value) {
    operands.add(new Constant(size, value));
  }

  public void makeClosure(final String name, final int nargs) {
    final List<Operand> upvalues = removeLastN(operands, nargs);
    operands.add(new Closure(name, upvalues));
  }

  public void makeCall(final String method, final int nargs) {
    final List<Operand> args = removeLastN(operands, nargs);
    final Operand callee = removeLast(operands);

    body.add(new Call(callee, method, args, null));
  }

  private static <T> T removeLast(final List<T> list) {
    return list.remove(list.size() - 1);
  }

  private static <T> List<T> removeLastN(final List<T> list, final int n) {
    final List<T> tail = list.subList(list.size() - n, list.size());
    final List<T> items = new java.util.ArrayList<T>(tail);
    tail.clear();

    return items;
  }
}
