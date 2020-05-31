package ru.ispras.microtesk.test.template;

interface CodeBlockBuilder<T> {
  void addCall(AbstractCall call);
  T build();
}
