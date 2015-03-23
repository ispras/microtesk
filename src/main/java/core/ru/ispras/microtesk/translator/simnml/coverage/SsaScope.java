package ru.ispras.microtesk.translator.simnml.coverage;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.expression.NodeVariable;

public interface SsaScope {
  boolean contains(String name);
  NodeVariable create(String name, Data data);
  NodeVariable fetch(String name);
  NodeVariable update(String name);
}
