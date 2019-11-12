package ru.ispras.microtesk.translator.mir;

public abstract class Lvalue implements Operand {
  @Override
  abstract public MirTy getType();
  abstract public MirTy getContainerType();
}
