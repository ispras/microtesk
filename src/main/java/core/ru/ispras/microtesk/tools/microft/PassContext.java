package ru.ispras.microtesk.tools.microft;

import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;

import java.util.List;
import java.util.Map;

class PassContext {
  private final Map<String, Object> irs = new java.util.LinkedHashMap<>();

  public <T> T getPassIr(final String name, final Class<T> c) {
    final Object ir = irs.get(name);
    if (c.isInstance(ir)) {
      return c.cast(ir);
    }
    throw new IllegalStateException();
  }

  public void fill(final List<PrimitiveAND> insn, final List<? extends IrPass<?>> passes) {
    for (final IrPass<?> pass : passes) {
      irs.put(pass.getName(), pass.run(insn, this));
    }
  }

  public <T> Map<String, T> select(final Class<T> c) {
    final Map<String, T> store = new java.util.TreeMap<>();
    select(c, store);
    return store;
  }

  public <T> void select(final Class<T> c, final Map<String, ? super T> store) {
    for (final Map.Entry<String, Object> entry : irs.entrySet()) {
      if (c.isInstance(entry.getValue())) {
        store.put(entry.getKey(), c.cast(entry.getValue()));
      }
    }
  }
}
