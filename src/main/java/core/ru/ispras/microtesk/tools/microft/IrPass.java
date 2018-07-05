package ru.ispras.microtesk.tools.microft;

import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;

import java.util.Arrays;
import java.util.List;

abstract class IrPass <T> {
  private final String name;
  private final List<String> deps;

  public IrPass(final String name, final String... deps) {
    this(name, Arrays.asList(deps));
  }

  public IrPass(final String name, final List<String> deps) {
    this.name = name;
    this.deps = deps;
  }

  public String getName() {
    return name;
  }

  public List<String> getDependencies() {
    return deps;
  }

  public abstract T run(final List<PrimitiveAND> insn, final PassContext ctx);
}
