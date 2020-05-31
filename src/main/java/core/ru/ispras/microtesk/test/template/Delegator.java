package ru.ispras.microtesk.test.template;

interface Delegator extends DelegatorTrait {
  LazyValue delegateValue();
  LazyValue delegateValue(int start, int end);
}
