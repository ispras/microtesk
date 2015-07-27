package ru.ispras.microtesk.mmu.translator.ir.spec.builder;

import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Variable;

public class VariableStorage extends ScopeStorage<Variable> {
  public Variable declare(final String name, final Type type) {
    final Variable var = new Variable(newPath(name), type);
    put(name, var);

    return var;
  }
}
