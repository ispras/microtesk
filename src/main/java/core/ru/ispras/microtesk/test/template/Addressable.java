package ru.ispras.microtesk.test.template;

interface Addressable extends DelegatorTrait {
  LazyValue newAddressReference(int level);
  LazyValue newAddressReference(int level, int start, int end);
}
